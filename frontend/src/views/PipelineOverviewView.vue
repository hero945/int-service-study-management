<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { OverviewProject, OverviewStudy, PipelineOverview, Study } from '../api/types'
import {
  PHASE_TAGS,
  originLabel,
  sourceLabel,
  type PipelinePhase,
} from '../domain/pipeline-status'
import { furthestPhaseOf, getProjectCell } from '../domain/pipeline-aggregation'
import PageState from '../components/PageState.vue'
import ProjectStudiesDrawer from '../components/ProjectStudiesDrawer.vue'
import StudyDetailDrawer from '../components/StudyDetailDrawer.vue'

// 后端已按 TA 聚合的 project，附带 TA 信息便于筛选
interface ProjectRow extends OverviewProject {
  therapeuticAreaCode: string
  therapeuticAreaName: string
}

const router = useRouter()
const phases = PHASE_TAGS
const overview = ref<PipelineOverview>()
const loading = ref(true)
const errorMessage = ref('')

// 筛选状态
const query = ref('')
const therapeuticArea = ref('全部')
const program = ref('全部')
const phaseFilter = ref<'全部' | PipelinePhase>('全部')
const statusFilter = ref('')

const projectDrawerOpen = ref(false)
const selectedProject = ref<OverviewProject | null>(null)
const selectedAreaName = ref('')
const studyDrawerOpen = ref(false)
const selectedStudy = ref<Study | null>(null)

const allProjects = computed<ProjectRow[]>(() =>
  (overview.value?.areas ?? []).flatMap((area) =>
    area.projects.map((project) => ({
      ...project,
      therapeuticAreaCode: area.therapeuticAreaCode,
      therapeuticAreaName: area.therapeuticAreaName,
    }))))
const allStudies = computed(() => allProjects.value.flatMap((project) => project.studies))

const areas = computed(() => [
  '全部',
  ...new Set(allProjects.value.map((p) => p.therapeuticAreaName).filter(Boolean)),
])
const programs = computed(() => [
  '全部',
  ...new Set(allProjects.value.map((p) => p.programCode).filter(Boolean)),
])
const phaseOptions = computed(() => ['全部', ...phases] as const)

// quick chip：从全部 study 按 StudyStatus 统计
const statusMetrics = computed(() => {
  const defs = [
    { status: 'PLANNED', label: '计划中', tone: 'neutral' },
    { status: 'ACTIVE', label: '进行中', tone: 'positive' },
    { status: 'ON_HOLD', label: '已暂停', tone: 'warning' },
    { status: 'COMPLETED', label: '已完成', tone: 'info' },
  ] as const
  return defs.map((def) => ({
    ...def,
    count: allStudies.value.filter((s) => s.status === def.status).length,
  }))
})

// project 级筛选（TA / Program / 状态 / 阶段 / 关键词）
const filteredProjects = computed(() => allProjects.value.filter((project) => {
  const text = [
    project.code,
    project.indication,
    project.productName,
    project.programCode,
    ...project.studies.map((s) => s.code),
  ].join(' ').toLowerCase()
  const matchesQuery = text.includes(query.value.toLowerCase())
  const matchesArea =
    therapeuticArea.value === '全部' || project.therapeuticAreaName === therapeuticArea.value
  const matchesProgram = program.value === '全部' || project.programCode === program.value
  const matchesStatus = !statusFilter.value || project.studies.some((s) => s.status === statusFilter.value)
  const matchesPhase = phaseFilter.value === '全部' || furthestPhaseOf(project.studies) === phaseFilter.value
  return matchesQuery && matchesArea && matchesProgram && matchesStatus && matchesPhase
}))

// 筛选后按 TA 重新分组展示
const areaGroups = computed(() => {
  const map = new Map<string, ProjectRow[]>()
  for (const project of filteredProjects.value) {
    const key = project.therapeuticAreaName || '其他'
    const list = map.get(key)
    if (list) list.push(project)
    else map.set(key, [project])
  }
  return [...map.entries()].map(([therapeuticAreaName, projects]) => ({
    therapeuticAreaName,
    projects,
  }))
})
const resultCount = computed(() => filteredProjects.value.length)
const hasActiveFilter = computed(() =>
  query.value !== '' ||
  therapeuticArea.value !== '全部' ||
  program.value !== '全部' ||
  phaseFilter.value !== '全部' ||
  statusFilter.value !== '')

const cell = (project: OverviewProject, phase: PipelinePhase) => getProjectCell(project.studies, phase)

function toggleStatus(status: string) {
  statusFilter.value = statusFilter.value === status ? '' : status
}
function clearFilters() {
  query.value = ''
  therapeuticArea.value = '全部'
  program.value = '全部'
  phaseFilter.value = '全部'
  statusFilter.value = ''
}
function openStudy(studyId?: number) {
  if (studyId != null) router.push(`/milestones/${studyId}`)
}

function toStudy(o: OverviewStudy, p: OverviewProject): Study {
  return {
    id: o.id,
    code: o.code,
    indication: p.indication || '',
    phase: o.phase,
    status: o.status,
    statusLabel: o.statusLabel,
    statusTone: o.statusTone,
    ownerName: '',
    startDate: o.startDate,
    updatedAt: o.updatedAt,
    programCode: p.programCode,
    projectCode: p.code,
    productName: p.productName,
    currentPhase: o.mainStageLabel ?? undefined,
    currentStatus: o.subStatusLabel ?? undefined,
  }
}

function openProjectDrawer(project: OverviewProject, areaName: string) {
  selectedProject.value = project
  selectedAreaName.value = areaName
  projectDrawerOpen.value = true
}

function closeProjectDrawer() {
  projectDrawerOpen.value = false
  studyDrawerOpen.value = false
  selectedStudy.value = null
}

function selectStudy(study: OverviewStudy) {
  if (!selectedProject.value) return
  selectedStudy.value = toStudy(study, selectedProject.value)
  studyDrawerOpen.value = true
}

function closeStudyDrawer() {
  studyDrawerOpen.value = false
  selectedStudy.value = null
}

onMounted(async () => {
  try {
    overview.value = await apiClient.getPipelineOverview()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '管线数据加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="filter-bar">
      <label>
        <span>TA</span>
        <select v-model="therapeuticArea">
          <option v-for="area in areas" :key="area">{{ area }}</option>
        </select>
      </label>
      <label>
        <span>Program</span>
        <select v-model="program">
          <option v-for="item in programs" :key="item">{{ item }}</option>
        </select>
      </label>
      <label>
        <span>阶段</span>
        <select v-model="phaseFilter">
          <option v-for="phase in phaseOptions" :key="phase">{{ phase }}</option>
        </select>
      </label>
      <label>
        <span>状态</span>
        <select v-model="statusFilter">
          <option value="">全部</option>
          <option v-for="item in statusMetrics" :key="item.status" :value="item.status">
            {{ item.label }}
          </option>
        </select>
      </label>
      <label>
        <span>关键词</span>
        <input v-model.trim="query" type="search" placeholder="Product / Program / Project / Study">
      </label>
      <div class="quick-metrics">
        <button
          v-for="metric in statusMetrics"
          :key="metric.status"
          type="button"
          class="quick-chip"
          :class="[`quick-chip--${metric.tone}`, { 'quick-chip--active': statusFilter === metric.status }]"
          @click="toggleStatus(metric.status)"
        >
          <strong>{{ metric.count }}</strong>{{ metric.label }}
        </button>
        <button v-if="hasActiveFilter" type="button" class="quick-clear" @click="clearFilters">
          清除筛选
        </button>
      </div>
      <span class="result-summary">{{ resultCount }} 个项目</span>
    </div>

    <div class="legend-bar">
      <span>状态图例</span>
      <span><i class="legend-dot legend-dot--blue"></i>进行中</span>
      <span><i class="legend-dot legend-dot--green"></i>已完成</span>
      <span><i class="legend-dot legend-dot--gray"></i>准备中</span>
      <span><i class="legend-dot legend-dot--red"></i>延期</span>
    </div>

    <PageState
      :loading="loading"
      :error="errorMessage"
      :empty="!loading && !errorMessage && !areaGroups.length"
      empty-title="暂无匹配项目"
      empty-description="请调整筛选条件后重试。"
    >
      <div class="pipeline-table-wrap">
        <table class="pipeline-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Program (MOA)</th>
              <th>Project (Indication)</th>
              <th v-for="phase in phases" :key="phase">{{ phase }}</th>
            </tr>
          </thead>
          <tbody v-for="area in areaGroups" :key="area.therapeuticAreaName">
            <tr class="area-row">
              <td colspan="3" class="area-row-sticky">
                <span class="area-dot"></span>{{ area.therapeuticAreaName }}
                <small>{{ area.projects.length }} 个项目</small>
              </td>
              <td v-for="phase in phases" :key="`area-${phase}`" class="area-row-fill"></td>
            </tr>
            <tr v-for="project in area.projects" :key="project.code">
              <td
                class="pipeline-id-cell"
                @click="openProjectDrawer(project, area.therapeuticAreaName)"
              >
                <strong>{{ project.productName || project.code }}</strong>
                <small>{{ sourceLabel(project.sourceCode) }} · {{ originLabel(project.originCode) }}</small>
              </td>
              <td
                class="pipeline-id-cell"
                @click="openProjectDrawer(project, area.therapeuticAreaName)"
              >
                <strong class="mono">{{ project.programCode }}</strong>
                <small>{{ project.moa }}</small>
              </td>
              <td
                class="pipeline-id-cell"
                @click="openProjectDrawer(project, area.therapeuticAreaName)"
              >
                <strong>{{ project.code }}</strong>
                <small>{{ project.indication }}</small>
              </td>
              <td
                v-for="phase in phases"
                :key="phase"
                class="pipeline-stage-td"
                :class="{ 'cell-clickable': cell(project, phase).clickable }"
                @click="cell(project, phase).clickable && openStudy(cell(project, phase).studyId)"
              >
                <div
                  v-if="cell(project, phase).tone !== 'empty'"
                  class="pipeline-stage-wrap"
                >
                  <span
                    v-if="cell(project, phase).subText"
                    class="cell-stage-caption"
                  >{{ cell(project, phase).subText }}</span>
                  <span
                    class="status-chip"
                    :class="`status-chip--${cell(project, phase).tone}`"
                    :title="cell(project, phase).explanation"
                  >{{ cell(project, phase).label }}</span>
                </div>
                <span
                  v-else
                  class="status-chip status-chip--empty"
                >{{ cell(project, phase).label }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </PageState>

    <ProjectStudiesDrawer
      :open="projectDrawerOpen"
      :project="selectedProject"
      :area-name="selectedAreaName"
      @close="closeProjectDrawer"
      @select-study="selectStudy"
    />
    <StudyDetailDrawer
      :open="studyDrawerOpen"
      :study="selectedStudy"
      @close="closeStudyDrawer"
    />
  </section>
</template>

<style scoped>
.pipeline-stage-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 3px;
  min-height: 44px;
}
.cell-stage-caption {
  display: block;
  font-size: 9px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.35px;
  text-transform: uppercase;
  color: #9aa2ad;
  font-family: "IBM Plex Mono", "Cascadia Mono", monospace;
  white-space: nowrap;
}
</style>
