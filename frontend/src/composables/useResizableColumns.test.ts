import { describe, expect, it, beforeEach } from 'vitest'
import { useResizableColumns } from './useResizableColumns'

describe('useResizableColumns', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('starts from defaults then overlays stored widths', () => {
    localStorage.setItem('table-col-widths:demo', JSON.stringify({ a: 200 }))
    const { widths, colStyle } = useResizableColumns('demo', { a: 120, b: 80 })
    expect(colStyle('b')).toEqual({ width: '80px', minWidth: '80px', maxWidth: '80px' })
    expect(widths.a).toBe(200)
  })

  it('fills the container while keeping a min width equal to the column sum', () => {
    const { tableStyle, fluidColStyle } = useResizableColumns('sum', { a: 120, b: 80 })
    expect(tableStyle()).toMatchObject({
      width: '100%',
      minWidth: '200px',
      '--col-a': '120px',
      '--col-b': '80px',
    })
    expect(fluidColStyle('b')).toEqual({ minWidth: '80px' })
  })

  it('fillColStyle keeps min width without locking maxWidth so columns can share leftover space', () => {
    const { fillColStyle, totalWidth } = useResizableColumns('fill', { a: 120, b: 80 })
    expect(fillColStyle('a')).toEqual({ width: '120px', minWidth: '120px' })
    expect(totalWidth()).toBe(200)
  })

  it('ignores leftover stored keys that are no longer in the table defaults', () => {
    localStorage.setItem('table-col-widths:stale', JSON.stringify({ a: 120, b: 80, indication: 160 }))
    const { totalWidth } = useResizableColumns('stale', { a: 120, b: 80 })
    expect(totalWidth()).toBe(200)
  })
})
