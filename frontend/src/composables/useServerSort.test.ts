import { describe, expect, it, vi } from 'vitest'
import { useServerSort } from './useServerSort'

describe('useServerSort', () => {
  it('初始为指定列与方向', () => {
    const { sortKey, sortDirection, sortClass } = useServerSort({
      initialKey: 'updatedAt' as string,
      initialDirection: 'desc' as const,
    })
    expect(sortKey.value).toBe('updatedAt')
    expect(sortDirection.value).toBe('desc')
    expect(sortClass('updatedAt')).toBe('sortable sort-desc')
    expect(sortClass('score')).toBe('sortable')
  })

  it('同列切换 asc/desc，并触发 onChange', () => {
    const onChange = vi.fn()
    const { sortDirection, toggle } = useServerSort({
      initialKey: 'score' as string,
      onChange,
    })
    expect(sortDirection.value).toBe('asc')
    toggle('score')
    expect(sortDirection.value).toBe('desc')
    toggle('score')
    expect(sortDirection.value).toBe('asc')
    expect(onChange).toHaveBeenCalledTimes(2)
  })

  it('切到新列时使用 defaultDirection', () => {
    const { sortKey, sortDirection, toggle, sortClass } = useServerSort({
      initialKey: 'updatedAt' as string,
      initialDirection: 'desc' as const,
      defaultDirection: (key) => (key === 'updatedAt' ? 'desc' : 'asc'),
    })
    toggle('riskCode')
    expect(sortKey.value).toBe('riskCode')
    expect(sortDirection.value).toBe('asc')
    expect(sortClass('riskCode')).toBe('sortable sort-asc')
    toggle('updatedAt')
    expect(sortDirection.value).toBe('desc')
  })
})
