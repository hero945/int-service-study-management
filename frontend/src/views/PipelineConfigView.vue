<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ApiError, apiClient } from '../api/client'
import type {
  PipelineConfigRow, PipelineProgram, PipelineProject, ProgramInput,
  ProgramUpdateInput, ProjectInput, ProjectUpdateInput, TherapeuticArea,
} from '../api/types'
import PageState from '../components/PageState.vue'
import { PIPELINE_PHASE_STATUS_OPTIONS } from '../domain/milestone-filters'
import { phaseDisplayLabel } from '../domain/pipeline-status'
import { session } from '../session'

const phaseStatusOptions = PIPELINE_PHASE_STATUS_OPTIONS

type ViewMode = 'studies' | 'entities'
type EntityKind = 'program' | 'project'

const view = ref<ViewMode>('studies')
const rows = ref<PipelineConfigRow[]>([])
const programs = ref<PipelineProgram[]>([])
const projects = ref<PipelineProject[]>([])
const therapeuticAreas = ref<TherapeuticArea[]>([])
const selectedProgramId = ref<number>()
const projectDrawerOpen = ref(false)
const loading = ref(true)
const error = ref('')
const notice = ref('')
const saving = ref(false)
const entityDialog = ref<EntityKind>()
const editingProgram = ref<PipelineProgram>()
const editingProject = ref<PipelineProject>()
const studyDialog = ref(false)
const editingStudy = ref<PipelineConfigRow>()
const returnToStudyAfterCreate = ref(false)
const studyProgramDetails = ref<HTMLDetailsElement>()
const studyProjectDetails = ref<HTMLDetailsElement>()
const studyQuery = ref('')
const studyPage = ref(1)
const STUDY_PAGE_SIZE = 10

const permissions = computed(() => session.currentUser.value?.permissions ?? [])
const canCreate = computed(() => permissions.value.includes('config.create'))
const canUpdate = computed(() => permissions.value.includes('config.update'))
const canDelete = computed(() => permissions.value.includes('config.delete'))
const selectedProgram = computed(() => programs.value.find((item) => item.id === selectedProgramId.value))
const selectedProjects = computed(() => projects.value.filter((item) => item.programId === selectedProgramId.value))
const filteredStudyRows = computed(() => {
  const keyword = studyQuery.value.trim().toLowerCase()
  if (!keyword) return rows.value
  return rows.value.filter((row) => [
    row.studyCode, row.therapeuticAreaCode, row.therapeuticAreaName, row.programCode,
  ].some((value) => value.toLowerCase().includes(keyword)))
})
const studyTotalPages = computed(() => Math.max(1, Math.ceil(filteredStudyRows.value.length / STUDY_PAGE_SIZE)))
const pagedStudyRows = computed(() => {
  const start = (studyPage.value - 1) * STUDY_PAGE_SIZE
  return filteredStudyRows.value.slice(start, start + STUDY_PAGE_SIZE)
})

const programForm = reactive<ProgramInput>({
  code: '', productName: '', moa: '', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC',
})
const projectForm = reactive<ProjectInput>({
  code: '', programId: 0, indication: '', therapeuticAreaCode: '',
})
const studyForm = reactive({ code: '', programId: 0, projectId: 0, phaseStatusCode: 'PRE_IND' })

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const [nextRows, nextPrograms, nextProjects, nextAreas] = await Promise.all([
      apiClient.listPipelineConfig(), apiClient.listPrograms(), apiClient.listProjects(),
      apiClient.listTherapeuticAreas(),
    ])
    rows.value = nextRows
    studyPage.value = Math.min(studyPage.value, studyTotalPages.value)
    programs.value = nextPrograms
    projects.value = nextProjects
    therapeuticAreas.value = nextAreas
    if (selectedProgramId.value && !nextPrograms.some((item) => item.id === selectedProgramId.value)) {
      selectedProgramId.value = undefined
      projectDrawerOpen.value = false
    }
  } catch (reason) {
    error.value = messageOf(reason, '管线配置加载失败')
  } finally {
    loading.value = false
  }
}

function resetFeedback() {
  error.value = ''
  notice.value = ''
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

async function saveProgram() {
  saving.value = true
  resetFeedback()
  try {
    let saved: PipelineProgram
    if (!editingProgram.value) saved = await apiClient.createProgram({ ...programForm })
    else {
      const input: ProgramUpdateInput = {
        productName: programForm.productName, moa: programForm.moa,
        sourceCode: programForm.sourceCode, originCode: programForm.originCode,
      }
      saved = await apiClient.updateProgram(editingProgram.value.id, input)
    }
    entityDialog.value = undefined
    studyForm.programId = saved.id
    studyForm.projectId = 0
    selectedProgramId.value = saved.id
    notice.value = 'Program 已保存'
    await loadAll()
    if (returnToStudyAfterCreate.value) returnToStudy()
  } catch (reason) {
    error.value = messageOf(reason, 'Program 保存失败')
  } finally {
    saving.value = false
  }
}

async function saveProject() {
  saving.value = true
  resetFeedback()
  try {
    let saved: PipelineProject
    if (!editingProject.value) saved = await apiClient.createProject({ ...projectForm })
    else {
      const input: ProjectUpdateInput = {
        indication: projectForm.indication,
        therapeuticAreaCode: projectForm.therapeuticAreaCode,
      }
      saved = await apiClient.updateProject(editingProject.value.id, input)
    }
    entityDialog.value = undefined
    studyForm.programId = saved.programId
    studyForm.projectId = saved.id
    selectedProgramId.value = saved.programId
    notice.value = 'Project 已保存'
    await loadAll()
    if (returnToStudyAfterCreate.value) returnToStudy()
  } catch (reason) {
    error.value = messageOf(reason, 'Project 保存失败')
  } finally {
    saving.value = false
  }
}

function openStudy(row?: PipelineConfigRow) {
  resetFeedback()
  returnToStudyAfterCreate.value = false
  editingStudy.value = row
  Object.assign(studyForm, row ? {
    code: row.studyCode, programId: row.programId,
    projectId: row.projectId, phaseStatusCode: row.phaseStatusCode,
  } : { code: '', programId: programs.value[0]?.id ?? 0, projectId: 0, phaseStatusCode: 'PRE_IND' })
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

function filterStudies() {
  studyPage.value = 1
}

function goToStudyPage(page: number) {
  studyPage.value = Math.min(Math.max(1, page), studyTotalPages.value)
}

function quickCreateProgram() {
  returnToStudyAfterCreate.value = true
  studyDialog.value = false
  view.value = 'entities'
  openProgram()
}

function quickCreateProject() {
  if (!studyForm.programId) {
    error.value = '请先选择或新建 Program'
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
  if (returnToStudyAfterCreate.value) returnToStudy()
}

function selectedProjectsForStudy() {
  return projects.value.filter((item) => item.programId === studyForm.programId)
}

async function saveStudy() {
  saving.value = true
  resetFeedback()
  try {
    if (editingStudy.value) {
      await apiClient.updateStudyConfig(editingStudy.value.studyId, {
        projectId: studyForm.projectId, phaseStatusCode: studyForm.phaseStatusCode,
      })
    } else {
      await apiClient.createStudyConfig({
        code: studyForm.code, projectId: studyForm.projectId, phase: studyForm.phaseStatusCode,
      })
    }
    studyDialog.value = false
    notice.value = 'Study 配置已保存'
    await loadAll()
  } catch (reason) {
    error.value = messageOf(reason, 'Study 保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(kind: 'program' | 'project' | 'study', id: number, label: string) {
  if (!window.confirm(`确认删除 ${label}？存在引用时系统会拒绝删除。`)) return
  resetFeedback()
  try {
    if (kind === 'program') await apiClient.deleteProgram(id)
    else if (kind === 'project') await apiClient.deleteProject(id)
    else await apiClient.deleteStudyConfig(id)
    notice.value = `${label} 已删除`
    await loadAll()
  } catch (reason) {
    error.value = messageOf(reason, '删除失败')
  }
}

function phaseLabel(code: string) {
  return phaseDisplayLabel(code)
}

function messageOf(reason: unknown, fallback: string) {
  if (reason instanceof ApiError && reason.status === 409 && reason.details) {
    const counts = Object.entries(reason.details).filter(([, value]) => value !== '0')
      .map(([key, value]) => `${key}: ${value}`).join('，')
    return `${reason.message}${counts ? `（${counts}）` : ''}`
  }
  return reason instanceof Error ? reason.message : fallback
}

function onEscape(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  if (entityDialog.value) closeEntityDialog()
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
})
</script>

<template>
  <section class="page-content config-page">
    <div class="config-tabs" role="tablist" aria-label="管线配置视图">
      <button :class="{ active: view === 'studies' }" role="tab" type="button" @click="view = 'studies'">Study 明细</button>
      <button :class="{ active: view === 'entities' }" role="tab" type="button" @click="view = 'entities'">Program / Project 管理</button>
    </div>
    <div v-if="notice" class="config-notice" role="status">{{ notice }}<button type="button" aria-label="关闭提示" @click="notice = ''">×</button></div>
    <div v-if="error" class="form-error config-error" role="alert">{{ error }}</div>

    <template v-if="view === 'studies'">
      <div class="page-toolbar">
        <label class="config-study-search"><span class="sr-only">搜索 Study、TA 或 Program</span><input v-model="studyQuery" type="search" placeholder="搜索 Study / TA / Program" @input="filterStudies"></label>
        <button v-if="canCreate" class="primary-button" type="button" @click="openStudy()">＋ 新增 Study</button>
      </div>
      <PageState :loading :error="''" :empty="!filteredStudyRows.length">
        <div class="data-card config-table-card"><table class="data-table config-table"><thead><tr>
          <th>Source</th><th>Origin</th><th>Product</th><th>Program</th><th>MOA</th><th>Project</th><th>TA</th><th>Indication</th><th>Study No.</th><th>Phase Status</th><th>操作</th>
        </tr></thead><tbody><tr v-for="row in pagedStudyRows" :key="row.studyId">
          <td>{{ row.sourceLabel }}</td><td>{{ row.originLabel }}</td><td>{{ row.productName }}</td><td class="mono">{{ row.programCode }}</td>
          <td>{{ row.moa || '—' }}</td><td class="mono">{{ row.projectCode }}</td><td>{{ row.therapeuticAreaName }}</td><td>{{ row.indication }}</td>
          <td class="mono">{{ row.studyCode }}</td><td><span class="status-chip status-chip--blue">{{ phaseLabel(row.phaseStatusCode) }}</span></td>
          <td class="row-actions"><button v-if="canUpdate" type="button" @click="openStudy(row)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('study', row.studyId, row.studyCode)">删除</button></td>
        </tr></tbody></table></div>
        <nav class="study-pagination" aria-label="Study 分页">
          <span>共 {{ filteredStudyRows.length }} 条 · 第 {{ studyPage }} / {{ studyTotalPages }} 页</span>
          <div><button type="button" :disabled="studyPage === 1" @click="goToStudyPage(studyPage - 1)">上一页</button><button type="button" :disabled="studyPage === studyTotalPages" @click="goToStudyPage(studyPage + 1)">下一页</button></div>
        </nav>
      </PageState>
    </template>

    <template v-else>
      <div class="entity-toolbar"><div><strong>Program 管理</strong><span>Project 在对应 Program 的管理抽屉中维护。</span></div><button v-if="canCreate" class="primary-button" type="button" @click="openProgram()">＋ 新增 Program</button></div>
      <PageState :loading :error="''" :empty="!programs.length">
        <div class="data-card"><table class="data-table entity-program-table"><thead><tr><th>Program</th><th>Source</th><th>Origin</th><th>Product</th><th>MOA</th><th>操作</th></tr></thead><tbody>
          <tr v-for="program in programs" :key="program.id"><td class="mono strong">{{ program.code }}</td><td>{{ program.sourceLabel }}</td><td>{{ program.originLabel }}</td><td>{{ program.productName }}</td><td>{{ program.moa || '—' }}</td><td class="row-actions"><button type="button" @click="manageProgram(program)">管理</button><button v-if="canUpdate" type="button" @click="openProgram(program)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('program', program.id, program.code)">删除</button></td></tr>
        </tbody></table></div>
      </PageState>
    </template>

    <div v-if="projectDrawerOpen" class="drawer-backdrop" @mousedown.self="projectDrawerOpen = false">
      <aside class="project-drawer" role="dialog" aria-modal="true" aria-labelledby="project-drawer-title">
        <header><div><h2 id="project-drawer-title">{{ selectedProgram?.code }} 的 Project</h2><p>在当前 Program 下新增、编辑或删除 Project。</p></div><button type="button" aria-label="关闭 Project 管理" @click="projectDrawerOpen = false">×</button></header>
        <div class="drawer-toolbar"><span>{{ selectedProjects.length }} 个 Project</span><button v-if="canCreate" class="primary-button" type="button" @click="openProject(undefined, selectedProgramId)">＋ 新增 Project</button></div>
        <div v-if="!selectedProjects.length" class="empty-inline">该 Program 尚无 Project</div>
        <div v-else class="drawer-project-list"><article v-for="project in selectedProjects" :key="project.id"><div><strong class="mono">{{ project.code }}</strong><p>{{ project.indication }}</p><small>{{ project.therapeuticAreaName }} · {{ project.studyCount }} Study</small></div><div class="row-actions"><button v-if="canUpdate" type="button" @click="openProject(project)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('project', project.id, project.code)">删除</button></div></article></div>
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
        <footer><button class="secondary-button" type="button" @click="closeEntityDialog">取消</button><button class="primary-button" type="submit" :disabled="saving">{{ saving ? '保存中…' : entityDialog === 'program' ? '保存 Program' : '保存 Project' }}</button></footer>
      </form>
    </div>

    <div v-if="studyDialog" class="dialog-backdrop" @mousedown.self="studyDialog = false">
      <form class="role-dialog config-dialog" role="dialog" aria-modal="true" @submit.prevent="saveStudy">
        <header><div><h2>{{ editingStudy ? '编辑 Study 配置' : '新增 Study 配置' }}</h2><p>Study 必须绑定已落库的 Program 和 Project 编码。</p></div><button type="button" aria-label="关闭" @click="studyDialog = false">×</button></header>
        <div class="role-form-grid">
          <label>Study No. *<input v-model="studyForm.code" required maxlength="64" :disabled="!!editingStudy"></label>
          <label>Program *<details ref="studyProgramDetails" class="entity-select"><summary>{{ programs.find(item => item.id === studyForm.programId)?.code || '请选择 Program' }}</summary><div class="entity-select-menu" role="listbox"><button v-for="program in programs" :key="program.id" type="button" role="option" @click="selectStudyProgram(program.id, $event)">{{ program.code }}</button><button v-if="canCreate" class="entity-select-create" type="button" @click="quickCreateProgram">＋ 新建 Program</button></div></details></label>
          <label>Project *<details ref="studyProjectDetails" class="entity-select"><summary>{{ projects.find(item => item.id === studyForm.projectId)?.code || '请选择 Project' }}</summary><div class="entity-select-menu" role="listbox"><button v-for="project in selectedProjectsForStudy()" :key="project.id" type="button" role="option" @click="selectStudyProject(project.id, $event)">{{ project.code }}</button><button v-if="canCreate" class="entity-select-create" type="button" @click="quickCreateProject">＋ 新建 Project</button></div></details></label>
          <label>Phase Status *<select v-model="studyForm.phaseStatusCode"><option v-for="opt in phaseStatusOptions" :key="opt.code" :value="opt.code">{{ opt.label }}</option></select></label>
        </div>
        <footer><button class="secondary-button" type="button" @click="studyDialog = false">取消</button><button class="primary-button" type="submit" :disabled="saving || !studyForm.projectId">{{ saving ? '保存中…' : '保存 Study' }}</button></footer>
      </form>
    </div>
  </section>
</template>
