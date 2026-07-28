<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type {
  FunctionLineHistory,
  FunctionLineReport,
  MonthlyEntryUpdateInput,
  MonthlyReportEntry,
  MonthlyReportPage,
} from '../api/types'
import PageState from '../components/PageState.vue'
import { formatIsoMinute, todayIso } from '../domain/date-format'
import { usePermissions } from '../composables/usePermissions'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const error = ref('')
const page = ref<MonthlyReportPage>()
const month = ref(new Date().toISOString().slice(0, 7))
const editing = ref<Set<number>>(new Set())
const saving = ref<Set<string>>(new Set())
const deleting = ref<Set<number>>(new Set())
const creatingFor = ref<number>()
const createFormError = ref('')
const editFormError = reactive<Record<number, string>>({})

const studyId = computed(() => Number(route.params.studyId))
const editForm = reactive<Record<number, MonthlyEntryUpdateInput>>({})
const createForm = reactive({ entryDate: '', content: '' })

function validateEntry(entryDate?: string, content?: string): string {
  if (!entryDate?.trim()) return '请选择日期'
  if (!content?.trim()) return '请填写进展内容'
  if (content.length > 4000) return '进展内容不能超过 4000 字'
  return ''
}

const { can } = usePermissions()
const canRead = can('monthly.read')
const canCreate = can('monthly.create')
const canUpdate = can('monthly.update')
const canDelete = can('monthly.update')

// 历史面板状态（按 reportId 维度）
const historyOpen = ref<Set<number>>(new Set())
const history = reactive<Record<number, FunctionLineHistory>>({})
const historyLoading = ref<Set<number>>(new Set())

async function load(showLoading = true) {
  if (!studyId.value) { router.push('/studies'); return }
  if (showLoading) loading.value = true
  error.value = ''
  try {
    page.value = await apiClient.getMonthlyReports(studyId.value, month.value)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '月报数据加载失败'
  } finally { loading.value = false }
}

onMounted(() => load())
watch(month, () => {
  editing.value = new Set()
  creatingFor.value = undefined
  createFormError.value = ''
  for (const key of Object.keys(editFormError)) delete editFormError[Number(key)]
  historyOpen.value = new Set()
  historyLoading.value = new Set()
  for (const key of Object.keys(history)) delete history[Number(key)]
  load()
})

function isEditing(entryId: number) {
  return editing.value.has(entryId)
}
function startEdit(entry: MonthlyReportEntry) {
  if (!canUpdate.value || !entry.editable) return
  editForm[entry.entryId] = { entryDate: entry.entryDate, content: entry.content }
  delete editFormError[entry.entryId]
  editing.value = new Set([...editing.value, entry.entryId])
}
function cancelEdit(entryId: number) {
  const next = new Set(editing.value)
  next.delete(entryId)
  editing.value = next
  delete editForm[entryId]
  delete editFormError[entryId]
}
async function saveEdit(entry: MonthlyReportEntry) {
  if (!canUpdate.value) return
  const draft = editForm[entry.entryId] ?? {}
  const validation = validateEntry(draft.entryDate, draft.content)
  if (validation) {
    editFormError[entry.entryId] = validation
    return
  }
  delete editFormError[entry.entryId]
  const key = `edit-${entry.entryId}`
  saving.value = new Set([...saving.value, key])
  try {
    page.value = await apiClient.updateMonthlyEntry(entry.entryId, {
      entryDate: draft.entryDate,
      content: draft.content?.trim(),
    })
    cancelEdit(entry.entryId)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '保存失败'
  } finally {
    const next = new Set(saving.value)
    next.delete(key)
    saving.value = next
  }
}
function startCreate(line: FunctionLineReport) {
  if (!canCreate.value || !line.editable) return
  createForm.entryDate = todayIso()
  createForm.content = ''
  createFormError.value = ''
  creatingFor.value = line.reportId
}
function cancelCreate() {
  creatingFor.value = undefined
  createFormError.value = ''
}
async function saveCreate(line: FunctionLineReport) {
  if (!canCreate.value) return
  const validation = validateEntry(createForm.entryDate, createForm.content)
  if (validation) {
    createFormError.value = validation
    return
  }
  createFormError.value = ''
  const key = `create-${line.reportId}`
  saving.value = new Set([...saving.value, key])
  try {
    page.value = await apiClient.createMonthlyEntry(line.reportId, {
      entryDate: createForm.entryDate,
      content: createForm.content.trim(),
    })
    cancelCreate()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '保存失败'
  } finally {
    const next = new Set(saving.value)
    next.delete(key)
    saving.value = next
  }
}

async function confirmDelete(entry: MonthlyReportEntry) {
  if (!canDelete.value || !entry.editable) return
  if (!window.confirm('确定删除这条进展吗？删除后不可恢复。')) return
  const key = entry.entryId
  const next = new Set(deleting.value)
  next.add(key)
  deleting.value = next
  try {
    page.value = await apiClient.deleteMonthlyEntry(key)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '删除失败'
  } finally {
    const after = new Set(deleting.value)
    after.delete(key)
    deleting.value = after
  }
}

async function toggleHistory(line: FunctionLineReport) {
  if (historyOpen.value.has(line.reportId)) {
    historyOpen.value = new Set([...historyOpen.value].filter((id) => id !== line.reportId))
    return
  }
  historyOpen.value = new Set([...historyOpen.value, line.reportId])
  if (!history[line.reportId]) {
    const loadSet = new Set([...historyLoading.value, line.reportId])
    historyLoading.value = loadSet
    try {
      history[line.reportId] = await apiClient.getMonthlyReportHistory(
        studyId.value, line.functionLineId, month.value)
    } catch (reason) {
      error.value = reason instanceof Error ? reason.message : '历史加载失败'
      historyOpen.value = new Set([...historyOpen.value].filter((id) => id !== line.reportId))
    } finally {
      historyLoading.value = new Set([...historyLoading.value].filter((id) => id !== line.reportId))
    }
  }
}

function goBack() { router.push('/studies') }
</script>

<template>
  <section class="page-content monthly-page">
    <div class="page-toolbar milestone-toolbar">
      <div>
        <button class="text-button" type="button" @click="goBack">&larr; 返回 Study 列表</button>
        <h1 v-if="page" class="milestone-title">{{ page.studyCode }} · 月报填写</h1>
      </div>
      <label class="monthly-month-picker"><span>月份</span><input v-model="month" type="month"></label>
    </div>

    <PageState :loading :error :empty="!page?.functionLines.length" empty-title="暂无月报数据">
      <div v-if="page" class="monthly-lines">
        <div v-for="line in page.functionLines" :key="line.reportId" class="data-card monthly-line-card">
          <header class="monthly-line-header">
            <span class="monthly-line-name">
              {{ line.functionName }}<span class="mono monthly-line-code">{{ line.functionCode }}</span>
            </span>
            <span v-if="!line.editable" class="milestone-readonly">只读</span>
          </header>

          <ul class="monthly-entry-list">
            <li v-for="entry in line.entries" :key="entry.entryId" class="monthly-entry">
              <template v-if="isEditing(entry.entryId)">
                <input v-model="editForm[entry.entryId].entryDate" type="date" class="milestone-input monthly-date-input" required>
                <textarea
                  v-model="editForm[entry.entryId].content"
                  class="monthly-textarea"
                  rows="3"
                  maxlength="4000"
                  :aria-invalid="Boolean(editFormError[entry.entryId])"
                  :aria-describedby="editFormError[entry.entryId] ? `monthly-edit-error-${entry.entryId}` : undefined"
                ></textarea>
                <div class="monthly-entry-actions">
                  <button class="text-button" type="button" :disabled="saving.has(`edit-${entry.entryId}`)" @click="saveEdit(entry)">
                    {{ saving.has(`edit-${entry.entryId}`) ? '保存中…' : '保存' }}
                  </button>
                  <button class="text-button" type="button" @click="cancelEdit(entry.entryId)">取消</button>
                </div>
                <p
                  v-if="editFormError[entry.entryId]"
                  :id="`monthly-edit-error-${entry.entryId}`"
                  class="form-error monthly-form-error"
                  role="alert"
                >{{ editFormError[entry.entryId] }}</p>
              </template>
              <template v-else>
                <span class="mono monthly-entry-date">{{ entry.entryDate }}</span>
                <p class="monthly-entry-content">{{ entry.content }}</p>
                <span class="monthly-entry-meta">{{ entry.updatedBy }} · {{ formatIsoMinute(entry.updatedAt) }}</span>
                <div class="monthly-entry-view-actions">
                  <button v-if="entry.editable && canUpdate" class="text-button monthly-entry-edit" type="button" @click="startEdit(entry)">编辑</button>
                  <button v-if="entry.editable && canDelete" class="danger-button" type="button"
                    :disabled="deleting.has(entry.entryId)" @click="confirmDelete(entry)">
                    {{ deleting.has(entry.entryId) ? '删除中…' : '删除' }}
                  </button>
                </div>
              </template>
            </li>
            <li v-if="!line.entries.length" class="monthly-entry-empty">本月暂无进展明细</li>
          </ul>

          <div v-if="canRead" class="monthly-line-history">
            <button class="text-button monthly-line-history-btn" type="button"
              :class="{ active: historyOpen.has(line.reportId) }"
              :disabled="historyLoading.has(line.reportId)"
              @click="toggleHistory(line)">
              {{ historyLoading.has(line.reportId) ? '加载中…' : (historyOpen.has(line.reportId) ? '收起历史' : '查看前 2 月历史') }}
            </button>
            <div v-if="historyOpen.has(line.reportId)" class="monthly-history">
              <p class="monthly-history-title">前 2 个月进展（不含本月）</p>
              <template v-if="history[line.reportId]">
                <div v-for="m in history[line.reportId].months" :key="m.month" class="monthly-history-month">
                  <div class="monthly-history-month-title">{{ m.month }}</div>
                  <div v-for="h in m.entries" :key="h.entryId" class="monthly-history-entry">
                    <span class="monthly-history-date">{{ h.entryDate }}</span>
                    <p class="monthly-history-content">{{ h.content }}</p>
                  </div>
                  <p v-if="!m.entries.length" class="monthly-history-empty">该月暂无进展</p>
                </div>
              </template>
              <p v-else class="monthly-history-empty">加载中…</p>
            </div>
          </div>

          <div v-if="line.editable && canCreate" class="monthly-create">
            <template v-if="creatingFor === line.reportId">
              <input v-model="createForm.entryDate" type="date" class="milestone-input monthly-date-input" required>
              <textarea
                v-model="createForm.content"
                class="monthly-textarea"
                rows="3"
                maxlength="4000"
                placeholder="填写本月进展…"
                :aria-invalid="Boolean(createFormError)"
                :aria-describedby="createFormError ? 'monthly-create-error' : undefined"
              ></textarea>
              <div class="monthly-entry-actions">
                <button class="text-button" type="button" :disabled="saving.has(`create-${line.reportId}`)" @click="saveCreate(line)">
                  {{ saving.has(`create-${line.reportId}`) ? '保存中…' : '保存' }}
                </button>
                <button class="text-button" type="button" @click="cancelCreate">取消</button>
              </div>
              <p
                v-if="createFormError"
                id="monthly-create-error"
                class="form-error monthly-form-error"
                role="alert"
              >{{ createFormError }}</p>
            </template>
            <button v-else class="text-button monthly-create-toggle" type="button" @click="startCreate(line)">＋ 新增进展</button>
          </div>
        </div>
      </div>
    </PageState>
  </section>
</template>
