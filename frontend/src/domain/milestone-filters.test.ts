import { describe, expect, it } from 'vitest'
import {
  PIPELINE_PHASE_STATUS_OPTIONS,
  phaseCodeToColumn,
  pipelineStatusOptions,
} from './milestone-filters'

describe('phaseCodeToColumn', () => {
  it('maps Phase Status codes to overview column keys', () => {
    expect(phaseCodeToColumn('PRE_IND')).toBe('PRE_IND')
    expect(phaseCodeToColumn('IND')).toBe('IND')
    expect(phaseCodeToColumn('PHASE_1')).toBe('PHASE_1')
    expect(phaseCodeToColumn('PRE_3')).toBe('PRE_3')
    expect(phaseCodeToColumn('')).toBeUndefined()
  })
})

describe('PIPELINE_PHASE_STATUS_OPTIONS', () => {
  it('uses DB clinical phase codes as value and human-readable labels', () => {
    expect(PIPELINE_PHASE_STATUS_OPTIONS.map((o) => o.label)).toEqual([
      'Pre-IND',
      'IND',
      'Ph1',
      'Ph2',
      'Pre-3',
      'Ph3',
      'PreNDA/BLA',
      'NDA/BLA',
    ])
    expect(PIPELINE_PHASE_STATUS_OPTIONS.find((o) => o.code === 'PHASE_1')?.label).toBe('Ph1')
  })
})

describe('pipelineStatusOptions', () => {
  it('returns empty when no Phase column is selected', () => {
    expect(pipelineStatusOptions('')).toEqual([])
  })

  it('returns main-stage nodes for PRE_IND / IND / PRE_3 / PRE_NDA / NDA', () => {
    expect(pipelineStatusOptions('PRE_IND')[0]).toBe('已完成')
    expect(pipelineStatusOptions('PRE_IND')).toContain('PreIND 递交')
    expect(pipelineStatusOptions('IND')).toContain('IND 获批')
    expect(pipelineStatusOptions('PRE_3')).toContain('Pre3 递交')
    expect(pipelineStatusOptions('PRE_NDA')).toContain('PreNDA 递交')
    expect(pipelineStatusOptions('NDA')).toContain('NDA/BLA 获批')
  })

  it('returns all sub-statuses for clinical Phase columns', () => {
    const opts = pipelineStatusOptions('PHASE_1')
    expect(opts[0]).toBe('已完成')
    expect(opts.length).toBeGreaterThan(20)
    expect(opts).toContain('FPI')
    expect(opts).toContain('方案摘要定稿')
    expect(pipelineStatusOptions('PHASE_3')[0]).toBe('已完成')
    expect(pipelineStatusOptions('PHASE_3')).toContain('DBL')
  })
})
