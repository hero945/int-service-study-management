import { describe, expect, it } from 'vitest'
import { safeRedirectPath } from './login-redirect'

describe('safeRedirectPath', () => {
  it('keeps in-app relative paths', () => {
    expect(safeRedirectPath('/pipeline')).toBe('/pipeline')
    expect(safeRedirectPath('/config?view=project#editor')).toBe('/config?view=project#editor')
  })

  it('rejects absolute and protocol-relative URLs', () => {
    expect(safeRedirectPath('https://evil.com')).toBeUndefined()
    expect(safeRedirectPath('//evil.com')).toBeUndefined()
    expect(safeRedirectPath('javascript:alert(1)')).toBeUndefined()
  })

  it('rejects non-string and missing values', () => {
    expect(safeRedirectPath(undefined)).toBeUndefined()
    expect(safeRedirectPath(null)).toBeUndefined()
    expect(safeRedirectPath([])).toBeUndefined()
  })

  it('uses the first value when the query param repeats', () => {
    expect(safeRedirectPath(['/pipeline', 'https://evil.com'])).toBe('/pipeline')
    expect(safeRedirectPath(['https://evil.com', '/pipeline'])).toBeUndefined()
  })
})
