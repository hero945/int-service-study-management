<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ApiError, apiClient } from '../api/client'
import type {
  RiskAction, RiskActionInput, RiskActionStatus, RiskDetail, RiskFormOptions, RiskStatus,
} from '../api/types'
import {
  allowedNextActionStatuses,
  riskActionStatusLabel,
  riskFactorScaleHint,
  riskLevelLabel,
  riskScoreLevelLabel,
  riskStatusLabel,
} from '../domain/risk-labels'
import { riskBadge } from '../risk-badge'
import { session } from '../session'

const props = defineProps<{ open: boolean; riskCode?: string }>()
const emit = defineEmits<{ close: []; saved: [] }>()

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
const actionEditor = ref<{ id?: number; version?: number; fromStatus?: string }>()
const dirty = ref(false)
const snapshot = ref('')
const form = reactive({
  studyId: 0, functionLineId: 0, ownerUserId: 0,
  description: '', registeredDate: new Date().toISOString().slice(0, 10),
  status: 'OPEN' as RiskStatus, statusReason: '',
  impact: 1, likelihood: 1, detectability: 1, assessmentReason: '',
})
const actionForm = reactive<RiskActionInput>({
  description: '', ownerUserId: 0, plannedDate: '', completedDate: '',
  status: 'OPEN', completionNote: '', changeReason: '',
})

const permissions = computed(() => session.currentUser.value?.permissions ?? [])
const canUpdate = computed(() => props.riskCode
  ? permissions.value.includes('risk.update')
  : permissions.value.includes('risk.create'))
const canDelete = computed(() => !!props.riskCode && permissions.value.includes('risk.delete'))
const riskClosed = computed(() => detail.value?.risk.status === 'CLOSED')
const riskEditable = computed(() => canUpdate.value && !riskClosed.value)
const score = computed(() => form.impact * form.likelihood * form.detectability)
const level = computed(() => riskScoreLevelLabel(score.value, options.value.scoringRule))
const selectedStudy = computed(() => options.value.studies.find(item => item.id === form.studyId))
const scaleHint = riskFactorScaleHint()
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
const activityRows = computed(() =>
  (detail.value?.activities ?? []).map(activity => ({
    ...activity,
    payload: parseActivityPayload(activity.type, activity.detail),
    time: formatActivityTime(activity.at),
  })),
)

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
        description: '', registeredDate: new Date().toISOString().slice(0, 10),
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

function actionStatusChipClass(status: string, overdue?: boolean) {
  if (overdue && (status === 'OPEN' || status === 'IN_PROGRESS')) return 'status-chip--red'
  if (status === 'COMPLETED') return 'status-chip--green'
  if (status === 'IN_PROGRESS') return 'status-chip--orange'
  if (status === 'CANCELLED') return 'status-chip--empty'
  return 'status-chip--blue'
}

function actionCardTone(status: string, overdue?: boolean) {
  if (overdue && (status === 'OPEN' || status === 'IN_PROGRESS')) return 'overdue'
  if (status === 'COMPLETED') return 'completed'
  if (status === 'IN_PROGRESS') return 'progress'
  if (status === 'CANCELLED') return 'cancelled'
  return 'open'
}

function ownerDisplayName(ownerUserId: number) {
  return options.value.owners.find(item => item.id === ownerUserId)?.displayName ?? '—'
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

function activityTone(type: string) {
  if (type === 'STATUS') return 'risk-axis-node--status'
  if (type === 'ACTION') return 'risk-axis-node--action'
  return 'risk-axis-node--assessment'
}

function activityTypeLabel(type: string) {
  if (type === 'STATUS') return '状态'
  if (type === 'ACTION') return '措施'
  return '评估'
}

function formatActivityTime(value: string) {
  try {
    const date = new Date(value)
    return {
      date: date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
      time: date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      full: date.toLocaleString('zh-CN'),
    }
  } catch {
    return { date: value, time: '', full: value }
  }
}

function parseActivityPayload(type: string, detail: string) {
  const trimmed = (detail || '').trim()
  if (type !== 'ACTION' || !trimmed) {
    return { description: trimmed || '', meta: [] as string[] }
  }
  if (trimmed.startsWith('{')) {
    try {
      const parsed = JSON.parse(trimmed) as Record<string, string>
      const meta = [
        parsed.owner || '',
        parsed.status ? riskActionStatusLabel(parsed.status) : '',
        parsed.plannedDate ? `计划完成 ${parsed.plannedDate}` : '',
        parsed.note || '',
      ].filter(Boolean)
      return { description: parsed.description || '', meta }
    } catch {
      return { description: trimmed, meta: [] as string[] }
    }
  }
  const parts = trimmed.split(/[；;]/).map(item => item.trim()).filter(Boolean)
  if (parts.length <= 1) return { description: trimmed, meta: [] as string[] }
  return { description: parts[0], meta: parts.slice(1) }
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

        <fieldset :disabled="!canUpdate || saving || (riskClosed && form.status === 'CLOSED')">
          <legend>基本信息</legend>
          <div class="risk-form-grid">
            <label :class="{ 'field-invalid': fieldErrors.studyId }">Study No.
              <select v-model.number="form.studyId" required :disabled="!!detail" @change="studyChanged">
                <option v-for="study in options.studies" :key="study.id" :value="study.id">{{ study.studyCode }}</option>
              </select>
              <small v-if="fieldErrors.studyId">{{ fieldErrors.studyId }}</small>
            </label>
            <label>Program / Project
              <input :value="selectedStudy ? `${selectedStudy.programCode} / ${selectedStudy.projectCode}` : ''" readonly>
            </label>
            <label :class="{ 'field-invalid': fieldErrors.functionLineId }">功能线
              <select v-model.number="form.functionLineId" required :disabled="riskClosed && form.status === 'CLOSED'">
                <option v-for="item in options.functions" :key="item.id" :value="item.id">{{ item.name }}</option>
              </select>
              <small v-if="fieldErrors.functionLineId">{{ fieldErrors.functionLineId }}</small>
            </label>
            <label :class="{ 'field-invalid': fieldErrors.ownerUserId }">Owner
              <select v-model.number="form.ownerUserId" required :disabled="riskClosed && form.status === 'CLOSED'">
                <option v-for="owner in options.owners" :key="owner.id" :value="owner.id">{{ owner.displayName }}</option>
              </select>
              <small v-if="fieldErrors.ownerUserId">{{ fieldErrors.ownerUserId }}</small>
            </label>
            <label>登记日期
              <input v-model="form.registeredDate" type="date" required :disabled="riskClosed && form.status === 'CLOSED'">
            </label>
            <label v-if="detail" :class="{ 'field-invalid': fieldErrors.status }">状态
              <select v-model="form.status">
                <option value="OPEN">未关闭</option>
                <option value="CLOSED">已关闭</option>
              </select>
              <small v-if="fieldErrors.status">{{ fieldErrors.status }}</small>
            </label>
          </div>
          <label class="risk-full-field" :class="{ 'field-invalid': fieldErrors.description }">风险描述
            <textarea v-model="form.description" maxlength="4000" required :disabled="riskClosed && form.status === 'CLOSED'"></textarea>
            <small v-if="fieldErrors.description">{{ fieldErrors.description }}</small>
          </label>
          <p v-if="detail?.closeReason" class="risk-close-reason">上次关闭原因：{{ detail.closeReason }}</p>
          <label
            v-if="detail && form.status !== detail.risk.status"
            class="risk-full-field"
            :class="{ 'field-invalid': fieldErrors.statusReason }"
          >本次状态变更原因
            <textarea v-model="form.statusReason" maxlength="2000" required></textarea>
            <small v-if="fieldErrors.statusReason">{{ fieldErrors.statusReason }}</small>
          </label>
        </fieldset>

        <fieldset :disabled="!canUpdate || saving || (riskClosed && form.status === 'CLOSED')">
          <legend>风险评估</legend>
          <label v-if="detail && !riskClosed" class="risk-assessment-toggle">
            <input v-model="includeAssessment" type="checkbox"> 选中进行重新评估
          </label>
          <p class="risk-scale-hint">评分因子：{{ scaleHint }}</p>
          <div class="risk-score-grid">
            <label>影响程度 a
              <select v-model.number="form.impact" :disabled="!!detail && !includeAssessment">
                <option v-for="n in 5" :key="n" :value="n">{{ n }}</option>
              </select>
            </label>
            <label>发生可能性 b
              <select v-model.number="form.likelihood" :disabled="!!detail && !includeAssessment">
                <option v-for="n in 5" :key="n" :value="n">{{ n }}</option>
              </select>
            </label>
            <label>可探测性 c
              <select v-model.number="form.detectability" :disabled="!!detail && !includeAssessment">
                <option v-for="n in 5" :key="n" :value="n">{{ n }}</option>
              </select>
            </label>
            <div class="risk-score-result">
              <span>总分 a × b × c</span>
              <strong>{{ score }}</strong>
              <small>{{ level }}</small>
            </div>
          </div>
          <label
            v-if="!detail || includeAssessment"
            class="risk-full-field"
            :class="{ 'field-invalid': fieldErrors.assessmentReason }"
          >评估原因
            <textarea v-model="form.assessmentReason" maxlength="1000"></textarea>
            <small v-if="fieldErrors.assessmentReason">{{ fieldErrors.assessmentReason }}</small>
          </label>
        </fieldset>

        <fieldset>
          <legend>控制措施 · 后续跟踪 <span>{{ detail?.actions.length ?? pendingActions.length }} 项</span></legend>
          <p v-if="detail && !riskClosed" class="risk-action-hint">措施保存后立即生效，不必再点底部「保存」。</p>
          <p v-else-if="riskClosed" class="risk-action-hint">风险已关闭，控制措施只读。重新打开后可继续维护。</p>
          <div class="risk-action-list">
            <article
              v-for="(action, index) in detail?.actions ?? []"
              :key="action.id"
              class="risk-action-card"
              :class="`risk-action-card--${actionCardTone(action.status, action.overdue)}`"
            >
              <header class="risk-action-card__head">
                <div class="risk-action-card__badges">
                  <span class="risk-action-card__index">措施 {{ index + 1 }}</span>
                  <span class="status-chip" :class="actionStatusChipClass(action.status, action.overdue)">
                    {{ riskActionStatusLabel(action.status) }}
                  </span>
                  <span v-if="action.overdue" class="risk-action-card__flag">逾期</span>
                </div>
                <div v-if="riskEditable" class="risk-action-card__ops">
                  <button class="text-button" type="button" @click="editAction(action)">编辑</button>
                  <button class="text-button danger" type="button" @click="removeAction(action)">删除</button>
                </div>
              </header>
              <p class="risk-action-card__body">{{ action.description }}</p>
              <dl class="risk-action-card__facts">
                <div>
                  <dt>责任人</dt>
                  <dd>{{ action.ownerName }}</dd>
                </div>
                <div>
                  <dt>计划完成</dt>
                  <dd>{{ action.plannedDate || '—' }}</dd>
                </div>
                <div>
                  <dt>实际完成</dt>
                  <dd>{{ action.completedDate || '—' }}</dd>
                </div>
              </dl>
              <p v-if="action.completionNote" class="risk-action-card__note">
                <span>说明</span>{{ action.completionNote }}
              </p>
            </article>
            <article
              v-for="(action, index) in pendingActions"
              :key="`pending-${index}`"
              class="risk-action-card risk-action-card--pending"
              :class="`risk-action-card--${actionCardTone(action.status || 'OPEN')}`"
            >
              <header class="risk-action-card__head">
                <div class="risk-action-card__badges">
                  <span class="risk-action-card__index">待保存 {{ index + 1 }}</span>
                  <span class="status-chip" :class="actionStatusChipClass(action.status || 'OPEN')">
                    {{ riskActionStatusLabel(action.status || 'OPEN') }}
                  </span>
                </div>
                <div class="risk-action-card__ops">
                  <button class="text-button" type="button" @click="editPending(index)">编辑</button>
                  <button class="text-button danger" type="button" @click="pendingActions.splice(index, 1)">移除</button>
                </div>
              </header>
              <p class="risk-action-card__body">{{ action.description }}</p>
              <dl class="risk-action-card__facts">
                <div>
                  <dt>责任人</dt>
                  <dd>{{ ownerDisplayName(action.ownerUserId) }}</dd>
                </div>
                <div>
                  <dt>计划完成</dt>
                  <dd>{{ action.plannedDate || '—' }}</dd>
                </div>
                <div>
                  <dt>实际完成</dt>
                  <dd>{{ action.completedDate || '—' }}</dd>
                </div>
              </dl>
            </article>
          </div>
          <button
            v-if="(riskEditable || !detail) && canUpdate && !actionEditor && pendingEditIndex === undefined"
            class="secondary-button"
            type="button"
            @click="editAction()"
          >＋ 添加措施</button>
          <div v-if="actionEditor || pendingEditIndex !== undefined" class="risk-action-editor">
            <label :class="{ 'field-invalid': fieldErrors.actionDescription }">措施内容
              <textarea v-model="actionForm.description" maxlength="4000"></textarea>
              <small v-if="fieldErrors.actionDescription">{{ fieldErrors.actionDescription }}</small>
            </label>
            <div class="risk-form-grid">
              <label :class="{ 'field-invalid': fieldErrors.actionOwner }">责任人
                <select v-model.number="actionForm.ownerUserId">
                  <option v-for="owner in options.owners" :key="owner.id" :value="owner.id">{{ owner.displayName }}</option>
                </select>
                <small v-if="fieldErrors.actionOwner">{{ fieldErrors.actionOwner }}</small>
              </label>
              <label>状态
                <select v-model="actionForm.status" @change="onActionStatusChange">
                  <option v-for="status in uniqueActionStatuses" :key="status" :value="status">
                    {{ riskActionStatusLabel(status) }}
                  </option>
                </select>
              </label>
              <label :class="{ 'field-invalid': fieldErrors.actionPlanned }">计划完成时间
                <input v-model="actionForm.plannedDate" type="date">
                <small v-if="fieldErrors.actionPlanned">{{ fieldErrors.actionPlanned }}</small>
              </label>
              <label :class="{ 'field-invalid': fieldErrors.actionCompleted }">实际完成时间
                <input v-model="actionForm.completedDate" type="date" @change="onCompletedDateInput">
                <small v-if="fieldErrors.actionCompleted">{{ fieldErrors.actionCompleted }}</small>
                <small v-else-if="actionForm.completedDate && actionForm.status === 'COMPLETED'">已自动切换为「已完成」</small>
              </label>
            </div>
            <label :class="{ 'field-invalid': fieldErrors.actionNote }">完成 / 取消说明
              <textarea v-model="actionForm.completionNote"></textarea>
              <small v-if="fieldErrors.actionNote">{{ fieldErrors.actionNote }}</small>
            </label>
            <label
              v-if="actionEditor?.fromStatus === 'COMPLETED' || actionEditor?.fromStatus === 'CANCELLED'"
              :class="{ 'field-invalid': fieldErrors.actionChangeReason }"
            >重新打开原因
              <textarea v-model="actionForm.changeReason"></textarea>
              <small v-if="fieldErrors.actionChangeReason">{{ fieldErrors.actionChangeReason }}</small>
            </label>
            <div>
              <button class="secondary-button" type="button" @click="actionEditor = undefined; pendingEditIndex = undefined">取消</button>
              <button class="primary-button" type="button" :disabled="saving" @click="saveAction">
                {{ detail ? '保存措施' : '确认措施' }}
              </button>
            </div>
          </div>
        </fieldset>

        <fieldset v-if="detail">
          <legend>活动时间线</legend>
          <ol v-if="activityRows.length" class="risk-axis-timeline">
            <li
              v-for="(activity, index) in activityRows"
              :key="`${activity.type}-${index}-${activity.at}`"
              class="risk-axis-item"
            >
              <div class="risk-axis-track" aria-hidden="true">
                <span class="risk-axis-node" :class="activityTone(activity.type)" :title="activityTypeLabel(activity.type)"></span>
                <span class="risk-axis-time">
                  <strong>{{ activity.time.date }}</strong>
                  <small>{{ activity.time.time }}</small>
                </span>
              </div>
              <div class="risk-axis-content">
                <div class="risk-axis-head">
                  <span class="risk-axis-type" :class="activityTone(activity.type)">{{ activityTypeLabel(activity.type) }}</span>
                  <strong>{{ activity.title }}</strong>
                </div>
                <template v-if="activity.type === 'ACTION'">
                  <p v-if="activity.payload.description" class="risk-axis-desc">{{ activity.payload.description }}</p>
                  <p v-if="activity.payload.meta.length" class="risk-axis-meta-line">
                    <span v-for="(item, metaIndex) in activity.payload.meta" :key="`${activity.at}-${metaIndex}`">{{ item }}</span>
                  </p>
                </template>
                <p v-else-if="activity.detail" class="risk-axis-desc">{{ activity.detail }}</p>
                <p class="risk-axis-meta" :title="activity.time.full">{{ activity.by }}</p>
              </div>
            </li>
          </ol>
          <p v-else class="risk-action-hint">暂无活动记录</p>
        </fieldset>
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
