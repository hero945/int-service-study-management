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
})
