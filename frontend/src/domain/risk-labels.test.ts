import { describe, expect, it } from 'vitest'
import {
  riskActionStatusLabel,
  riskActionSummaryLabel,
  riskLevelLabel,
  riskLevelTone,
  riskScoreLevelLabel,
  riskScoreRuleLines,
  riskStatusLabel,
} from './risk-labels'

describe('risk-labels', () => {
  it('maps risk levels to Chinese labels', () => {
    expect(riskLevelLabel('LOW')).toBe('低风险')
    expect(riskLevelLabel('MEDIUM')).toBe('中风险')
    expect(riskLevelLabel('HIGH')).toBe('高危')
  })

  it('maps risk statuses to Chinese labels', () => {
    expect(riskStatusLabel('OPEN')).toBe('未关闭')
    expect(riskStatusLabel('CLOSED')).toBe('已关闭')
  })

  it('maps action statuses to Chinese labels', () => {
    expect(riskActionStatusLabel('OPEN')).toBe('未开始')
    expect(riskActionStatusLabel('IN_PROGRESS')).toBe('进行中')
    expect(riskActionStatusLabel('COMPLETED')).toBe('已完成')
    expect(riskActionStatusLabel('CANCELLED')).toBe('已取消')
  })

  it('falls back to the original code for unknown values', () => {
    expect(riskLevelLabel('CRITICAL')).toBe('CRITICAL')
    expect(riskStatusLabel('PENDING')).toBe('PENDING')
    expect(riskActionStatusLabel('BLOCKED')).toBe('BLOCKED')
  })

  it('maps tones and score thresholds from the active scoring rule', () => {
    expect(riskLevelTone('HIGH')).toBe('red')
    expect(riskLevelTone('MEDIUM')).toBe('orange')
    expect(riskLevelTone('LOW')).toBe('green')
    expect(riskScoreLevelLabel(12)).toBe('低风险')
    expect(riskScoreLevelLabel(13)).toBe('中风险')
    expect(riskScoreLevelLabel(36)).toBe('中风险')
    expect(riskScoreLevelLabel(37)).toBe('高危')
    expect(riskScoreLevelLabel(20, { lowMax: 24, mediumMax: 48 })).toBe('低风险')
    expect(riskScoreLevelLabel(25, { lowMax: 24, mediumMax: 48 })).toBe('中风险')
    expect(riskScoreLevelLabel(49, { lowMax: 24, mediumMax: 48 })).toBe('高危')
  })

  it('builds score rule tip lines from the active scoring thresholds', () => {
    const lines = riskScoreRuleLines({ lowMax: 12, mediumMax: 36 })
    expect(lines.join(' ')).toContain('影响程度 a')
    expect(lines.join(' ')).toContain('≤12 低风险')
    expect(lines.join(' ')).toContain('13–36 中风险')
    expect(lines.join(' ')).toContain('≥37 高危')
  })

  it('summarizes open and overdue actions for list display', () => {
    expect(riskActionSummaryLabel({ actionCount: 0, openActionCount: 0, overdueActionCount: 0 })).toBe('—')
    expect(riskActionSummaryLabel({ actionCount: 2, openActionCount: 0, overdueActionCount: 0 })).toBe('全部完成')
    expect(riskActionSummaryLabel({ actionCount: 3, openActionCount: 2, overdueActionCount: 1 }))
      .toBe('未完成 2 · 逾期 1')
  })
})
