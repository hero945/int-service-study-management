/**
 * 里程碑导出/总览完成标志推导（与后端 MonthlyExportManager / MilestoneManager 对齐）。
 * 供 mock 与前端展示规则复用；不代替服务端权威计算。
 */

export const EXPORT_STATUS = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
} as const

export type ExportStatus = (typeof EXPORT_STATUS)[keyof typeof EXPORT_STATUS]

export interface MilestoneNodeDates {
  actualStartDate: string | null
  actualEndDate: string | null
}

export interface MilestoneNodeWithStage extends MilestoneNodeDates {
  stageCode: string
  milestoneCode?: string
}

/** Study 完成节点：中心关闭（Data & Report 最后一节点） */
export const STUDY_COMPLETED_CODE = 'Data_Report-7'

/**
 * 导出 Study 状态（对齐 MonthlyExportManager.deriveMilestoneStatus）：
 * - 未开始：无节点，或任何节点都无 actual start/end
 * - 已完成：定义顺序最后一个节点有 actual_end
 * - 进行中：其余
 */
export function deriveMilestoneExportStatus(nodes: MilestoneNodeDates[]): ExportStatus {
  if (!nodes.length) return EXPORT_STATUS.NOT_STARTED
  const anyActual = nodes.some((n) => n.actualStartDate != null || n.actualEndDate != null)
  if (!anyActual) return EXPORT_STATUS.NOT_STARTED
  const last = nodes[nodes.length - 1]
  return last.actualEndDate != null ? EXPORT_STATUS.COMPLETED : EXPORT_STATUS.IN_PROGRESS
}

function isStageLastNodeCompleted(nodes: MilestoneNodeWithStage[], stageCode: string): boolean {
  let found = false
  let lastActualEnd: string | null = null
  for (const node of nodes) {
    if (node.stageCode === stageCode) {
      found = true
      lastActualEnd = node.actualEndDate
    }
  }
  return found && lastActualEnd != null
}

/**
 * 总览完成标志（对齐 MilestoneManager.computeOverviewStatus）：
 * - preindCompleted / indCompleted：对应 stage 最后一个节点有 actual_end
 * - globallyCompleted：Data_Report-7（中心关闭）有 actual_end；缺行视为未完成
 */
export function deriveOverviewCompletionFlags(nodes: MilestoneNodeWithStage[]): {
  preindCompleted: boolean
  indCompleted: boolean
  globallyCompleted: boolean
} {
  if (!nodes.length) {
    return { preindCompleted: false, indCompleted: false, globallyCompleted: false }
  }
  return {
    preindCompleted: isStageLastNodeCompleted(nodes, 'PreIND'),
    indCompleted: isStageLastNodeCompleted(nodes, 'IND'),
    globallyCompleted: nodes.some(
      (n) => n.milestoneCode === STUDY_COMPLETED_CODE && n.actualEndDate != null,
    ),
  }
}

/** 从 MilestonePage.groups 展平为定义顺序节点（含 stageCode） */
export function flattenMilestoneNodes(
  groups: Array<{ stageCode: string; nodes: Array<MilestoneNodeDates & { milestoneCode?: string }> }>,
): MilestoneNodeWithStage[] {
  return groups.flatMap((g) =>
    g.nodes.map((n) => ({
      stageCode: g.stageCode,
      milestoneCode: n.milestoneCode,
      actualStartDate: n.actualStartDate,
      actualEndDate: n.actualEndDate,
    })),
  )
}

/** 注册/监管阶段编码（Project 维度里程碑） */
export const REGULATORY_STAGE_CODES = ['PreIND', 'IND', 'Pre3', 'PreNDA_BLA', 'NDA_BLA'] as const
export type RegulatoryStageCode = (typeof REGULATORY_STAGE_CODES)[number]

export function isRegulatoryStageCode(code: string): code is RegulatoryStageCode {
  return (REGULATORY_STAGE_CODES as readonly string[]).includes(code)
}

/** 临床阶段编码（Study 维度里程碑） */
export const CLINICAL_STAGE_CODES = ['Protocol', 'SSU', 'Enrollment', 'IA', 'Data_Report'] as const
export type ClinicalStageCode = (typeof CLINICAL_STAGE_CODES)[number]

export function isClinicalStageCode(code: string): code is ClinicalStageCode {
  return (CLINICAL_STAGE_CODES as readonly string[]).includes(code)
}

export interface StageProjection {
  currentStageCode: string
  currentStageName: string
  currentMilestoneCode: string
  currentMilestoneName: string
  statusText: string
}

export interface MilestoneNodeLike extends MilestoneNodeDates {
  milestoneCode?: string
  milestoneName?: string
}

/**
 * 从阶段组推导当前投影状态（对齐后端 MilestoneManager.projectionFromNodes）。
 * 按 groups 顺序遍历节点，最后一个有 actual start/end 的节点为 frontier。
 * 最末节点有 actualEnd → 已完成；无 frontier → 未开始；否则 → 进行中。
 */
export function deriveMilestoneProjection(
  groups: Array<{ stageCode: string; stageName: string; nodes: MilestoneNodeLike[] }>,
): StageProjection {
  let frontier: { stageCode: string; stageName: string; milestoneCode: string; milestoneName: string } | null = null
  let lastNode: MilestoneNodeLike | null = null

  for (const group of groups) {
    for (const node of group.nodes) {
      lastNode = node
      if (node.actualStartDate != null || node.actualEndDate != null) {
        frontier = {
          stageCode: group.stageCode,
          stageName: group.stageName,
          milestoneCode: node.milestoneCode ?? '',
          milestoneName: node.milestoneName ?? '',
        }
      }
    }
  }

  if (lastNode && lastNode.actualEndDate != null) {
    return {
      currentStageCode: '',
      currentStageName: '',
      currentMilestoneCode: '',
      currentMilestoneName: '',
      statusText: '已完成',
    }
  }

  if (frontier == null) {
    return {
      currentStageCode: '',
      currentStageName: '',
      currentMilestoneCode: '',
      currentMilestoneName: '',
      statusText: '未开始',
    }
  }

  return {
    currentStageCode: frontier.stageCode,
    currentStageName: frontier.stageName,
    currentMilestoneCode: frontier.milestoneCode,
    currentMilestoneName: frontier.milestoneName,
    statusText: '进行中',
  }
}

/** 单个里程碑节点状态的中文标签（MilestoneView / StudyDetailDrawer 共用） */
export function milestoneNodeStatusLabel(status: string): string {
  return { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' }[status] ?? status
}

/** 单个里程碑节点状态的着色 class（green/blue/''） */
export function milestoneNodeStatusClass(status: string): string {
  return status === 'COMPLETED' ? 'green' : status === 'IN_PROGRESS' ? 'blue' : ''
}
