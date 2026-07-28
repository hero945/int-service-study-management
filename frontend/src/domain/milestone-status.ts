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
}

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
 * - globallyCompleted：全局最后一个里程碑节点有 actual_end
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
    globallyCompleted: nodes[nodes.length - 1].actualEndDate != null,
  }
}

/** 从 MilestonePage.groups 展平为定义顺序节点（含 stageCode） */
export function flattenMilestoneNodes(
  groups: Array<{ stageCode: string; nodes: MilestoneNodeDates[] }>,
): MilestoneNodeWithStage[] {
  return groups.flatMap((g) =>
    g.nodes.map((n) => ({
      stageCode: g.stageCode,
      actualStartDate: n.actualStartDate,
      actualEndDate: n.actualEndDate,
    })),
  )
}

/** 单个里程碑节点状态的中文标签（MilestoneView / StudyDetailDrawer 共用） */
export function milestoneNodeStatusLabel(status: string): string {
  return { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' }[status] ?? status
}

/** 单个里程碑节点状态的着色 class（green/blue/''） */
export function milestoneNodeStatusClass(status: string): string {
  return status === 'COMPLETED' ? 'green' : status === 'IN_PROGRESS' ? 'blue' : ''
}
