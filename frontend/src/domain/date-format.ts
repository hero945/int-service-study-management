/**
 * 统一的日期/时间展示格式化，替换各视图散落的
 * toLocaleDateString / toLocaleString / slice(0, 16).replace('T', ' ') 等写法。
 * 空值一律返回 fallback（默认 '—'），非法日期原样返回。
 */

export function formatDate(value: string | null | undefined, fallback = '—'): string {
  if (!value) return fallback
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('zh-CN')
}

/** '2026/08/17 14:05:30' 风格（zh-CN 区域，精确到秒） */
export function formatDateTimeSeconds(value: string | null | undefined, fallback = '—'): string {
  if (!value) return fallback
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
}

/** '2026-07-28 14:05' 风格（zh-CN 区域，精确到分钟） */
export function formatDateTime(value: string | null | undefined, fallback = '—'): string {
  if (!value) return fallback
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** ISO 串截取到分钟：'2026-07-28T14:05:30Z' → '2026-07-28 14:05' */
export function formatIsoMinute(value: string | null | undefined, fallback = '—'): string {
  if (!value) return fallback
  return value.length >= 16 ? value.slice(0, 16).replace('T', ' ') : value
}

/** ISO 串的日期部分：'2026-07-28T14:05:30Z' → '2026-07-28' */
export function formatIsoDate(value: string | null | undefined, fallback = '—'): string {
  if (!value) return fallback
  return value.slice(0, 10)
}

/** 今天的 ISO 日期（'2026-07-28'），用于表单默认值 */
export function todayIso(): string {
  return new Date().toISOString().slice(0, 10)
}
