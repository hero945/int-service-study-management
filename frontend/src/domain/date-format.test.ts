import { describe, expect, it } from 'vitest'
import { formatDate, formatDateTime, formatDateTimeSeconds, formatIsoDate, formatIsoMinute, todayIso } from './date-format'

describe('date-format', () => {
  it('formatDate 空值返回 fallback，非法值原样返回', () => {
    expect(formatDate(null)).toBe('—')
    expect(formatDate(undefined)).toBe('—')
    expect(formatDate('')).toBe('—')
    expect(formatDate(null, '')).toBe('')
    expect(formatDate('not-a-date')).toBe('not-a-date')
  })

  it('formatDate 输出 zh-CN 日期', () => {
    const result = formatDate('2026-07-28T10:00:00Z')
    expect(result).toContain('2026')
    expect(result).toContain('7')
  })

  it('formatDateTimeSeconds 精确到秒', () => {
    const result = formatDateTimeSeconds('2026-07-28T10:05:09')
    expect(result).toContain('2026')
    expect(result).toMatch(/10:05:09/)
    expect(formatDateTimeSeconds(null)).toBe('—')
  })

  it('formatDateTime 精确到分钟', () => {
    const result = formatDateTime('2026-07-28T10:05:00')
    expect(result).toContain('2026')
    expect(result).toContain('10:05')
    expect(formatDateTime(null)).toBe('—')
  })

  it('formatIsoMinute 截取到分钟并把 T 换成空格', () => {
    expect(formatIsoMinute('2026-07-28T14:05:30Z')).toBe('2026-07-28 14:05')
    expect(formatIsoMinute('2026-07-28')).toBe('2026-07-28')
    expect(formatIsoMinute(null)).toBe('—')
  })

  it('formatIsoDate 取日期部分', () => {
    expect(formatIsoDate('2026-07-28T14:05:30Z')).toBe('2026-07-28')
    expect(formatIsoDate(null)).toBe('—')
  })

  it('todayIso 输出 YYYY-MM-DD', () => {
    expect(todayIso()).toMatch(/^\d{4}-\d{2}-\d{2}$/)
  })
})
