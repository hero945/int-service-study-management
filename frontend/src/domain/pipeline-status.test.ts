import { describe, expect, it } from 'vitest'
import {
  CLINICAL_PHASE_CODES,
  normalizePhase,
  originLabel,
  sourceLabel,
  toneForStatus,
} from './pipeline-status'

describe('pipeline phase dictionary', () => {
  it('keeps the seven clinical phase codes in DB order', () => {
    expect(CLINICAL_PHASE_CODES).toEqual([
      'PRE_IND',
      'IND',
      'PHASE_1',
      'PHASE_2',
      'PRE_3',
      'PHASE_3_1',
      'PHASE_3_2',
    ])
  })

  it('accepts known backend phase codes as overview columns', () => {
    expect(normalizePhase('PRE_IND')).toBe('PRE_IND')
    expect(normalizePhase('IND')).toBe('IND')
    expect(normalizePhase('PHASE_1')).toBe('PHASE_1')
    expect(normalizePhase('PHASE_2')).toBe('PHASE_2')
    expect(normalizePhase('PRE_3')).toBe('PRE_3')
    expect(normalizePhase('PHASE_3_1')).toBe('PHASE_3_1')
    expect(normalizePhase('PHASE_3_2')).toBe('PHASE_3_2')
  })

  it('returns undefined for unknown phase codes', () => {
    expect(normalizePhase('NDA')).toBeUndefined()
    expect(normalizePhase('Phase 1')).toBeUndefined()
    expect(normalizePhase('')).toBeUndefined()
  })

  it('maps study status tones to overview tones', () => {
    expect(toneForStatus('positive')).toBe('blue')
    expect(toneForStatus('info')).toBe('green')
    expect(toneForStatus('neutral')).toBe('blue')
    expect(toneForStatus('warning')).toBe('orange')
    expect(toneForStatus('danger')).toBe('red')
    expect(toneForStatus('unknown')).toBe('blue')
  })

  it('maps source and origin codes to Chinese labels', () => {
    expect(sourceLabel('SELF_DEVELOPED')).toBe('自研')
    expect(sourceLabel('IN_LICENSE')).toBe('引进')
    expect(sourceLabel('COOPERATION')).toBe('合作')
    expect(originLabel('DOMESTIC')).toBe('国产')
    expect(originLabel('IMPORTED')).toBe('进口')
    expect(sourceLabel(undefined)).toBe('')
    expect(originLabel(undefined)).toBe('')
  })
})
