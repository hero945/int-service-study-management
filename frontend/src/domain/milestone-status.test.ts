import { describe, expect, it } from 'vitest'
import {
  CLINICAL_STAGE_CODES,
  EXPORT_STATUS,
  REGULATORY_STAGE_CODES,
  deriveMilestoneExportStatus,
  deriveMilestoneProjection,
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

describe('stage code classification', () => {
  it('lists all registration stage codes', () => {
    expect(REGULATORY_STAGE_CODES).toEqual(['PreIND', 'IND', 'Pre3', 'PreNDA_BLA', 'NDA_BLA'])
  })

  it('lists all clinical stage codes', () => {
    expect(CLINICAL_STAGE_CODES).toEqual(['Protocol', 'SSU', 'Enrollment', 'IA', 'Data_Report'])
  })
})

describe('deriveMilestoneProjection', () => {
  const group = (stageCode: string, stageName: string, nodes: Array<{ milestoneCode: string; milestoneName: string; actualStartDate: string | null; actualEndDate: string | null }>) => ({
    stageCode,
    stageName,
    nodes,
  })

  it('returns 未开始 when no nodes have actual dates', () => {
    const result = deriveMilestoneProjection([
      group('Protocol', 'Protocol', [
        { milestoneCode: 'Protocol-0', milestoneName: '方案摘要定稿', actualStartDate: null, actualEndDate: null },
      ]),
    ])
    expect(result.statusText).toBe('未开始')
    expect(result.currentStageCode).toBe('')
  })

  it('returns 进行中 with the latest node that has actual dates as frontier', () => {
    const result = deriveMilestoneProjection([
      group('Protocol', 'Protocol', [
        { milestoneCode: 'Protocol-0', milestoneName: '方案摘要定稿', actualStartDate: '2026-01-01', actualEndDate: '2026-01-05' },
        { milestoneCode: 'Protocol-1', milestoneName: '方案讨论会', actualStartDate: '2026-02-01', actualEndDate: null },
      ]),
    ])
    expect(result.statusText).toBe('进行中')
    expect(result.currentStageCode).toBe('Protocol')
    expect(result.currentMilestoneCode).toBe('Protocol-1')
    expect(result.currentMilestoneName).toBe('方案讨论会')
  })

  it('returns 已完成 when the last node has actual end', () => {
    const result = deriveMilestoneProjection([
      group('Protocol', 'Protocol', [
        { milestoneCode: 'Protocol-0', milestoneName: '方案摘要定稿', actualStartDate: '2026-01-01', actualEndDate: '2026-01-05' },
      ]),
    ])
    expect(result.statusText).toBe('已完成')
  })

  it('ignores filtered-out registration stages when computing clinical projection', () => {
    const result = deriveMilestoneProjection([
      group('PreIND', 'PreIND', [
        { milestoneCode: 'PreIND-0', milestoneName: 'PreIND 递交', actualStartDate: '2026-01-01', actualEndDate: '2026-01-10' },
      ]),
      group('Protocol', 'Protocol', [
        { milestoneCode: 'Protocol-0', milestoneName: '方案摘要定稿', actualStartDate: '2026-02-01', actualEndDate: null },
      ]),
    ])
    expect(result.statusText).toBe('进行中')
    expect(result.currentStageCode).toBe('Protocol')
    expect(result.currentMilestoneCode).toBe('Protocol-0')
  })
})
