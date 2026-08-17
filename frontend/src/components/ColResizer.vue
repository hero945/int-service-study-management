<script setup lang="ts">
defineProps<{
  colKey: string
  startResize: (key: string, event: PointerEvent) => void
}>()
</script>

<template>
  <span class="col-resizer" @pointerdown="startResize(colKey, $event)" @click.stop></span>
</template>
