<script setup lang="ts">
defineProps<{
  loading?: boolean
  error?: string
  empty?: boolean
  emptyTitle?: string
  emptyDescription?: string
}>()
</script>

<template>
  <div v-if="loading" class="state-panel" aria-live="polite">正在加载数据…</div>
  <div v-else-if="error" class="state-panel state-panel--error" role="alert">{{ error }}</div>
  <div v-else-if="empty" class="state-panel">
    <strong>{{ emptyTitle ?? '暂无数据' }}</strong>
    <span>{{ emptyDescription ?? '后端接口返回数据后将在这里展示。' }}</span>
  </div>
  <slot v-else />
</template>
