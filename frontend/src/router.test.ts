import { describe, expect, it } from 'vitest'
import { firstAllowedHome } from './navigation'

describe('firstAllowedHome', () => {
  it('returns pipeline when user has pipeline.page.view', () => {
    expect(firstAllowedHome(['pipeline.page.view'])).toBe('pipeline')
  })

  it('returns studies when user has study.read', () => {
    expect(firstAllowedHome(['study.read'])).toBe('studies')
  })

  it('returns login when permissions do not expose a primary destination', () => {
    expect(firstAllowedHome(['monthly.read'])).toBe('login')
  })

  it('falls back to login when no permissions match', () => {
    expect(firstAllowedHome([])).toBe('login')
  })
})
