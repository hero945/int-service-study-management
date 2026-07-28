import { ref, type Ref } from 'vue'
import type { SortDirection } from './useClientSort'

export interface UseServerSortOptions<K extends string> {
  initialKey: K
  initialDirection?: SortDirection
  /** 切换到新列时的默认方向（例如时间列默认 desc） */
  defaultDirection?: (key: K) => SortDirection
  /** 排序变化后的回调（通常是重新请求列表） */
  onChange?: () => void
}

export interface UseServerSortReturn<K extends string> {
  sortKey: Ref<K>
  sortDirection: Ref<SortDirection>
  /** 同一列 asc/desc 互切，新列按 defaultDirection（默认 asc） */
  toggle: (key: K) => void
  /** 表头 class：'sortable' | 'sortable sort-asc' | 'sortable sort-desc' */
  sortClass: (key: K) => string
}

/**
 * 服务端排序状态（与 useClientSort 同风格 API）：
 * 只维护排序键/方向并触发 onChange，实际排序由后端完成。
 */
export function useServerSort<K extends string>(options: UseServerSortOptions<K>): UseServerSortReturn<K> {
  const sortKey = ref(options.initialKey) as Ref<K>
  const sortDirection = ref<SortDirection>(options.initialDirection ?? 'asc')

  function toggle(key: K) {
    if (sortKey.value === key) {
      sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
    } else {
      sortKey.value = key
      sortDirection.value = options.defaultDirection?.(key) ?? 'asc'
    }
    options.onChange?.()
  }

  function sortClass(key: K): string {
    if (sortKey.value !== key) return 'sortable'
    return sortDirection.value === 'asc' ? 'sortable sort-asc' : 'sortable sort-desc'
  }

  return { sortKey, sortDirection, toggle, sortClass }
}
