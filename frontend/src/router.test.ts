import { describe, expect, it } from 'vitest'
import { firstAllowedHome } from './router'

describe('firstAllowedHome', () => {
  it('returns pipeline when user has pipeline.page.view', () => {
    expect(firstAllowedHome(['pipeline.page.view'])).toBe('pipeline')
  })

  it('returns studies when user has study.read', () => {
    expect(firstAllowedHome(['study.read'])).toBe('studies')
  })

  it('returns monthly when user only has monthly.read', () => {
    expect(firstAllowedHome(['monthly.read'])).toBe('monthly')
  })

  it('falls back to login when no permissions match', () => {
    expect(firstAllowedHome([])).toBe('login')
  })
})
