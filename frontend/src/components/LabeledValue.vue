<script setup lang="ts">
defineProps<{
  label: string
  value?: string | number | null
}>()
</script>

<template>
  <span class="labeled-value">
    <span class="labeled-value__label">{{ label }}</span>
    <span class="labeled-value__text">{{ value ?? '—' }}</span>
  </span>
</template>
