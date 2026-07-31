<script setup lang="ts">
defineProps<{
  loading?: boolean
  error?: string
  retryable?: boolean
  empty?: boolean
  emptyTitle?: string
  emptyDescription?: string
}>()

defineEmits<{
  retry: []
}>()
</script>

<template>
  <div
    v-if="loading"
    class="state-panel state-panel--loading"
    aria-busy="true"
    aria-label="正在加载数据"
  >
    <span class="state-panel__skeleton-heading"></span>
    <span v-for="index in 4" :key="index" class="state-panel__skeleton-row"></span>
  </div>
  <div v-else-if="error" class="state-panel state-panel--error" role="alert">
    <svg class="state-panel__icon" viewBox="0 0 48 48" width="44" height="44" fill="none" aria-hidden="true">
      <circle cx="24" cy="24" r="18" stroke="currentColor" stroke-width="2.4" />
      <path d="M24 14v12M24 33h.01" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
    </svg>
    <strong>暂时无法加载</strong>
    <span>{{ error }}</span>
    <button v-if="retryable" class="secondary-button" type="button" @click="$emit('retry')">重新加载</button>
  </div>
  <div v-else-if="empty" class="state-panel state-panel--empty" role="status">
    <svg class="state-panel__icon" viewBox="0 0 48 48" width="44" height="44" fill="none" aria-hidden="true">
      <path d="M8 30v8a2 2 0 0 0 2 2h28a2 2 0 0 0 2-2v-8" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
      <path d="M8 30l4.5-16.5A2 2 0 0 1 14.4 12h19.2a2 2 0 0 1 1.9 1.5L40 30H8z" stroke="currentColor" stroke-width="2.4" stroke-linejoin="round"/>
      <path d="M18 30a6 6 0 0 0 12 0" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
    </svg>
    <strong>{{ emptyTitle ?? '暂无数据' }}</strong>
    <span>{{ emptyDescription ?? '后端接口返回数据后将在这里展示。' }}</span>
    <slot name="empty-action" />
  </div>
  <slot v-else />
</template>
