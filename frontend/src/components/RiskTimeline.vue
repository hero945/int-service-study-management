<script setup lang="ts">
import { computed } from 'vue'
import type { RiskActivity, RiskActivityType } from '../api/types'
import { riskActionStatusLabel } from '../domain/risk-labels'

const props = defineProps<{ activities: RiskActivity[] }>()

interface ActivityRow extends RiskActivity {
  payload: { description: string; meta: string[] }
  time: { date: string; time: string; full: string }
}

const activityRows = computed<ActivityRow[]>(() =>
  props.activities.map(activity => ({
    ...activity,
    payload: parseActivityPayload(activity.type, activity.detail),
    time: formatActivityTime(activity.at),
  })),
)

function activityTone(type: RiskActivityType) {
  if (type === 'STATUS') return 'risk-axis-node--status'
  if (type === 'ACTION') return 'risk-axis-node--action'
  return 'risk-axis-node--assessment'
}

function activityTypeLabel(type: RiskActivityType) {
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

function parseActivityPayload(type: RiskActivityType, detail: string) {
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
  <fieldset>
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
</template>
