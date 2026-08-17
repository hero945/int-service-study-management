import {
  CLINICAL_PHASE_CODES,
  normalizePhase,
  phaseLabel,
  type PipelinePhase,
} from './pipeline-status'

/** 筛选用：里程碑主阶段 → 子节点标签（与 MilestoneDefinition 对齐） */
export const MILESTONE_STATUS_BY_STAGE: Record<string, string[]> = {
  PreIND: [
    'PreIND 递交',
    'PreIND 反馈-临床医学',
    'PreIND 反馈-数统',
    'PreIND 反馈-临床药理',
    'PreIND 反馈-非临床',
    'PreIND 反馈-药学',
  ],
  IND: ['IND 递交', 'IND 形审发补', 'IND 形审补正', 'IND 受理', 'IND 获批'],
  Pre3: [
    'Pre3 递交',
    'Pre3 反馈-临床医学',
    'Pre3 反馈-数统',
    'Pre3 反馈-临床药理',
    'Pre3 反馈-非临床',
    'Pre3 反馈-药学',
  ],
  Protocol: ['方案摘要定稿', '方案讨论会', '方案定稿'],
  SSU: [
    '组长单位立项递交',
    '组长单位立项获批',
    '组长单位伦理递交',
    '组长单位伦理获批',
    '组长单位合同签署',
    '首家中心启动',
    '组长单位启动',
    '所有中心启动',
    '人遗递交',
    '人遗批准',
    'CDE 平台登记',
    'ClinicalTrial 登记',
  ],
  Enrollment: ['FPI', 'LPI', 'LPO'],
  IA: ['IA 数据冻结', 'IA 数据分析'],
  'Data & Report': ['DBL', 'TLR初稿', 'TLR定稿', 'TFL初稿', 'TFL定稿', 'CSR初稿', 'CSR定稿', '中心关闭'],
  'PreNDA/BLA': [
    'PreNDA 递交',
    'PreNDA 反馈-临床医学',
    'PreNDA 反馈-数统',
    'PreNDA 反馈-临床药理',
    'PreNDA 反馈-非临床',
    'PreNDA 反馈-药学',
  ],
  'NDA/BLA': [
    'NDA/BLA 递交',
    'NDA/BLA 形审发补',
    'NDA/BLA 形审补正',
    'NDA/BLA 受理',
    '临床核查',
    '药学核查',
    'NDA/BLA 发补',
    'NDA/BLA 补正',
    'NDA/BLA 获批',
  ],
}

/** Study 列表「当前阶段」筛选项（里程碑主阶段） */
export const STUDY_MILESTONE_PHASE_OPTIONS = Object.keys(MILESTONE_STATUS_BY_STAGE)

/** 全部里程碑子状态 */
export const ALL_MILESTONE_SUB_STATUSES = Object.values(MILESTONE_STATUS_BY_STAGE).flat()

/**
 * 管线配置页 Study 的 Phase Status 下拉：仅保留临床阶段 Ph1/Ph2/Ph3。
 */
export const PIPELINE_CONFIG_PHASE_STATUS_OPTIONS = ['PHASE_1', 'PHASE_2', 'PHASE_3'].map(
  (code) => ({
    code,
    label: phaseLabel(code),
  }),
)

/**
 * 管线总览临床 phase 枚举：用来选定表格列（不做行过滤）。
 * value 为 DB code，与 CLINICAL_PHASE_CODES 一致；label 为人类可读标签。
 */
export const PIPELINE_PHASE_STATUS_OPTIONS = CLINICAL_PHASE_CODES.map((code) => ({
  code,
  label: phaseLabel(code),
}))

/** PreIND / IND / PRE-3 / PRE-NDA / NDA 对应的里程碑主阶段 key（状态下拉用） */
const MAIN_STAGE_BY_PHASE_CODE: Record<string, string> = {
  PRE_IND: 'PreIND',
  IND: 'IND',
  PRE_3: 'Pre3',
  PRE_NDA: 'PreNDA/BLA',
  NDA: 'NDA/BLA',
}

/** Phase 下拉 code → 总览表格列 tag */
export function phaseCodeToColumn(phaseCode: string): PipelinePhase | undefined {
  if (!phaseCode) return undefined
  return normalizePhase(phaseCode)
}

/**
 * 管线总览「状态」下拉选项（依赖已选列）：
 * - 未选 Phase → 空
 * - PreIND / IND / PRE-3 → 「已完成」+ 对应主阶段里程碑节点
 * - 其他 Phase → 「已完成」+ 全部里程碑子状态
 * 「已完成」与表格 chip 文案一致，用于筛已完成列单元格。
 */
export function pipelineStatusOptions(phaseCode: string): string[] {
  if (!phaseCode) return []
  const stageKey = MAIN_STAGE_BY_PHASE_CODE[phaseCode]
  const nodes = stageKey
    ? (MILESTONE_STATUS_BY_STAGE[stageKey] ?? [])
    : ALL_MILESTONE_SUB_STATUSES
  return ['已完成', ...nodes]
}

/** Study 列表：选了里程碑主阶段则列其子状态，否则列全部子状态 */
export function studyStatusOptions(milestonePhase: string): string[] {
  if (milestonePhase) return MILESTONE_STATUS_BY_STAGE[milestonePhase] ?? []
  return ALL_MILESTONE_SUB_STATUSES
}
