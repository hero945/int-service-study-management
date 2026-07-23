import { describe, expect, it } from 'vitest'
import {
  PHASE_CODE_TO_TAG,
  PHASE_TAGS,
  PHASE_TAG_TO_CODE,
  normalizePhase,
  originLabel,
  sourceLabel,
  toneForStatus,
} from './pipeline-status'

describe('pipeline phase dictionary', () => {
  it('keeps the seven historical overview columns in order', () => {
    expect(PHASE_TAGS).toEqual([
      'PreIND',
      'IND',
      'Phase 1',
      'Phase 2',
      'PRE-3',
      'Phase 3-1',
      'Phase 3-2',
    ])
  })

  it('maps every backend phase code to an overview column', () => {
    expect(normalizePhase('PRE_IND')).toBe('PreIND')
    expect(normalizePhase('IND')).toBe('IND')
    expect(normalizePhase('PHASE_1')).toBe('Phase 1')
    expect(normalizePhase('PHASE_2')).toBe('Phase 2')
    expect(normalizePhase('PRE_3')).toBe('PRE-3')
    expect(normalizePhase('PHASE_3_1')).toBe('Phase 3-1')
    expect(normalizePhase('PHASE_3_2')).toBe('Phase 3-2')
  })

  it('returns undefined for unknown phase codes', () => {
    expect(normalizePhase('NDA')).toBeUndefined()
    expect(normalizePhase('')).toBeUndefined()
  })

  it('keeps code and tag dictionaries inverse to each other', () => {
    for (const [code, tag] of Object.entries(PHASE_CODE_TO_TAG)) {
      expect(PHASE_TAG_TO_CODE[tag as keyof typeof PHASE_TAG_TO_CODE]).toBe(code)
    }
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
