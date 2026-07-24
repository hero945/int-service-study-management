import { describe, expect, it } from 'vitest'
import {
  EXPORT_STATUS,
  deriveMilestoneExportStatus,
  deriveOverviewCompletionFlags,
  flattenMilestoneNodes,
} from './milestone-status'

describe('deriveMilestoneExportStatus', () => {
  it('returns 未开始 when empty or no actual dates', () => {
    expect(deriveMilestoneExportStatus([])).toBe(EXPORT_STATUS.NOT_STARTED)
    expect(deriveMilestoneExportStatus([
      { actualStartDate: null, actualEndDate: null },
    ])).toBe(EXPORT_STATUS.NOT_STARTED)
  })

  it('returns 进行中 when some actuals exist but last node incomplete', () => {
    expect(deriveMilestoneExportStatus([
      { actualStartDate: '2026-01-01', actualEndDate: '2026-01-02' },
      { actualStartDate: '2026-02-01', actualEndDate: null },
    ])).toBe(EXPORT_STATUS.IN_PROGRESS)
  })

  it('returns 已完成 when last node has actual_end', () => {
    expect(deriveMilestoneExportStatus([
      { actualStartDate: '2026-01-01', actualEndDate: '2026-01-02' },
      { actualStartDate: '2026-02-01', actualEndDate: '2026-02-03' },
    ])).toBe(EXPORT_STATUS.COMPLETED)
  })
})

describe('deriveOverviewCompletionFlags', () => {
  it('marks PreIND/IND complete only when stage last node has actual_end', () => {
    const flags = deriveOverviewCompletionFlags([
      { stageCode: 'PreIND', actualStartDate: 'a', actualEndDate: 'b' },
      { stageCode: 'PreIND', actualStartDate: 'c', actualEndDate: 'd' },
      { stageCode: 'IND', actualStartDate: 'e', actualEndDate: null },
      { stageCode: 'NDA_BLA', actualStartDate: null, actualEndDate: null },
    ])
    expect(flags.preindCompleted).toBe(true)
    expect(flags.indCompleted).toBe(false)
    expect(flags.globallyCompleted).toBe(false)
  })

  it('marks globallyCompleted from the absolute last node', () => {
    const flags = deriveOverviewCompletionFlags([
      { stageCode: 'PreIND', actualStartDate: 'a', actualEndDate: 'b' },
      { stageCode: 'NDA_BLA', actualStartDate: 'c', actualEndDate: 'd' },
    ])
    expect(flags.globallyCompleted).toBe(true)
  })
})

describe('flattenMilestoneNodes', () => {
  it('preserves group order and attaches stageCode', () => {
    const nodes = flattenMilestoneNodes([
      { stageCode: 'PreIND', nodes: [{ actualStartDate: 'a', actualEndDate: 'b' }] },
      { stageCode: 'IND', nodes: [{ actualStartDate: null, actualEndDate: null }] },
    ])
    expect(nodes).toEqual([
      { stageCode: 'PreIND', actualStartDate: 'a', actualEndDate: 'b' },
      { stageCode: 'IND', actualStartDate: null, actualEndDate: null },
    ])
  })
})
