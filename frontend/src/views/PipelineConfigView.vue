<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError, apiClient } from '../api/client'
import type {
  PipelineConfigRow, PipelineProgram, PipelineProject, ProgramInput,
  ProgramUpdateInput, ProjectInput, ProjectUpdateInput, TherapeuticArea,
} from '../api/types'
import PageState from '../components/PageState.vue'
import { session } from '../session'

type ViewMode = 'studies' | 'entities'
type EntityKind = 'program' | 'project'

const view = ref<ViewMode>('studies')
const rows = ref<PipelineConfigRow[]>([])
const programs = ref<PipelineProgram[]>([])
const projects = ref<PipelineProject[]>([])
const therapeuticAreas = ref<TherapeuticArea[]>([])
const selectedProgramId = ref<number>()
const loading = ref(true)
const error = ref('')
const notice = ref('')
const saving = ref(false)
const entityDialog = ref<EntityKind>()
const editingProgram = ref<PipelineProgram>()
const editingProject = ref<PipelineProject>()
const studyDialog = ref(false)
const editingStudy = ref<PipelineConfigRow>()

const permissions = computed(() => session.currentUser.value?.permissions ?? [])
const canCreate = computed(() => permissions.value.includes('config.create'))
const canUpdate = computed(() => permissions.value.includes('config.update'))
const canDelete = computed(() => permissions.value.includes('config.delete'))
const selectedProjects = computed(() => projects.value.filter((item) => item.programId === selectedProgramId.value))

const programForm = reactive<ProgramInput>({
  code: '', productName: '', moa: '', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC',
})
const projectForm = reactive<ProjectInput>({
  code: '', programId: 0, indication: '', therapeuticAreaCode: '',
})
const studyForm = reactive({ code: '', name: '', programId: 0, projectId: 0, phaseStatusCode: 'PRE_IND' })

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const [nextRows, nextPrograms, nextProjects, nextAreas] = await Promise.all([
      apiClient.listPipelineConfig(), apiClient.listPrograms(), apiClient.listProjects(),
      apiClient.listTherapeuticAreas(),
    ])
    rows.value = nextRows
    programs.value = nextPrograms
    projects.value = nextProjects
    therapeuticAreas.value = nextAreas
    if (!selectedProgramId.value || !nextPrograms.some((item) => item.id === selectedProgramId.value)) {
      selectedProgramId.value = nextPrograms[0]?.id
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
  editingProject.value = undefined
  editingProgram.value = program
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

async function resolveProgramCode() {
  const code = programForm.code.trim().toUpperCase()
  programForm.code = code
  if (!code) return
  try {
    const existing = (await apiClient.listPrograms(code))
      .find((item) => item.code.toUpperCase() === code)
    if (existing && existing.id !== editingProgram.value?.id) openProgram(existing)
  } catch (reason) {
    error.value = messageOf(reason, 'Program 编码查询失败')
  }
}

async function resolveProjectCode() {
  const code = projectForm.code.trim().toUpperCase()
  projectForm.code = code
  if (!code) return
  try {
    const existing = (await apiClient.listProjects(undefined, code))
      .find((item) => item.code.toUpperCase() === code)
    if (existing && existing.id !== editingProject.value?.id) openProject(existing)
  } catch (reason) {
    error.value = messageOf(reason, 'Project 编码查询失败')
  }
}

async function saveProgram() {
  saving.value = true
  resetFeedback()
  try {
    if (!editingProgram.value) {
      await apiClient.createProgram({ ...programForm })
    } else {
      const input: ProgramUpdateInput = {
        productName: programForm.productName, moa: programForm.moa,
        sourceCode: programForm.sourceCode, originCode: programForm.originCode,
      }
      await apiClient.updateProgram(editingProgram.value.id, input)
    }
    entityDialog.value = undefined
    notice.value = 'Program 已保存'
    await loadAll()
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
    if (!editingProject.value) {
      saved = await apiClient.createProject({ ...projectForm })
    } else {
      const input: ProjectUpdateInput = {
        indication: projectForm.indication,
        therapeuticAreaCode: projectForm.therapeuticAreaCode,
      }
      saved = await apiClient.updateProject(editingProject.value.id, input)
    }
    entityDialog.value = undefined
    studyForm.programId = saved.programId
    studyForm.projectId = saved.id
    notice.value = 'Project 实体已保存并选中'
    await loadAll()
  } catch (reason) {
    error.value = messageOf(reason, 'Project 保存失败')
  } finally {
    saving.value = false
  }
}

function openStudy(row?: PipelineConfigRow) {
  resetFeedback()
  editingStudy.value = row
  Object.assign(studyForm, row ? {
    code: row.studyCode, name: row.studyName, programId: row.programId,
    projectId: row.projectId, phaseStatusCode: row.phaseStatusCode,
  } : { code: '', name: '', programId: programs.value[0]?.id ?? 0, projectId: 0, phaseStatusCode: 'PRE_IND' })
  studyDialog.value = true
}

function onStudyProgramChanged() {
  if (!selectedProjectsForStudy().some((item) => item.id === studyForm.projectId)) studyForm.projectId = 0
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
        name: studyForm.name, projectId: studyForm.projectId, phaseStatusCode: studyForm.phaseStatusCode,
      })
    } else {
      await apiClient.createStudyConfig({
        code: studyForm.code, name: studyForm.name, projectId: studyForm.projectId,
        phase: studyForm.phaseStatusCode,
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

function messageOf(reason: unknown, fallback: string) {
  if (reason instanceof ApiError && reason.status === 409 && reason.details) {
    const counts = Object.entries(reason.details).filter(([, value]) => value !== '0')
      .map(([key, value]) => `${key}: ${value}`).join('，')
    return `${reason.message}${counts ? `（${counts}）` : ''}`
  }
  return reason instanceof Error ? reason.message : fallback
}

onMounted(loadAll)
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
        <span>以 Study（临床研究）为一行；Phase Status 决定其在管线总览中的起始阶段。</span>
        <button v-if="canCreate" class="primary-button" type="button" @click="openStudy()">＋ 新增 Study</button>
      </div>
      <PageState :loading :error="''" :empty="!rows.length">
        <div class="data-card config-table-card"><table class="data-table config-table"><thead><tr>
          <th>Source</th><th>Origin</th><th>Product</th><th>MOA</th><th>Program</th><th>Indication</th><th>Project</th><th>TA</th><th>Study No.</th><th>Study 名称</th><th>Phase Status</th><th>操作</th>
        </tr></thead><tbody><tr v-for="row in rows" :key="row.studyId">
          <td>{{ row.sourceLabel }}</td><td>{{ row.originLabel }}</td><td>{{ row.productName }}</td><td>{{ row.moa || '—' }}</td>
          <td><span class="mono">{{ row.programCode }}</span><small>{{ row.programName }}</small></td>
          <td>{{ row.indication }}</td><td><span class="mono">{{ row.projectCode }}</span><small>{{ row.projectName }}</small></td>
          <td>{{ row.therapeuticAreaName }}</td><td class="mono">{{ row.studyCode }}</td><td>{{ row.studyName }}</td>
          <td><span class="status-chip status-chip--blue">{{ row.phaseStatusLabel }}</span></td>
          <td class="row-actions"><button v-if="canUpdate" type="button" @click="openStudy(row)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('study', row.studyId, row.studyCode)">删除</button></td>
        </tr></tbody></table></div>
      </PageState>
    </template>

    <template v-else>
      <div class="entity-toolbar"><div><strong>实体主数据</strong><span>输入编码后，已存在则进入编辑，不存在则创建新实体。</span></div><div><button v-if="canCreate" class="secondary-button" type="button" @click="openProgram()">＋ Program</button><button v-if="canCreate" class="primary-button" type="button" @click="openProject()">＋ Project</button></div></div>
      <PageState :loading :error="''" :empty="!programs.length">
        <div class="entity-grid">
          <aside class="entity-list"><header><strong>Program</strong><span>{{ programs.length }}</span></header><button v-for="program in programs" :key="program.id" :class="{ active: selectedProgramId === program.id }" type="button" @click="selectedProgramId = program.id"><span><b class="mono">{{ program.code }}</b></span><small>{{ program.projectCount }} Project · {{ program.studyCount }} Study</small></button></aside>
          <div class="entity-detail">
            <header v-if="programs.find(item => item.id === selectedProgramId)" class="entity-summary"><div><strong>{{ programs.find(item => item.id === selectedProgramId)?.code }}</strong><span>{{ programs.find(item => item.id === selectedProgramId)?.productName }}</span></div><div class="row-actions"><button v-if="canUpdate" type="button" @click="openProgram(programs.find(item => item.id === selectedProgramId))">编辑 Program</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('program', selectedProgramId!, programs.find(item => item.id === selectedProgramId)?.code || 'Program')">删除</button></div></header>
            <div class="project-cards"><article v-for="project in selectedProjects" :key="project.id"><div><strong class="mono">{{ project.code }}</strong><p>{{ project.indication }}</p><small>{{ project.therapeuticAreaName }} · {{ project.studyCount }} Study</small></div><div class="row-actions"><button v-if="canUpdate" type="button" @click="openProject(project)">编辑</button><button v-if="canDelete" class="danger-link" type="button" @click="remove('project', project.id, project.code)">删除</button></div></article><div v-if="!selectedProjects.length" class="empty-inline">该 Program 尚无 Project</div></div>
          </div>
        </div>
      </PageState>
    </template>

    <div v-if="entityDialog" class="dialog-backdrop entity-dialog-backdrop" @mousedown.self="entityDialog = undefined">
      <form class="role-dialog config-dialog" role="dialog" aria-modal="true" @submit.prevent="entityDialog === 'program' ? saveProgram() : saveProject()">
        <header><div><h2>{{ entityDialog === 'program' ? (editingProgram ? '编辑 Program' : '新增 Program') : (editingProject ? '编辑 Project' : '新增 Project') }}</h2><p>编号失焦后自动判断新建或更新；实体编号创建后不可修改。</p></div><button type="button" aria-label="关闭" @click="entityDialog = undefined">×</button></header>
        <div v-if="entityDialog === 'program'" class="role-form-grid">
          <label>Program *<input v-model="programForm.code" required maxlength="64" :disabled="!!editingProgram" placeholder="例如 PRG-001" @blur="resolveProgramCode"></label>
          <label>Source *<select v-model="programForm.sourceCode"><option value="SELF_DEVELOPED">自研</option><option value="IN_LICENSE">引进</option><option value="COOPERATION">合作</option></select></label>
          <label>Origin *<select v-model="programForm.originCode"><option value="DOMESTIC">国产</option><option value="IMPORTED">进口</option></select></label>
          <label>Product *<input v-model="programForm.productName" required maxlength="200"></label>
          <label>MOA<input v-model="programForm.moa" maxlength="500"><small>MOA：药物通过什么机制产生作用。</small></label>
        </div>
        <div v-else class="role-form-grid">
          <label>所属 Program *<select v-model.number="projectForm.programId" required :disabled="!!editingProject"><option :value="0" disabled>请选择</option><option v-for="program in programs" :key="program.id" :value="program.id">{{ program.code }}</option></select></label>
          <label>Project 编号 *<input v-model="projectForm.code" required maxlength="64" :disabled="!!editingProject" placeholder="例如 PRJ-001" @blur="resolveProjectCode"></label>
          <label>TA *<select v-model="projectForm.therapeuticAreaCode" required><option value="" disabled>请选择治疗领域</option><option v-for="area in therapeuticAreas" :key="area.id" :value="area.code">{{ area.name }}（{{ area.code }}）</option></select><small>TA：治疗领域，如肿瘤、自身免疫。</small></label>
          <label class="form-wide">Indication 适应症 *<textarea v-model="projectForm.indication" required maxlength="500" rows="3"></textarea></label>
        </div>
        <footer><button class="secondary-button" type="button" @click="entityDialog = undefined">取消</button><button class="primary-button" type="submit" :disabled="saving">{{ saving ? '保存中…' : editingProgram || editingProject ? '更新实体' : '创建实体' }}</button></footer>
      </form>
    </div>

    <div v-if="studyDialog" class="dialog-backdrop" @mousedown.self="studyDialog = false">
      <form class="role-dialog config-dialog" role="dialog" aria-modal="true" @submit.prevent="saveStudy">
        <header><div><h2>{{ editingStudy ? '编辑 Study 配置' : '新增 Study 配置' }}</h2><p>Study 必须绑定已落库的 Project 实体。</p></div><button type="button" aria-label="关闭" @click="studyDialog = false">×</button></header>
        <div class="role-form-grid">
          <label>Study No. *<input v-model="studyForm.code" required maxlength="64" :disabled="!!editingStudy"></label>
          <label>Study 名称 *<input v-model="studyForm.name" required maxlength="200"></label>
          <label>Program *<select v-model.number="studyForm.programId" required @change="onStudyProgramChanged"><option :value="0" disabled>请选择</option><option v-for="program in programs" :key="program.id" :value="program.id">{{ program.code }} · {{ program.name }}</option></select></label>
          <label>Project *<span class="inline-control"><select v-model.number="studyForm.projectId" required><option :value="0" disabled>请选择</option><option v-for="project in selectedProjectsForStudy()" :key="project.id" :value="project.id">{{ project.code }} · {{ project.name }}</option></select><button v-if="canCreate" type="button" @click="openProject(undefined, studyForm.programId)">快捷新建</button></span></label>
          <label>Phase Status *<select v-model="studyForm.phaseStatusCode"><option value="PRE_IND">Pre-IND</option><option value="IND">IND</option><option value="PHASE_1">I期</option><option value="PHASE_2">II期</option><option value="PRE_3">Pre-III</option><option value="PHASE_3_1">III期-1</option><option value="PHASE_3_2">III期-2</option></select></label>
        </div>
        <footer><button class="secondary-button" type="button" @click="studyDialog = false">取消</button><button class="primary-button" type="submit" :disabled="saving || !studyForm.projectId">{{ saving ? '保存中…' : '保存 Study' }}</button></footer>
      </form>
    </div>

  </section>
</template>
