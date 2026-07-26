<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ApiError, apiClient } from '../api/client'
import type { RiskAction, RiskActionInput, RiskDetail, RiskFormOptions, RiskStatus } from '../api/types'
import {
  riskActionStatusLabel,
  riskLevelLabel,
  riskScoreLevelLabel,
} from '../domain/risk-labels'
import { riskBadge } from '../risk-badge'
import { session } from '../session'

const props = defineProps<{ open: boolean; riskCode?: string }>()
const emit = defineEmits<{ close: []; saved: [] }>()
const detail = ref<RiskDetail>()
const options = ref<RiskFormOptions>({
  studies: [],
  functions: [],
  owners: [],
  scoringRule: { id: 0, lowMax: 12, mediumMax: 36 },
})
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const closeButton = ref<HTMLButtonElement>()
const includeAssessment = ref(true)
const pendingActions = ref<RiskActionInput[]>([])
const actionEditor = ref<{ id?: number; version?: number }>()
const form = reactive({ studyId: 0, functionLineId: 0, ownerUserId: 0,
  description: '', registeredDate: new Date().toISOString().slice(0, 10),
  status: 'OPEN' as RiskStatus, statusReason: '', impact: 1, likelihood: 1,
  detectability: 1, assessmentReason: '' })
const actionForm = reactive<RiskActionInput>({ description: '', ownerUserId: 0,
  plannedDate: '', completedDate: '', status: 'OPEN', completionNote: '' })

const permissions = computed(() => session.currentUser.value?.permissions ?? [])
const canUpdate = computed(() => props.riskCode
  ? permissions.value.includes('risk.update')
  : permissions.value.includes('risk.create'))
const canDelete = computed(() => !!props.riskCode && permissions.value.includes('risk.delete'))
const score = computed(() => form.impact * form.likelihood * form.detectability)
const level = computed(() => riskScoreLevelLabel(score.value, options.value.scoringRule))
const selectedStudy = computed(() => options.value.studies.find(item => item.id === form.studyId))

watch(() => props.open, async open => {
  if (!open) return
  await initialize()
  await nextTick(); closeButton.value?.focus()
})

async function initialize() {
  loading.value = true; error.value = ''; detail.value = undefined; pendingActions.value = []
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
      Object.assign(form, { studyId: initial.studies[0]?.id ?? 0, functionLineId: 0,
        ownerUserId: 0, description: '', registeredDate: new Date().toISOString().slice(0, 10),
        status: 'OPEN', statusReason: '', impact: 1, likelihood: 1,
        detectability: 1, assessmentReason: '首次评估' })
      includeAssessment.value = true
      if (form.studyId) await studyChanged()
    }
  } catch (reason) { error.value = message(reason, '风险详情加载失败') }
  finally { loading.value = false }
}

function fillFromDetail(value: RiskDetail) {
  const latest = value.assessments[0]
  Object.assign(form, { studyId: value.risk.studyId,
    functionLineId: options.value.functions.find(item => item.code === value.risk.functionCode)?.id ?? 0,
    ownerUserId: value.risk.ownerUserId, description: value.risk.description,
    registeredDate: value.registeredDate, status: value.risk.status,
    statusReason: '', impact: latest?.impact ?? 1, likelihood: latest?.likelihood ?? 1,
    detectability: latest?.detectability ?? 1, assessmentReason: '' })
}

async function studyChanged() {
  if (!form.studyId) return
  options.value = await apiClient.getRiskFormOptions(form.studyId)
  if (!options.value.functions.some(item => item.id === form.functionLineId)) form.functionLineId = options.value.functions[0]?.id ?? 0
  if (!options.value.owners.some(item => item.id === form.ownerUserId)) form.ownerUserId = options.value.owners[0]?.id ?? 0
}

function assessmentInput() { return { impact: form.impact, likelihood: form.likelihood,
  detectability: form.detectability, reason: form.assessmentReason } }

async function save() {
  if (!canUpdate.value || !form.studyId || !form.functionLineId || !form.ownerUserId || !form.description.trim()) {
    error.value = '请完整填写 Study、功能线、Owner 和风险描述。'; return
  }
  if (detail.value && form.status !== detail.value.risk.status && !form.statusReason.trim()) {
    error.value = '关闭或重新打开风险时必须填写原因。'; return
  }
  saving.value = true; error.value = ''
  try {
    if (!detail.value) {
      await apiClient.createRisk({ studyId: form.studyId, functionLineId: form.functionLineId,
        ownerUserId: form.ownerUserId, description: form.description.trim(),
        registeredDate: form.registeredDate, assessment: assessmentInput(), actions: pendingActions.value })
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
    emit('saved')
  } catch (reason) { error.value = reason instanceof ApiError && reason.code === 'RISK_VERSION_CONFLICT'
    ? '风险已被其他用户修改，请关闭后重新打开。' : message(reason, '风险保存失败') }
  finally { saving.value = false }
}

async function removeRisk() {
  if (!detail.value || !window.confirm(`确定删除 ${detail.value.risk.riskCode}？该操作会保留审计记录。`)) return
  saving.value = true
  try {
    await apiClient.deleteRisk(detail.value.risk.riskCode, detail.value.risk.version)
    await riskBadge.refresh()
    emit('saved')
  }
  catch (reason) { error.value = message(reason, '风险删除失败') }
  finally { saving.value = false }
}

function editAction(action?: RiskAction) {
  actionEditor.value = action ? { id: action.id, version: action.version } : {}
  Object.assign(actionForm, action ? { description: action.description, ownerUserId: action.ownerUserId,
    plannedDate: action.plannedDate ?? '', completedDate: action.completedDate ?? '',
    status: action.status, completionNote: action.completionNote } : {
    description: '', ownerUserId: options.value.owners[0]?.id ?? 0,
    plannedDate: '', completedDate: '', status: 'OPEN', completionNote: '' })
}

async function saveAction() {
  if (!actionForm.description.trim() || !actionForm.ownerUserId) { error.value = '请填写措施内容和责任人。'; return }
  if (!detail.value) { pendingActions.value.push({ ...actionForm }); actionEditor.value = undefined; return }
  saving.value = true; error.value = ''
  try {
    detail.value = actionEditor.value?.id
      ? await apiClient.updateRiskAction(detail.value.risk.riskCode, actionEditor.value.id,
          actionEditor.value.version ?? 0, { ...actionForm })
      : await apiClient.addRiskAction(detail.value.risk.riskCode, detail.value.risk.version, { ...actionForm })
    actionEditor.value = undefined
  } catch (reason) { error.value = message(reason, '措施保存失败') }
  finally { saving.value = false }
}

async function removeAction(action: RiskAction) {
  if (!detail.value || !window.confirm('确定删除这条风险控制措施？')) return
  saving.value = true
  try { detail.value = await apiClient.deleteRiskAction(detail.value.risk.riskCode, action.id, action.version) }
  catch (reason) { error.value = message(reason, '措施删除失败') }
  finally { saving.value = false }
}

function message(reason: unknown, fallback: string) { return reason instanceof Error ? reason.message : fallback }
</script>

<template>
  <div v-if="open" class="drawer-backdrop risk-drawer-backdrop" @click.self="emit('close')">
    <section class="risk-drawer" role="dialog" aria-modal="true" aria-labelledby="risk-drawer-title">
      <header><div><h2 id="risk-drawer-title">{{ riskCode ? '风险详情与跟踪' : '新建风险' }}</h2><p>{{ detail?.risk.riskCode ?? '编号将在保存后生成' }}</p></div><button ref="closeButton" type="button" aria-label="关闭风险抽屉" @click="emit('close')">×</button></header>
      <div v-if="loading" class="state-panel" aria-busy="true">正在加载风险数据…</div>
      <form v-else class="risk-drawer-body" @submit.prevent="save">
        <p v-if="error" class="form-error" role="alert">{{ error }}</p>
        <fieldset :disabled="!canUpdate || saving"><legend>基本信息</legend>
          <div class="risk-form-grid"><label>Study No.<select v-model.number="form.studyId" required @change="studyChanged"><option v-for="study in options.studies" :key="study.id" :value="study.id">{{ study.studyCode }}</option></select></label><label>Program / Project<input :value="selectedStudy ? `${selectedStudy.programCode} / ${selectedStudy.projectCode}` : ''" readonly></label><label>功能线<select v-model.number="form.functionLineId" required><option v-for="item in options.functions" :key="item.id" :value="item.id">{{ item.name }}</option></select></label><label>Owner<select v-model.number="form.ownerUserId" required><option v-for="owner in options.owners" :key="owner.id" :value="owner.id">{{ owner.displayName }}</option></select></label><label>登记日期<input v-model="form.registeredDate" type="date" required></label><label v-if="detail">状态<select v-model="form.status"><option value="OPEN">未关闭</option><option value="CLOSED">已关闭</option></select></label></div>
          <label class="risk-full-field">风险描述<textarea v-model="form.description" maxlength="4000" required></textarea></label>
          <label v-if="detail && form.status !== detail.risk.status" class="risk-full-field">状态变更原因<textarea v-model="form.statusReason" maxlength="2000" required></textarea></label>
        </fieldset>

        <fieldset :disabled="!canUpdate || saving"><legend>风险评估</legend><label v-if="detail" class="risk-assessment-toggle"><input v-model="includeAssessment" type="checkbox"> 本次保存新增一条评估记录</label><div class="risk-score-grid"><label>影响程度 a<select v-model.number="form.impact" :disabled="detail && !includeAssessment"><option v-for="n in 5" :key="n">{{ n }}</option></select></label><label>发生可能性 b<select v-model.number="form.likelihood" :disabled="detail && !includeAssessment"><option v-for="n in 5" :key="n">{{ n }}</option></select></label><label>可探测性 c<select v-model.number="form.detectability" :disabled="detail && !includeAssessment"><option v-for="n in 5" :key="n">{{ n }}</option></select></label><div class="risk-score-result"><span>总分 a × b × c</span><strong>{{ score }}</strong><small>{{ level }}</small></div></div><label v-if="!detail || includeAssessment" class="risk-full-field">评估原因<textarea v-model="form.assessmentReason" maxlength="1000"></textarea></label></fieldset>

        <fieldset><legend>控制措施 <span>{{ detail?.actions.length ?? pendingActions.length }} 项</span></legend><div class="risk-action-list"><article v-for="action in detail?.actions ?? []" :key="action.id"><div><strong>{{ action.description }}</strong><p>{{ action.ownerName }} · {{ riskActionStatusLabel(action.status) }} · 计划 {{ action.plannedDate || '未设置' }}</p></div><div v-if="canUpdate"><button class="text-button" type="button" @click="editAction(action)">编辑</button><button class="text-button danger" type="button" @click="removeAction(action)">删除</button></div></article><article v-for="(action, index) in pendingActions" :key="index"><div><strong>{{ action.description }}</strong><p>待随风险一并保存</p></div><button class="text-button danger" type="button" @click="pendingActions.splice(index, 1)">移除</button></article></div><button v-if="canUpdate && !actionEditor" class="secondary-button" type="button" @click="editAction()">＋ 添加措施</button><div v-if="actionEditor" class="risk-action-editor"><label>措施内容<textarea v-model="actionForm.description" maxlength="4000"></textarea></label><div class="risk-form-grid"><label>责任人<select v-model.number="actionForm.ownerUserId"><option v-for="owner in options.owners" :key="owner.id" :value="owner.id">{{ owner.displayName }}</option></select></label><label>状态<select v-model="actionForm.status"><option value="OPEN">未开始</option><option value="IN_PROGRESS">进行中</option><option value="COMPLETED">已完成</option><option value="CANCELLED">已取消</option></select></label><label>计划日期<input v-model="actionForm.plannedDate" type="date"></label><label>实际日期<input v-model="actionForm.completedDate" type="date"></label></div><label>完成 / 取消说明<textarea v-model="actionForm.completionNote"></textarea></label><div><button class="secondary-button" type="button" @click="actionEditor = undefined">取消</button><button class="primary-button" type="button" :disabled="saving" @click="saveAction">保存措施</button></div></div></fieldset>

        <fieldset v-if="detail"><legend>评估时间线</legend><ol class="risk-timeline"><li v-for="assessment in detail.assessments" :key="assessment.id"><strong>第 {{ assessment.number }} 次 · {{ assessment.score }} 分 · {{ riskLevelLabel(assessment.level) }}</strong><span>{{ assessment.impact }} × {{ assessment.likelihood }} × {{ assessment.detectability }} · {{ new Date(assessment.assessedAt).toLocaleString('zh-CN') }}</span><p>{{ assessment.reason || '未填写评估原因' }} · {{ assessment.assessedBy }}</p></li></ol></fieldset>
      </form>
      <footer v-if="!loading"><button v-if="canDelete" class="danger-button" type="button" :disabled="saving" @click="removeRisk">删除风险</button><span></span><button class="secondary-button" type="button" @click="emit('close')">取消</button><button v-if="canUpdate" class="primary-button" type="button" :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存' }}</button></footer>
    </section>
  </div>
</template>
