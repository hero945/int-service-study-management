import { describe, expect, it } from 'vitest'
import {
  riskActionStatusLabel,
  riskLevelLabel,
  riskLevelTone,
  riskScoreLevelLabel,
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

  it('maps tones and score thresholds without changing scoring rules', () => {
    expect(riskLevelTone('HIGH')).toBe('red')
    expect(riskLevelTone('MEDIUM')).toBe('orange')
    expect(riskLevelTone('LOW')).toBe('green')
    expect(riskScoreLevelLabel(12)).toBe('低风险')
    expect(riskScoreLevelLabel(13)).toBe('中风险')
    expect(riskScoreLevelLabel(36)).toBe('中风险')
    expect(riskScoreLevelLabel(37)).toBe('高危')
  })
})
