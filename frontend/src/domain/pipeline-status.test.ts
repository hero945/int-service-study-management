import { describe, expect, it } from 'vitest'
import {
  CLINICAL_PHASE_CODES,
  normalizePhase,
  originLabel,
  phaseLabel,
  sourceLabel,
  toneForStatus,
} from './pipeline-status'

describe('pipeline phase dictionary', () => {
  it('keeps the nine clinical phase codes in DB order', () => {
    expect(CLINICAL_PHASE_CODES).toEqual([
      'PRE_IND',
      'IND',
      'PHASE_1',
      'PHASE_2',
      'PRE_3',
      'PHASE_3_1',
      'PHASE_3_2',
      'PRE_NDA',
      'NDA',
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
    expect(normalizePhase('PRE_NDA')).toBe('PRE_NDA')
    expect(normalizePhase('NDA')).toBe('NDA')
  })

  it('returns undefined for unknown phase codes', () => {
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

  it('maps clinical phase codes to human-readable labels', () => {
    expect(phaseLabel('PRE_IND')).toBe('Pre-IND')
    expect(phaseLabel('IND')).toBe('IND')
    expect(phaseLabel('PHASE_1')).toBe('I期临床')
    expect(phaseLabel('PHASE_2')).toBe('II期临床')
    expect(phaseLabel('PRE_3')).toBe('Pre-III')
    expect(phaseLabel('PHASE_3_1')).toBe('III期临床（A）')
    expect(phaseLabel('PHASE_3_2')).toBe('III期临床（B）')
    expect(phaseLabel('PRE_NDA')).toBe('PreNDA/BLA')
    expect(phaseLabel('NDA')).toBe('NDA/BLA')
    expect(phaseLabel('UNKNOWN')).toBe('UNKNOWN')
    expect(phaseLabel(undefined)).toBe('')
  })
})
