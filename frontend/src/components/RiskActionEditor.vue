<script setup lang="ts">
import type { RiskActionStatus, RiskMemberOption } from '../api/types'
import { riskActionStatusLabel } from '../domain/risk-labels'
import type { RiskActionFormState } from './risk-editor-types'

defineProps<{
  actionForm: RiskActionFormState
  fieldErrors: Record<string, string>
  owners: RiskMemberOption[]
  /** 状态流转允许的状态选项 */
  statuses: RiskActionStatus[]
  /** 编辑已保存措施时的原状态；新增 / 待保存措施为 undefined */
  fromStatus?: string
  saving: boolean
  hasDetail: boolean
}>()
const emit = defineEmits<{
  save: []
  cancel: []
  'completed-date-input': []
  'status-change': []
}>()
</script>

<template>
  <div class="risk-action-editor">
    <label :class="{ 'field-invalid': fieldErrors.actionDescription }">措施内容
      <textarea v-model="actionForm.description" maxlength="4000"></textarea>
      <small v-if="fieldErrors.actionDescription">{{ fieldErrors.actionDescription }}</small>
    </label>
    <div class="risk-form-grid">
      <label :class="{ 'field-invalid': fieldErrors.actionOwner }">责任人
        <select v-model.number="actionForm.ownerUserId">
          <option v-for="owner in owners" :key="owner.id" :value="owner.id">{{ owner.displayName }}</option>
        </select>
        <small v-if="fieldErrors.actionOwner">{{ fieldErrors.actionOwner }}</small>
      </label>
      <label>状态
        <select v-model="actionForm.status" @change="emit('status-change')">
          <option v-for="status in statuses" :key="status" :value="status">
            {{ riskActionStatusLabel(status) }}
          </option>
        </select>
      </label>
      <label :class="{ 'field-invalid': fieldErrors.actionPlanned }">计划完成时间
        <input v-model="actionForm.plannedDate" type="date">
        <small v-if="fieldErrors.actionPlanned">{{ fieldErrors.actionPlanned }}</small>
      </label>
      <label :class="{ 'field-invalid': fieldErrors.actionCompleted }">实际完成时间
        <input v-model="actionForm.completedDate" type="date" @change="emit('completed-date-input')">
        <small v-if="fieldErrors.actionCompleted">{{ fieldErrors.actionCompleted }}</small>
        <small v-else-if="actionForm.completedDate && actionForm.status === 'COMPLETED'">已自动切换为「已完成」</small>
      </label>
    </div>
    <label :class="{ 'field-invalid': fieldErrors.actionNote }">完成 / 取消说明
      <textarea v-model="actionForm.completionNote"></textarea>
      <small v-if="fieldErrors.actionNote">{{ fieldErrors.actionNote }}</small>
    </label>
    <label
      v-if="fromStatus === 'COMPLETED' || fromStatus === 'CANCELLED'"
      :class="{ 'field-invalid': fieldErrors.actionChangeReason }"
    >重新打开原因
      <textarea v-model="actionForm.changeReason"></textarea>
      <small v-if="fieldErrors.actionChangeReason">{{ fieldErrors.actionChangeReason }}</small>
    </label>
    <div>
      <button class="secondary-button" type="button" @click="emit('cancel')">取消</button>
      <button class="primary-button" type="button" :disabled="saving" @click="emit('save')">
        {{ hasDetail ? '保存措施' : '确认措施' }}
      </button>
    </div>
  </div>
</template>
