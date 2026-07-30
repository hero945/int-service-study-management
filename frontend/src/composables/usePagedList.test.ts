import { describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'
import { SYSTEM_ERROR_MESSAGE } from '../api/errors'
import { usePagedList } from './usePagedList'

interface Row { id: number }
interface Page { data: Row[]; totalItems: number }

function makeFetcher(pages: Record<number, Page>) {
  return vi.fn(async (q: { keyword: string; page: number; pageSize: number }) => pages[q.page] ?? { data: [], totalItems: 0 })
}

describe('usePagedList', () => {
  it('load 成功写入 result 并复位 loading', async () => {
    const fetcher = makeFetcher({ 1: { data: [{ id: 1 }], totalItems: 1 } })
    const { result, loading, error, load } = usePagedList({
      filters: reactive({ keyword: '' }),
      fetcher,
      errorMessage: '加载失败',
    })
    expect(loading.value).toBe(true)
    await load()
    expect(loading.value).toBe(false)
    expect(error.value).toBe('')
    expect(result.value?.data).toHaveLength(1)
    expect(fetcher).toHaveBeenCalledWith({ keyword: '', page: 1, pageSize: 10 })
  })

  it('load 失败写入统一系统文案', async () => {
    const { error, loading, load } = usePagedList({
      filters: reactive({ keyword: '' }),
      fetcher: async () => { throw new Error('boom') },
      errorMessage: '加载失败',
    })
    await load()
    expect(loading.value).toBe(false)
    expect(error.value).toBe(SYSTEM_ERROR_MESSAGE)
  })

  it('非 Error 异常也写入统一系统文案', async () => {
    const { error, load } = usePagedList({
      filters: reactive({ keyword: '' }),
      fetcher: async () => { throw 'oops' },
      errorMessage: '加载失败',
    })
    await load()
    expect(error.value).toBe(SYSTEM_ERROR_MESSAGE)
  })

  it('changePage / changePageSize / applyFilters 的分页行为', async () => {
    const fetcher = makeFetcher({
      1: { data: [{ id: 1 }], totalItems: 30 },
      2: { data: [{ id: 2 }], totalItems: 30 },
    })
    const { page, pageSize, load, applyFilters, changePage, changePageSize } = usePagedList({
      filters: reactive({ keyword: '' }),
      fetcher,
    })
    await load()
    changePage(2)
    await vi.waitFor(() => expect(page.value).toBe(2))
    expect(fetcher).toHaveBeenLastCalledWith({ keyword: '', page: 2, pageSize: 10 })

    changePageSize(50)
    await vi.waitFor(() => expect(fetcher).toHaveBeenLastCalledWith({ keyword: '', page: 1, pageSize: 50 }))
    expect(pageSize.value).toBe(50)
    expect(page.value).toBe(1)

    changePage(2)
    await vi.waitFor(() => expect(page.value).toBe(2))
    applyFilters()
    await vi.waitFor(() => expect(fetcher).toHaveBeenLastCalledWith({ keyword: '', page: 1, pageSize: 50 }))
    expect(page.value).toBe(1)
  })

  it('onLoaded 在成功后被调用', async () => {
    const onLoaded = vi.fn()
    const { load } = usePagedList({
      filters: reactive({ keyword: '' }),
      fetcher: makeFetcher({ 1: { data: [], totalItems: 0 } }),
      onLoaded,
    })
    await load()
    expect(onLoaded).toHaveBeenCalledWith({ data: [], totalItems: 0 })
  })
})
