/** 风险等级 / 风险状态 / 措施状态展示文案（API 仍用英文 code） */

const RISK_LEVEL_LABELS: Record<string, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高危',
}

const RISK_STATUS_LABELS: Record<string, string> = {
  OPEN: '未关闭',
  CLOSED: '已关闭',
}

const RISK_ACTION_STATUS_LABELS: Record<string, string> = {
  OPEN: '未开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

/** 未知 code 回退为原值，避免空白 */
export function riskLevelLabel(code: string): string {
  return RISK_LEVEL_LABELS[code] ?? code
}

export function riskStatusLabel(code: string): string {
  return RISK_STATUS_LABELS[code] ?? code
}

export function riskActionStatusLabel(code: string): string {
  return RISK_ACTION_STATUS_LABELS[code] ?? code
}

export function riskLevelTone(level: string): 'red' | 'orange' | 'green' {
  if (level === 'HIGH') return 'red'
  if (level === 'MEDIUM') return 'orange'
  return 'green'
}

/** 按评分推导展示等级文案（阈值与前后端一致：≤12 / ≤36） */
export function riskScoreLevelLabel(score: number): string {
  if (score <= 12) return RISK_LEVEL_LABELS.LOW
  if (score <= 36) return RISK_LEVEL_LABELS.MEDIUM
  return RISK_LEVEL_LABELS.HIGH
}
