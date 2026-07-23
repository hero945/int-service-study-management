<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { OverviewProject, OverviewStudy, PipelineOverview, Study } from '../api/types'
import {
  PHASE_TAGS,
  originLabel,
  sourceLabel,
  type PipelinePhase,
} from '../domain/pipeline-status'
import { getProjectCell, type ProjectCell } from '../domain/pipeline-aggregation'
import {
  PIPELINE_PHASE_STATUS_OPTIONS,
  phaseCodeToColumn,
  pipelineStatusOptions,
} from '../domain/milestone-filters'
import PageState from '../components/PageState.vue'
import ProjectStudiesDrawer from '../components/ProjectStudiesDrawer.vue'
import StudyDetailDrawer from '../components/StudyDetailDrawer.vue'

// 后端已按 TA 聚合的 project，附带 TA 信息便于筛选
interface ProjectRow extends OverviewProject {
  therapeuticAreaCode: string
  therapeuticAreaName: string
}

interface HoverTip {
  stage: string
  status: string
  explanation?: string
  updated: string
  owner: string
  tone: string
  x: number
  y: number
}

const TA_OPTIONS = ['肿瘤', '自身免疫', '代谢与心血管', '呼吸系统', '感染性疾病', '神经科学']

const router = useRouter()
const phases = PHASE_TAGS
const overview = ref<PipelineOverview>()
const loading = ref(true)
const errorMessage = ref('')
const hoverTip = ref<HoverTip | null>(null)

const filters = reactive({ ta: '', program: '', phase: '', status: '' })

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

const phaseStatusOptions = PIPELINE_PHASE_STATUS_OPTIONS
const statusOptions = computed(() => pipelineStatusOptions(filters.phase))
const selectedColumn = computed(() => phaseCodeToColumn(filters.phase))

function onPhaseChange() {
  filters.status = ''
}

function cell(project: OverviewProject, phase: PipelinePhase): ProjectCell {
  return getProjectCell(
    project.studies.map((study) => ({
      ...study,
      productName: project.productName,
    })),
    phase,
  )
}

// Phase 只定列；仅当选了状态时，按该列单元格文案过滤
const filteredProjects = computed(() => allProjects.value.filter((project) => {
  if (filters.ta && project.therapeuticAreaName !== filters.ta) return false
  if (filters.program && !project.programCode.toLowerCase().includes(filters.program.toLowerCase())) {
    return false
  }
  if (filters.status && selectedColumn.value) {
    return cell(project, selectedColumn.value).label === filters.status
  }
  return true
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

const tipStyle = computed(() => {
  const tip = hoverTip.value
  if (!tip) return {}
  const width = 210
  const left = Math.min(tip.x + 14, (typeof window !== 'undefined' ? window.innerWidth : 1400) - width - 12)
  const top = Math.min(tip.y + 16, (typeof window !== 'undefined' ? window.innerHeight : 800) - 120)
  return { left: `${left}px`, top: `${top}px` }
})

function openStudy(studyId?: number) {
  if (studyId != null) router.push(`/milestones/${studyId}`)
}

function showCellTip(event: MouseEvent, project: OverviewProject, phase: PipelinePhase) {
  const item = cell(project, phase)
  if (item.tone === 'empty' || !item.tipStage || !item.tipStatus) {
    hoverTip.value = null
    return
  }
  hoverTip.value = {
    stage: item.tipStage,
    status: item.tipStatus,
    explanation: item.explanation,
    updated: item.tipUpdated || '—',
    owner: item.tipOwner || '—',
    tone: item.tone,
    x: event.clientX,
    y: event.clientY,
  }
}

function moveCellTip(event: MouseEvent) {
  if (!hoverTip.value) return
  hoverTip.value = {
    ...hoverTip.value,
    x: event.clientX,
    y: event.clientY,
  }
}

function hideCellTip() {
  hoverTip.value = null
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
    plName: o.plName,
    pmName: o.pmName,
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
          <span class="filter-field__label">Phase</span>
          <select v-model="filters.phase" class="filter-select" @change="onPhaseChange">
            <option value="">全部</option>
            <option v-for="o in phaseStatusOptions" :key="o.code" :value="o.code">{{ o.label }}</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-field__label">状态</span>
          <select v-model="filters.status" class="filter-select filter-select--status" :disabled="!filters.phase">
            <option value="">全部</option>
            <option v-for="o in statusOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </label>
      </div>
      <span class="filter-count">{{ resultCount }} 个项目</span>
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
                @mouseenter="showCellTip($event, project, phase)"
                @mousemove="moveCellTip"
                @mouseleave="hideCellTip"
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

    <Teleport to="body">
      <div
        v-if="hoverTip"
        class="pipeline-hover-tip"
        :style="tipStyle"
        aria-hidden="true"
      >
        <div class="pipeline-hover-tip__title">
          <i class="pipeline-hover-tip__dot" :class="`pipeline-hover-tip__dot--${hoverTip.tone}`"></i>
          {{ hoverTip.stage }} · {{ hoverTip.status }}
        </div>
        <div v-if="hoverTip.explanation" class="pipeline-hover-tip__sub">
          {{ hoverTip.explanation }}
        </div>
        <div class="pipeline-hover-tip__meta">
          <div>最近更新 · {{ hoverTip.updated }}</div>
          <div>负责人 · {{ hoverTip.owner }}</div>
        </div>
      </div>
    </Teleport>

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
  gap: 4px;
  min-height: 46px;
}
.cell-stage-caption {
  display: block;
  font-size: 9px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.4px;
  text-transform: uppercase;
  color: var(--caption);
  font-family: var(--font-mono);
  white-space: nowrap;
}
</style>
