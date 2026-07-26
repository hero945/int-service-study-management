<script setup lang="ts">
withDefaults(defineProps<{
  total: number
  page: number
  pageSize: number
  totalPages: number
  ariaLabel?: string
}>(), {
  ariaLabel: '列表分页',
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:pageSize': [pageSize: number]
}>()

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100]
</script>

<script lang="ts">
export const DEFAULT_PAGE_SIZE = 10
</script>

<template>
  <nav class="study-pagination" :aria-label="ariaLabel">
    <div class="study-pagination__meta">
      <span>共 {{ total }} 条</span>
      <label class="study-pagination__size">
        <span>每页</span>
        <select
          class="filter-select"
          :value="pageSize"
          @change="emit('update:pageSize', Number(($event.target as HTMLSelectElement).value))"
        >
          <option v-for="size in PAGE_SIZE_OPTIONS" :key="size" :value="size">{{ size }}</option>
        </select>
        <span>条</span>
      </label>
    </div>
    <div class="study-pagination__controls">
      <button
        class="secondary-button"
        type="button"
        :disabled="page <= 1"
        @click="emit('update:page', page - 1)"
      >上一页</button>
      <span>第 {{ page }} / {{ Math.max(totalPages, 1) }} 页</span>
      <button
        class="secondary-button"
        type="button"
        :disabled="page >= Math.max(totalPages, 1)"
        @click="emit('update:page', page + 1)"
      >下一页</button>
    </div>
  </nav>
</template>
