<script setup lang="ts">
import { computed, nextTick, reactive, ref, toRef, watch } from 'vue'
import { ApiError, apiClient } from '../api/client'
import type {
  RiskAction, RiskActionInput, RiskActionStatus, RiskDetail, RiskFormOptions, RiskStatus,
} from '../api/types'
import {
  allowedNextActionStatuses,
  riskLevelLabel,
  riskStatusLabel,
} from '../domain/risk-labels'
import { riskBadge } from '../risk-badge'
import { todayIso } from '../domain/date-format'
import { useEscapeClose } from '../composables/useEscapeClose'
import { usePermissions } from '../composables/usePermissions'
import RiskActionEditor from './RiskActionEditor.vue'
import RiskActionList from './RiskActionList.vue'
import RiskForm from './RiskForm.vue'
import RiskTimeline from './RiskTimeline.vue'
import type { RiskActionEditorState, RiskFormState } from './risk-editor-types'

const props = defineProps<{ open: boolean; riskCode?: string }>()
const emit = defineEmits<{ close: []; saved: [] }>()

useEscapeClose(toRef(props, 'open'), () => requestClose())

const detail = ref<RiskDetail>()
const options = ref<RiskFormOptions>({
  studies: [], functions: [], owners: [],
  scoringRule: { id: 0, lowMax: 12, mediumMax: 36 },
})
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
const fieldErrors = reactive<Record<string, string>>({})
const closeButton = ref<HTMLButtonElement>()
const includeAssessment = ref(true)
const pendingActions = ref<RiskActionInput[]>([])
const pendingEditIndex = ref<number>()
const actionEditor = ref<RiskActionEditorState>()
const dirty = ref(false)
const snapshot = ref('')
const form = reactive<RiskFormState>({
  studyId: 0, functionLineId: 0, ownerUserId: 0,
  description: '', registeredDate: todayIso(),
  status: 'OPEN' as RiskStatus, statusReason: '',
  impact: 1, likelihood: 1, detectability: 1, assessmentReason: '',
})
const actionForm = reactive<RiskActionInput>({
  description: '', ownerUserId: 0, plannedDate: '', completedDate: '',
  status: 'OPEN', completionNote: '', changeReason: '',
})

const { permissions } = usePermissions()
const canUpdate = computed(() => props.riskCode
  ? permissions.value.includes('risk.update')
  : permissions.value.includes('risk.create'))
const canDelete = computed(() => !!props.riskCode && permissions.value.includes('risk.delete'))
const riskClosed = computed(() => detail.value?.risk.status === 'CLOSED')
const riskEditable = computed(() => canUpdate.value && !riskClosed.value)
const completedActionCount = computed(() =>
  (detail.value?.actions ?? []).filter(item => item.status === 'COMPLETED' || item.status === 'CANCELLED').length)
const uniqueActionStatuses = computed(() => {
  const from = actionEditor.value?.fromStatus
  if (!from) {
    return ['OPEN', 'IN_PROGRESS', 'COMPLETED'] as RiskActionStatus[]
  }
  const set = new Set<string>([from, ...allowedNextActionStatuses(from)])
  if (actionForm.completedDate && canMarkCompleted(from)) set.add('COMPLETED')
  return [...set] as RiskActionStatus[]
})
const showAddAction = computed(() =>
  (riskEditable.value || !detail.value) && canUpdate.value
  && !actionEditor.value && pendingEditIndex.value === undefined)

watch(() => props.open, async open => {
  if (!open) return
  await initialize()
  await nextTick(); closeButton.value?.focus()
})

watch(form, () => {
  if (!loading.value) dirty.value = serializeForm() !== snapshot.value
}, { deep: true })

watch(includeAssessment, () => {
  if (!loading.value) dirty.value = serializeForm() !== snapshot.value
})

function serializeForm() {
  return JSON.stringify({ ...form, includeAssessment: includeAssessment.value })
}

async function initialize() {
  loading.value = true; error.value = ''; success.value = ''; detail.value = undefined
  pendingActions.value = []; actionEditor.value = undefined; pendingEditIndex.value = undefined
  Object.keys(fieldErrors).forEach(key => delete fieldErrors[key])
  try {
    const initial = await apiClient.getRiskFormOptions()
    options.value = initial
    if (props.riskCode) {
      detail.value = await apiClient.getRisk(props.riskCode)
      form.studyId = detail.value.risk.studyId
      options.value = await apiClient.getRiskFormOptions(form.studyId)
      fillFromDetail(detail.value)
      includeAssessment.value = false
    } else {
      Object.assign(form, {
        studyId: initial.studies[0]?.id ?? 0, functionLineId: 0, ownerUserId: 0,
        description: '', registeredDate: todayIso(),
        status: 'OPEN', statusReason: '', impact: 1, likelihood: 1, detectability: 1,
        assessmentReason: '首次评估',
      })
      includeAssessment.value = true
      if (form.studyId) await studyChanged()
    }
    snapshot.value = serializeForm()
    dirty.value = false
  } catch (reason) { error.value = message(reason, '风险详情加载失败') }
  finally { loading.value = false }
}

function fillFromDetail(value: RiskDetail) {
  const latest = value.assessments[0]
  Object.assign(form, {
    studyId: value.risk.studyId,
    functionLineId: options.value.functions.find(item => item.code === value.risk.functionCode)?.id ?? 0,
    ownerUserId: value.risk.ownerUserId, description: value.risk.description,
    registeredDate: value.registeredDate, status: value.risk.status, statusReason: '',
    impact: latest?.impact ?? 1, likelihood: latest?.likelihood ?? 1,
    detectability: latest?.detectability ?? 1, assessmentReason: '',
  })
}

async function studyChanged() {
  if (!form.studyId) return
  options.value = await apiClient.getRiskFormOptions(form.studyId)
  if (!options.value.functions.some(item => item.id === form.functionLineId)) {
    form.functionLineId = options.value.functions[0]?.id ?? 0
  }
  if (!options.value.owners.some(item => item.id === form.ownerUserId)) {
    form.ownerUserId = options.value.owners[0]?.id ?? 0
  }
}

function assessmentInput() {
  return {
    impact: form.impact, likelihood: form.likelihood,
    detectability: form.detectability, reason: form.assessmentReason,
  }
}

function validateRiskForm(): boolean {
  Object.keys(fieldErrors).forEach(key => delete fieldErrors[key])
  if (!form.studyId) fieldErrors.studyId = '请选择 Study'
  if (!form.functionLineId) fieldErrors.functionLineId = '请选择功能线'
  if (!form.ownerUserId) fieldErrors.ownerUserId = '请选择 Owner'
  if (!form.description.trim()) fieldErrors.description = '请填写风险描述'
  if (detail.value && form.status !== detail.value.risk.status && !form.statusReason.trim()) {
    fieldErrors.statusReason = '关闭或重新打开风险时必须填写原因'
  }
  if (detail.value && form.status === 'CLOSED' && detail.value.risk.status !== 'CLOSED') {
    const active = detail.value.actions.some(item => item.status === 'OPEN' || item.status === 'IN_PROGRESS')
    if (active) {
      fieldErrors.status = '存在未完成的控制措施，请先完成或取消后再关闭'
    }
  }
  if ((!detail.value || includeAssessment.value) && !form.assessmentReason.trim()) {
    fieldErrors.assessmentReason = '请填写评估原因'
  }
  return Object.keys(fieldErrors).length === 0
}

function canMarkCompleted(fromStatus?: string) {
  if (!fromStatus) return true
  if (fromStatus === 'COMPLETED') return true
  return allowedNextActionStatuses(fromStatus).includes('COMPLETED')
}

function applyCompletedDateStatus() {
  if (!actionForm.completedDate) return
  const from = actionEditor.value?.fromStatus
  if (!canMarkCompleted(from)) {
    fieldErrors.actionCompleted = '已取消的措施请先重新打开，再标记完成'
    return
  }
  delete fieldErrors.actionCompleted
  if (actionForm.status !== 'COMPLETED') actionForm.status = 'COMPLETED'
}

function validateActionForm(): boolean {
  Object.keys(fieldErrors).forEach(key => {
    if (key.startsWith('action')) delete fieldErrors[key]
  })
  applyCompletedDateStatus()
  if (!actionForm.description.trim()) fieldErrors.actionDescription = '请填写措施内容'
  if (!actionForm.ownerUserId) fieldErrors.actionOwner = '请选择责任人'
  if (!actionForm.plannedDate) fieldErrors.actionPlanned = '请填写计划完成时间'
  if (actionForm.completedDate && !canMarkCompleted(actionEditor.value?.fromStatus)) {
    fieldErrors.actionCompleted = '已取消的措施请先重新打开，再标记完成'
  }
  if (actionForm.status === 'COMPLETED') {
    if (!actionForm.completedDate) fieldErrors.actionCompleted = '完成时必须填写实际完成时间'
    if (!actionForm.completionNote?.trim()) fieldErrors.actionNote = '完成时必须填写完成说明'
  }
  if (actionForm.status === 'CANCELLED' && !actionForm.completionNote?.trim()) {
    fieldErrors.actionNote = '取消时必须填写取消说明'
  }
  const from = actionEditor.value?.fromStatus
  if (from && (from === 'COMPLETED' || from === 'CANCELLED')
      && actionForm.status === 'IN_PROGRESS' && !actionForm.changeReason?.trim()) {
    fieldErrors.actionChangeReason = '重新打开措施时必须填写原因'
  }
  return !Object.keys(fieldErrors).some(key => key.startsWith('action'))
}

async function save() {
  if (!canUpdate.value || riskClosed.value && form.status === 'CLOSED') {
    if (riskClosed.value && form.status === 'CLOSED') {
      error.value = '已关闭的风险不可编辑，请先重新打开'
    }
    return
  }
  if (!validateRiskForm()) {
    error.value = '请修正标红的字段后再保存'
    return
  }
  saving.value = true; error.value = ''; success.value = ''
  try {
    if (!detail.value) {
      await apiClient.createRisk({
        studyId: form.studyId, functionLineId: form.functionLineId,
        ownerUserId: form.ownerUserId, description: form.description.trim(),
        registeredDate: form.registeredDate, assessment: assessmentInput(),
        actions: pendingActions.value,
      })
    } else {
      await apiClient.updateRisk(detail.value.risk.riskCode, {
        expectedVersion: detail.value.risk.version, studyId: form.studyId,
        functionLineId: form.functionLineId, ownerUserId: form.ownerUserId,
        description: form.description.trim(), registeredDate: form.registeredDate,
        status: form.status, statusReason: form.statusReason,
        assessment: includeAssessment.value ? assessmentInput() : undefined,
      })
    }
    await riskBadge.refresh()
    dirty.value = false
    emit('saved')
  } catch (reason) {
    error.value = reason instanceof ApiError && reason.code === 'RISK_VERSION_CONFLICT'
      ? '风险已被其他用户修改，请关闭后重新打开。' : message(reason, '风险保存失败')
  } finally { saving.value = false }
}

async function removeRisk() {
  if (!detail.value || !window.confirm(`确定删除 ${detail.value.risk.riskCode}？该操作会保留审计记录。`)) return
  saving.value = true
  try {
    await apiClient.deleteRisk(detail.value.risk.riskCode, detail.value.risk.version)
    await riskBadge.refresh()
    dirty.value = false
    emit('saved')
  } catch (reason) { error.value = message(reason, '风险删除失败') }
  finally { saving.value = false }
}

function editAction(action?: RiskAction) {
  pendingEditIndex.value = undefined
  actionEditor.value = action
    ? { id: action.id, version: action.version, fromStatus: action.status }
    : { fromStatus: undefined }
  Object.assign(actionForm, action ? {
    description: action.description, ownerUserId: action.ownerUserId,
    plannedDate: action.plannedDate ?? '', completedDate: action.completedDate ?? '',
    status: action.status, completionNote: action.completionNote, changeReason: '',
  } : {
    description: '', ownerUserId: options.value.owners[0]?.id ?? 0,
    plannedDate: '', completedDate: '', status: 'OPEN', completionNote: '', changeReason: '',
  })
  applyCompletedDateStatus()
}

function editPending(index: number) {
  actionEditor.value = undefined
  pendingEditIndex.value = index
  const action = pendingActions.value[index]
  Object.assign(actionForm, { ...action, changeReason: '' })
  applyCompletedDateStatus()
}

function cancelActionEditor() {
  actionEditor.value = undefined
  pendingEditIndex.value = undefined
}

function onCompletedDateInput() {
  applyCompletedDateStatus()
}

function onActionStatusChange() {
  if (actionForm.status !== 'COMPLETED' && actionForm.completedDate) {
    actionForm.completedDate = ''
  }
}

async function saveAction() {
  if (!validateActionForm()) {
    error.value = '请修正措施表单中的标红字段'
    return
  }
  if (!detail.value) {
    const payload = { ...actionForm }
    if (pendingEditIndex.value !== undefined) {
      pendingActions.value.splice(pendingEditIndex.value, 1, payload)
    } else {
      pendingActions.value.push(payload)
    }
    actionEditor.value = undefined
    pendingEditIndex.value = undefined
    success.value = '措施已加入待保存列表，将随风险一并保存'
    return
  }
  saving.value = true; error.value = ''; success.value = ''
  try {
    detail.value = actionEditor.value?.id
      ? await apiClient.updateRiskAction(detail.value.risk.riskCode, actionEditor.value.id,
          actionEditor.value.version ?? 0, { ...actionForm })
      : await apiClient.addRiskAction(detail.value.risk.riskCode, detail.value.risk.version, { ...actionForm })
    actionEditor.value = undefined
    success.value = '措施已即时保存'
  } catch (reason) { error.value = message(reason, '措施保存失败') }
  finally { saving.value = false }
}

async function removeAction(action: RiskAction) {
  if (!detail.value || !window.confirm('确定删除这条风险控制措施？')) return
  saving.value = true
  try {
    detail.value = await apiClient.deleteRiskAction(
      detail.value.risk.riskCode, action.id, action.version)
    success.value = '措施已删除'
  } catch (reason) { error.value = message(reason, '措施删除失败') }
  finally { saving.value = false }
}

function requestClose() {
  if (dirty.value && !window.confirm('有未保存的风险信息，确定关闭？')) return
  emit('close')
}

function message(reason: unknown, fallback: string) {
  return reason instanceof Error ? reason.message : fallback
}
</script>

<template>
  <div v-if="open" class="drawer-backdrop risk-drawer-backdrop" @click.self="requestClose">
    <section class="risk-drawer" role="dialog" aria-modal="true" aria-labelledby="risk-drawer-title">
      <header>
        <div>
          <h2 id="risk-drawer-title">{{ riskCode ? '风险详情与跟踪' : '新建风险' }}</h2>
          <p>{{ detail?.risk.riskCode ?? '编号将在保存后生成' }}</p>
        </div>
        <button ref="closeButton" type="button" aria-label="关闭风险抽屉" @click="requestClose">×</button>
      </header>

      <div v-if="loading" class="state-panel" aria-busy="true">正在加载风险数据…</div>
      <div v-else class="risk-drawer-body">
        <p v-if="error" class="form-error" role="alert">{{ error }}</p>
        <p v-if="success" class="form-success" role="status">{{ success }}</p>

        <div v-if="detail" class="risk-status-bar" aria-label="风险跟踪摘要">
          <span class="status-chip" :class="detail.risk.status === 'OPEN' ? 'status-chip--orange' : 'status-chip--green'">
            {{ riskStatusLabel(detail.risk.status) }}
          </span>
          <span class="status-chip status-chip--blue">{{ riskLevelLabel(detail.risk.level) }} · {{ detail.risk.score }} 分</span>
          <span class="status-chip">措施 {{ completedActionCount }}/{{ detail.actions.length }}</span>
          <span v-if="detail.risk.overdueActionCount" class="status-chip status-chip--red">
            逾期 {{ detail.risk.overdueActionCount }}
          </span>
          <span v-if="dirty" class="risk-dirty-flag">风险信息未保存</span>
          <span v-else-if="detail" class="risk-saved-hint">措施变更已即时保存</span>
        </div>

        <RiskForm
          v-model:include-assessment="includeAssessment"
          :form="form"
          :options="options"
          :field-errors="fieldErrors"
          :detail="detail"
          :can-update="canUpdate"
          :saving="saving"
          :risk-closed="riskClosed"
          @study-change="studyChanged"
        />

        <fieldset>
          <legend>控制措施 · 后续跟踪 <span>{{ detail?.actions.length ?? pendingActions.length }} 项</span></legend>
          <RiskActionList
            :actions="detail?.actions ?? []"
            :pending-actions="pendingActions"
            :owners="options.owners"
            :risk-editable="riskEditable"
            :risk-closed="riskClosed"
            :has-detail="!!detail"
            :show-add="showAddAction"
            @add="editAction()"
            @edit="editAction"
            @remove="removeAction"
            @edit-pending="editPending"
            @remove-pending="pendingActions.splice($event, 1)"
          />
          <RiskActionEditor
            v-if="actionEditor || pendingEditIndex !== undefined"
            :action-form="actionForm"
            :field-errors="fieldErrors"
            :owners="options.owners"
            :statuses="uniqueActionStatuses"
            :from-status="actionEditor?.fromStatus"
            :saving="saving"
            :has-detail="!!detail"
            @save="saveAction"
            @cancel="cancelActionEditor"
            @completed-date-input="onCompletedDateInput"
            @status-change="onActionStatusChange"
          />
        </fieldset>

        <RiskTimeline v-if="detail" :activities="detail.activities" />
      </div>

      <footer v-if="!loading">
        <button v-if="canDelete && !riskClosed" class="danger-button" type="button" :disabled="saving" @click="removeRisk">删除风险</button>
        <span></span>
        <button class="secondary-button" type="button" @click="requestClose">取消</button>
        <button
          v-if="canUpdate && (!riskClosed || form.status === 'OPEN')"
          class="primary-button"
          type="button"
          :disabled="saving"
          @click="save"
        >{{ saving ? '保存中…' : (riskClosed ? '重新打开并保存' : '保存') }}</button>
      </footer>
    </section>
  </div>
</template>
