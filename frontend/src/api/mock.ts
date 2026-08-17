import type { ApiClient } from './client'
import type {
  AssignRolesInput,
  ChangePasswordInput,
  CreateUserInput,
  CurrentUser,
  PipelineProgram,
  PipelineProject,
  PlatformPermission,
  PlatformRole,
  RiskDetail,
  MilestonePage,
  MilestoneNode,
  MilestoneUpdateInput,
  FunctionLineReport,
  ProjectMilestonePage,
  MonthlyReportEntry,
  MonthlyReportPage,
  FunctionLineHistory,
  MonthlyExportFormat,
  MonthlyExportQuery,
  MonthlyExportReport,
  StageProjection,
  OverviewArea,
  OverviewProject,
  Study,
  TeamMatrixAssignment,
  TeamMatrixRole,
  TherapeuticArea,
  UpdateUserInput,
} from './types'
import {
  ORIGIN_LABELS,
  SOURCE_LABELS,
  originLabel,
  sourceLabel,
} from '../domain/pipeline-status'
import {
  EXPORT_STATUS,
  isRegulatoryStageCode,
  deriveMilestoneExportStatus,
  deriveMilestoneProjection,
  deriveOverviewCompletionFlags,
  flattenMilestoneNodes,
} from '../domain/milestone-status'
import { deriveRiskLevel } from '../domain/risk-labels'

const users: Array<CurrentUser & { password: string }> = [
  {
    username: 'chen@eastchinapharm.com',
    displayName: '陈研发',
    title: '系统管理员',
    roles: ['ADMIN'],
    permissions: [
      'pipeline.page.view',
      'study.read',
      'milestone.read',
      'milestone.update',
      'project.milestone.read',
      'project.milestone.update',
      'config.page.view',
      'config.create',
      'config.update',
      'config.delete',
      'account.page.view',
      'account.create',
      'platform.setting.read',
      'platform.setting.update',
      'role.page.view',
      'role.create',
      'role.update',
      'role.delete',
      'audit.read',
      'team.page.view',
      'team.edit_mode',
      'team.update',
      'risk.page.view',
      'risk.read',
      'risk.create',
      'risk.update',
      'risk.delete',
      'monthly.read',
      'monthly.create',
      'monthly.update',
      'report.page.view',
      'report.export',
    ],
    dataScope: 'ALL',
    password: '1234',
  },
  {
    username: 'zhangwei@eastchinapharm.com',
    displayName: '张伟',
    title: '项目负责人 · PL',
    roles: ['USER'],
    permissions: ['pipeline.page.view', 'study.read', 'milestone.read', 'milestone.update', 'project.milestone.read', 'project.milestone.update', 'monthly.read', 'monthly.create', 'monthly.update', 'report.page.view', 'report.export', 'risk.page.view', 'risk.read', 'risk.create', 'risk.update'],
    dataScope: 'ALL',
    password: '1234',
  },
  {
    username: 'liuyang@eastchinapharm.com',
    displayName: '刘洋',
    title: '质量观察员',
    roles: ['VIEWER'],
    permissions: ['pipeline.page.view', 'study.read', 'milestone.read', 'monthly.read', 'report.page.view', 'risk.page.view', 'risk.read'],
    dataScope: 'ALL',
    password: '1234',
  },
]

const STUDY_STATUS_META = {
  PLANNED: { label: '计划中', tone: 'neutral' },
  ACTIVE: { label: '进行中', tone: 'positive' },
  COMPLETED: { label: '已完成', tone: 'info' },
} as const

type StudySeed = Omit<Study, 'id' | 'status' | 'statusLabel' | 'statusTone'> & {
  status: keyof typeof STUDY_STATUS_META
}
type StudyBase = Omit<StudySeed, 'code' | 'phase' | 'status' | 'ownerName' | 'startDate' | 'updatedAt'>
type StudyVariant = Pick<StudySeed, 'code' | 'phase' | 'status' | 'ownerName' | 'startDate' | 'updatedAt'>

function expand(base: StudyBase, variants: StudyVariant[]): StudySeed[] {
  return variants.map((variant) => ({ ...base, ...variant }))
}

// 演示数据集：HDM1005 · 1 Program · 2 Project · 8 Study（覆盖全部 phase，公开叙事）
const studySeeds: StudySeed[] = [
  ...expand({
    indication: '2型糖尿病',
    therapeuticAreaCode: 'METABOLIC_CARDIOVASCULAR',
    therapeuticAreaName: '代谢与心血管',
    programCode: 'HDM1005',
    projectCode: 'HDM1005-T2DM',
    productName: 'HDM1005',
    moa: 'Peptide',
    sourceCode: 'SELF_DEVELOPED',
    originCode: 'DOMESTIC',
  }, [
    { code: 'HDM1005-T2DM-00', phase: 'PRE_IND', status: 'PLANNED', ownerName: '张伟', startDate: '2023-10-01', updatedAt: '2026-07-15T09:20:00' },
    { code: 'HDM1005-T2DM-01', phase: 'IND', status: 'ACTIVE', ownerName: '张伟', startDate: '2023-12-22', updatedAt: '2026-07-14T10:00:00' },
    { code: 'HDM1005-T2DM-02', phase: 'PHASE_1', status: 'COMPLETED', ownerName: '李静', startDate: '2024-04-15', updatedAt: '2025-01-31T16:00:00' },
    { code: 'HDM1005-T2DM-03', phase: 'PHASE_2', status: 'COMPLETED', ownerName: '李静', startDate: '2025-03-01', updatedAt: '2026-02-28T16:00:00' },
  ]),
  ...expand({
    indication: '超重或肥胖',
    therapeuticAreaCode: 'METABOLIC_CARDIOVASCULAR',
    therapeuticAreaName: '代谢与心血管',
    programCode: 'HDM1005',
    projectCode: 'HDM1005-OBE',
    productName: 'HDM1005',
    moa: 'Peptide',
    sourceCode: 'SELF_DEVELOPED',
    originCode: 'DOMESTIC',
  }, [
    { code: 'HDM1005-OBE-01', phase: 'PRE_3', status: 'PLANNED', ownerName: '王芳', startDate: '2026-06-01', updatedAt: '2026-07-12T11:00:00' },
    { code: 'HDM1005-OBE-02', phase: 'PHASE_3', status: 'ACTIVE', ownerName: '王芳', startDate: '2026-02-28', updatedAt: '2026-07-20T09:00:00' },
    { code: 'HDM1005-OBE-03', phase: 'PHASE_3', status: 'ACTIVE', ownerName: '张伟', startDate: '2026-03-02', updatedAt: '2026-07-21T14:00:00' },
  ]),
  // 追加在同 Project 的 I 期第二条 Study，演示临床列多 Study 纵向列表
  {
    code: 'HDM1005-T2DM-04',
    indication: '2型糖尿病',
    therapeuticAreaCode: 'METABOLIC_CARDIOVASCULAR',
    therapeuticAreaName: '代谢与心血管',
    programCode: 'HDM1005',
    projectCode: 'HDM1005-T2DM',
    productName: 'HDM1005',
    moa: 'Peptide',
    sourceCode: 'SELF_DEVELOPED',
    originCode: 'DOMESTIC',
    phase: 'PHASE_1',
    status: 'ACTIVE',
    ownerName: '李静',
    startDate: '2026-01-15',
    updatedAt: '2026-07-25T10:00:00',
  },
]

export const demoStudies: Study[] = studySeeds.map((seed, index) => ({
  ...seed,
  id: index + 1,
  statusLabel: STUDY_STATUS_META[seed.status].label,
  statusTone: STUDY_STATUS_META[seed.status].tone,
}))

// 每个 study 的里程碑总览状态（演示用，模拟里程碑推导出的【主状态/子状态/当前阶段完成】）。
const mockOverviewMilestoneView: Record<string, {
  mainStageLabel: string
  subStatusLabel: string
  currentPhaseCompleted: boolean
}> = {
  'HDM1005-T2DM-00': { mainStageLabel: 'PreIND', subStatusLabel: 'PreIND 反馈-数统', currentPhaseCompleted: false },
  'HDM1005-T2DM-01': { mainStageLabel: 'IND', subStatusLabel: 'IND 获批', currentPhaseCompleted: false },
  'HDM1005-T2DM-02': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPO', currentPhaseCompleted: true },
  'HDM1005-T2DM-03': { mainStageLabel: 'Data & Report', subStatusLabel: 'TLR定稿', currentPhaseCompleted: true },
  'HDM1005-OBE-01': { mainStageLabel: 'Pre3', subStatusLabel: 'Pre3 反馈-临床医学', currentPhaseCompleted: false },
  'HDM1005-OBE-02': { mainStageLabel: 'Enrollment', subStatusLabel: 'FPI', currentPhaseCompleted: false },
  'HDM1005-OBE-03': { mainStageLabel: 'Enrollment', subStatusLabel: 'FPI', currentPhaseCompleted: false },
  'HDM1005-T2DM-04': { mainStageLabel: 'Enrollment', subStatusLabel: 'FPI', currentPhaseCompleted: false },
}

/** 全量里程碑 code 顺序（与 MilestoneDefinition 一致） */
const MILESTONE_CODE_ORDER = [
  ...Array.from({ length: 6 }, (_, i) => `PreIND-${i}`),
  ...Array.from({ length: 5 }, (_, i) => `IND-${i}`),
  ...Array.from({ length: 6 }, (_, i) => `Pre3-${i}`),
  ...Array.from({ length: 3 }, (_, i) => `Protocol-${i}`),
  ...Array.from({ length: 12 }, (_, i) => `SSU-${i}`),
  ...Array.from({ length: 3 }, (_, i) => `Enrollment-${i}`),
  ...Array.from({ length: 2 }, (_, i) => `IA-${i}`),
  ...Array.from({ length: 8 }, (_, i) => `Data_Report-${i}`),
  ...Array.from({ length: 6 }, (_, i) => `PreNDA_BLA-${i}`),
  ...Array.from({ length: 9 }, (_, i) => `NDA_BLA-${i}`),
] as const

/** 每个 Study 的里程碑 frontier（与 mockOverviewMilestoneView 对齐） */
const STUDY_MILESTONE_FRONTIER: Record<string, { frontierCode: string; completed?: boolean }> = {
  'HDM1005-T2DM-00': { frontierCode: 'PreIND-2' },
  'HDM1005-T2DM-01': { frontierCode: 'IND-4' },
  'HDM1005-T2DM-02': { frontierCode: 'Enrollment-2', completed: true },
  'HDM1005-T2DM-03': { frontierCode: 'Data_Report-2', completed: true },
  'HDM1005-OBE-01': { frontierCode: 'Pre3-3' },
  'HDM1005-OBE-02': { frontierCode: 'Enrollment-0' },
  'HDM1005-OBE-03': { frontierCode: 'Enrollment-0' },
  'HDM1005-T2DM-04': { frontierCode: 'Enrollment-0' },
}

const SOURCE_LABEL = SOURCE_LABELS
const ORIGIN_LABEL = ORIGIN_LABELS

/** Overview seed stage order (aligned with MilestoneDefinition / mockOverviewMilestoneView). */
const OVERVIEW_STAGE_ORDER = [
  'PreIND', 'IND', 'Pre3', 'Protocol', 'SSU', 'Enrollment', 'IA', 'Data & Report', 'PreNDA/BLA', 'NDA/BLA',
] as const

/**
 * When no full milestone tree exists, approximate MilestoneManager completion flags
 * from the overview frontier stage: stages already passed count as completed;
 * current stage counts when currentPhaseCompleted.
 */
function deriveOverviewCompletionFromStage(
  mainStageLabel: string | null | undefined,
  currentPhaseCompleted: boolean,
): { preindCompleted: boolean; indCompleted: boolean; globallyCompleted: boolean } {
  const rank = mainStageLabel
    ? (OVERVIEW_STAGE_ORDER as readonly string[]).indexOf(mainStageLabel)
    : -1
  if (rank < 0) {
    return { preindCompleted: false, indCompleted: false, globallyCompleted: false }
  }
  return {
    preindCompleted: rank > 0 || (rank === 0 && currentPhaseCompleted),
    indCompleted: rank > 1 || (rank === 1 && currentPhaseCompleted),
    globallyCompleted: rank === OVERVIEW_STAGE_ORDER.length - 1 && currentPhaseCompleted,
  }
}

let nextRiskId = 6
let nextRiskActionId = 10

/** find 的断言版本：mock 场景找不到即数据错误，直接抛出明确信息 */
function mustFind<T>(items: T[], predicate: (item: T) => boolean, message: string): T {
  const found = items.find(predicate)
  if (!found) throw new Error(message)
  return found
}

function actionOverdue(plannedDate: string | null | undefined, status: string): boolean {
  if (!plannedDate || status === 'COMPLETED' || status === 'CANCELLED') return false
  return plannedDate < new Date().toISOString().slice(0, 10)
}

function syncRiskTracking(detail: RiskDetail) {
  const open = detail.actions.filter(item => item.status === 'OPEN' || item.status === 'IN_PROGRESS')
  detail.risk.actionCount = detail.actions.length
  detail.risk.openActionCount = open.length
  detail.risk.overdueActionCount = open.filter(item => actionOverdue(item.plannedDate, item.status)).length
  const dates = open.map(item => item.plannedDate).filter((value): value is string => !!value).sort()
  detail.risk.nextPlannedDate = dates[0] ?? null
  for (const action of detail.actions) {
    action.overdue = actionOverdue(action.plannedDate, action.status)
  }
  if (!detail.activities) detail.activities = []
  if (detail.closedTime === undefined) detail.closedTime = null
}

const mockRisks: RiskDetail[] = [
  {
    risk: {
      riskId: 1, riskCode: 'RSK-2026-000006', studyId: 1, studyCode: 'HDM1005-T2DM-00',
      programCode: 'HDM1005', projectCode: 'HDM1005-T2DM', functionCode: 'RA',
      functionName: '注册', description: 'Pre-IND 沟通窗口可能推迟 IND 资料定稿与递交节奏',
      ownerUserId: 2, ownerName: '张伟', score: 24, level: 'LOW', status: 'OPEN',
      actionCount: 1, openActionCount: 1, overdueActionCount: 0, nextPlannedDate: '2026-08-15',
      version: 0, updatedAt: '2026-07-22T09:00:00Z',
    },
    registeredDate: '2026-07-15', closeReason: '', closedTime: null,
    assessments: [{ id: 1, number: 1, impact: 3, likelihood: 2, detectability: 4,
      score: 24, level: 'LOW', reason: '首次评估', assessedBy: '张伟',
      assessedAt: '2026-07-15T09:00:00Z' }],
    actions: [{ id: 1, description: '锁定 Pre-IND 问题清单与责任人周报', ownerUserId: 2,
      ownerName: '张伟', plannedDate: '2026-08-15', completedDate: null,
      status: 'OPEN', completionNote: '', version: 0, overdue: false }],
    activities: [{
      type: 'ASSESSMENT', title: '第 1 次评估 · 24 分 · LOW',
      detail: '3 × 2 × 4', at: '2026-07-15T09:00:00Z', by: '张伟',
    }],
  },
  {
    risk: {
      riskId: 2, riskCode: 'RSK-2026-000007', studyId: 2, studyCode: 'HDM1005-T2DM-01',
      programCode: 'HDM1005', projectCode: 'HDM1005-T2DM', functionCode: 'RA',
      functionName: '注册', description: 'IND 获批后多适应症并行可能稀释注册资源（T2DM + 体重管理）',
      ownerUserId: 2, ownerName: '张伟', score: 27, level: 'MEDIUM', status: 'OPEN',
      actionCount: 1, openActionCount: 1, overdueActionCount: 0, nextPlannedDate: '2026-08-01',
      version: 0, updatedAt: '2026-07-18T10:00:00Z',
    },
    registeredDate: '2026-07-15', closeReason: '', closedTime: null,
    assessments: [{ id: 2, number: 1, impact: 3, likelihood: 3, detectability: 3,
      score: 27, level: 'MEDIUM', reason: '首次评估', assessedBy: '张伟',
      assessedAt: '2026-07-15T09:00:00Z' }],
    actions: [{ id: 2, description: '按适应症拆分注册计划与文档责任矩阵', ownerUserId: 2,
      ownerName: '张伟', plannedDate: '2026-08-01', completedDate: null,
      status: 'OPEN', completionNote: '', version: 0, overdue: false }],
    activities: [],
  },
  {
    risk: {
      riskId: 3, riskCode: 'RSK-2026-000009', studyId: 4, studyCode: 'HDM1005-T2DM-03',
      programCode: 'HDM1005', projectCode: 'HDM1005-T2DM', functionCode: 'CM',
      functionName: '临床医学', description: 'II 期主要终点 HbA1c 解读与阳性对照（度拉糖肽）供应波动可能影响总结时间表',
      ownerUserId: 2, ownerName: '张伟', score: 24, level: 'MEDIUM', status: 'OPEN',
      actionCount: 1, openActionCount: 1, overdueActionCount: 0, nextPlannedDate: '2026-08-10',
      version: 0, updatedAt: '2026-07-20T08:00:00Z',
    },
    registeredDate: '2026-07-15', closeReason: '', closedTime: null,
    assessments: [{ id: 3, number: 1, impact: 4, likelihood: 2, detectability: 3,
      score: 24, level: 'MEDIUM', reason: '首次评估', assessedBy: '张伟',
      assessedAt: '2026-07-15T09:00:00Z' }],
    actions: [{ id: 3, description: '与供应与统计周会对齐对照药与分析锁定日期', ownerUserId: 2,
      ownerName: '张伟', plannedDate: '2026-08-10', completedDate: null,
      status: 'IN_PROGRESS', completionNote: '', version: 0, overdue: false }],
    activities: [],
  },
  {
    risk: {
      riskId: 4, riskCode: 'RSK-2026-000011', studyId: 6, studyCode: 'HDM1005-OBE-02',
      programCode: 'HDM1005', projectCode: 'HDM1005-OBE', functionCode: 'PM',
      functionName: '项目管理', description: 'III 期经治人群（2026-02-28 FPI）多中心并行下监察资源紧张',
      ownerUserId: 2, ownerName: '张伟', score: 36, level: 'MEDIUM', status: 'OPEN',
      actionCount: 1, openActionCount: 1, overdueActionCount: 0, nextPlannedDate: '2026-08-12',
      version: 0, updatedAt: '2026-07-21T09:00:00Z',
    },
    registeredDate: '2026-07-15', closeReason: '', closedTime: null,
    assessments: [{ id: 4, number: 1, impact: 4, likelihood: 3, detectability: 3,
      score: 36, level: 'MEDIUM', reason: '首次评估', assessedBy: '张伟',
      assessedAt: '2026-07-15T09:00:00Z' }],
    actions: [{ id: 4, description: '按区域分派监察访视优先级与后备监察员', ownerUserId: 2,
      ownerName: '张伟', plannedDate: '2026-08-12', completedDate: null,
      status: 'OPEN', completionNote: '', version: 0, overdue: false }],
    activities: [],
  },
  {
    risk: {
      riskId: 5, riskCode: 'RSK-2026-000012', studyId: 7, studyCode: 'HDM1005-OBE-03',
      programCode: 'HDM1005', projectCode: 'HDM1005-OBE', functionCode: 'CM',
      functionName: '临床医学', description: 'III 期初治人群（2026-03-02 FPI）筛查失败率偏高可能影响入组曲线',
      ownerUserId: 2, ownerName: '张伟', score: 36, level: 'MEDIUM', status: 'OPEN',
      actionCount: 1, openActionCount: 1, overdueActionCount: 0, nextPlannedDate: '2026-08-08',
      version: 0, updatedAt: '2026-07-21T14:00:00Z',
    },
    registeredDate: '2026-07-15', closeReason: '', closedTime: null,
    assessments: [{ id: 5, number: 1, impact: 3, likelihood: 3, detectability: 4,
      score: 36, level: 'MEDIUM', reason: '首次评估', assessedBy: '张伟',
      assessedAt: '2026-07-15T09:00:00Z' }],
    actions: [{ id: 5, description: '优化筛查教育材料并复核入排执行一致性', ownerUserId: 2,
      ownerName: '张伟', plannedDate: '2026-08-08', completedDate: null,
      status: 'OPEN', completionNote: '', version: 0, overdue: false }],
    activities: [],
  },
]
mockRisks.forEach((detail) => syncRiskTracking(detail))


const teamRoles: TeamMatrixRole[] = [
  ['PL', 'PL 项目负责人', 'PM', '项目管理'],
  ['APL', 'APL 副项目负责人', 'PM', '项目管理'],
  ['PM', 'PM 项目经理', 'PM', '项目管理'],
  ['APM', 'APM 副项目经理', 'PM', '项目管理'],
  ['RA_SPONSOR', 'RA Sponsor', 'RA', '注册'],
  ['RA_MANAGER', 'RA Manager', 'RA', '注册'],
  ['RA_SPECIALIST', 'RA Specialist', 'RA', '注册'],
  ['RA_CMC', 'RA CMC', 'RA', '注册'],
  ['CM_SPONSOR', 'CM Sponsor', 'CM', '临床医学'],
  ['CM', 'CM', 'CM', '临床医学'],
  ['CP_SPONSOR', 'CP Sponsor', 'CP', '临床药理'],
  ['CP', 'CP', 'CP', '临床药理'],
  ['PV_SPONSOR', 'PV Sponsor', 'PV', '药物警戒'],
  ['PVP', 'PVP', 'PV', '药物警戒'],
  ['PVO', 'PVO', 'PV', '药物警戒'],
  ['TM_SPONSOR', 'TM Sponsor', 'TM', '试验管理'],
  ['TM', 'TM', 'TM', '试验管理'],
  ['CO_SPONSOR', 'CO Sponsor', 'CO', '临床运营'],
  ['CTM', 'CTM', 'CO', '临床运营'],
  ['ACTM', 'ACTM', 'CO', '临床运营'],
  ['LAB', 'Lab', 'LAB', '中心实验室'],
  ['LAB_BACKUP', 'Lab backup', 'LAB', '中心实验室'],
  ['SUPPLY', 'Supply', 'SUPPLY', '供应保障'],
  ['SUPPLY_BACKUP', 'Supply backup', 'SUPPLY', '供应保障'],
  ['CTA_PROCESS', 'CTA process', 'CTA', '临床试验协调'],
  ['CTA_TMF', 'CTA TMF', 'CTA', '临床试验协调'],
  ['ST_SPONSOR', 'ST Sponsor', 'ST', '生物统计'],
  ['ST', 'ST', 'ST', '生物统计'],
  ['PG_SPONSOR', 'PG Sponsor', 'PG', '统计编程'],
  ['PG', 'PG', 'PG', '统计编程'],
  ['DM_SPONSOR', 'DM Sponsor', 'DM', '数据管理'],
  ['DM', 'DM', 'DM', '数据管理'],
  ['MW', 'MW', 'MW', '医学写作'],
  ['NC_CONTACT', 'NC-contact', 'NC', '非临床'],
  ['NC_PK', 'NC-PK', 'NC', '非临床'],
  ['NC_PD', 'NC-PD', 'NC', '非临床'],
  ['NC_TOX', 'NC-TOX', 'NC', '非临床'],
  ['CMC_PL', 'CMC-PL', 'CMC', '药学CMC'],
  ['CMC_PM', 'CMC-PM', 'CMC', '药学CMC'],
  ['CMC_DS', 'CMC-DS', 'CMC', '药学CMC'],
  ['CMC_DP', 'CMC-DP', 'CMC', '药学CMC'],
  ['CMC_OA', 'CMC-OA', 'CMC', '药学CMC'],
  ['CMC_RA', 'CMC-RA', 'CMC', '药学CMC'],
  ['IP', 'IP', 'IP', '药品管理'],
].map(([roleCode, roleName, functionCode, functionName]) => ({
  roleCode, roleName, functionCode, functionName,
}))

// Mock 里程碑演示数据

function delay(ms: number) { return new Promise(r => setTimeout(r, ms)) }

const mockMilestones = new Map<number, MilestonePage>()
const mockProjectMilestones = new Map<string, ProjectMilestonePage>()

const mockProjectRegulatoryStatus: Record<string, import('./types').RegulatoryStatus> = {
  'HDM1005-T2DM': {
    mainStageCode: 'IND',
    mainStageLabel: 'IND',
    subStatusLabel: 'IND 获批',
    preindCompleted: true,
    indCompleted: false,
    pre3Completed: false,
    prendaCompleted: false,
    ndaCompleted: false,
    preindSubStatusLabel: 'PreIND 获批',
    indSubStatusLabel: 'IND 获批',
  },
  'HDM1005-OBE': {
    mainStageCode: 'Pre3',
    mainStageLabel: 'Pre3',
    subStatusLabel: 'Pre3 反馈-临床医学',
    preindCompleted: true,
    indCompleted: true,
    pre3Completed: false,
    prendaCompleted: false,
    ndaCompleted: false,
    preindSubStatusLabel: 'PreIND 获批',
    indSubStatusLabel: 'IND 获批',
    pre3SubStatusLabel: 'Pre3 反馈-临床医学',
  },
}

function buildProjectMilestones(projectCode: string): ProjectMilestonePage {
  const regulatory = mockProjectRegulatoryStatus[projectCode]
  const base = buildBaseMilestones(projectCode)
  const page: ProjectMilestonePage = {
    projectCode,
    groups: base.groups.map((g) => ({
      stageCode: g.stageCode,
      stageName: g.stageName,
      nodes: g.nodes.map((n) => ({ ...n })),
    })),
  }
  if (regulatory) {
    // Derive a frontier from the regulatory status for demo realism
    const frontier = frontierMilestoneForRegulatory(regulatory)
    if (frontier) {
      applyMilestoneFrontier(page as unknown as MilestonePage, frontier.code, frontier.completed)
    }
  }
  return page
}

function frontierMilestoneForRegulatory(regulatory: import('./types').RegulatoryStatus):
    { code: string; completed: boolean } | undefined {
  if (regulatory.ndaCompleted) return { code: 'NDA_BLA-8', completed: true }
  if (regulatory.prendaCompleted) return { code: 'PreNDA_BLA-5', completed: true }
  if (regulatory.pre3Completed) return { code: 'Pre3-5', completed: true }
  if (regulatory.indCompleted) return { code: 'IND-4', completed: true }
  if (regulatory.preindCompleted) return { code: 'PreIND-5', completed: false }
  return undefined
}

function findProjectMilestoneNode(page: ProjectMilestonePage, code: string): MilestoneNode | undefined {
  for (const group of page.groups) {
    const node = group.nodes.find((n) => n.milestoneCode === code)
    if (node) return node
  }
  return undefined
}

function findMilestoneNode(page: MilestonePage, code: string): MilestoneNode | undefined {
  for (const group of page.groups) {
    const node = group.nodes.find((n) => n.milestoneCode === code)
    if (node) return node
  }
  return undefined
}

function milestoneDateOffset(baseIndex: number): string {
  const dt = new Date('2025-01-01T00:00:00')
  dt.setDate(dt.getDate() + baseIndex * 7)
  return dt.toISOString().slice(0, 10)
}

/** 将 frontier 之前节点标为已完成，frontier 本身进行中或已完成。 */
function applyMilestoneFrontier(
  page: MilestonePage,
  frontierCode: string,
  completed = false,
): void {
  const frontierIdx = MILESTONE_CODE_ORDER.indexOf(frontierCode as typeof MILESTONE_CODE_ORDER[number])
  if (frontierIdx < 0) throw new Error(`Unknown milestone frontier: ${frontierCode}`)

  for (let i = 0; i < MILESTONE_CODE_ORDER.length; i++) {
    const code = MILESTONE_CODE_ORDER[i]
    const node = findMilestoneNode(page, code)
    if (!node) continue

    if (i < frontierIdx) {
      node.planV1Date = milestoneDateOffset(i)
      node.actualStartDate = milestoneDateOffset(i)
      node.actualEndDate = milestoneDateOffset(i + 1)
      node.status = 'COMPLETED'
      continue
    }
    if (i === frontierIdx) {
      node.planV1Date = milestoneDateOffset(i)
      node.actualStartDate = milestoneDateOffset(i)
      if (completed) {
        node.actualEndDate = milestoneDateOffset(i + 1)
        node.status = 'COMPLETED'
      } else {
        node.actualEndDate = null
        node.status = 'IN_PROGRESS'
      }
      continue
    }
    node.planV1Date = null
    node.planV2Date = null
    node.actualStartDate = null
    node.actualEndDate = null
    node.status = 'NOT_STARTED'
    node.deviationNote = null
  }
}

function projectionFromMilestonePage(page: {
  groups: { stageCode: string; stageName: string; nodes: MilestoneNode[] }[]
}): StageProjection {
  return deriveMilestoneProjection(page.groups)
}

function buildBaseMilestones(studyCode: string): MilestonePage {
  const notStarted = (code: string, name: string): MilestoneNode => ({
    milestoneCode: code,
    milestoneName: name,
    planV1Date: null,
    planV2Date: null,
    actualStartDate: null,
    actualEndDate: null,
    status: 'NOT_STARTED',
    deviationNote: null,
  })
  const now = new Date()
  const yyyy = now.getFullYear()
  const mm = String(now.getMonth() + 1).padStart(2, '0')
  const dd = String(now.getDate()).padStart(2, '0')
  const d = (days: number) => {
    const dt = new Date(now)
    dt.setDate(dt.getDate() + days)
    return dt.toISOString().slice(0, 10)
  }
  return {
    studyCode,
    groups: [
      { stageCode: 'PreIND', stageName: 'PreIND', nodes: [
        { milestoneCode: 'PreIND-0', milestoneName: 'PreIND 递交', planV1Date: d(-180), planV2Date: d(-175), actualStartDate: d(-178), actualEndDate: d(-176), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-1', milestoneName: 'PreIND 反馈-临床医学', planV1Date: d(-150), planV2Date: d(-145), actualStartDate: d(-148), actualEndDate: d(-140), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-2', milestoneName: 'PreIND 反馈-数统', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-142), actualEndDate: d(-138), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-3', milestoneName: 'PreIND 反馈-临床药理', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-140), actualEndDate: d(-135), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-4', milestoneName: 'PreIND 反馈-非临床', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-138), actualEndDate: d(-130), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-5', milestoneName: 'PreIND 反馈-药学', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-135), actualEndDate: d(-125), status: 'COMPLETED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'IND', stageName: 'IND', nodes: [
        { milestoneCode: 'IND-0', milestoneName: 'IND 递交', planV1Date: d(-100), planV2Date: d(-95), actualStartDate: d(-98), actualEndDate: d(-96), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-1', milestoneName: 'IND 形审发补', planV1Date: d(-80), planV2Date: d(-78), actualStartDate: d(-82), actualEndDate: d(-75), status: 'COMPLETED', deviationNote: 'CDE要求补充稳定性数据' },
        { milestoneCode: 'IND-2', milestoneName: 'IND 形审补正', planV1Date: d(-60), planV2Date: d(-58), actualStartDate: d(-62), actualEndDate: d(-55), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-3', milestoneName: 'IND 受理', planV1Date: d(-50), planV2Date: d(-48), actualStartDate: d(-52), actualEndDate: d(-46), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-4', milestoneName: 'IND 获批', planV1Date: d(-30), planV2Date: d(-28), actualStartDate: d(-30), actualEndDate: null, status: 'IN_PROGRESS', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'Pre3', stageName: 'Pre3', nodes: [
        { milestoneCode: 'Pre3-0', milestoneName: 'Pre3 递交', planV1Date: d(60), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-1', milestoneName: 'Pre3 反馈-临床医学', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-2', milestoneName: 'Pre3 反馈-数统', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-3', milestoneName: 'Pre3 反馈-临床药理', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-4', milestoneName: 'Pre3 反馈-非临床', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-5', milestoneName: 'Pre3 反馈-药学', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'Protocol', stageName: 'Protocol', nodes: [
        { milestoneCode: 'Protocol-0', milestoneName: '方案摘要定稿', planV1Date: d(30), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Protocol-1', milestoneName: '方案讨论会', planV1Date: d(90), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Protocol-2', milestoneName: '方案定稿', planV1Date: d(150), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'SSU', stageName: 'SSU', nodes: [
        notStarted('SSU-0', '组长单位立项递交'),
        notStarted('SSU-1', '组长单位立项获批'),
        notStarted('SSU-2', '组长单位伦理递交'),
        notStarted('SSU-3', '组长单位伦理获批'),
        notStarted('SSU-4', '组长单位合同签署'),
        notStarted('SSU-5', '首家中心启动'),
        notStarted('SSU-6', '组长单位启动'),
        notStarted('SSU-7', '所有中心启动'),
        notStarted('SSU-8', '人遗递交'),
        notStarted('SSU-9', '人遗批准'),
        notStarted('SSU-10', 'CDE 平台登记'),
        notStarted('SSU-11', 'ClinicalTrial 登记'),
      ] satisfies MilestoneNode[]},
      { stageCode: 'Enrollment', stageName: 'Enrollment', nodes: [
        { milestoneCode: 'Enrollment-0', milestoneName: 'FPI', planV1Date: d(360), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Enrollment-1', milestoneName: 'LPI', planV1Date: d(720), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Enrollment-2', milestoneName: 'LPO', planV1Date: d(730), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'IA', stageName: 'IA', nodes: [
        { milestoneCode: 'IA-0', milestoneName: 'IA 数据冻结', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'IA-1', milestoneName: 'IA 数据分析', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'Data_Report', stageName: 'Data & Report', nodes: [
        { milestoneCode: 'Data_Report-0', milestoneName: 'DBL', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-1', milestoneName: 'TLR初稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-2', milestoneName: 'TLR定稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-3', milestoneName: 'TFL初稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-4', milestoneName: 'TFL定稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-5', milestoneName: 'CSR初稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-6', milestoneName: 'CSR定稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-7', milestoneName: '中心关闭', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'PreNDA_BLA', stageName: 'PreNDA/BLA', nodes: [
        notStarted('PreNDA_BLA-0', 'PreNDA 递交'),
        notStarted('PreNDA_BLA-1', 'PreNDA 反馈-临床医学'),
        notStarted('PreNDA_BLA-2', 'PreNDA 反馈-数统'),
        notStarted('PreNDA_BLA-3', 'PreNDA 反馈-临床药理'),
        notStarted('PreNDA_BLA-4', 'PreNDA 反馈-非临床'),
        notStarted('PreNDA_BLA-5', 'PreNDA 反馈-药学'),
      ] satisfies MilestoneNode[]},
      { stageCode: 'NDA_BLA', stageName: 'NDA/BLA', nodes: [
        notStarted('NDA_BLA-0', 'NDA/BLA 递交'),
        notStarted('NDA_BLA-1', 'NDA/BLA 形审发补'),
        notStarted('NDA_BLA-2', 'NDA/BLA 形审补正'),
        notStarted('NDA_BLA-3', 'NDA/BLA 受理'),
        notStarted('NDA_BLA-4', '临床核查'),
        notStarted('NDA_BLA-5', '药学核查'),
        notStarted('NDA_BLA-6', 'NDA/BLA 发补'),
        notStarted('NDA_BLA-7', 'NDA/BLA 补正'),
        notStarted('NDA_BLA-8', 'NDA/BLA 获批'),
      ] satisfies MilestoneNode[]},
    ],
  }
}

function buildStudyMilestones(studyId: number, studyCode: string): MilestonePage {
  const page = buildBaseMilestones(studyCode)
  const profile = STUDY_MILESTONE_FRONTIER[studyCode]
  if (profile) {
    applyMilestoneFrontier(page, profile.frontierCode, profile.completed ?? false)
  }
  return page
}

function mergeProjectMilestonesIntoStudy(studyPage: MilestonePage, study: Study | undefined): MilestonePage {
  if (!study || !study.projectCode) return studyPage
  let projectPage = mockProjectMilestones.get(study.projectCode)
  if (!projectPage) {
    projectPage = buildProjectMilestones(study.projectCode)
    mockProjectMilestones.set(study.projectCode, projectPage)
  }
  const projectGroups = new Map(projectPage.groups.map((g) => [g.stageCode, g]))
  return {
    ...studyPage,
    groups: studyPage.groups.map((group) => {
      if (!isRegulatoryStageCode(group.stageCode)) return group
      const projectGroup = projectGroups.get(group.stageCode)
      if (!projectGroup) return group
      return {
        stageCode: group.stageCode,
        stageName: group.stageName,
        nodes: projectGroup.nodes.map((n) => ({ ...n, source: 'PROJECT' as const })),
      }
    }),
  }
}

for (const study of demoStudies) {
  mockMilestones.set(study.id, buildStudyMilestones(study.id, study.code))
}

function overviewCompletionForStudy(study: Study, mv: {
  mainStageLabel: string
  currentPhaseCompleted: boolean
} | undefined) {
  const stored = mockMilestones.get(study.id)
  if (stored) {
    return deriveOverviewCompletionFlags(flattenMilestoneNodes(stored.groups))
  }
  return deriveOverviewCompletionFromStage(mv?.mainStageLabel, mv?.currentPhaseCompleted ?? false)
}

function nodeSortOrder(node: MilestoneNode, index: number): number {
  const order = (node as MilestoneNode & { sortOrder?: number }).sortOrder
  return typeof order === 'number' ? order : index
}

function deriveCurrentPhaseStatus(page: MilestonePage): { currentPhase: string; currentStatus: string } {
  const groups = [...page.groups].sort((a, b) => {
    const order = ['PreIND', 'IND', 'Pre3', 'Protocol', 'SSU', 'Enrollment', 'IA', 'Data_Report', 'PreNDA_BLA', 'NDA_BLA']
    return order.indexOf(b.stageCode) - order.indexOf(a.stageCode)
  })
  const currentGroup = groups.find(group =>
    group.nodes.some(node => node.actualStartDate != null || node.actualEndDate != null))
  if (!currentGroup) {
    return { currentPhase: '', currentStatus: '' }
  }
  const nodes = currentGroup.nodes
    .map((node, index) => ({ node, order: nodeSortOrder(node, index) }))
    .sort((a, b) => b.order - a.order)
    .map((item) => item.node)
  const currentNode = nodes.find(node => node.actualStartDate != null || node.actualEndDate != null)
  return {
    currentPhase: currentGroup.stageName,
    currentStatus: currentNode?.milestoneName ?? '',
  }
}


// Mock 月报演示数据
// 功能线 code/name 取自 V8__team_matrix.sql 种子；可编辑性模拟"当前用户被分配到该功能线"：
// study 1 临床医学+生物统计可编辑，study 2 注册可编辑，study 3 全部只读。

let nextMonthlyEntryId = 100
const mockMonthlyPages = new Map<string, MonthlyReportPage>()

function buildDemoMonthlyPage(studyId: number, month: string): MonthlyReportPage | undefined {
  const study = demoStudies.find((item) => item.id === studyId)
  if (!study) return undefined
  const me = 'zhangwei@eastchinapharm.com'
  const date = (day: string) => `${month}-${day}`
  const at = (day: string, time: string) => `${date(day)}T${time}Z`
  const line = (
    reportId: number, functionLineId: number, functionCode: string, functionName: string,
    editable: boolean,
    entries: Array<[number, string, string, string, string]>,
  ): FunctionLineReport => ({
    reportId, functionLineId, functionCode, functionName, editable,
    entries: entries.map(([entryId, day, content, updatedBy, time]) => ({
      entryId, entryDate: date(day), content, updatedBy, updatedAt: at(day, time), editable,
    })),
  })
  const linesByStudy: Record<number, FunctionLineReport[]> = {
    1: [
      line(101, 2, 'RA', '注册', true, [
        [1, '12', 'Pre-IND 资料包定稿中：对齐 GLP-1R/GIPR 双靶点长效激动剂 HDM1005 申报路径。', me, '09:30:00'],
      ]),
      line(102, 3, 'CM', '临床医学', true, [
        [2, '20', '确认首个临床适应症为饮食运动或二甲双胍治疗后血糖控制不佳的 T2DM 人群。', me, '10:00:00'],
      ]),
    ],
    4: [
      line(201, 3, 'CM', '临床医学', true, [
        [3, '10', '中国 II 期 T2DM：220 例，20 周，安慰剂与度拉糖肽对照；主要终点 HbA1c。', me, '09:00:00'],
        [4, '25', '公开披露显示降糖与减重疗效积极。', me, '16:00:00'],
      ]),
    ],
    6: [
      line(301, 7, 'CO', '临床运营', false, [
        [5, '05', 'T2DM III 期经治人群：2026-02-28 完成首例入组。', 'lijing@eastchinapharm.com', '13:00:00'],
      ]),
      line(302, 3, 'CM', '临床医学', true, [
        [6, '11', '确认筛选失败原因分类与 HbA1c 入排复核流程。', me, '10:30:00'],
      ]),
    ],
  }
  const functionLines = linesByStudy[studyId]
  if (!functionLines) {
    return { studyId, studyCode: study.code, month, functionLines: [] }
  }
  return { studyId, studyCode: study.code, month, functionLines }
}

function getMockMonthlyPage(studyId: number, month: string): MonthlyReportPage {
  const key = `${studyId}|${month}`
  let page = mockMonthlyPages.get(key)
  if (!page) {
    page = buildDemoMonthlyPage(studyId, month)
    if (!page) throw new Error('Study 不存在')
    mockMonthlyPages.set(key, page)
  }
  return page
}

function sortByUpdatedAtDesc<T extends { updatedAt?: string }>(items: T[]): T[] {
  return [...items].sort((a, b) => (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''))
}

export function createMockApiClient(): ApiClient {
  let currentUser: CurrentUser | undefined
  const teamVersions = new Map(demoStudies.map((study) => [study.id, 0]))
  const teamAssignments = new Map<string, number[]>(
    demoStudies.flatMap((study) => [
      [`${study.id}|PL`, [2]],
      [`${study.id}|PM`, [2]],
      [`${study.id}|RA_SPECIALIST`, [2]],
      [`${study.id}|CM`, [2]],
      [`${study.id}|CTM`, [2]],
    ]),
  )
  const permissions: PlatformPermission[] = [
    ['pipeline', 'pipeline.page.view', '查看管线总览', 'PAGE', 'view'],
    ['study', 'study.read', '查看 Study', 'ACTION', 'read'],
    ['milestone', 'milestone.read', '查看里程碑', 'DATA', 'read'],
    ['milestone', 'milestone.update', '修改里程碑', 'DATA', 'update'],
    ['config', 'config.page.view', '查看管线配置', 'PAGE', 'view'],
    ['config', 'config.create', '维护管线配置', 'ACTION', 'create'],
    ['config', 'config.update', '修改管线配置', 'ACTION', 'update'],
    ['config', 'config.delete', '删除管线配置', 'ACTION', 'delete'],
    ['account', 'account.page.view', '查看账号管理', 'PAGE', 'view'],
    ['account', 'account.create', '新增账号', 'ACTION', 'create'],
    ['role', 'role.page.view', '查看角色权限管理', 'PAGE', 'view'],
    ['role', 'role.create', '新增角色', 'ACTION', 'create'],
    ['role', 'role.update', '编辑角色权限', 'ACTION', 'update'],
    ['role', 'role.delete', '删除角色', 'ACTION', 'delete'],
    ['team', 'team.page.view', '查看团队矩阵', 'PAGE', 'view'],
    ['team', 'team.edit_mode', '进入团队编辑模式', 'PAGE_OPERATION', 'edit_mode'],
    ['team', 'team.update', '更新团队分配', 'ACTION', 'update'],
  ].map(([moduleCode, permissionCode, permissionName, permissionType, actionCode], index) => ({
    id: index + 1,
    moduleCode,
    permissionCode,
    permissionName,
    permissionType,
    actionCode,
    permissionDescription: null,
    sortOrder: (index + 1) * 10,
  }))
  let nextRoleId = 4
  const roles: PlatformRole[] = [
    {
      id: 1,
      roleCode: 'ADMIN',
      roleDescription: '系统管理员',
      dataScopeMode: 'ALL',
      status: 'ACTIVE',
      systemRole: true,
      assignedUserCount: 1,
      permissionCodes: permissions.map((permission) => permission.permissionCode),
      updatedAt: '2026-07-21T09:00:00',
    },
    {
      id: 2,
      roleCode: 'USER',
      roleDescription: '普通业务用户',
      dataScopeMode: 'ALL',
      status: 'ACTIVE',
      systemRole: true,
      assignedUserCount: 1,
      permissionCodes: ['pipeline.page.view', 'study.read'],
      updatedAt: '2026-07-21T09:00:00',
    },
    {
      id: 3,
      roleCode: 'VIEWER',
      roleDescription: '只读用户',
      dataScopeMode: 'ASSIGNED_STUDY',
      status: 'ACTIVE',
      systemRole: true,
      assignedUserCount: 1,
      permissionCodes: ['pipeline.page.view', 'study.read'],
      updatedAt: '2026-07-21T09:00:00',
    },
  ]
  const now = () => new Date().toISOString()
  const therapeuticAreas: TherapeuticArea[] = [
    { id: 1, code: 'ONCOLOGY', name: '肿瘤', englishName: 'Oncology' },
    { id: 2, code: 'AUTOIMMUNE', name: '自身免疫', englishName: 'Autoimmune Disease' },
    { id: 3, code: 'METABOLIC_CARDIOVASCULAR', name: '代谢与心血管', englishName: 'Metabolic and Cardiovascular' },
    { id: 4, code: 'RESPIRATORY', name: '呼吸系统', englishName: 'Respiratory' },
    { id: 5, code: 'INFECTIOUS_DISEASE', name: '感染性疾病', englishName: 'Infectious Disease' },
    { id: 6, code: 'NEUROSCIENCE', name: '神经科学', englishName: 'Neuroscience' },
  ]
  // 一个 program/project 下可有多个 study，按 code 去重派生，避免重复行
  const uniqueBy = <T,>(items: T[], key: (item: T) => string) =>
    [...new Map(items.map((item) => [key(item), item])).values()]
  const programs: PipelineProgram[] = uniqueBy(demoStudies, (s) => s.programCode ?? '')
    .map((study, index) => ({
      id: index + 1,
      code: study.programCode ?? '',
      productName: study.productName ?? '',
      moa: study.moa ?? null,
      sourceCode: study.sourceCode ?? '',
      sourceLabel: SOURCE_LABEL[study.sourceCode ?? ''] ?? '',
      originCode: study.originCode ?? '',
      originLabel: ORIGIN_LABEL[study.originCode ?? ''] ?? '',
      projectCount: new Set(demoStudies.filter((s) => s.programCode === study.programCode).map((s) => s.projectCode)).size,
      studyCount: demoStudies.filter((s) => s.programCode === study.programCode).length,
      version: 1,
      updatedAt: study.updatedAt,
    }))
  const projects: PipelineProject[] = uniqueBy(demoStudies, (s) => s.projectCode ?? '')
    .map((study, index) => ({
      id: index + 1,
      code: study.projectCode ?? '',
      programId: programs.find((p) => p.code === study.programCode)?.id ?? 0,
      programCode: study.programCode ?? '',
      indication: study.indication,
      therapeuticAreaId: therapeuticAreas.find((t) => t.code === study.therapeuticAreaCode)?.id ?? 0,
      therapeuticAreaCode: study.therapeuticAreaCode ?? '',
      therapeuticAreaName: study.therapeuticAreaName ?? '',
      studyCount: demoStudies.filter((s) => s.projectCode === study.projectCode).length,
      version: 1,
      updatedAt: study.updatedAt,
    }))
  let nextProgramId = programs.length + 1
  let nextProjectId = projects.length + 1
  let nextStudyId = demoStudies.length + 1

  return {
    async listAuditLogs(query) {
      return {
        data: [],
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 20,
        totalItems: 0,
        totalPages: 0,
      }
    },
    async getCurrentUser() {
      if (!currentUser) throw new Error('请先登录')
      return currentUser
    },
    async login(credentials) {
      const account = users.find(
        (item) =>
          item.username === credentials.username &&
          item.password === credentials.password,
      )
      if (!account) throw new Error('账号或密码错误')
      const { password: _password, ...user } = account
      currentUser = user
      return user
    },
    async logout() {
      currentUser = undefined
    },
    async getPipelineOverview() {
      // 按 projectCode 聚合 study → project，再按 TA code 分组 → area
      const byProject = new Map<string, Study[]>()
      for (const s of demoStudies) {
        const key = s.projectCode ?? s.code
        const list = byProject.get(key)
        if (list) list.push(s)
        else byProject.set(key, [s])
      }
      const byArea = new Map<string, { name: string; projects: OverviewProject[] }>()
      for (const [projectCode, studies] of byProject) {
        const first = studies[0]

        const project: OverviewProject = {
          id: first.id,
          code: projectCode,
          indication: first.indication,
          programCode: first.programCode ?? '',
          productName: first.productName ?? '',
          moa: first.moa ?? '',
          sourceCode: first.sourceCode ?? '',
          originCode: first.originCode ?? '',
          studies: studies.map((s) => {
            const mv = mockOverviewMilestoneView[s.code]
            const nameOf = (userId: number) => users[userId - 1]?.displayName ?? ''
            const roleNames = (studyId: number, roleCode: string) =>
              (teamAssignments.get(`${studyId}|${roleCode}`) ?? []).map(nameOf).filter(Boolean).join(' / ')
            return {
              id: s.id,
              code: s.code,
              phase: s.phase,
              status: s.status,
              statusLabel: s.statusLabel,
              statusTone: s.statusTone,
              mainStageCode: null,
              mainStageLabel: mv?.mainStageLabel ?? null,
              subStatusLabel: mv?.subStatusLabel ?? null,
              ...overviewCompletionForStudy(s, mv),
              currentPhaseCompleted: mv?.currentPhaseCompleted ?? false,
              startDate: s.startDate,
              updatedAt: s.updatedAt,
              plName: roleNames(s.id, 'PL'),
              pmName: roleNames(s.id, 'PM'),
              openRiskCount: mockRisks.filter(({ risk }) =>
                risk.studyId === s.id && risk.status === 'OPEN').length,
            }
          }),
          regulatoryStatus: mockProjectRegulatoryStatus[projectCode],
        }
        const taCode = first.therapeuticAreaCode ?? 'OTHER'
        const taName = first.therapeuticAreaName ?? '其他'
        const entry = byArea.get(taCode)
        if (entry) entry.projects.push(project)
        else byArea.set(taCode, { name: taName, projects: [project] })
      }
      const areas: OverviewArea[] = [...byArea.entries()].map(([code, { name, projects }]) => ({
        therapeuticAreaCode: code,
        therapeuticAreaName: name,
        projects,
      }))
      return { title: '临床研发管线', areas }
    },
    async listStudies(query = {}) {
      const nameOf = (userId: number) => users[userId - 1]?.displayName ?? ''
      const roleNames = (studyId: number, roleCode: string) =>
        (teamAssignments.get(`${studyId}|${roleCode}`) ?? []).map(nameOf).filter(Boolean).join(' / ')
      const all = demoStudies.map((study) => {
        const milestones = mockMilestones.get(study.id) ?? buildStudyMilestones(study.id, study.code)
        const { currentPhase, currentStatus } = deriveCurrentPhaseStatus(milestones)
        return {
          ...study,
          therapeuticAreaCode: study.therapeuticAreaCode ?? study.therapeuticAreaEn ?? study.therapeuticArea,
          therapeuticAreaName: study.therapeuticAreaName ?? study.therapeuticArea,
          programCode: study.programCode ?? study.program,
          projectCode: study.projectCode ?? study.project ?? study.product,
          plName: roleNames(study.id, 'PL'),
          pmName: roleNames(study.id, 'PM'),
          currentPhase,
          currentStatus,
          openRiskCount: mockRisks.filter(({ risk }) =>
            risk.studyId === study.id && risk.status === 'OPEN').length,
        }
      }).filter((study) => {
        if (query.therapeuticArea && !(
          study.therapeuticAreaName === query.therapeuticArea ||
          study.therapeuticArea === query.therapeuticArea ||
          study.therapeuticAreaCode === query.therapeuticArea
        )) return false
        if (query.program && !(
          (study.programCode ?? '').includes(query.program) ||
          (study.program ?? '').includes(query.program)
        )) return false
        if (query.product && !(
          (study.productName ?? '').includes(query.product) ||
          (study.product ?? '').includes(query.product)
        )) return false
        if (query.studyCode && !(study.code ?? '').includes(query.studyCode)) return false
        if (query.milestoneStatus && study.currentStatus !== query.milestoneStatus) return false
        return true
      })
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      const sorted = sortByUpdatedAtDesc(all)
      const total = sorted.length
      const totalPages = Math.max(1, Math.ceil(total / pageSize))
      return {
        data: sorted.slice((page - 1) * pageSize, page * pageSize),
        total,
        page,
        pageSize,
        totalPages,
      }
    },
    async listRisks(query = {}) {
      const keyword = query.query?.trim().toLowerCase() ?? ''
      const base = mockRisks.filter(({ risk }) =>
        (!keyword || [risk.riskCode, risk.description, risk.ownerName, risk.programCode,
          risk.studyCode, risk.projectCode]
          .some(value => value.toLowerCase().includes(keyword))) &&
        (!query.functionCode || risk.functionCode === query.functionCode))
      const filtered = base.filter(({ risk }) =>
        (!query.status || risk.status === query.status) &&
        (!query.level || risk.level === query.level) &&
        (!query.ownerUserId || risk.ownerUserId === query.ownerUserId) &&
        (!query.overdueOnly || risk.overdueActionCount > 0))
      filtered.forEach(item => syncRiskTracking(item))
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      const sorted = sortByUpdatedAtDesc(filtered.map(item => item.risk))
      return {
        data: sorted.slice((page - 1) * pageSize, page * pageSize),
        stats: {
          total: base.length,
          open: base.filter(item => item.risk.status === 'OPEN').length,
          high: base.filter(item => item.risk.level === 'HIGH').length,
          medium: base.filter(item => item.risk.level === 'MEDIUM').length,
        },
        pagination: { page, pageSize, totalItems: filtered.length,
          totalPages: Math.max(1, Math.ceil(filtered.length / pageSize)) },
      }
    },
    async getRisk(riskCode) {
      const risk = mockRisks.find(item => item.risk.riskCode === riskCode)
      if (!risk) throw new Error('风险不存在')
      return structuredClone(risk)
    },
    async getRiskFormOptions(studyId) {
      return {
        studies: demoStudies.map(study => ({ id: study.id, studyCode: study.code,
          programCode: study.programCode ?? '', projectCode: study.projectCode ?? '' })),
        functions: studyId ? [
          { id: 1, code: 'PM', name: '项目管理' },
          { id: 2, code: 'RA', name: '注册' },
          { id: 3, code: 'CLINICAL', name: '临床医学' },
        ] : [],
        owners: studyId ? users.map((item, index) => ({ id: index + 1,
          email: item.username, displayName: item.displayName })) : [],
        scoringRule: { id: 1, lowMax: 12, mediumMax: 36 },
      }
    },
    async createRisk(input) {
      const study = mustFind(demoStudies, item => item.id === input.studyId, 'Study 不存在')
      const options = await this.getRiskFormOptions(input.studyId)
      const fn = mustFind(options.functions, item => item.id === input.functionLineId, '功能线不存在')
      const owner = mustFind(options.owners, item => item.id === input.ownerUserId, 'Owner 不存在')
      const score = input.assessment.impact * input.assessment.likelihood * (6 - input.assessment.detectability)
      const level = deriveRiskLevel(score, options.scoringRule)
      const now = new Date().toISOString()
      const detail: RiskDetail = {
        risk: { riskId: nextRiskId, riskCode: `RSK-${new Date().getFullYear()}-${String(nextRiskId++).padStart(6, '0')}`,
          studyId: study.id, studyCode: study.code, programCode: study.programCode ?? '',
          projectCode: study.projectCode ?? '', functionCode: fn.code, functionName: fn.name,
          description: input.description, ownerUserId: owner.id, ownerName: owner.displayName,
          score, level, status: 'OPEN', actionCount: 0, openActionCount: 0, overdueActionCount: 0,
          nextPlannedDate: null, version: 0, updatedAt: now },
        registeredDate: input.registeredDate ?? now.slice(0, 10), closeReason: '', closedTime: null,
        assessments: [{ id: Date.now(), number: 1, ...input.assessment, score, level,
          reason: input.assessment.reason ?? '', assessedBy: currentUser?.displayName ?? '', assessedAt: now }],
        actions: input.actions.map(action => ({ id: nextRiskActionId++, description: action.description,
          ownerUserId: action.ownerUserId,
          ownerName: options.owners.find(item => item.id === action.ownerUserId)?.displayName ?? '',
          plannedDate: action.plannedDate ?? null, completedDate: action.completedDate ?? null,
          status: action.status ?? 'OPEN', completionNote: action.completionNote ?? '', version: 0,
          overdue: false })),
        activities: [{
          type: 'ASSESSMENT', title: `第 1 次评估 · ${score} 分 · ${level}`,
          detail: `${input.assessment.impact} × ${input.assessment.likelihood} × ${input.assessment.detectability}`,
          at: now, by: currentUser?.displayName ?? '',
        }],
      }
      for (const action of detail.actions) {
        detail.activities.push({
          type: 'ACTION', title: '新增控制措施', detail: action.description,
          at: now, by: currentUser?.displayName ?? '',
        })
      }
      syncRiskTracking(detail)
      mockRisks.unshift(detail)
      return structuredClone(detail)
    },
    async updateRisk(riskCode, input) {
      const detail = mockRisks.find(item => item.risk.riskCode === riskCode)
      if (!detail) throw new Error('风险不存在')
      if (detail.risk.version !== input.expectedVersion) throw new Error('风险已被其他用户修改')
      if (detail.risk.status === 'CLOSED' && input.status !== 'OPEN') {
        throw new Error('已关闭的风险不可编辑，请先重新打开')
      }
      if (input.status === 'CLOSED') {
        const active = detail.actions.some(item => item.status === 'OPEN' || item.status === 'IN_PROGRESS')
        if (active) throw new Error('存在未完成的控制措施，请先完成或取消后再关闭风险')
      }
      const options = await this.getRiskFormOptions(input.studyId)
      const study = mustFind(demoStudies, item => item.id === input.studyId, 'Study 不存在')
      const fn = mustFind(options.functions, item => item.id === input.functionLineId, '功能线不存在')
      const owner = mustFind(options.owners, item => item.id === input.ownerUserId, 'Owner 不存在')
      const previousStatus = detail.risk.status
      Object.assign(detail.risk, { studyId: study.id, studyCode: study.code,
        programCode: study.programCode ?? '', projectCode: study.projectCode ?? '',
        functionCode: fn.code, functionName: fn.name, ownerUserId: owner.id,
        ownerName: owner.displayName, description: input.description, status: input.status,
        version: detail.risk.version + 1, updatedAt: new Date().toISOString() })
      detail.registeredDate = input.registeredDate ?? detail.registeredDate
      if (previousStatus !== input.status) {
        if (input.status === 'CLOSED') {
          detail.closeReason = input.statusReason ?? ''
          detail.closedTime = new Date().toISOString()
        }
        detail.activities.unshift({
          type: 'STATUS', title: `状态 ${previousStatus} → ${input.status}`,
          detail: input.statusReason ?? '', at: new Date().toISOString(),
          by: currentUser?.displayName ?? '',
        })
      }
      if (input.assessment) {
        const score = input.assessment.impact * input.assessment.likelihood * (6 - input.assessment.detectability)
        const level = deriveRiskLevel(score, options.scoringRule)
        Object.assign(detail.risk, { score, level })
        detail.assessments.unshift({ id: Date.now(), number: detail.assessments.length + 1,
          ...input.assessment, score, level, reason: input.assessment.reason ?? '',
          assessedBy: currentUser?.displayName ?? '', assessedAt: new Date().toISOString() })
        detail.activities.unshift({
          type: 'ASSESSMENT', title: `第 ${detail.assessments[0].number} 次评估 · ${score} 分 · ${level}`,
          detail: input.assessment.reason ?? '', at: new Date().toISOString(),
          by: currentUser?.displayName ?? '',
        })
      }
      syncRiskTracking(detail)
      return structuredClone(detail)
    },
    async deleteRisk(riskCode, expectedVersion) {
      const index = mockRisks.findIndex(item => item.risk.riskCode === riskCode)
      if (index < 0 || mockRisks[index].risk.version !== expectedVersion) throw new Error('风险删除失败')
      mockRisks.splice(index, 1)
    },
    async addRiskAction(riskCode, expectedRiskVersion, action) {
      const detail = mustFind(mockRisks, item => item.risk.riskCode === riskCode, '风险不存在')
      if (detail.risk.status === 'CLOSED') throw new Error('已关闭的风险不可再维护控制措施')
      if (detail.risk.version !== expectedRiskVersion) throw new Error('风险已被修改')
      const options = await this.getRiskFormOptions(detail.risk.studyId)
      detail.actions.push({ id: nextRiskActionId++, description: action.description,
        ownerUserId: action.ownerUserId,
        ownerName: options.owners.find(item => item.id === action.ownerUserId)?.displayName ?? '',
        plannedDate: action.plannedDate ?? null, completedDate: action.completedDate ?? null,
        status: action.status ?? 'OPEN', completionNote: action.completionNote ?? '', version: 0,
        overdue: false })
      detail.activities.unshift({
        type: 'ACTION', title: '新增控制措施', detail: action.description,
        at: new Date().toISOString(), by: currentUser?.displayName ?? '',
      })
      detail.risk.version++
      syncRiskTracking(detail)
      return structuredClone(detail)
    },
    async updateRiskAction(riskCode, actionId, expectedVersion, action) {
      const detail = mustFind(mockRisks, item => item.risk.riskCode === riskCode, '风险不存在')
      if (detail.risk.status === 'CLOSED') throw new Error('已关闭的风险不可再维护控制措施')
      const target = mustFind(detail.actions, item => item.id === actionId, '措施不存在')
      if (target.version !== expectedVersion) throw new Error('措施已被修改')
      const from = target.status
      Object.assign(target, {
        description: action.description,
        ownerUserId: action.ownerUserId,
        plannedDate: action.plannedDate ?? null,
        completedDate: action.completedDate ?? null,
        status: action.status ?? target.status,
        completionNote: action.completionNote ?? '',
        version: target.version + 1,
      })
      detail.activities.unshift({
        type: 'ACTION',
        title: `更新措施 ${from} → ${target.status}`,
        detail: action.changeReason || action.completionNote || action.description,
        at: new Date().toISOString(), by: currentUser?.displayName ?? '',
      })
      detail.risk.version++
      syncRiskTracking(detail)
      return structuredClone(detail)
    },
    async deleteRiskAction(riskCode, actionId, expectedVersion) {
      const detail = mustFind(mockRisks, item => item.risk.riskCode === riskCode, '风险不存在')
      if (detail.risk.status === 'CLOSED') throw new Error('已关闭的风险不可再维护控制措施')
      const index = detail.actions.findIndex(item => item.id === actionId && item.version === expectedVersion)
      if (index < 0) throw new Error('措施删除失败')
      const removed = detail.actions[index]
      detail.actions.splice(index, 1)
      detail.activities.unshift({
        type: 'ACTION', title: '删除控制措施', detail: removed.description,
        at: new Date().toISOString(), by: currentUser?.displayName ?? '',
      })
      detail.risk.version++
      syncRiskTracking(detail)
      return structuredClone(detail)
    },
    async getMonthlyReports(studyId, month) {
      await delay(150)
      return structuredClone(getMockMonthlyPage(studyId, month))
    },
    async createMonthlyEntry(reportId, input) {
      await delay(150)
      for (const page of mockMonthlyPages.values()) {
        const line = page.functionLines.find((item) => item.reportId === reportId)
        if (line) {
          if (!line.editable) throw new Error('无权在该功能线下填写月报')
          line.entries.push({
            entryId: nextMonthlyEntryId++,
            entryDate: input.entryDate ?? new Date().toISOString().slice(0, 10),
            content: input.content ?? '',
            updatedBy: currentUser?.username ?? 'demo@eastchinapharm.com',
            updatedAt: new Date().toISOString(),
            editable: true,
          })
          return structuredClone(page)
        }
      }
      throw new Error('月报应填项不存在: ' + reportId)
    },
    async updateMonthlyEntry(entryId, input) {
      await delay(150)
      for (const page of mockMonthlyPages.values()) {
        for (const line of page.functionLines) {
          const entry = line.entries.find((item) => item.entryId === entryId)
          if (entry) {
            if (input.entryDate !== undefined) entry.entryDate = input.entryDate
            if (input.content !== undefined) entry.content = input.content
            entry.updatedBy = currentUser?.username ?? entry.updatedBy
            entry.updatedAt = new Date().toISOString()
            return structuredClone(page)
          }
        }
      }
      throw new Error('月报条目不存在: ' + entryId)
    },
    async deleteMonthlyEntry(entryId) {
      await delay(120)
      for (const page of mockMonthlyPages.values()) {
        for (const line of page.functionLines) {
          const index = line.entries.findIndex((item) => item.entryId === entryId)
          if (index >= 0) {
            if (!line.editable) throw new Error('无权删除该功能线下的进展')
            line.entries.splice(index, 1)
            return structuredClone(page)
          }
        }
      }
      throw new Error('月报条目不存在: ' + entryId)
    },
    async getMonthlyReportHistory(studyId, functionLineId, month) {
      await delay(150)
      const page = getMockMonthlyPage(studyId, month)
      const line = page.functionLines.find((item) => item.functionLineId === functionLineId)
      if (!line) throw new Error('功能线不存在')
      // 推前 2 个月；minusMonths 逻辑正确处理跨年（如 2026-01 → 2025-12 / 2025-11）
      const [year, mon] = month.split('-').map(Number)
      const prev = (yy: number, mm: number): [number, number] =>
        mm === 1 ? [yy - 1, 12] : [yy, mm - 1]
      const [y1, m1] = prev(year, mon)
      const [y2, m2] = prev(y1, m1)
      const pad = (n: number) => String(n).padStart(2, '0')
      const month1 = `${y1}-${pad(m1)}`
      const month2 = `${y2}-${pad(m2)}`
      const author = line.entries[0]?.updatedBy ?? 'history@eastchinapharm.com'
      const demoEntries = (mo: string, salt: number): MonthlyReportEntry[] => [
        {
          entryId: 9000 + functionLineId * 10 + salt,
          entryDate: `${mo}-15`,
          content: `历史记录：${line.functionName} 在 ${mo} 的月度进展。`,
          updatedBy: author,
          updatedAt: `${mo}-15T10:00:00Z`,
          editable: false,
        },
        {
          entryId: 9100 + functionLineId * 10 + salt,
          entryDate: `${mo}-22`,
          content: `补充记录：${line.functionName} 在 ${mo} 的跟进事项。`,
          updatedBy: author,
          updatedAt: `${mo}-22T10:00:00Z`,
          editable: false,
        },
      ]
      const result: FunctionLineHistory = {
        functionLineId,
        functionCode: line.functionCode,
        functionName: line.functionName,
        months: [
          { month: month1, entries: demoEntries(month1, m1) },
          { month: month2, entries: demoEntries(month2, m2) },
        ],
      }
      return result
    },
    async previewMonthlyExport(query) {
      await delay(180)
      return buildMockMonthlyExport(query, therapeuticAreas, programs)
    },
    async downloadMonthlyExport(query, format) {
      await delay(180)
      const report = buildMockMonthlyExport(query, therapeuticAreas, programs)
      const blob = mockExportBlob(report, format)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = `研发管线月报_${query.startDate}_${query.endDate}.${format}`
      document.body.appendChild(anchor)
      anchor.click()
      anchor.remove()
      URL.revokeObjectURL(url)
    },
    async listTeamMatrix(query = {}) {
      const studyQuery = query.studyQuery?.trim().toLowerCase() ?? ''
      const roleQuery = query.roleQuery?.trim().toLowerCase() ?? ''
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      const filteredStudies = demoStudies.filter(study =>
        !studyQuery ||
        study.code.toLowerCase().includes(studyQuery) ||
        study.indication.toLowerCase().includes(studyQuery))
      const roles = teamRoles.filter(role =>
        !roleQuery ||
        role.roleCode.toLowerCase().includes(roleQuery) ||
        role.roleName.toLowerCase().includes(roleQuery) ||
        (role.functionName ?? '').toLowerCase().includes(roleQuery))
      const studies = filteredStudies.slice((page - 1) * pageSize, page * pageSize)
        .map(study => ({
          studyId: study.id,
          studyCode: study.code,
          indication: study.indication,
          statusCode: study.status,
          statusLabel: study.statusLabel,
          currentStatus: study.currentStatus ?? '',
          version: teamVersions.get(study.id) ?? 0,
        }))
      const assignments: TeamMatrixAssignment[] = []
      for (const study of studies) {
        for (const role of roles) {
          const ids = teamAssignments.get(`${study.studyId}|${role.roleCode}`) ?? []
          const members = ids.map(id => users[id - 1]).filter(Boolean).map((member, index) => ({
            userId: ids[index],
            email: member.username,
            displayName: member.displayName,
            enabled: true,
          }))
          if (members.length) assignments.push({
            studyId: study.studyId,
            roleCode: role.roleCode,
            members,
          })
        }
      }
      return {
        studies,
        roles,
        assignments,
        totalRoles: roles.length,
        pagination: {
          page,
          pageSize,
          totalItems: filteredStudies.length,
          totalPages: Math.max(1, Math.ceil(filteredStudies.length / pageSize)),
        },
      }
    },
    async getStudyTeam(studyId) {
      const study = demoStudies.find(s => s.id === studyId)
      if (!study) throw new Error('Study 不存在')
      const page = await this.listTeamMatrix({ studyQuery: study.code, page: 1, pageSize: 100 })
      const studies = page.studies.filter(s => s.studyId === studyId)
      return {
        ...page,
        studies,
        assignments: page.assignments.filter(a => a.studyId === studyId),
        pagination: {
          page: 1,
          pageSize: 1,
          totalItems: studies.length,
          totalPages: 1,
        },
      }
    },
    async replaceTeamAssignments(input) {
      for (const study of input.studies) {
        const currentVersion = teamVersions.get(study.studyId) ?? 0
        if (currentVersion !== study.expectedVersion) {
          throw new Error('团队矩阵已被其他用户修改，请刷新后重试')
        }
        for (const role of study.roles) {
          teamAssignments.set(`${study.studyId}|${role.roleCode}`, [...role.userIds])
        }
        teamVersions.set(study.studyId, currentVersion + 1)
      }
      return {
        studies: input.studies.map(study => ({
          studyId: study.studyId,
          version: teamVersions.get(study.studyId) ?? study.expectedVersion,
        })),
      }
    },
    async listPipelineConfig(query = {}) {
      const keyword = query.keyword?.trim().toLowerCase() ?? ''
      const all = demoStudies.map((study) => {
        const project = mustFind(projects, (item) => item.code === study.projectCode, 'Project 不存在')
        const program = mustFind(programs, (item) => item.id === project.programId, 'Program 不存在')
        return {
          studyId: study.id,
          studyCode: study.code,
          phaseStatusCode: study.phase.toUpperCase().replaceAll(' ', '_'),
          projectId: project.id,
          projectCode: project.code,
          indication: project.indication,
          therapeuticAreaCode: project.therapeuticAreaCode,
          therapeuticAreaName: project.therapeuticAreaName,
          programId: program.id,
          programCode: program.code,
          productName: program.productName,
          moa: program.moa,
          sourceCode: program.sourceCode,
          sourceLabel: program.sourceLabel,
          originCode: program.originCode,
          originLabel: program.originLabel,
          version: 1,
          updatedAt: study.updatedAt,
        }
      }).filter((row) => !keyword || [
        row.studyCode, row.therapeuticAreaCode, row.therapeuticAreaName, row.programCode,
        row.projectCode,
      ].some((value) => value.toLowerCase().includes(keyword)))
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      const sorted = sortByUpdatedAtDesc(all)
      const totalItems = sorted.length
      return {
        data: sorted.slice((page - 1) * pageSize, page * pageSize),
        page,
        pageSize,
        totalItems,
        totalPages: Math.max(1, Math.ceil(totalItems / pageSize)),
      }
    },
    async listTherapeuticAreas() {
      return therapeuticAreas
    },
    async listPrograms(keyword = '') {
      const query = keyword.trim().toLowerCase()
      return sortByUpdatedAtDesc(programs.filter((item) => !query || [item.code, item.productName]
        .some((value) => value.toLowerCase().includes(query))))
    },
    async createProgram(input) {
      if (programs.some((item) => item.code === input.code)) throw new Error('Program 编码已存在')
      const program: PipelineProgram = {
        id: nextProgramId++, code: input.code, productName: input.productName,
        moa: input.moa ?? null, sourceCode: input.sourceCode,
        sourceLabel: sourceLabel(input.sourceCode),
        originCode: input.originCode, originLabel: originLabel(input.originCode),
        projectCount: 0, studyCount: 0, version: 1, updatedAt: now(),
      }
      programs.push(program)
      return program
    },
    async updateProgram(id, input) {
      const program = programs.find((item) => item.id === id)
      if (!program) throw new Error('Program 不存在')
      if (program.version !== input.expectedVersion) throw new Error('Program 已被其他用户修改')
      const { expectedVersion, ...rest } = input
      Object.assign(program, rest, { updatedAt: now(), version: program.version + 1 })
      return program
    },
    async deleteProgram(id) {
      const index = programs.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Program 不存在')
      if (programs[index].projectCount) throw new Error('Program 仍有关联 Project，不能删除')
      programs.splice(index, 1)
    },
    async listProjects(programId, keyword = '') {
      const query = keyword.trim().toLowerCase()
      return sortByUpdatedAtDesc(projects.filter((item) => (!programId || item.programId === programId) &&
        (!query || item.code.toLowerCase().includes(query))))
    },
    async createProject(input) {
      if (projects.some((item) => item.code === input.code)) throw new Error('Project 编码已存在')
      const program = programs.find((item) => item.id === input.programId)
      if (!program) throw new Error('Program 不存在')
      const project: PipelineProject = {
        id: nextProjectId++, code: input.code, programId: input.programId,
        programCode: program.code, indication: input.indication, therapeuticAreaId: 99,
        therapeuticAreaCode: input.therapeuticAreaCode,
        therapeuticAreaName: therapeuticAreas.find((item) => item.code === input.therapeuticAreaCode)?.name ?? input.therapeuticAreaCode,
        studyCount: 0, version: 1, updatedAt: now(),
      }
      projects.push(project)
      program.projectCount++
      return project
    },
    async updateProject(id, input) {
      const project = projects.find((item) => item.id === id)
      if (!project) throw new Error('Project 不存在')
      if (project.version !== input.expectedVersion) throw new Error('Project 已被其他用户修改')
      const { expectedVersion, ...rest } = input
      Object.assign(project, rest, { updatedAt: now(), version: project.version + 1 })
      return project
    },
    async deleteProject(id) {
      const index = projects.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Project 不存在')
      if (projects[index].studyCount) throw new Error('Project 仍有关联 Study，不能删除')
      projects.splice(index, 1)
    },
    async createStudyConfig(input) {
      const project = projects.find((item) => item.id === input.projectId)
      if (!project) throw new Error('Project 不存在')
      demoStudies.push({ id: nextStudyId++, code: input.code,
        indication: project.indication, phase: input.phase, status: 'ACTIVE', statusLabel: '进行中',
        statusTone: 'positive', ownerName: '', startDate: null, updatedAt: now(),
        programCode: project.programCode, projectCode: project.code,
        therapeuticAreaCode: project.therapeuticAreaCode, therapeuticAreaName: project.therapeuticAreaName,
        productName: programs.find((item) => item.id === project.programId)?.productName,
        moa: programs.find((item) => item.id === project.programId)?.moa ?? undefined,
        sourceCode: programs.find((item) => item.id === project.programId)?.sourceCode,
        originCode: programs.find((item) => item.id === project.programId)?.originCode })
      project.studyCount++
    },
    async updateStudyConfig(id, input) {
      const study = demoStudies.find((item) => item.id === id)
      const project = projects.find((item) => item.id === input.projectId)
      if (!study || !project) throw new Error('Study 或 Project 不存在')
      const page = await this.listPipelineConfig({ page: 1, pageSize: 500 })
      const row = page.data.find((item) => item.studyId === id)
      if (!row) throw new Error('Study 配置不存在')
      if (row.version !== input.expectedVersion) throw new Error('Study 已被其他用户修改')
      study.phase = input.phaseStatusCode
      study.projectCode = project.code
      study.programCode = project.programCode
      study.therapeuticAreaCode = project.therapeuticAreaCode
      study.therapeuticAreaName = project.therapeuticAreaName
      study.indication = project.indication
      row.version++
      return row
    },
    async getStudyDeletePreview(id) {
      const study = demoStudies.find((item) => item.id === id)
      if (!study) throw new Error('Study 不存在')
      const milestoneCount = 0
      const riskCount = mockRisks.filter(({ risk }) => risk.studyId === id).length
      const teamCount = [...teamAssignments.keys()].filter((key) => key.startsWith(`${id}|`)).length
      const monthlyReportCount = 0
      return {
        studyId: id,
        studyCode: study.code,
        milestoneCount,
        riskCount,
        teamCount,
        monthlyReportCount,
      }
    },
    async deleteStudyConfig(id) {
      const index = demoStudies.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Study 不存在')
      demoStudies.splice(index, 1)
    },
    async listUsers(query = {}) {
      const keyword = query.keyword ?? ''
      const roleCode = query.roleCode ?? ''
      let filtered = users.map((user, index) => ({
        id: index + 1,
        username: user.username,
        displayName: user.displayName,
        roles: user.roles,
        roleDescriptions: user.roles.map(r => {
          switch (r) {
            case 'ADMIN': return '系统管理员'
            case 'USER': return '业务用户'
            case 'VIEWER': return '只读用户'
            default: return r
          }
        }),
        dataScope: user.dataScope,
        visibleStudyCount: user.roles.includes('ADMIN') ? 10 : (user.roles.includes('USER') ? 8 : 5),
        enabled: true,
      }))
      if (keyword) {
        const lower = keyword.toLowerCase()
        filtered = filtered.filter(u =>
          u.displayName.toLowerCase().includes(lower) ||
          u.username.toLowerCase().includes(lower))
      }
      if (roleCode) {
        filtered = filtered.filter(u => u.roles.includes(roleCode))
      }
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      const totalItems = filtered.length
      return {
        data: filtered.slice((page - 1) * pageSize, page * pageSize),
        page,
        pageSize,
        totalItems,
        totalPages: Math.max(1, Math.ceil(totalItems / pageSize)),
      }
    },
    async createUser(input: CreateUserInput) {
      if (users.some(u => u.username === input.username)) {
        throw new Error('账号已存在')
      }
      users.push({
        username: input.username,
        displayName: input.displayName,
        title: '',
        roles: input.roleCodes,
        permissions: [],
        dataScope: 'ALL',
        password: 'Hd123456',
      })
    },
    async changePassword(input: ChangePasswordInput) {
      const account = users.find(item => item.username === currentUser?.username)
      if (!account) throw new Error('账号或密码错误')
      if (account.password !== input.currentPassword) {
        throw new Error('当前密码错误')
      }
      if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,128}$/.test(input.newPassword)) {
        throw new Error('新密码至少 8 位，且必须包含大小写字母和数字')
      }
      if (input.currentPassword === input.newPassword) {
        throw new Error('新密码不能与当前密码相同')
      }
      account.password = input.newPassword
    },
    async resetPassword(id: number) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('账号不存在')
      users[index].password = 'Hd123456'
    },
    async updateUser(id: number, input: UpdateUserInput) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('账号不存在')
      users[index].displayName = input.displayName
    },
    async deleteUser(id: number) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('账号不存在')
      users.splice(index, 1)
    },
    async assignRoles(_id: number, _input: AssignRolesInput) {
      // mock: no-op, just return
    },
    async listRoles(filters = {}) {
      const keyword = filters.keyword?.trim().toLowerCase() ?? ''
      const filtered = roles.filter((role) =>
        (!keyword || role.roleCode.toLowerCase().includes(keyword) ||
          role.roleDescription?.toLowerCase().includes(keyword)) &&
        (!filters.status || role.status === filters.status),
      )
      const sorted = sortByUpdatedAtDesc(filtered)
      const page = filters.page ?? 1
      const pageSize = filters.pageSize ?? 10
      const start = (page - 1) * pageSize
      return {
        data: sorted.slice(start, start + pageSize),
        page,
        pageSize,
        totalItems: filtered.length,
        totalPages: Math.ceil(filtered.length / pageSize),
      }
    },
    async listPermissions() {
      return permissions
    },
    async createRole(input) {
      if (roles.some((role) => role.roleCode === input.roleCode)) {
        throw new Error('角色编码已存在')
      }
      const role: PlatformRole = {
        id: nextRoleId++,
        roleCode: input.roleCode ?? '',
        roleDescription: input.roleDescription,
        dataScopeMode: input.dataScopeMode,
        status: 'ACTIVE',
        systemRole: false,
        assignedUserCount: 0,
        permissionCodes: [...input.permissionCodes],
        updatedAt: new Date().toISOString(),
      }
      roles.push(role)
      return role
    },
    async updateRole(roleId, input) {
      const role = roles.find((item) => item.id === roleId)
      if (!role) throw new Error('角色不存在')
      role.roleDescription = input.roleDescription
      role.dataScopeMode = input.dataScopeMode
      role.status = input.status ?? role.status
      role.permissionCodes = [...input.permissionCodes]
      role.updatedAt = new Date().toISOString()
      return { role, invalidatedUserCount: role.assignedUserCount, currentSessionInvalidated: false }
    },
    async deleteRole(roleId) {
      const index = roles.findIndex((role) => role.id === roleId)
      if (index < 0) throw new Error('角色不存在')
      if (roles[index].systemRole) throw new Error('系统角色不可删除')
      if (roles[index].assignedUserCount) throw new Error('角色仍关联用户，不能删除')
      roles.splice(index, 1)
    },
    async getMilestones(studyId) {
      await delay(200)
      let data = mockMilestones.get(studyId)
      if (!data) {
        const study = demoStudies.find((row) => row.id === studyId)
        if (!study) throw new Error('Study 不存在或暂无里程碑数据')
        data = buildStudyMilestones(studyId, study.code)
        mockMilestones.set(studyId, data)
      }
      const study = demoStudies.find((row) => row.id === studyId)
      const merged = mergeProjectMilestonesIntoStudy(structuredClone(data), study)
      return merged
    },
    async updateMilestone(studyId, milestoneCode, input) {
      await delay(200)
      const page = mockMilestones.get(studyId)
      if (!page) throw new Error('Study 不存在')
      for (const group of page.groups) {
        const node = group.nodes.find(n => n.milestoneCode === milestoneCode)
        if (node) {
          if (input.planV1Date !== undefined) node.planV1Date = input.planV1Date
          if (input.planV2Date !== undefined) node.planV2Date = input.planV2Date
          if (input.actualStartDate !== undefined) node.actualStartDate = input.actualStartDate
          if (input.actualEndDate !== undefined) node.actualEndDate = input.actualEndDate
          if (input.deviationNote !== undefined) node.deviationNote = input.deviationNote
          // Re-derive status
          if (node.actualEndDate) node.status = 'COMPLETED'
          else if (node.actualStartDate) node.status = 'IN_PROGRESS'
          else node.status = 'NOT_STARTED'
          return structuredClone(page)
        }
      }
      throw new Error('里程碑节点不存在: ' + milestoneCode)
    },
    async getStageProjection(studyId) {
      await delay(100)
      const page = mockMilestones.get(studyId)
      if (!page) throw new Error('Study 不存在')
      return projectionFromMilestonePage(page)
    },
    async getProjectMilestones(studyId) {
      await delay(200)
      const study = demoStudies.find((row) => row.id === studyId)
      if (!study || !study.projectCode) throw new Error('Study 不存在或无所属 Project')
      let data = mockProjectMilestones.get(study.projectCode)
      if (!data) {
        data = buildProjectMilestones(study.projectCode)
        mockProjectMilestones.set(study.projectCode, data)
      }
      return structuredClone(data)
    },
    async updateProjectMilestone(studyId, milestoneCode, input) {
      await delay(200)
      const study = demoStudies.find((row) => row.id === studyId)
      if (!study || !study.projectCode) throw new Error('Study 不存在')
      const page = mockProjectMilestones.get(study.projectCode)
      if (!page) throw new Error('Project milestone 未初始化')
      const node = findProjectMilestoneNode(page, milestoneCode)
      if (!node) throw new Error('里程碑节点不存在: ' + milestoneCode)
      if (input.planV1Date !== undefined) node.planV1Date = input.planV1Date
      if (input.planV2Date !== undefined) node.planV2Date = input.planV2Date
      if (input.actualStartDate !== undefined) node.actualStartDate = input.actualStartDate
      if (input.actualEndDate !== undefined) node.actualEndDate = input.actualEndDate
      if (input.deviationNote !== undefined) node.deviationNote = input.deviationNote
      if (node.actualEndDate) node.status = 'COMPLETED'
      else if (node.actualStartDate) node.status = 'IN_PROGRESS'
      else node.status = 'NOT_STARTED'
      return structuredClone(page)
    },
    async getProjectStageProjection(studyId) {
      await delay(100)
      const page = await this.getProjectMilestones(studyId)
      return projectionFromMilestonePage(page)
    },
  }
}

function buildMockMonthlyExport(
  query: MonthlyExportQuery,
  therapeuticAreas: TherapeuticArea[],
  programs: PipelineProgram[],
): MonthlyExportReport {
  if (!query.startDate || !query.endDate) {
    throw new Error('请选择导出开始和结束日期')
  }
  if (query.endDate < query.startDate) {
    throw new Error('结束日期不能早于开始日期')
  }
  let scoped = [...demoStudies]
  let scopeLabels = ['全部项目']
  if (query.scopeType === 'TA') {
    if (!query.taIds?.length) throw new Error('请至少选择一个治疗领域')
    const selected = therapeuticAreas.filter((area) => query.taIds!.includes(area.id))
    const codes = new Set(selected.map((area) => area.code))
    scoped = demoStudies.filter((study) => codes.has(study.therapeuticAreaCode ?? ''))
    scopeLabels = selected.map((area) => area.name || area.code)
  } else if (query.scopeType === 'PROGRAM') {
    if (!query.programIds?.length) throw new Error('请至少选择一个 Program')
    const selected = programs.filter((program) => query.programIds!.includes(program.id))
    const codes = new Set(selected.map((program) => program.code))
    scoped = demoStudies.filter((study) => codes.has(study.programCode ?? ''))
    scopeLabels = selected.map((program) => program.code)
  }

  const progress = scoped.flatMap((study) => {
    const page = mockMonthlyPages.get(`${study.id}|${query.startDate.slice(0, 7)}`)
      ?? getMockMonthlyPage(study.id, query.startDate.slice(0, 7))
    return page.functionLines.flatMap((line) =>
      line.entries
        .filter((entry) => entry.entryDate >= query.startDate && entry.entryDate <= query.endDate
          && entry.content.trim())
        .map((entry) => ({
          studyCode: study.code,
          programCode: study.programCode ?? '',
          taName: study.therapeuticAreaName ?? '',
          entryDate: entry.entryDate,
          functionCode: line.functionCode,
          functionName: line.functionName,
          content: entry.content,
        })))
  })

  const openRisks = mockRisks
    .filter((item) => item.risk.status === 'OPEN'
      && scoped.some((study) => study.id === item.risk.studyId))
    .map((item) => ({
      riskCode: item.risk.riskCode,
      programCode: item.risk.programCode,
      description: item.risk.description,
      score: item.risk.score,
      level: item.risk.level,
      ownerName: item.risk.ownerName,
    }))

  const groupMap = new Map<string, MonthlyExportReport['snapshotGroups'][number]>()
  for (const study of scoped) {
    const key = `${study.therapeuticAreaCode ?? ''}\0${study.therapeuticAreaName ?? ''}`
    let group = groupMap.get(key)
    if (!group) {
      group = {
        taCode: study.therapeuticAreaCode ?? '',
        taName: study.therapeuticAreaName ?? '',
        rows: [],
      }
      groupMap.set(key, group)
    }
    group.rows.push({
      programCode: study.programCode ?? '',
      productName: study.productName ?? '',
      studyCode: study.code,
      indication: study.indication,
      phase: study.phase,
      projectStatus: deriveMockStatus(study),
    })
  }

  return {
    meta: {
      startDate: query.startDate,
      endDate: query.endDate,
      scopeType: query.scopeType,
      scopeLabels,
      generatedAt: new Date().toISOString(),
    },
    summary: {
      total: scoped.length,
      notStarted: scoped.filter((s) => deriveMockStatus(s) === EXPORT_STATUS.NOT_STARTED).length,
      inProgress: scoped.filter((s) => deriveMockStatus(s) === EXPORT_STATUS.IN_PROGRESS).length,
      completed: scoped.filter((s) => deriveMockStatus(s) === EXPORT_STATUS.COMPLETED).length,
      reportedStudyCount: new Set(progress.map((item) => item.studyCode)).size,
      openRiskCount: openRisks.length,
    },
    snapshotGroups: [...groupMap.values()],
    progress,
    openRisks,
  }
}

/**
 * Mock export status: same rules as MonthlyExportManager.deriveMilestoneStatus.
 * Prefer stored milestone tree; otherwise approximate from overview frontier.
 */
function deriveMockStatus(study: Study): string {
  const stored = mockMilestones.get(study.id)
  if (stored) {
    return deriveMilestoneExportStatus(flattenMilestoneNodes(stored.groups))
  }
  const mv = mockOverviewMilestoneView[study.code]
  if (!mv) return EXPORT_STATUS.NOT_STARTED
  const { globallyCompleted } = deriveOverviewCompletionFromStage(
    mv.mainStageLabel,
    mv.currentPhaseCompleted,
  )
  if (globallyCompleted) return EXPORT_STATUS.COMPLETED
  // 计划中的 Study 且未完成当前 frontier 时按未开始处理
  if (study.status === 'PLANNED' && !mv.currentPhaseCompleted) {
    return EXPORT_STATUS.NOT_STARTED
  }
  return EXPORT_STATUS.IN_PROGRESS
}

function mockExportBlob(report: MonthlyExportReport, format: MonthlyExportFormat): Blob {
  if (format === 'xlsx') {
    return new Blob([JSON.stringify(report)], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
  }
  const html = `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8"><title>\u7814\u53D1\u7BA1\u7EBF\u6708\u62A5</title></head>
<body><h1>\u7814\u53D1\u7BA1\u7EBF\u6708\u62A5</h1>
<p>${report.meta.startDate} \u81F3 ${report.meta.endDate}\uFF0C\u8303\u56F4\uFF1A${report.meta.scopeLabels.join('\u3001')}</p>
<p>Study ${report.summary.total} \u4E2A\uFF0C\u8FDB\u5C55 ${report.progress.length} \u6761\uFF0COpen \u98CE\u9669 ${report.summary.openRiskCount}</p>
</body></html>`
  return new Blob([html], { type: 'text/html;charset=utf-8' })
}
