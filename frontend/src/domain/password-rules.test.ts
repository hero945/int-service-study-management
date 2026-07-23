import { describe, expect, it } from 'vitest'
import { validateNewPassword } from './password-rules'

describe('validateNewPassword', () => {
  it('accepts passwords with mixed case and digits', () => {
    expect(validateNewPassword('Hd123456')).toBeNull()
    expect(validateNewPassword('NewPass12')).toBeNull()
  })

  it('rejects weak passwords', () => {
    expect(validateNewPassword('')).toBeTruthy()
    expect(validateNewPassword('short')).toBeTruthy()
    expect(validateNewPassword('alllowercase1')).toBeTruthy()
    expect(validateNewPassword('ALLUPPERCASE1')).toBeTruthy()
    expect(validateNewPassword('NoDigitsHere')).toBeTruthy()
  })
})
