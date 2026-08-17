<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { apiClient } from '../api/client'
import { formatApiError } from '../api/errors'
import type {
  PipelineConfigRow, PipelineProgram, PipelineProject, ProgramInput,
  ProgramUpdateInput, ProjectInput, ProjectUpdateInput, StudyDeletePreview, TherapeuticArea,
} from '../api/types'
import ListPagination from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import LabeledValue from '../components/LabeledValue.vue'
import AuditLogDrawer from '../components/AuditLogDrawer.vue'
import { PIPELINE_CONFIG_PHASE_STATUS_OPTIONS } from '../domain/milestone-filters'
import { useClientSort } from '../composables/useClientSort'
import { usePagedList } from '../composables/usePagedList'
import { usePermissions } from '../composables/usePermissions'
import { useAuditLogDrawer } from '../composables/useAuditLogDrawer'
import { useNotice } from '../composables/useNotice'
import { useEscapeClose } from '../composables/useEscapeClose'

const phaseStatusOptions = PIPELINE_CONFIG_PHASE_STATUS_OPTIONS

type ViewMode = 'studies' | 'entities'
type EntityKind = 'program' | 'project'

const view = ref<ViewMode>('studies')
const programs = ref<PipelineProgram[]>([])
const projects = ref<PipelineProject[]>([])
const therapeuticAreas = ref<TherapeuticArea[]>([])
const selectedProgramId = ref<number>()
const projectDrawerOpen = ref(false)
const expandedProgramIds = ref<Set<number>>(new Set())
const { notice, noticeType, showNotice, hideNotice } = useNotice()
const programSaving = ref(false)
const projectSaving = ref(false)
const studySaving = ref(false)
const entityDialog = ref<EntityKind>()
const editingProgram = ref<PipelineProgram>()
const editingProject = ref<PipelineProject>()
const studyDialog = ref(false)
const editingStudy = ref<PipelineConfigRow>()
const returnToStudyAfterCreate = ref(false)
const studyProgramDetails = ref<HTMLDetailsElement>()
const studyProjectDetails = ref<HTMLDetailsElement>()
const studyDeleteDialog = ref(false)
const studyDeleteTarget = ref<{ id: number; label: string }>()
const studyDeletePreview = ref<StudyDeletePreview>()
const studyDeleteLoading = ref(false)
const studyDeleteSaving = ref(false)
const filters = reactive({ keyword: '' })

const { can } = usePermissions()
const canCreate = can('config.create')
const canUpdate = can('config.update')
const canDelete = can('config.delete')
const canAudit = can('audit.read')
const { auditDrawer, openRecordAuditLogs, closeAuditLogs } =
  useAuditLogDrawer('CONFIG')

const {
  result, loading, error,
  page: studyPage, pageSize: studyPageSize,
  load, applyFilters,
  changePage: goToStudyPage, changePageSize: changeStudyPageSize,
} = usePagedList({
  filters,
  errorMessage: 'Study 列表加载失败',
  fetcher: (q) => apiClient.listPipelineConfig({
    keyword: q.keyword.trim() || undefined,
    page: q.page,
    pageSize: q.pageSize,
  }),
  onLoaded: (r) => {
    studyPage.value = r.page
    studyPageSize.value = r.pageSize
  },
})

const rows = computed(() => result.value?.data ?? [])
const studyTotalItems = computed(() => result.value?.totalItems ?? 0)
const studyTotalPages = computed(() => Math.max(result.value?.totalPages ?? 1, 1))
const selectedProgram = computed(() => programs.value.find((item) => item.id === selectedProgramId.value))
const selectedProjects = computed(() => projects.value.filter((item) => item.programId === selectedProgramId.value))
const selectedStudyProject = computed(() => projects.value.find((item) => item.id === studyForm.projectId))

const {
  sorted: sortedRows,
  registerMany: registerRowSortColumns,
  sortHeader: rowSortHeader,
} = useClientSort({ items: rows })

registerRowSortColumns([
  { key: 'source', resolver: (r) => r.sourceLabel, type: 'string' },
  { key: 'origin', resolver: (r) => r.originLabel, type: 'string' },
  { key: 'product', resolver: (r) => r.productName, type: 'string' },
  { key: 'program', resolver: (r) => r.programCode, type: 'string' },
  { key: 'moa', resolver: (r) => r.moa, type: 'string' },
  { key: 'project', resolver: (r) => r.projectCode, type: 'string' },
  { key: 'ta', resolver: (r) => r.therapeuticAreaName, type: 'string' },
  { key: 'indication', resolver: (r) => r.indication, type: 'string' },
  { key: 'studyNo', resolver: (r) => r.studyCode, type: 'string' },
  { key: 'phaseStatus', resolver: (r) => r.phaseStatusCode, type: 'string' },
])

const {
  sorted: sortedPrograms,
  registerMany: registerProgramSortColumns,
  sortHeader: programSortHeader,
} = useClientSort({ items: programs })

registerProgramSortColumns([
  { key: 'program', resolver: (p) => p.code, type: 'string' },
  { key: 'source', resolver: (p) => p.sourceLabel, type: 'string' },
  { key: 'origin', resolver: (p) => p.originLabel, type: 'string' },
  { key: 'product', resolver: (p) => p.productName, type: 'string' },
  { key: 'moa', resolver: (p) => p.moa, type: 'string' },
])

const programForm = reactive<ProgramInput>({
  code: '', productName: '', moa: '', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC',
})
const projectForm = reactive<ProjectInput>({
  code: '', programId: 0, indication: '', therapeuticAreaCode: '',
})
const studyForm = reactive({ code: '', programId: 0, projectId: 0, phaseStatusCode: 'PHASE_1' })

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const [nextPrograms, nextProjects, nextAreas] = await Promise.all([
      apiClient.listPrograms(), apiClient.listProjects(),
      apiClient.listTherapeuticAreas(),
    ])
    programs.value = nextPrograms
    projects.value = nextProjects
    therapeuticAreas.value = nextAreas
    if (selectedProgramId.value && !nextPrograms.some((item) => item.id === selectedProgramId.value)) {
      selectedProgramId.value = undefined
      projectDrawerOpen.value = false
    }
    expandedProgramIds.value = new Set(
      [...expandedProgramIds.value].filter((id) => nextPrograms.some((item) => item.id === id)),
    )
    await load()
  } catch (reason) {
    showNotice(messageOf(reason, '管线配置加载失败'), 'error')
    loading.value = false
  }
}

function resetFeedback() {
  error.value = ''
  hideNotice()
}

function openProgram(program?: PipelineProgram) {
  resetFeedback()
  editingProgram.value = program
  editingProject.value = undefined
  Object.assign(programForm, program ? {
    code: program.code, productName: program.productName,
    moa: program.moa ?? '', sourceCode: program.sourceCode, originCode: program.originCode,
  } : { code: '', productName: '', moa: '', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' })
  entityDialog.value = 'program'
}

function openProject(project?: PipelineProject, programId = selectedProgramId.value) {
  resetFeedback()
  editingProgram.value = undefined
  editingProject.value = project
  Object.assign(projectForm, project ? {
    code: project.code, programId: project.programId,
    indication: project.indication, therapeuticAreaCode: project.therapeuticAreaCode,
  } : { code: '', programId: programId ?? 0, indication: '', therapeuticAreaCode: therapeuticAreas.value[0]?.code ?? '' })
  entityDialog.value = 'project'
}

function manageProgram(program: PipelineProgram) {
  selectedProgramId.value = program.id
  projectDrawerOpen.value = true
}

function projectsOfProgram(programId: number) {
  return projects.value.filter((item) => item.programId === programId)
}

function toggleProgramExpand(program: PipelineProgram) {
  const next = new Set(expandedProgramIds.value)
  if (next.has(program.id)) next.delete(program.id)
  else next.add(program.id)
  expandedProgramIds.value = next
}

function isRowEditing(project: PipelineProject) {
  return !entityDialog.value && editingProject.value?.id === project.id
}

function startProjectRowEdit(project: PipelineProject) {
  resetFeedback()
  editingProgram.value = undefined
  editingProject.value = project
  Object.assign(projectForm, {
    code: project.code, programId: project.programId,
    indication: project.indication, therapeuticAreaCode: project.therapeuticAreaCode,
  })
}

function cancelProjectRowEdit() {
  editingProject.value = undefined
}

async function saveProgram() {
  programSaving.value = true
  resetFeedback()
  try {
    let saved: PipelineProgram
    if (!editingProgram.value) saved = await apiClient.createProgram({ ...programForm })
    else {
      const input: ProgramUpdateInput = {
        productName: programForm.productName, moa: programForm.moa,
        sourceCode: programForm.sourceCode, originCode: programForm.originCode,
        expectedVersion: editingProgram.value.version,
      }
      saved = await apiClient.updateProgram(editingProgram.value.id, input)
    }
    entityDialog.value = undefined
    studyForm.programId = saved.id
    studyForm.projectId = 0
    selectedProgramId.value = saved.id
    showNotice('Program 已保存')
    await loadAll()
    if (returnToStudyAfterCreate.value) returnToStudy()
  } catch (reason) {
    showNotice(messageOf(reason, 'Program 保存失败'), 'error')
  } finally {
    programSaving.value = false
  }
}

async function saveProject() {
  projectSaving.value = true
  resetFeedback()
  try {
    let saved: PipelineProject
    if (!editingProject.value) saved = await apiClient.createProject({ ...projectForm })
    else {
      const input: ProjectUpdateInput = {
        indication: projectForm.indication,
        therapeuticAreaCode: projectForm.therapeuticAreaCode,
        expectedVersion: editingProject.value.version,
      }
      saved = await apiClient.updateProject(editingProject.value.id, input)
    }
    entityDialog.value = undefined
    editingProject.value = undefined
    studyForm.programId = saved.programId
    studyForm.projectId = saved.id
    selectedProgramId.value = saved.programId
    showNotice('Project 已保存')
    await loadAll()
    if (returnToStudyAfterCreate.value) returnToStudy()
  } catch (reason) {
    showNotice(messageOf(reason, 'Project 保存失败'), 'error')
  } finally {
    projectSaving.value = false
  }
}

function openStudy(row?: PipelineConfigRow) {
  resetFeedback()
  returnToStudyAfterCreate.value = false
  editingStudy.value = row
  Object.assign(studyForm, row ? {
    code: row.studyCode, programId: row.programId,
    projectId: row.projectId, phaseStatusCode: row.phaseStatusCode,
  } : { code: '', programId: programs.value[0]?.id ?? 0, projectId: 0, phaseStatusCode: 'PHASE_1' })
  studyDialog.value = true
}

function selectStudyProgram(id: number, event: MouseEvent) {
  studyForm.programId = id
  if (!selectedProjectsForStudy().some((item) => item.id === studyForm.projectId)) studyForm.projectId = 0
  closeDetails(event)
}

function selectStudyProject(id: number, event: MouseEvent) {
  studyForm.projectId = id
  closeDetails(event)
}

function closeDetails(event: MouseEvent) {
  ;(event.currentTarget as HTMLElement).closest('details')?.removeAttribute('open')
}

function onDocumentPointerDown(event: PointerEvent) {
  const target = event.target as Node
  for (const details of [studyProgramDetails.value, studyProjectDetails.value]) {
    if (details?.open && !details.contains(target)) details.removeAttribute('open')
  }
}

const studySearchTimer = ref<ReturnType<typeof setTimeout>>()
function filterStudies() {
  if (studySearchTimer.value) clearTimeout(studySearchTimer.value)
  studySearchTimer.value = setTimeout(applyFilters, 300)
}

function quickCreateProgram() {
  returnToStudyAfterCreate.value = true
  studyDialog.value = false
  view.value = 'entities'
  openProgram()
}

function quickCreateProject() {
  if (!studyForm.programId) {
    showNotice('请先选择或新建 Program', 'error')
    return
  }
  returnToStudyAfterCreate.value = true
  studyDialog.value = false
  view.value = 'entities'
  selectedProgramId.value = studyForm.programId
  projectDrawerOpen.value = true
  openProject(undefined, studyForm.programId)
}

function returnToStudy() {
  returnToStudyAfterCreate.value = false
  projectDrawerOpen.value = false
  view.value = 'studies'
  studyDialog.value = true
}

function closeEntityDialog() {
  entityDialog.value = undefined
  editingProgram.value = undefined
  editingProject.value = undefined
  if (returnToStudyAfterCreate.value) returnToStudy()
}

function selectedProjectsForStudy() {
  return projects.value.filter((item) => item.programId === studyForm.programId)
}

async function saveStudy() {
  studySaving.value = true
  resetFeedback()
  try {
    if (editingStudy.value) {
      await apiClient.updateStudyConfig(editingStudy.value.studyId, {
        projectId: studyForm.projectId, phaseStatusCode: studyForm.phaseStatusCode,
        expectedVersion: editingStudy.value.version,
      })
    } else {
      await apiClient.createStudyConfig({
        code: studyForm.code, projectId: studyForm.projectId, phase: studyForm.phaseStatusCode,
      })
    }
    studyDialog.value = false
    showNotice('Study 配置已保存')
    await loadAll()
  } catch (reason) {
    showNotice(messageOf(reason, 'Study 保存失败'), 'error')
  } finally {
    studySaving.value = false
  }
}

async function remove(kind: 'program' | 'project' | 'study', id: number, label: string) {
  if (kind === 'study') {
    await confirmRemoveStudy(id, label)
    return
  }
  if (!window.confirm(`确认删除 ${label}？存在引用时系统会拒绝删除。`)) return
  resetFeedback()
  try {
    if (kind === 'program') await apiClient.deleteProgram(id)
    else await apiClient.deleteProject(id)
    showNotice(`${label} 已删除`)
    await loadAll()
  } catch (reason) {
    showNotice(messageOf(reason, '删除失败'), 'error')
  }
}

async function confirmRemoveStudy(id: number, label: string) {
  studyDeleteTarget.value = { id, label }
  studyDeletePreview.value = undefined
  studyDeleteDialog.value = true
  studyDeleteLoading.value = true
  resetFeedback()
  try {
    studyDeletePreview.value = await apiClient.getStudyDeletePreview(id)
  } catch (reason) {
    closeStudyDeleteDialog()
    showNotice(messageOf(reason, '无法加载删除预览'), 'error')
  } finally {
    studyDeleteLoading.value = false
  }
}

function closeStudyDeleteDialog() {
  studyDeleteDialog.value = false
  studyDeleteTarget.value = undefined
  studyDeletePreview.value = undefined
}

async function executeRemoveStudy() {
  if (!studyDeleteTarget.value) return
  studyDeleteSaving.value = true
  resetFeedback()
  try {
    await apiClient.deleteStudyConfig(studyDeleteTarget.value.id)
    const label = studyDeleteTarget.value.label
    closeStudyDeleteDialog()
    showNotice(`${label} 已删除`)
    await loadAll()
  } catch (reason) {
    showNotice(messageOf(reason, '删除失败'), 'error')
  } finally {
    studyDeleteSaving.value = false
  }
}

const studyDeleteReferenceTotal = computed(() => {
  const preview = studyDeletePreview.value
  if (!preview) return 0
  return preview.milestoneCount + preview.riskCount + preview.teamCount + preview.monthlyReportCount
})

useEscapeClose(studyDeleteDialog, closeStudyDeleteDialog)

function messageOf(reason: unknown, fallback: string) {
  return formatApiError(reason, fallback)
}

function onEscape(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  if (studyDeleteDialog.value) closeStudyDeleteDialog()
  else if (entityDialog.value) closeEntityDialog()
  else if (studyDialog.value) studyDialog.value = false
  else projectDrawerOpen.value = false
}

onMounted(() => {
  window.addEventListener('keydown', onEscape)
  document.addEventListener('pointerdown', onDocumentPointerDown)
  void loadAll()
})
onUnmounted(() => {
  window.removeEventListener('keydown', onEscape)
  document.removeEventListener('pointerdown', onDocumentPointerDown)
  if (studySearchTimer.value) clearTimeout(studySearchTimer.value)
})
</script>

<template>
  <section class="page-content page-content--fill config-page">
    <div class="config-tabs" role="tablist" aria-label="管线配置视图">
      <button :class="{ active: view === 'studies' }" role="tab" type="button" @click="view = 'studies'">Study 明细</button>
      <button :class="{ active: view === 'entities' }" role="tab" type="button" @click="view = 'entities'">Program / Project 管理</button>
    </div>
    <div v-if="notice" class="config-notice" :class="{ 'config-notice--error': noticeType === 'error' }" role="status">{{ notice }}<button type="button" aria-label="关闭提示" @click="hideNotice">×</button></div>

    <template v-if="view === 'studies'">
      <div class="page-toolbar">
        <label class="config-study-search"><span class="sr-only">搜索 Study、TA、Program 或 Project</span><input v-model="filters.keyword" type="search" placeholder="搜索 Study / TA / Program / Project" @input="filterStudies"></label>
        <button v-if="canCreate" class="primary-button" type="button" @click="openStudy()">＋ 新增 Study</button>
      </div>
      <PageState :loading :empty="!rows.length">
        <div class="data-card config-table-card"><table class="data-table config-table"><thead><tr>
          <th v-bind="rowSortHeader('source')">Source</th>
          <th v-bind="rowSortHeader('origin')">Origin</th>
          <th v-bind="rowSortHeader('product')">Product</th>
          <th v-bind="rowSortHeader('program')">Program</th>
          <th v-bind="rowSortHeader('moa')">MOA</th>
          <th v-bind="rowSortHeader('project')">Project</th>
          <th v-bind="rowSortHeader('ta')">TA</th>
          <th v-bind="rowSortHeader('indication')">Indication</th>
          <th v-bind="rowSortHeader('studyNo')">Study No.</th>
          <th v-bind="rowSortHeader('phaseStatus')">Phase Status</th>
          <th>操作</th>
          <th v-if="canAudit">操作日志</th>
        </tr></thead><tbody><tr v-for="row in sortedRows" :key="row.studyId">
          <td>{{ row.sourceLabel }}</td><td>{{ row.originLabel }}</td><td>{{ row.productName }}</td><td class="mono">{{ row.programCode }}</td>
          <td>{{ row.moa || '—' }}</td><td class="mono">{{ row.projectCode }}</td><td>{{ row.therapeuticAreaName }}</td><td>{{ row.indication }}</td>
          <td class="mono">{{ row.studyCode }}</td><td><span class="status-chip status-chip--blue">{{ row.phaseStatusCode }}</span></td>
          <td class="row-actions"><button v-if="canUpdate" type="button" @click="openStudy(row)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('study', row.studyId, row.studyCode)">删除</button></td>
          <td v-if="canAudit"><button class="text-button" type="button" @click="openRecordAuditLogs(`${row.studyCode} 操作日志`, 'STUDY', row.studyId)">查看</button></td>
        </tr></tbody></table></div>
      </PageState>
      <ListPagination
        v-if="!loading"
        :total="studyTotalItems"
        :page="studyPage"
        :page-size="studyPageSize"
        :total-pages="studyTotalPages"
        aria-label="Study 分页"
        @update:page="goToStudyPage"
        @update:page-size="changeStudyPageSize"
      />
    </template>

    <template v-else>
      <div class="page-toolbar entity-toolbar"><div><strong>Program 管理</strong><span>点击 Program 行展开 Project 列表并直接编辑；新增 Project 在「管理」抽屉中维护。</span></div><button v-if="canCreate" class="primary-button" type="button" @click="openProgram()">＋ 新增 Program</button></div>
      <PageState :loading :empty="!programs.length">
        <div class="data-card"><table class="data-table entity-program-table"><thead><tr>
          <th v-bind="programSortHeader('program')">Program</th>
          <th v-bind="programSortHeader('source')">Source</th>
          <th v-bind="programSortHeader('origin')">Origin</th>
          <th v-bind="programSortHeader('product')">Product</th>
          <th v-bind="programSortHeader('moa')">MOA</th>
          <th>操作</th>
          <th v-if="canAudit">操作日志</th>
        </tr></thead><tbody>
          <template v-for="program in sortedPrograms" :key="program.id">
            <tr class="program-row" @click="toggleProgramExpand(program)"><td class="mono strong"><span class="expand-caret" :class="{ open: expandedProgramIds.has(program.id) }">▸</span>{{ program.code }}</td><td>{{ program.sourceLabel }}</td><td>{{ program.originLabel }}</td><td>{{ program.productName }}</td><td>{{ program.moa || '—' }}</td><td class="row-actions"><button type="button" @click.stop="manageProgram(program)">管理</button><button v-if="canUpdate" type="button" @click.stop="openProgram(program)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click.stop="remove('program', program.id, program.code)">删除</button></td><td v-if="canAudit"><button class="text-button" type="button" @click.stop="openRecordAuditLogs(`${program.code} 操作日志`, 'PROGRAM', program.id)">查看</button></td></tr>
            <template v-if="expandedProgramIds.has(program.id)">
              <tr v-if="!projectsOfProgram(program.id).length" class="project-sub-row"><td class="empty-inline" :colspan="canAudit ? 7 : 6">该 Program 尚无 Project</td></tr>
              <tr v-for="(project, index) in projectsOfProgram(program.id)" :key="project.id" class="project-sub-row">
                <td class="mono"><span class="project-index">({{ index + 1 }})</span><LabeledValue label="Project:" :value="project.code" /></td>
                <td><select v-if="isRowEditing(project)" v-model="projectForm.therapeuticAreaCode" required @click.stop><option value="" disabled>请选择治疗领域</option><option v-for="area in therapeuticAreas" :key="area.id" :value="area.code">{{ area.name }}（{{ area.code }}）</option></select><LabeledValue v-else label="TA:" :value="project.therapeuticAreaName" /></td>
                <td colspan="3"><textarea v-if="isRowEditing(project)" v-model="projectForm.indication" required maxlength="500" rows="1" @click.stop></textarea><LabeledValue v-else label="Indication:" :value="project.indication" /></td>
                <td class="row-actions">
                  <template v-if="isRowEditing(project)"><button type="button" :disabled="projectSaving" @click.stop="saveProject()">{{ projectSaving ? '保存中…' : '保存' }}</button><button type="button" @click.stop="cancelProjectRowEdit">取消</button></template>
                  <template v-else><button v-if="canUpdate" type="button" @click.stop="startProjectRowEdit(project)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click.stop="remove('project', project.id, project.code)">删除</button></template>
                </td>
                <td v-if="canAudit"><button class="text-button" type="button" @click.stop="openRecordAuditLogs(`${project.code} 操作日志`, 'PROJECT', project.id)">查看</button></td>
              </tr>
            </template>
          </template>
        </tbody></table></div>
      </PageState>
    </template>

    <div v-if="projectDrawerOpen" class="drawer-backdrop" @mousedown.self="projectDrawerOpen = false">
      <aside class="project-drawer" role="dialog" aria-modal="true" aria-labelledby="project-drawer-title">
        <header><div><h2 id="project-drawer-title">{{ selectedProgram?.code }} 的 Project</h2><p>在当前 Program 下新增、编辑或删除 Project。</p></div><button type="button" aria-label="关闭 Project 管理" @click="projectDrawerOpen = false">×</button></header>
        <div class="drawer-toolbar"><span>{{ selectedProjects.length }} 个 Project</span><button v-if="canCreate" class="primary-button" type="button" @click="openProject(undefined, selectedProgramId)">＋ 新增 Project</button></div>
        <div v-if="!selectedProjects.length" class="empty-inline">该 Program 尚无 Project</div>
        <div v-else class="drawer-project-list">
          <table class="data-table drawer-project-table">
            <thead><tr><th>Project</th><th>Study</th><th>操作</th><th v-if="canAudit">操作日志</th></tr></thead>
            <tbody><tr v-for="project in selectedProjects" :key="project.id">
              <td><strong class="mono"><LabeledValue label="Project:" :value="project.code" /></strong><p><LabeledValue label="Indication:" :value="project.indication" /></p><small><LabeledValue label="TA:" :value="project.therapeuticAreaName" /></small></td>
              <td><LabeledValue label="Study:" :value="project.studyCount" /></td>
              <td class="row-actions"><button v-if="canUpdate" type="button" @click="openProject(project)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('project', project.id, project.code)">删除</button></td>
              <td v-if="canAudit"><button class="text-button" type="button" @click="openRecordAuditLogs(`${project.code} 操作日志`, 'PROJECT', project.id)">查看</button></td>
            </tr></tbody>
          </table>
        </div>
      </aside>
    </div>

    <div v-if="entityDialog" class="drawer-backdrop entity-form-backdrop" @mousedown.self="closeEntityDialog">
      <form class="entity-form-drawer config-dialog" role="dialog" aria-modal="true" @submit.prevent="entityDialog === 'program' ? saveProgram() : saveProject()">
        <header><div><h2>{{ entityDialog === 'program' ? (editingProgram ? '编辑 Program' : '新增 Program') : (editingProject ? '编辑 Project' : '新增 Project') }}</h2><p>实体编号创建后不可修改。</p></div><button type="button" aria-label="关闭" @click="closeEntityDialog">×</button></header>
        <div v-if="entityDialog === 'program'" class="role-form-grid">
          <label>Product *<input v-model="programForm.productName" required maxlength="200"></label>
          <label>Program *<input v-model="programForm.code" required maxlength="64" :disabled="!!editingProgram" placeholder="例如 PRG-001"></label>
          <label>MOA<input v-model="programForm.moa" maxlength="500"><small>MOA：药物通过什么机制产生作用。</small></label>
          <label>Source *<select v-model="programForm.sourceCode"><option value="SELF_DEVELOPED">自研</option><option value="IN_LICENSE">引进</option><option value="COOPERATION">合作</option></select></label>
          <label>Origin *<select v-model="programForm.originCode"><option value="DOMESTIC">国产</option><option value="IMPORTED">进口</option></select></label>
        </div>
        <div v-else class="role-form-grid">
          <label>所属 Program *<select v-model.number="projectForm.programId" required disabled><option v-for="program in programs" :key="program.id" :value="program.id">{{ program.code }}</option></select></label>
          <label>Project 编号 *<input v-model="projectForm.code" required maxlength="64" :disabled="!!editingProject" placeholder="例如 PRJ-001"></label>
          <label>TA *<select v-model="projectForm.therapeuticAreaCode" required><option value="" disabled>请选择治疗领域</option><option v-for="area in therapeuticAreas" :key="area.id" :value="area.code">{{ area.name }}（{{ area.code }}）</option></select><small>TA：治疗领域，如肿瘤、自身免疫。</small></label>
          <label class="form-wide">Indication 适应症 *<textarea v-model="projectForm.indication" required maxlength="500" rows="3"></textarea></label>
        </div>
        <footer><button class="secondary-button" type="button" @click="closeEntityDialog">取消</button><button class="primary-button" type="submit" :disabled="entityDialog === 'program' ? programSaving : projectSaving">{{ entityDialog === 'program' ? (programSaving ? '保存中…' : '保存 Program') : (projectSaving ? '保存中…' : '保存 Project') }}</button></footer>
      </form>
    </div>

    <div v-if="studyDeleteDialog" class="dialog-backdrop" @mousedown.self="closeStudyDeleteDialog">
      <div class="role-dialog role-dialog--sm" role="dialog" aria-modal="true" aria-labelledby="study-delete-title">
        <header>
          <div>
            <h2 id="study-delete-title">确认删除 Study</h2>
            <p>删除后关联业务数据将一并移除，且不可恢复。</p>
          </div>
          <button type="button" aria-label="关闭" @click="closeStudyDeleteDialog">×</button>
        </header>
        <div class="dialog-body">
          <p v-if="studyDeleteLoading" class="dialog-text">正在统计关联数据…</p>
          <template v-else-if="studyDeletePreview && studyDeleteTarget">
            <p class="dialog-text">
              确定删除 Study <strong class="mono">{{ studyDeleteTarget.label }}</strong> 吗？
            </p>
            <ul class="delete-preview-list">
              <li><span>里程碑</span><strong>{{ studyDeletePreview.milestoneCount }}</strong></li>
              <li><span>风险</span><strong>{{ studyDeletePreview.riskCount }}</strong></li>
              <li><span>团队管理</span><strong>{{ studyDeletePreview.teamCount }}</strong></li>
              <li v-if="studyDeletePreview.monthlyReportCount > 0">
                <span>Study 月报</span><strong>{{ studyDeletePreview.monthlyReportCount }}</strong>
              </li>
            </ul>
            <p v-if="studyDeleteReferenceTotal > 0" class="dialog-text dialog-text--muted">
              以上 {{ studyDeleteReferenceTotal }} 条关联数据将随 Study 一并删除。
            </p>
          </template>
        </div>
        <footer>
          <button class="secondary-button" type="button" @click="closeStudyDeleteDialog">取消</button>
          <button
            class="primary-button danger-solid-button"
            type="button"
            :disabled="studyDeleteLoading || studyDeleteSaving || !studyDeletePreview"
            @click="executeRemoveStudy"
          >
            {{ studyDeleteSaving ? '删除中…' : '确认删除' }}
          </button>
        </footer>
      </div>
    </div>

    <div v-if="studyDialog" class="dialog-backdrop" @mousedown.self="studyDialog = false">
      <form class="role-dialog config-dialog" role="dialog" aria-modal="true" @submit.prevent="saveStudy">
        <header><div><h2>{{ editingStudy ? '编辑 Study 配置' : '新增 Study 配置' }}</h2><p>Study 必须绑定已落库的 Program 和 Project 编码。</p></div><button type="button" aria-label="关闭" @click="studyDialog = false">×</button></header>
        <div class="role-form-grid">
          <label>Study No. *<input v-model="studyForm.code" required maxlength="64" :disabled="!!editingStudy"></label>
          <label>Program *<details ref="studyProgramDetails" class="entity-select"><summary>{{ programs.find(item => item.id === studyForm.programId)?.code || '请选择 Program' }}</summary><div class="entity-select-menu" role="listbox"><button v-for="program in programs" :key="program.id" type="button" role="option" @click="selectStudyProgram(program.id, $event)">{{ program.code }}</button><button v-if="canCreate" class="entity-select-create" type="button" @click="quickCreateProgram">＋ 新建 Program</button></div></details></label>
          <label>Project *<details ref="studyProjectDetails" class="entity-select"><summary>{{ selectedStudyProject ? `${selectedStudyProject.code} — ${selectedStudyProject.indication || '—'}` : '请选择 Project' }}</summary><div class="entity-select-menu" role="listbox"><div class="entity-select-menu__header" aria-hidden="true"><span>Project</span><span>Indication</span><span>TA</span></div><button v-for="project in selectedProjectsForStudy()" :key="project.id" type="button" role="option" class="entity-select-menu__option" @click="selectStudyProject(project.id, $event)"><span class="mono">{{ project.code }}</span><span>{{ project.indication || '—' }}</span><span>{{ project.therapeuticAreaName }}</span></button><button v-if="canCreate" class="entity-select-create" type="button" @click="quickCreateProject">＋ 新建 Project</button></div></details></label>
          <label>Phase Status *<select v-model="studyForm.phaseStatusCode"><option v-for="opt in phaseStatusOptions" :key="opt.code" :value="opt.code">{{ opt.label }}</option></select></label>
        </div>
        <footer><button class="secondary-button" type="button" @click="studyDialog = false">取消</button><button class="primary-button" type="submit" :disabled="studySaving || !studyForm.projectId">{{ studySaving ? '保存中…' : '保存 Study' }}</button></footer>
      </form>
    </div>
    <AuditLogDrawer v-bind="auditDrawer" @close="closeAuditLogs" />
  </section>
</template>
