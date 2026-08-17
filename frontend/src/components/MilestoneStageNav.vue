<script setup lang="ts">
import { onMounted, watch } from 'vue'
import type { MilestoneStageItem } from '../composables/useMilestoneStageFocus'

const props = defineProps<{
  stages: MilestoneStageItem[]
  active?: string
}>()
const emit = defineEmits<{ select: [code: string] }>()

function scrollActiveIntoView() {
  if (!props.active) return
  const el = document.querySelector<HTMLElement>(`[data-stage-nav="${CSS.escape(props.active)}"]`)
  el?.scrollIntoView({ block: 'nearest', inline: 'center' })
}

onMounted(scrollActiveIntoView)
watch(() => props.active, () => scrollActiveIntoView())
</script>

<template>
  <div class="milestone-stage-nav" role="tablist" aria-label="里程碑阶段">
    <button
      v-for="stage in stages"
      :key="stage.code"
      type="button"
      role="tab"
      class="milestone-stage-nav__item"
      :class="{ 'is-active': stage.code === active }"
      :data-stage-nav="stage.code"
      :aria-selected="stage.code === active"
      @click="emit('select', stage.code)"
    >{{ stage.name }}</button>
  </div>
</template>
