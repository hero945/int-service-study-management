<script setup lang="ts">
import { computed } from 'vue'
import type { RiskDetail, RiskFormOptions } from '../api/types'
import { riskFactorScaleHint, riskScoreLevelLabel } from '../domain/risk-labels'
import type { RiskFormState } from './risk-editor-types'

const props = defineProps<{
  form: RiskFormState
  options: RiskFormOptions
  fieldErrors: Record<string, string>
  detail?: RiskDetail
  canUpdate: boolean
  saving: boolean
  riskClosed: boolean
  includeAssessment: boolean
}>()
const emit = defineEmits<{
  'update:includeAssessment': [value: boolean]
  'study-change': []
}>()

/** 已关闭且表单未重新打开时，整个表单只读 */
const fieldsDisabled = computed(() =>
  !props.canUpdate || props.saving || (props.riskClosed && props.form.status === 'CLOSED'))
const score = computed(() => props.form.impact * props.form.likelihood * (6 - props.form.detectability))
const level = computed(() => riskScoreLevelLabel(score.value, props.options.scoringRule))
const selectedStudy = computed(() => props.options.studies.find(item => item.id === props.form.studyId))
const scaleHint = riskFactorScaleHint()
</script>

<template>
  <fieldset :disabled="fieldsDisabled">
    <legend>基本信息</legend>
    <div class="risk-form-grid">
      <label :class="{ 'field-invalid': fieldErrors.studyId }">Study No.
        <select v-model.number="form.studyId" required :disabled="!!detail" @change="emit('study-change')">
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

  <fieldset :disabled="fieldsDisabled">
    <legend>风险评估</legend>
    <label v-if="detail && !riskClosed" class="risk-assessment-toggle">
      <input
        :checked="includeAssessment"
        type="checkbox"
        @change="emit('update:includeAssessment', ($event.target as HTMLInputElement).checked)"
      > 选中进行重新评估
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
        <small>风险分数：{{ 6 - form.detectability }}</small>
      </label>
      <div class="risk-score-result">
        <span>总分 a × b × 风险分数</span>
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
</template>
