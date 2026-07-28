import { computed, ref, type ComputedRef, type Ref } from 'vue'

export type SortValue = string | number | Date | null | undefined
export type SortType = 'string' | 'number' | 'date'
export type SortDirection = 'asc' | 'desc'

export interface SortColumn<T> {
  key: string
  resolver: (item: T) => SortValue
  type?: SortType
}

export interface UseClientSortOptions<T> {
  items: Ref<T[]> | ComputedRef<T[]>
  initialKey?: string
  initialDirection?: SortDirection
}

export interface UseClientSortReturn<T> {
  sorted: ComputedRef<T[]>
  sortKey: Ref<string | null>
  sortDirection: Ref<SortDirection | null>
  register: (column: SortColumn<T>) => void
  registerMany: (columns: SortColumn<T>[]) => void
  toggle: (key: string) => void
  setSort: (key: string | null, direction?: SortDirection | null) => void
  getDirection: (key: string) => SortDirection | null
  reset: () => void
}

function normalizeValue(value: SortValue, type?: SortType): number | string | null {
  if (value == null) return null
  if (type === 'number') {
    return typeof value === 'number' ? value : Number(value)
  }
  if (type === 'date') {
    if (value instanceof Date) return value.getTime()
    const parsed = new Date(value as string | number | Date)
    return Number.isNaN(parsed.getTime()) ? null : parsed.getTime()
  }
  return String(value)
}

function compareValues(
  aRaw: SortValue,
  bRaw: SortValue,
  type?: SortType,
): number {
  const a = normalizeValue(aRaw, type)
  const b = normalizeValue(bRaw, type)

  // null/undefined 始终排在末尾
  if (a == null && b == null) return 0
  if (a == null) return 1
  if (b == null) return -1

  if (typeof a === 'number' && typeof b === 'number') {
    return a - b
  }

  return String(a).localeCompare(String(b), 'zh-CN', { numeric: true })
}

export function useClientSort<T>(options: UseClientSortOptions<T>): UseClientSortReturn<T> {
  const { items } = options
  const columns = new Map<string, SortColumn<T>>()
  const sortKey: Ref<string | null> = ref(options.initialKey ?? null)
  const sortDirection: Ref<SortDirection | null> = ref(
    options.initialKey ? (options.initialDirection ?? 'asc') : null,
  )

  function register(column: SortColumn<T>) {
    columns.set(column.key, column)
  }

  function registerMany(columnList: SortColumn<T>[]) {
    for (const column of columnList) {
      register(column)
    }
  }

  function setSort(key: string | null, direction?: SortDirection | null) {
    sortKey.value = key
    sortDirection.value = direction ?? (key ? 'asc' : null)
  }

  function toggle(key: string) {
    if (sortKey.value !== key) {
      setSort(key, 'asc')
      return
    }
    if (sortDirection.value === 'asc') {
      sortDirection.value = 'desc'
    } else {
      // desc → 取消排序
      reset()
    }
  }

  function getDirection(key: string): SortDirection | null {
    return sortKey.value === key ? sortDirection.value : null
  }

  function reset() {
    sortKey.value = null
    sortDirection.value = null
  }

  const sorted = computed(() => {
    const key = sortKey.value
    const direction = sortDirection.value
    if (!key || !direction) return [...items.value]

    const column = columns.get(key)
    if (!column) return [...items.value]

    const sortedList = [...items.value].sort((a, b) => {
      const result = compareValues(
        column.resolver(a),
        column.resolver(b),
        column.type,
      )
      return direction === 'asc' ? result : -result
    })

    return sortedList
  })

  return {
    sorted,
    sortKey,
    sortDirection,
    register,
    registerMany,
    toggle,
    setSort,
    getDirection,
    reset,
  }
}
