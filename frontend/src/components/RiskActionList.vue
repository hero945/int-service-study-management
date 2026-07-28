<script setup lang="ts">
import type { RiskAction, RiskActionInput, RiskMemberOption } from '../api/types'
import { riskActionStatusLabel } from '../domain/risk-labels'

defineProps<{
  actions: RiskAction[]
  pendingActions: RiskActionInput[]
  owners: RiskMemberOption[]
  riskEditable: boolean
  riskClosed: boolean
  hasDetail: boolean
  showAdd: boolean
}>()
const emit = defineEmits<{
  add: []
  edit: [action: RiskAction]
  remove: [action: RiskAction]
  editPending: [index: number]
  removePending: [index: number]
}>()

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

function ownerDisplayName(owners: RiskMemberOption[], ownerUserId: number) {
  return owners.find(item => item.id === ownerUserId)?.displayName ?? '—'
}
</script>

<template>
  <p v-if="hasDetail && !riskClosed" class="risk-action-hint">措施保存后立即生效，不必再点底部「保存」。</p>
  <p v-else-if="riskClosed" class="risk-action-hint">风险已关闭，控制措施只读。重新打开后可继续维护。</p>
  <div class="risk-action-list">
    <article
      v-for="(action, index) in actions"
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
          <button class="text-button" type="button" @click="emit('edit', action)">编辑</button>
          <button class="text-button danger" type="button" @click="emit('remove', action)">删除</button>
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
          <button class="text-button" type="button" @click="emit('editPending', index)">编辑</button>
          <button class="text-button danger" type="button" @click="emit('removePending', index)">移除</button>
        </div>
      </header>
      <p class="risk-action-card__body">{{ action.description }}</p>
      <dl class="risk-action-card__facts">
        <div>
          <dt>责任人</dt>
          <dd>{{ ownerDisplayName(owners, action.ownerUserId) }}</dd>
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
    v-if="showAdd"
    class="secondary-button"
    type="button"
    @click="emit('add')"
  >＋ 添加措施</button>
</template>
