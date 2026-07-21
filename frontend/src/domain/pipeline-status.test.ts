import { describe, expect, it } from 'vitest'
import type { Study } from '../api/types'
import { PHASE_TAGS, getPipelineCell } from './pipeline-status'

const study = (phase: string, statusLabel = '进行中'): Study => ({
  id: 1,
  code: 'S1',
  name: '研究一',
  indication: '适应症',
  phase,
  status: 'ACTIVE',
  statusLabel,
  statusTone: 'info',
  ownerName: '张伟',
  startDate: null,
  updatedAt: '2026-07-17T00:00:00',
})

describe('pipeline phase cells', () => {
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

  it('backfills earlier columns for a study configured at a later phase', () => {
    expect(getPipelineCell(study('Phase 2'), 'PreIND')).toEqual({
      label: '已完成',
      tone: 'green',
      explanation: 'PreIND 实际无项目，由 Phase 2 回填',
    })
  })

  it('shows current status only in the configured phase and leaves future phases empty', () => {
    expect(getPipelineCell(study('IND', '已递交'), 'IND')).toEqual({
      label: '已递交',
      tone: 'blue',
      explanation: undefined,
    })
    expect(getPipelineCell(study('IND', '已递交'), 'Phase 1').label).toBe('—')
  })
})
