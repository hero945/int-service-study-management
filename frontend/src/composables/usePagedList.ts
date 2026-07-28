import { ref, type Ref } from 'vue'
import { DEFAULT_PAGE_SIZE } from '../components/ListPagination.vue'

export interface UsePagedListOptions<F extends object, R> {
  /** 业务筛选条件（reactive 对象，不含分页字段） */
  filters: F
  /** 取数函数，入参为 filters 展开 + 分页字段 */
  fetcher: (query: F & { page: number; pageSize: number }) => Promise<R>
  defaultPageSize?: number
  /** 加载失败时的兜底文案 */
  errorMessage?: string
  /** 加载成功后的回调（例如后端回钳页码时同步 page） */
  onLoaded?: (result: R) => void
}

export interface UsePagedListReturn<R> {
  /** 最近一次成功的响应（形状由各 API 决定） */
  result: Ref<R | undefined>
  loading: Ref<boolean>
  error: Ref<string>
  page: Ref<number>
  pageSize: Ref<number>
  load: () => Promise<void>
  /** 重置到第 1 页并重新加载（筛选变化时调用） */
  applyFilters: () => void
  changePage: (page: number) => void
  changePageSize: (pageSize: number) => void
}

/**
 * 列表页通用状态：loading/error/分页/加载，替换各列表视图里复制的
 * load()/changePage()/changePageSize()/applyFilters() 样板。
 * 视图中数据与总数用 computed 从 result 派生（各 API 分页结构不同）。
 */
export function usePagedList<F extends object, R>(
  options: UsePagedListOptions<F, R>,
): UsePagedListReturn<R> {
  const { filters, fetcher } = options
  const result = ref<R>() as Ref<R | undefined>
  const loading = ref(true)
  const error = ref('')
  const page = ref(1)
  const pageSize = ref(options.defaultPageSize ?? DEFAULT_PAGE_SIZE)

  async function load() {
    loading.value = true
    error.value = ''
    try {
      result.value = await fetcher({ ...filters, page: page.value, pageSize: pageSize.value })
      options.onLoaded?.(result.value)
    } catch (reason) {
      error.value = reason instanceof Error ? reason.message : (options.errorMessage ?? '数据加载失败')
    } finally {
      loading.value = false
    }
  }

  function applyFilters() {
    page.value = 1
    void load()
  }

  function changePage(next: number) {
    page.value = next
    void load()
  }

  function changePageSize(nextSize: number) {
    pageSize.value = nextSize
    page.value = 1
    void load()
  }

  return { result, loading, error, page, pageSize, load, applyFilters, changePage, changePageSize }
}
