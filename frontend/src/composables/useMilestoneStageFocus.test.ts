import { describe, expect, it } from 'vitest'
import { milestoneScrollOffset, resolveMilestoneStage } from './useMilestoneStageFocus'

describe('resolveMilestoneStage', () => {
  const codes = ['Protocol', 'SSU', 'Enrollment', 'IA', 'Data_Report']

  it('keeps a valid query stage so pipeline/deep links still land on the clicked section', () => {
    expect(resolveMilestoneStage('SSU', codes, 'Data_Report')).toBe('SSU')
  })

  it('falls back to the current frontier when query is missing', () => {
    expect(resolveMilestoneStage('', codes, 'Data_Report')).toBe('Data_Report')
    expect(resolveMilestoneStage(undefined, codes, 'Data_Report')).toBe('Data_Report')
  })

  it('falls back to the current frontier when query is not on this page', () => {
    expect(resolveMilestoneStage('PreIND', codes, 'Data_Report')).toBe('Data_Report')
  })

  it('uses the first stage when neither query nor frontier is usable', () => {
    expect(resolveMilestoneStage('', codes, '')).toBe('Protocol')
    expect(resolveMilestoneStage('PreIND', [], 'Data_Report')).toBe('')
  })
})

describe('milestoneScrollOffset', () => {
  it('places the target row just below the sticky table header', () => {
    expect(milestoneScrollOffset(120, 40, 80, 36)).toBe(124)
  })

  it('does not scroll above the top of the card', () => {
    expect(milestoneScrollOffset(10, 40, 0, 36)).toBe(0)
  })
})
