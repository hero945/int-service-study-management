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

/** 与后端 hd_plt_risk_rule_version 生效阈值对应；缺省时仅作离线回退 */
export interface RiskScoringRuleThresholds {
  lowMax: number
  mediumMax: number
}

const DEFAULT_SCORE_RULE: RiskScoringRuleThresholds = { lowMax: 12, mediumMax: 36 }

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

/** 按评分与生效规则阈值推导展示等级文案 */
export function riskScoreLevelLabel(
  score: number,
  rule: RiskScoringRuleThresholds = DEFAULT_SCORE_RULE,
): string {
  if (score <= rule.lowMax) return RISK_LEVEL_LABELS.LOW
  if (score <= rule.mediumMax) return RISK_LEVEL_LABELS.MEDIUM
  return RISK_LEVEL_LABELS.HIGH
}

/** 由生效评分规则生成悬停提示文案 */
export function riskScoreRuleLines(
  rule: RiskScoringRuleThresholds = DEFAULT_SCORE_RULE,
): string[] {
  return [
    '总分 = 影响程度 a × 发生可能性 b × 可探测性 c',
    'a、b、c 各取值 1–5，总分范围 1–125',
    `等级：≤${rule.lowMax} 低风险 · ${rule.lowMax + 1}–${rule.mediumMax} 中风险 · ≥${rule.mediumMax + 1} 高危`,
  ]
}

/** a/b/c 量表旁说明 */
export function riskFactorScaleHint(): string {
  return '1=很低 · 2=低 · 3=中 · 4=高 · 5=很高'
}

/** 列表措施摘要 */
export function riskActionSummaryLabel(risk: {
  actionCount: number
  openActionCount: number
  overdueActionCount: number
}): string {
  if (!risk.actionCount) return '—'
  if (!risk.openActionCount) return '全部完成'
  const parts = [`未完成 ${risk.openActionCount}`]
  if (risk.overdueActionCount) parts.push(`逾期 ${risk.overdueActionCount}`)
  return parts.join(' · ')
}

/** 措施允许的下一状态（与后端状态机一致） */
export function allowedNextActionStatuses(
  current: string,
): Array<'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'> {
  switch (current) {
    case 'OPEN':
      return ['IN_PROGRESS', 'COMPLETED', 'CANCELLED']
    case 'IN_PROGRESS':
      return ['COMPLETED', 'CANCELLED']
    case 'COMPLETED':
    case 'CANCELLED':
      return ['IN_PROGRESS']
    default:
      return ['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
  }
}
