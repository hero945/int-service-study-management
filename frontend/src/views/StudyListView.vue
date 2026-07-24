<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { Study } from '../api/types'
import PageState from '../components/PageState.vue'
import StudyDetailDrawer from '../components/StudyDetailDrawer.vue'
import { session } from '../session'
import { ALL_MILESTONE_SUB_STATUSES } from '../domain/milestone-filters'

const router = useRouter()
const canReadMonthly = computed(() =>
  session.currentUser.value?.permissions.includes('monthly.read') ?? false,
)
const canReadMilestone = computed(() =>
  session.currentUser.value?.permissions.includes('milestone.read') ?? false,
)

const studies = ref<Study[]>([])
const loading = ref(true)
const error = ref('')

// 筛选：TA / Program 模糊 / 里程碑子状态（命中 currentStatus）
const filters = reactive({ ta: '', program: '', status: '' })

const TA_OPTIONS = ['肿瘤', '自身免疫', '代谢与心血管', '呼吸系统', '感染性疾病', '神经科学']
const statusOptions = ALL_MILESTONE_SUB_STATUSES

const filtered = computed(() => studies.value.filter((study) => {
  const ta = study.therapeuticAreaName || study.therapeuticArea || study.therapeuticAreaCode || ''
  if (filters.ta && ta !== filters.ta) return false
  if (filters.program && !String(study.programCode || study.program || '')
    .toLowerCase()
    .includes(filters.program.toLowerCase())) return false
  if (filters.status && study.currentStatus !== filters.status) return false
  return true
}))

function plPm(study: Study): string {
  return [study.plName, study.pmName].filter(Boolean).join(' / ')
}

const drawerOpen = ref(false)
const selectedStudy = ref<Study | null>(null)

function openDrawer(study: Study) {
  selectedStudy.value = study
  drawerOpen.value = true
}
function closeDrawer() {
  drawerOpen.value = false
  selectedStudy.value = null
}

function goMilestones(studyId: number) {
  router.push(`/milestones/${studyId}`)
}

function goMonthlyReport(studyId: number) {
  router.push(`/studies/${studyId}/monthly-report`)
}

onMounted(async () => {
  try {
    studies.value = await apiClient.listStudies()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '研究数据加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar">
      <div class="filter-group">
        <label class="filter-field">
          <span class="filter-field__label">TA</span>
          <select v-model="filters.ta" class="filter-select">
            <option value="">全部</option>
            <option v-for="o in TA_OPTIONS" :key="o" :value="o">{{ o }}</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-field__label">Program</span>
          <input v-model.trim="filters.program" type="text" class="filter-input" placeholder="输入编号搜索">
        </label>
        <label class="filter-field">
          <span class="filter-field__label">里程碑节点</span>
          <select v-model="filters.status" class="filter-select filter-select--status">
            <option value="">全部</option>
            <option v-for="o in statusOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </label>
      </div>
      <span class="filter-count">{{ filtered.length }} 个研究</span>
    </div>
    <PageState :loading :error :empty="!filtered.length">
      <div class="data-card">
        <table class="data-table">
          <thead><tr>
            <th>TA</th>
            <th>Program</th>
            <th>Product</th>
            <th>Study No.</th>
            <th>适应症</th>
            <th>里程碑阶段</th>
            <th>里程碑节点</th>
            <th>PL/PM</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="study in filtered" :key="study.id" class="study-row--clickable" @click="openDrawer(study)">
              <td>{{ study.therapeuticAreaName || study.therapeuticArea || study.therapeuticAreaCode || '—' }}</td>
              <td class="mono">{{ study.programCode || study.program || '—' }}</td>
              <td class="mono">{{ study.productName || study.product || '—' }}</td>
              <td class="mono strong">{{ study.code }}</td>
              <td>{{ study.indication }}</td>
              <td>{{ study.currentPhase || '—' }}</td>
              <td>{{ study.currentStatus || '—' }}</td>
              <td>{{ plPm(study) || '—' }}</td>
              <td>{{ study.updatedAt ? new Date(study.updatedAt).toLocaleDateString('zh-CN') : '—' }}</td>
              <td class="actions">
                <button
                  v-if="canReadMilestone"
                  class="link-button"
                  @click.stop="goMilestones(study.id)"
                >里程碑</button>
                <button v-if="canReadMonthly" class="link-button" @click.stop="goMonthlyReport(study.id)">月报</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </PageState>
    <StudyDetailDrawer :open="drawerOpen" :study="selectedStudy" @close="closeDrawer" />
  </section>
</template>
