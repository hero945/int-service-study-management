<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { MilestoneNode, MilestonePage, MilestoneUpdateInput, StageProjection } from '../api/types'
import PageState from '../components/PageState.vue'
import { milestoneNodeStatusClass, milestoneNodeStatusLabel } from '../domain/milestone-status'
import { usePermissions } from '../composables/usePermissions'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const error = ref('')
const page = ref<MilestonePage>()
const projection = ref<StageProjection>()
const editing = ref<Set<string>>(new Set())
const saving = ref<Set<string>>(new Set())

const studyId = computed(() => Number(route.params.studyId))
const editForm = reactive<Record<string, MilestoneUpdateInput>>({})

const { can } = usePermissions()
const canEdit = can('milestone.update')

async function load(showLoading = true) {
  if (!studyId.value) { router.push('/studies'); return }
  if (showLoading) loading.value = true
  error.value = ''
  try {
    const [milestones, proj] = await Promise.all([
      apiClient.getMilestones(studyId.value),
      apiClient.getStageProjection(studyId.value).catch(() => undefined),
    ])
    page.value = milestones
    projection.value = proj
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '里程碑数据加载失败'
  } finally { loading.value = false }
}

onMounted(() => load())

function statusLabel(status: string) {
  return milestoneNodeStatusLabel(status)
}
function statusClass(status: string) {
  return milestoneNodeStatusClass(status)
}

function isEditing(milestoneCode: string) {
  return editing.value.has(milestoneCode)
}
function startEdit(node: MilestoneNode) {
  if (!canEdit.value) return
  const key = node.milestoneCode
  editForm[key] = {
    planV1Date: node.planV1Date ?? undefined,
    planV2Date: node.planV2Date ?? undefined,
    actualStartDate: node.actualStartDate ?? undefined,
    actualEndDate: node.actualEndDate ?? undefined,
    deviationNote: node.deviationNote ?? undefined,
  }
  editing.value = new Set([...editing.value, key])
}
function cancelEdit(milestoneCode: string) {
  const next = new Set(editing.value)
  next.delete(milestoneCode)
  editing.value = next
  delete editForm[milestoneCode]
}
async function saveEdit(node: MilestoneNode) {
  if (!canEdit.value) return
  const code = node.milestoneCode
  saving.value = new Set([...saving.value, code])
  try {
    await apiClient.updateMilestone(studyId.value, code, editForm[code] ?? {})
    cancelEdit(code)
    await load(false)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '保存失败'
  } finally {
    const next = new Set(saving.value)
    next.delete(code)
    saving.value = next
  }
}
function goBack() { router.push('/studies') }
</script>

<template>
  <section class="page-content milestone-page">
    <div class="page-toolbar milestone-toolbar">
      <div>
        <button class="text-button" type="button" @click="goBack">&larr; 返回 Study 列表</button>
        <h1 v-if="page" class="milestone-title">{{ page.studyCode }} · 里程碑跟踪</h1>
      </div>
      <div v-if="projection" class="milestone-projection">
        <span class="milestone-badge" :class="projection.statusText === '已完成' ? 'milestone-badge--green' : projection.statusText === '进行中' ? 'milestone-badge--blue' : ''">
          {{ projection.statusText }}
        </span>
        <span v-if="projection.currentMilestoneName" class="milestone-now">{{ projection.currentMilestoneName }}</span>
      </div>
    </div>

    <PageState :loading :error :empty="!page?.groups.length" empty-title="暂无里程碑数据">
      <div class="data-card milestone-card" v-if="page">
        <div class="milestone-table-wrap">
          <table class="data-table milestone-table">
            <thead>
              <tr>
                <th class="milestone-col-name">Milestone</th>
                <th class="milestone-col-date">Ver 1.0</th>
                <th class="milestone-col-date">Ver 2.0</th>
                <th class="milestone-col-date">Actual Start</th>
                <th class="milestone-col-date">Actual End</th>
                <th class="milestone-col-status">状态</th>
                <th class="milestone-col-note">偏差说明</th>
                <th class="milestone-col-action">操作</th>
              </tr>
            </thead>
            <template v-for="group in page.groups" :key="group.stageCode">
              <tbody>
                <tr class="milestone-stage-row">
                  <td colspan="8" class="milestone-stage-title">{{ group.stageName }}</td>
                </tr>
                <tr v-for="node in group.nodes" :key="node.milestoneCode"
                  :class="{ 'milestone-row--active': node.status === 'IN_PROGRESS', 'milestone-row--editing': isEditing(node.milestoneCode) }">
                  <td class="mono milestone-cell-name">
                    <span class="milestone-cell-name-inner">
                      <span class="milestone-dot" :class="`milestone-dot--${node.status}`" :aria-label="statusLabel(node.status)"></span>
                      {{ node.milestoneName }}
                    </span>
                  </td>
                  <td v-if="isEditing(node.milestoneCode)" class="milestone-cell-date"><input v-model="editForm[node.milestoneCode].planV1Date" type="date" class="milestone-input"></td>
                  <td v-else class="milestone-cell-date mono">{{ node.planV1Date || '—' }}</td>
                  <td v-if="isEditing(node.milestoneCode)" class="milestone-cell-date"><input v-model="editForm[node.milestoneCode].planV2Date" type="date" class="milestone-input"></td>
                  <td v-else class="milestone-cell-date mono">{{ node.planV2Date || '—' }}</td>
                  <td v-if="isEditing(node.milestoneCode)" class="milestone-cell-date"><input v-model="editForm[node.milestoneCode].actualStartDate" type="date" class="milestone-input"></td>
                  <td v-else class="milestone-cell-date mono">{{ node.actualStartDate || '—' }}</td>
                  <td v-if="isEditing(node.milestoneCode)" class="milestone-cell-date"><input v-model="editForm[node.milestoneCode].actualEndDate" type="date" class="milestone-input"></td>
                  <td v-else class="milestone-cell-date mono">{{ node.actualEndDate || '—' }}</td>
                  <td class="milestone-cell-status">
                    <span class="status-chip" :class="`status-chip--${statusClass(node.status)}`">{{ statusLabel(node.status) }}</span>
                  </td>
                  <td v-if="isEditing(node.milestoneCode)" class="milestone-cell-note">
                    <input v-model="editForm[node.milestoneCode].deviationNote" class="milestone-input milestone-input--note" placeholder="延迟或提前原因…" maxlength="4000">
                  </td>
                  <td v-else class="milestone-cell-note">{{ node.deviationNote || '—' }}</td>
                  <td class="milestone-cell-action">
                    <template v-if="isEditing(node.milestoneCode)">
                      <button class="text-button" type="button" :disabled="saving.has(node.milestoneCode)" @click="saveEdit(node)">
                        {{ saving.has(node.milestoneCode) ? '保存中…' : '保存' }}
                      </button>
                      <button class="text-button" type="button" @click="cancelEdit(node.milestoneCode)">取消</button>
                    </template>
                    <button v-else-if="canEdit" class="text-button" type="button" @click="startEdit(node)">编辑</button>
                    <span v-else class="milestone-readonly">只读</span>
                  </td>
                </tr>
              </tbody>
            </template>
          </table>
        </div>
      </div>
    </PageState>
  </section>
</template>
