<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { Study } from '../api/types'
import PageState from '../components/PageState.vue'
import StudyDetailDrawer from '../components/StudyDetailDrawer.vue'
import { session } from '../session'

const router = useRouter()
const canReadMonthly = computed(() =>
  session.currentUser.value?.permissions.includes('monthly.read') ?? false,
)

const studies = ref<Study[]>([])
const loading = ref(true)
const error = ref('')

// 筛选条件：固定枚举（阶段/状态来自里程碑定义）
const filters = reactive({ ta: '', program: '', phase: '', status: '' })

const TA_OPTIONS = ['肿瘤', '自身免疫', '代谢与心血管', '呼吸系统', '感染性疾病', '神经科学']
// 阶段/状态文案与 MilestoneDefinition stage/node label 对齐（筛选命中 currentPhase/currentStatus）
const PHASE_OPTIONS = ['PreIND', 'IND', 'Pre3', 'Protocol', 'SSU', 'Enrollment', 'IA', 'Data & Report', 'PreNDA/BLA', 'NDA/BLA']
const STATUS_BY_PHASE: Record<string, string[]> = {
  PreIND: ['PreIND 递交', 'PreIND 反馈-临床医学', 'PreIND 反馈-数统', 'PreIND 反馈-临床药理', 'PreIND 反馈-非临床', 'PreIND 反馈-药学'],
  IND: ['IND 递交', 'IND 形审发补', 'IND 形审补正', 'IND 受理', 'IND 获批'],
  Pre3: ['Pre3 递交', 'Pre3 反馈-临床医学', 'Pre3 反馈-数统', 'Pre3 反馈-临床药理', 'Pre3 反馈-非临床', 'Pre3 反馈-药学'],
  Protocol: ['方案摘要定稿', '方案讨论会', '方案定稿'],
  SSU: [
    '组长单位立项递交', '组长单位立项获批', '组长单位伦理递交', '组长单位伦理获批',
    '组长单位合同签署', '首家中心启动', '组长单位启动', '所有中心启动',
    '人遗递交', '人遗批准', 'CDE 平台登记', 'ClinicalTrial 登记',
  ],
  Enrollment: ['FPI', 'LPI', 'LPO'],
  IA: ['IA 数据冻结', 'IA 数据分析'],
  'Data & Report': ['DBL', 'TLR初稿', 'TLR定稿', 'TFL初稿', 'TFL定稿', 'CSR初稿', 'CSR定稿', '中心关闭'],
  'PreNDA/BLA': ['PreNDA 递交', 'PreNDA 反馈-临床医学', 'PreNDA 反馈-数统', 'PreNDA 反馈-临床药理', 'PreNDA 反馈-非临床', 'PreNDA 反馈-药学'],
  'NDA/BLA': [
    'NDA/BLA 递交', 'NDA/BLA 形审发补', 'NDA/BLA 形审补正', 'NDA/BLA 受理',
    '临床核查', '药学核查', 'NDA/BLA 发补', 'NDA/BLA 补正', 'NDA/BLA 获批',
  ],
}

function onPhaseChange() {
  filters.status = ''
}

const statusOptions = computed(() => {
  if (!filters.phase) return []
  return STATUS_BY_PHASE[filters.phase] ?? []
})

const filtered = computed(() => studies.value.filter((study) => {
  const ta = study.therapeuticAreaName || study.therapeuticArea || study.therapeuticAreaCode || ''
  if (filters.ta && ta !== filters.ta) return false
  if (filters.program && !String(study.programCode || study.program || '')
    .toLowerCase()
    .includes(filters.program.toLowerCase())) return false
  if (filters.phase && study.currentPhase !== filters.phase) return false
  if (filters.status && study.currentStatus !== filters.status) return false
  return true
}))

// PL/PM 合并为一列展示
function plPm(study: Study): string {
  return [study.plName, study.pmName].filter(Boolean).join(' / ')
}

// drawer state
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
        <select v-model="filters.ta" class="filter-select">
          <option value="">TA 全部</option>
          <option v-for="o in TA_OPTIONS" :key="o" :value="o">{{ o }}</option>
        </select>
        <input v-model.trim="filters.program" type="text" class="filter-input" placeholder="Program 搜索">
        <select v-model="filters.phase" class="filter-select" @change="onPhaseChange">
          <option value="">阶段 全部</option>
          <option v-for="o in PHASE_OPTIONS" :key="o" :value="o">{{ o }}</option>
        </select>
        <select v-model="filters.status" class="filter-select" :disabled="!filters.phase">
          <option value="">状态 全部</option>
          <option v-for="o in statusOptions" :key="o" :value="o">{{ o }}</option>
        </select>
      </div>
      <span class="filter-count">{{ filtered.length }} 个研究</span>
    </div>
    <PageState :loading :error :empty="!filtered.length">
      <div class="data-card">
        <table class="data-table">
          <thead><tr>
            <th>TA</th>
            <th>Program</th>
            <th>Compound</th>
            <th>Study No.</th>
            <th>适应症</th>
            <th>当前阶段</th>
            <th>状态</th>
            <th>PL/PM</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="study in filtered" :key="study.id" class="study-row--clickable" @click="openDrawer(study)">
              <td>{{ study.therapeuticAreaName || study.therapeuticArea || study.therapeuticAreaCode || '—' }}</td>
              <td class="mono">{{ study.programCode || study.program || '—' }}</td>
              <td class="mono">{{ study.projectCode || study.product || '—' }}</td>
              <td class="mono strong">{{ study.code }}</td>
              <td>{{ study.indication }}</td>
              <td>{{ study.currentPhase || '—' }}</td>
              <td>{{ study.currentStatus || '—' }}</td>
              <td>{{ plPm(study) || '—' }}</td>
              <td>{{ study.updatedAt ? new Date(study.updatedAt).toLocaleDateString('zh-CN') : '—' }}</td>
              <td class="actions">
                <button class="link-button" @click.stop="goMilestones(study.id)">里程碑</button>
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
