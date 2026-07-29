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
  deriveMilestoneExportStatus,
  deriveOverviewCompletionFlags,
  flattenMilestoneNodes,
} from '../domain/milestone-status'
import { deriveRiskLevel } from '../domain/risk-labels'

const users: Array<CurrentUser & { password: string }> = [
  {
    username: 'chen@eastchinapharm.com',
    displayName: '???',
    title: '?????',
    roles: ['ADMIN'],
    permissions: [
      'pipeline.page.view',
      'study.read',
      'milestone.read',
      'milestone.update',
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
    displayName: '??',
    title: '????? ? PL',
    roles: ['USER'],
    permissions: ['pipeline.page.view', 'study.read', 'milestone.read', 'milestone.update', 'monthly.read', 'monthly.create', 'monthly.update', 'report.page.view', 'report.export', 'risk.page.view', 'risk.read', 'risk.create', 'risk.update'],
    dataScope: 'ALL',
    password: '1234',
  },
  {
    username: 'liuyang@eastchinapharm.com',
    displayName: '??',
    title: '?????',
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

// ?? 6 ???????? project ?????? phase ? study????? byProject ?????
const studySeeds: StudySeed[] = [
  // ??
  ...expand({ indication: '?????', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '??', programCode: 'HDM2020', projectCode: 'HDM2020-1', productName: 'HDM2020', moa: 'ADC', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2020-001', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '??', startDate: '2025-03-10', updatedAt: '2026-07-15T09:20:00' },
    { code: 'HDM2020-002', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '??', startDate: '2025-09-01', updatedAt: '2026-07-10T14:05:00' },
  ]),
  ...expand({ indication: '??????', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '??', programCode: 'HDM2020', projectCode: 'HDM2020-2', productName: 'HDM2020', moa: 'ADC', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2020-101', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '??', startDate: '2025-06-20', updatedAt: '2026-07-08T10:30:00' },
  ]),
  ...expand({ indication: '???', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '??', programCode: 'HDM2020', projectCode: 'HDM2020-3', productName: 'HDM2020', moa: 'ADC', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2020-201', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '??', startDate: '2025-11-05', updatedAt: '2026-07-01T16:40:00' },
    { code: 'HDM2020-202', phase: 'PHASE_2', status: 'PLANNED', ownerName: '??', startDate: '2026-08-01', updatedAt: '2026-06-28T11:00:00' },
  ]),
  ...expand({ indication: '??', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '??', programCode: 'HDM2031', projectCode: 'HDM2031-1', productName: 'HDM2031', moa: '?????', sourceCode: 'IN_LICENSE', originCode: 'IMPORTED' }, [
    { code: 'HDM2031-001', phase: 'PHASE_3_1', status: 'ACTIVE', ownerName: '???', startDate: '2024-05-12', updatedAt: '2026-07-12T13:10:00' },
    { code: 'HDM2031-002', phase: 'PHASE_3_2', status: 'PLANNED', ownerName: '???', startDate: '2026-09-01', updatedAt: '2026-07-05T09:45:00' },
  ]),
  // ????
  ...expand({ indication: '???????', therapeuticAreaCode: 'AUTOIMMUNE', therapeuticAreaName: '????', programCode: 'HDM2015', projectCode: 'HDM2015-1', productName: 'HDM2015', moa: 'Small Molecule', sourceCode: 'COOPERATION', originCode: 'DOMESTIC' }, [
    { code: 'HDM2015-101', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '??', startDate: '2025-02-18', updatedAt: '2026-07-14T16:40:00' },
    { code: 'HDM2015-102', phase: 'PHASE_1', status: 'COMPLETED', ownerName: '??', startDate: '2023-08-01', updatedAt: '2025-12-20T10:00:00' },
  ]),
  ...expand({ indication: '??????', therapeuticAreaCode: 'AUTOIMMUNE', therapeuticAreaName: '????', programCode: 'HDM2015', projectCode: 'HDM2015-2', productName: 'HDM2015', moa: 'Small Molecule', sourceCode: 'COOPERATION', originCode: 'DOMESTIC' }, [
    { code: 'HDM2015-201', phase: 'IND', status: 'ACTIVE', ownerName: '??', startDate: '2025-10-10', updatedAt: '2026-07-09T15:20:00' },
  ]),
  // ??????
  ...expand({ indication: '2 ????', therapeuticAreaCode: 'METABOLIC_CARDIOVASCULAR', therapeuticAreaName: '??????', programCode: 'HDM1005', projectCode: 'HDM1005-3', productName: 'HDM1005', moa: 'Peptide', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM1005-301', phase: 'PHASE_1', status: 'COMPLETED', ownerName: '??', startDate: '2023-05-06', updatedAt: '2024-11-30T09:00:00' },
    { code: 'HDM1005-302', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '??', startDate: '2025-01-15', updatedAt: '2026-07-12T13:10:00' },
    { code: 'HDM1005-303', phase: 'PHASE_3_1', status: 'PLANNED', ownerName: '??', startDate: '2026-10-01', updatedAt: '2026-07-06T10:20:00' },
  ]),
  ...expand({ indication: '??', therapeuticAreaCode: 'METABOLIC_CARDIOVASCULAR', therapeuticAreaName: '??????', programCode: 'HDM1005', projectCode: 'HDM1005-5', productName: 'HDM1005', moa: 'Peptide', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM1005-501', phase: 'PRE_IND', status: 'ACTIVE', ownerName: '??', startDate: '2026-01-20', updatedAt: '2026-07-03T14:00:00' },
  ]),
  // ????
  ...expand({ indication: '??', therapeuticAreaCode: 'RESPIRATORY', therapeuticAreaName: '????', programCode: 'HDM2042', projectCode: 'HDM2042-1', productName: 'HDM2042', moa: '???', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2042-001', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '??', startDate: '2025-04-22', updatedAt: '2026-07-11T09:30:00' },
  ]),
  ...expand({ indication: '???', therapeuticAreaCode: 'RESPIRATORY', therapeuticAreaName: '????', programCode: 'HDM2042', projectCode: 'HDM2042-2', productName: 'HDM2042', moa: '???', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2042-201', phase: 'PHASE_1', status: 'PLANNED', ownerName: '??', startDate: '2026-07-01', updatedAt: '2026-06-30T17:00:00' },
  ]),
  ...expand({ indication: '???????', therapeuticAreaCode: 'RESPIRATORY', therapeuticAreaName: '????', programCode: 'HDM2042', projectCode: 'HDM2042-3', productName: 'HDM2042', moa: '???', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2042-301', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '??', startDate: '2025-12-08', updatedAt: '2026-07-07T11:15:00' },
  ]),
  // ?????
  ...expand({ indication: '????', therapeuticAreaCode: 'INFECTIOUS_DISEASE', therapeuticAreaName: '?????', programCode: 'HDM2050', projectCode: 'HDM2050-1', productName: 'HDM2050', moa: '???', sourceCode: 'COOPERATION', originCode: 'IMPORTED' }, [
    { code: 'HDM2050-001', phase: 'PHASE_3_1', status: 'ACTIVE', ownerName: '???', startDate: '2024-03-15', updatedAt: '2026-07-13T10:50:00' },
    { code: 'HDM2050-002', phase: 'PHASE_3_2', status: 'ACTIVE', ownerName: '???', startDate: '2024-11-20', updatedAt: '2026-07-04T15:35:00' },
  ]),
  // ????
  ...expand({ indication: '??????', therapeuticAreaCode: 'NEUROSCIENCE', therapeuticAreaName: '????', programCode: 'HDM2066', projectCode: 'HDM2066-1', productName: 'HDM2066', moa: '???', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2066-001', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '??', startDate: '2025-07-30', updatedAt: '2026-07-10T09:10:00' },
    { code: 'HDM2066-002', phase: 'PRE_IND', status: 'COMPLETED', ownerName: '??', startDate: '2024-02-14', updatedAt: '2025-06-30T14:20:00' },
  ]),
  ...expand({ indication: '????', therapeuticAreaCode: 'NEUROSCIENCE', therapeuticAreaName: '????', programCode: 'HDM2066', projectCode: 'HDM2066-2', productName: 'HDM2066', moa: '???', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2066-201', phase: 'IND', status: 'PLANNED', ownerName: '??', startDate: '2026-08-15', updatedAt: '2026-07-02T10:40:00' },
  ]),
  ...expand({ indication: '???', therapeuticAreaCode: 'NEUROSCIENCE', therapeuticAreaName: '????', programCode: 'HDM2066', projectCode: 'HDM2066-3', productName: 'HDM2066', moa: '???', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2066-301', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '??', startDate: '2025-05-25', updatedAt: '2026-07-09T13:25:00' },
  ]),
]

export const demoStudies: Study[] = studySeeds.map((seed, index) => ({
  ...seed,
  id: index + 1,
  statusLabel: STUDY_STATUS_META[seed.status].label,
  statusTone: STUDY_STATUS_META[seed.status].tone,
}))

// ?? study ??????????????????????????/???/?????????
// ??? = ????????? "LPI"??????? = ????stage ??? "Enrollment"??
const mockOverviewMilestoneView: Record<string, {
  mainStageLabel: string
  subStatusLabel: string
  currentPhaseCompleted: boolean
}> = {
  'HDM2020-001': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPI', currentPhaseCompleted: false },
  'HDM2020-002': { mainStageLabel: 'IA', subStatusLabel: 'IA ????', currentPhaseCompleted: false },
  'HDM2020-101': { mainStageLabel: 'SSU', subStatusLabel: '??????', currentPhaseCompleted: false },
  'HDM2020-201': { mainStageLabel: 'Enrollment', subStatusLabel: 'FPI', currentPhaseCompleted: false },
  'HDM2020-202': { mainStageLabel: 'Protocol', subStatusLabel: '????', currentPhaseCompleted: false },
  'HDM2031-001': { mainStageLabel: 'Data & Report', subStatusLabel: 'DBL', currentPhaseCompleted: false },
  'HDM2031-002': { mainStageLabel: 'NDA/BLA', subStatusLabel: 'NDA/BLA ??', currentPhaseCompleted: false },
  'HDM2015-101': { mainStageLabel: 'Data & Report', subStatusLabel: 'CSR??', currentPhaseCompleted: false },
  'HDM2015-102': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPO', currentPhaseCompleted: true },
  'HDM2015-201': { mainStageLabel: 'IND', subStatusLabel: 'IND ??', currentPhaseCompleted: false },
  'HDM1005-301': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPO', currentPhaseCompleted: true },
  'HDM1005-302': { mainStageLabel: 'Data & Report', subStatusLabel: 'TLR??', currentPhaseCompleted: false },
  'HDM1005-303': { mainStageLabel: 'NDA/BLA', subStatusLabel: 'NDA/BLA ??', currentPhaseCompleted: false },
  'HDM1005-501': { mainStageLabel: 'PreIND', subStatusLabel: 'PreIND ??-??', currentPhaseCompleted: false },
  'HDM2042-001': { mainStageLabel: 'IA', subStatusLabel: 'IA ????', currentPhaseCompleted: false },
  'HDM2042-201': { mainStageLabel: 'Protocol', subStatusLabel: '?????', currentPhaseCompleted: false },
  'HDM2042-301': { mainStageLabel: 'SSU', subStatusLabel: '??????', currentPhaseCompleted: false },
  'HDM2050-001': { mainStageLabel: 'Data & Report', subStatusLabel: 'CSR??', currentPhaseCompleted: false },
  'HDM2050-002': { mainStageLabel: 'NDA/BLA', subStatusLabel: '????', currentPhaseCompleted: false },
  'HDM2066-001': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPI', currentPhaseCompleted: false },
  'HDM2066-002': { mainStageLabel: 'PreIND', subStatusLabel: 'PreIND ??-??', currentPhaseCompleted: true },
  'HDM2066-201': { mainStageLabel: 'IND', subStatusLabel: 'IND ??', currentPhaseCompleted: false },
  'HDM2066-301': { mainStageLabel: 'SSU', subStatusLabel: '??????', currentPhaseCompleted: false },
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

let nextRiskId = 19
let nextRiskActionId = 2

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

const mockRisks: RiskDetail[] = [{
  risk: {
    riskId: 18, riskCode: 'RSK-2026-000018', studyId: 3, studyCode: 'HDM1005-302',
    programCode: 'HDM1005', projectCode: 'HDM1005-3', functionCode: 'RA',
    functionName: '注册', description: '监管沟通窗口可能影响计划节点',
    ownerUserId: 2, ownerName: '张伟', score: 48, level: 'HIGH', status: 'OPEN',
    actionCount: 1, openActionCount: 1, overdueActionCount: 0, nextPlannedDate: '2026-08-15',
    version: 0, updatedAt: '2026-07-22T09:00:00Z',
  },
  registeredDate: '2026-07-15', closeReason: '', closedTime: null,
  assessments: [{ id: 1, number: 1, impact: 4, likelihood: 4, detectability: 3,
    score: 48, level: 'HIGH', reason: '首次评估', assessedBy: '张伟',
    assessedAt: '2026-07-15T09:00:00Z' }],
  actions: [{ id: 1, description: '提前准备沟通材料', ownerUserId: 2,
    ownerName: '张伟', plannedDate: '2026-08-15', completedDate: null,
    status: 'IN_PROGRESS', completionNote: '', version: 0, overdue: false }],
  activities: [{
    type: 'ASSESSMENT', title: '第 1 次评估 · 48 分 · HIGH',
    detail: '4 × 4 × 3 · 首次评估', at: '2026-07-15T09:00:00Z', by: '张伟',
  }, {
    type: 'ACTION', title: '新增控制措施',
    detail: '提前准备沟通材料', at: '2026-07-15T09:05:00Z', by: '张伟',
  }],
}]
syncRiskTracking(mockRisks[0])


const teamRoles: TeamMatrixRole[] = [
  ['PL', 'PL ?????', 'PM', '????'],
  ['APL', 'APL ??????', 'PM', '????'],
  ['PM', 'PM ????', 'PM', '????'],
  ['APM', 'APM ?????', 'PM', '????'],
  ['RA_SPONSOR', 'RA Sponsor', 'RA', '??'],
  ['RA_MANAGER', 'RA Manager', 'RA', '??'],
  ['RA_SPECIALIST', 'RA Specialist', 'RA', '??'],
  ['RA_CMC', 'RA CMC', 'RA', '??'],
  ['CM_SPONSOR', 'CM Sponsor', 'CM', '????'],
  ['CM', 'CM', 'CM', '????'],
  ['CP_SPONSOR', 'CP Sponsor', 'CP', '????'],
  ['CP', 'CP', 'CP', '????'],
  ['PV_SPONSOR', 'PV Sponsor', 'PV', '????'],
  ['PVP', 'PVP', 'PV', '????'],
  ['PVO', 'PVO', 'PV', '????'],
  ['TM_SPONSOR', 'TM Sponsor', 'TM', '????'],
  ['TM', 'TM', 'TM', '????'],
  ['CO_SPONSOR', 'CO Sponsor', 'CO', '????'],
  ['CTM', 'CTM', 'CO', '????'],
  ['ACTM', 'ACTM', 'CO', '????'],
  ['LAB', 'Lab', 'LAB', '?????'],
  ['LAB_BACKUP', 'Lab backup', 'LAB', '?????'],
  ['SUPPLY', 'Supply', 'SUPPLY', '????'],
  ['SUPPLY_BACKUP', 'Supply backup', 'SUPPLY', '????'],
  ['CTA_PROCESS', 'CTA process', 'CTA', '??????'],
  ['CTA_TMF', 'CTA TMF', 'CTA', '??????'],
  ['ST_SPONSOR', 'ST Sponsor', 'ST', '????'],
  ['ST', 'ST', 'ST', '????'],
  ['PG_SPONSOR', 'PG Sponsor', 'PG', '????'],
  ['PG', 'PG', 'PG', '????'],
  ['DM_SPONSOR', 'DM Sponsor', 'DM', '????'],
  ['DM', 'DM', 'DM', '????'],
  ['MW', 'MW', 'MW', '????'],
  ['NC_CONTACT', 'NC-contact', 'NC', '???'],
  ['NC_PK', 'NC-PK', 'NC', '???'],
  ['NC_PD', 'NC-PD', 'NC', '???'],
  ['NC_TOX', 'NC-TOX', 'NC', '???'],
  ['CMC_PL', 'CMC-PL', 'CMC', '??CMC'],
  ['CMC_PM', 'CMC-PM', 'CMC', '??CMC'],
  ['CMC_DS', 'CMC-DS', 'CMC', '??CMC'],
  ['CMC_DP', 'CMC-DP', 'CMC', '??CMC'],
  ['CMC_OA', 'CMC-OA', 'CMC', '??CMC'],
  ['CMC_RA', 'CMC-RA', 'CMC', '??CMC'],
  ['IP', 'IP', 'IP', '????'],
].map(([roleCode, roleName, functionCode, functionName]) => ({
  roleCode, roleName, functionCode, functionName,
}))

// ?? Mock ????? ??

function delay(ms: number) { return new Promise(r => setTimeout(r, ms)) }

const mockMilestones = new Map<number, MilestonePage>()

function buildDemoMilestones(studyId: number, studyCode: string): MilestonePage {
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
        { milestoneCode: 'PreIND-0', milestoneName: 'PreIND ??', planV1Date: d(-180), planV2Date: d(-175), actualStartDate: d(-178), actualEndDate: d(-176), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-1', milestoneName: 'PreIND ??-????', planV1Date: d(-150), planV2Date: d(-145), actualStartDate: d(-148), actualEndDate: d(-140), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-2', milestoneName: 'PreIND ??-??', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-142), actualEndDate: d(-138), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-3', milestoneName: 'PreIND ??-????', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-140), actualEndDate: d(-135), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-4', milestoneName: 'PreIND ??-???', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-138), actualEndDate: d(-130), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-5', milestoneName: 'PreIND ??-??', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-135), actualEndDate: d(-125), status: 'COMPLETED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'IND', stageName: 'IND', nodes: [
        { milestoneCode: 'IND-0', milestoneName: 'IND ??', planV1Date: d(-100), planV2Date: d(-95), actualStartDate: d(-98), actualEndDate: d(-96), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-1', milestoneName: 'IND ????', planV1Date: d(-80), planV2Date: d(-78), actualStartDate: d(-82), actualEndDate: d(-75), status: 'COMPLETED', deviationNote: 'CDE?????????' },
        { milestoneCode: 'IND-2', milestoneName: 'IND ????', planV1Date: d(-60), planV2Date: d(-58), actualStartDate: d(-62), actualEndDate: d(-55), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-3', milestoneName: 'IND ??', planV1Date: d(-50), planV2Date: d(-48), actualStartDate: d(-52), actualEndDate: d(-46), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-4', milestoneName: 'IND ??', planV1Date: d(-30), planV2Date: d(-28), actualStartDate: d(-30), actualEndDate: null, status: 'IN_PROGRESS', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'Pre3', stageName: 'Pre3', nodes: [
        { milestoneCode: 'Pre3-0', milestoneName: 'Pre3 ??', planV1Date: d(60), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-1', milestoneName: 'Pre3 ??-????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-2', milestoneName: 'Pre3 ??-??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-3', milestoneName: 'Pre3 ??-????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-4', milestoneName: 'Pre3 ??-???', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-5', milestoneName: 'Pre3 ??-??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'Protocol', stageName: 'Protocol', nodes: [
        { milestoneCode: 'Protocol-0', milestoneName: '??????', planV1Date: d(30), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Protocol-1', milestoneName: '?????', planV1Date: d(90), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Protocol-2', milestoneName: '????', planV1Date: d(150), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'SSU', stageName: 'SSU', nodes: [
        notStarted('SSU-0', '????????'),
        notStarted('SSU-1', '????????'),
        notStarted('SSU-2', '????????'),
        notStarted('SSU-3', '????????'),
        notStarted('SSU-4', '????????'),
        notStarted('SSU-5', '??????'),
        notStarted('SSU-6', '??????'),
        notStarted('SSU-7', '??????'),
        notStarted('SSU-8', '????'),
        notStarted('SSU-9', '????'),
        notStarted('SSU-10', 'CDE ????'),
        notStarted('SSU-11', 'ClinicalTrial ??'),
      ] satisfies MilestoneNode[]},
      { stageCode: 'Enrollment', stageName: 'Enrollment', nodes: [
        { milestoneCode: 'Enrollment-0', milestoneName: 'FPI', planV1Date: d(360), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Enrollment-1', milestoneName: 'LPI', planV1Date: d(720), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Enrollment-2', milestoneName: 'LPO', planV1Date: d(730), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'IA', stageName: 'IA', nodes: [
        { milestoneCode: 'IA-0', milestoneName: 'IA ????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'IA-1', milestoneName: 'IA ????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'Data_Report', stageName: 'Data & Report', nodes: [
        { milestoneCode: 'Data_Report-0', milestoneName: 'DBL', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-1', milestoneName: 'TLR??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-2', milestoneName: 'TLR??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-3', milestoneName: 'TFL??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-4', milestoneName: 'TFL??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-5', milestoneName: 'CSR??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-6', milestoneName: 'CSR??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-7', milestoneName: '????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] satisfies MilestoneNode[]},
      { stageCode: 'PreNDA_BLA', stageName: 'PreNDA/BLA', nodes: [
        notStarted('PreNDA_BLA-0', 'PreNDA ??'),
        notStarted('PreNDA_BLA-1', 'PreNDA ??-????'),
        notStarted('PreNDA_BLA-2', 'PreNDA ??-??'),
        notStarted('PreNDA_BLA-3', 'PreNDA ??-????'),
        notStarted('PreNDA_BLA-4', 'PreNDA ??-???'),
        notStarted('PreNDA_BLA-5', 'PreNDA ??-??'),
      ] satisfies MilestoneNode[]},
      { stageCode: 'NDA_BLA', stageName: 'NDA/BLA', nodes: [
        notStarted('NDA_BLA-0', 'NDA/BLA ??'),
        notStarted('NDA_BLA-1', 'NDA/BLA ????'),
        notStarted('NDA_BLA-2', 'NDA/BLA ????'),
        notStarted('NDA_BLA-3', 'NDA/BLA ??'),
        notStarted('NDA_BLA-4', '????'),
        notStarted('NDA_BLA-5', '????'),
        notStarted('NDA_BLA-6', 'NDA/BLA ??'),
        notStarted('NDA_BLA-7', 'NDA/BLA ??'),
        notStarted('NDA_BLA-8', 'NDA/BLA ??'),
      ] satisfies MilestoneNode[]},
    ],
  }
}

// Populate with full SSU, PreNDA/BLA, NDA/BLA nodes for demo
;(() => {
  const demo = buildDemoMilestones(3, 'HDM1005-302')
  // SSU nodes
  demo.groups[4].nodes = [
    { milestoneCode: 'SSU-0', milestoneName: '????????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-1', milestoneName: '????????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-2', milestoneName: '????????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-3', milestoneName: '????????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-4', milestoneName: '????????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-5', milestoneName: '??????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-6', milestoneName: '??????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-7', milestoneName: '??????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-8', milestoneName: '????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-9', milestoneName: '????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-10', milestoneName: 'CDE ????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-11', milestoneName: 'ClinicalTrial ??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
  ]
  // PreNDA/BLA nodes
  demo.groups[8].nodes = [
    { milestoneCode: 'PreNDA_BLA-0', milestoneName: 'PreNDA ??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-1', milestoneName: 'PreNDA ??-????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-2', milestoneName: 'PreNDA ??-??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-3', milestoneName: 'PreNDA ??-????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-4', milestoneName: 'PreNDA ??-???', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-5', milestoneName: 'PreNDA ??-??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
  ]
  // NDA/BLA nodes
  demo.groups[9].nodes = [
    { milestoneCode: 'NDA_BLA-0', milestoneName: 'NDA/BLA ??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-1', milestoneName: 'NDA/BLA ????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-2', milestoneName: 'NDA/BLA ????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-3', milestoneName: 'NDA/BLA ??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-4', milestoneName: '????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-5', milestoneName: '????', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-6', milestoneName: 'NDA/BLA ??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-7', milestoneName: 'NDA/BLA ??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-8', milestoneName: 'NDA/BLA ??', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
  ]
  mockMilestones.set(3, demo)
  // Also for study ID 1 and 2
  mockMilestones.set(1, buildDemoMilestones(1, 'HDM1005-101'))
  mockMilestones.set(2, buildDemoMilestones(2, 'HDM1005-201'))

})()

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


// ?? Mock ?????? ??
// ??? code/name ?? V8__team_matrix.sql ?????????"????????????"?
// study 1 ????+????????study 2 ??????study 3 ?????

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
      line(101, 3, 'CM', '????', true, [
        [1, '06', '?? PreIND ????????????', me, '09:30:00'],
        [2, '18', '? CDE ??????????????????', me, '10:00:00'],
      ]),
      line(102, 11, 'ST', '????', true, [
        [3, '10', '??????????????????', me, '14:00:00'],
      ]),
      line(103, 2, 'RA', '??', false, [
        [4, '15', 'PreIND ?????????????', 'wangfang@eastchinapharm.com', '11:00:00'],
      ]),
    ],
    2: [
      line(201, 2, 'RA', '??', true, [
        [5, '08', 'IND ??????????', me, '09:00:00'],
        [6, '21', '???????????', me, '16:00:00'],
      ]),
      line(202, 3, 'CM', '????', false, [
        [7, '12', '????????????', 'lijing@eastchinapharm.com', '10:30:00'],
      ]),
    ],
    3: [
      line(301, 7, 'CO', '????', false, [
        [8, '09', 'FPI ???????????', 'lijing@eastchinapharm.com', '13:00:00'],
      ]),
      line(302, 13, 'DM', '????', false, []),
    ],
  }
  const functionLines = linesByStudy[studyId]
  if (!functionLines) return undefined
  return { studyId, studyCode: study.code, month, functionLines }
}

function getMockMonthlyPage(studyId: number, month: string): MonthlyReportPage {
  const key = `${studyId}|${month}`
  let page = mockMonthlyPages.get(key)
  if (!page) {
    page = buildDemoMonthlyPage(studyId, month)
    if (!page) throw new Error('Study ???')
    mockMonthlyPages.set(key, page)
  }
  return page
}

export function createMockApiClient(): ApiClient {
  let currentUser: CurrentUser | undefined
  const teamVersions = new Map(demoStudies.map((study) => [study.id, 0]))
  const teamAssignments = new Map<string, number[]>([
    [`${demoStudies[0].id}|PL`, [2]],
    [`${demoStudies[1].id}|PM`, [2]],
  ])
  const permissions: PlatformPermission[] = [
    ['pipeline', 'pipeline.page.view', '??????', 'PAGE', 'view'],
    ['study', 'study.read', '?? Study', 'ACTION', 'read'],
    ['milestone', 'milestone.read', '?????', 'DATA', 'read'],
    ['milestone', 'milestone.update', '?????', 'DATA', 'update'],
    ['config', 'config.page.view', '??????', 'PAGE', 'view'],
    ['config', 'config.create', '??????', 'ACTION', 'create'],
    ['config', 'config.update', '??????', 'ACTION', 'update'],
    ['config', 'config.delete', '??????', 'ACTION', 'delete'],
    ['account', 'account.page.view', '??????', 'PAGE', 'view'],
    ['account', 'account.create', '????', 'ACTION', 'create'],
    ['role', 'role.page.view', '????????', 'PAGE', 'view'],
    ['role', 'role.create', '????', 'ACTION', 'create'],
    ['role', 'role.update', '??????', 'ACTION', 'update'],
    ['role', 'role.delete', '????', 'ACTION', 'delete'],
    ['team', 'team.page.view', '??????', 'PAGE', 'view'],
    ['team', 'team.edit_mode', '????????', 'PAGE_OPERATION', 'edit_mode'],
    ['team', 'team.update', '??????', 'ACTION', 'update'],
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
      roleDescription: '?????',
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
      roleDescription: '??????',
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
      roleDescription: '????',
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
    { id: 1, code: 'ONCOLOGY', name: '??', englishName: 'Oncology' },
    { id: 2, code: 'AUTOIMMUNE', name: '????', englishName: 'Autoimmune Disease' },
    { id: 3, code: 'METABOLIC_CARDIOVASCULAR', name: '??????', englishName: 'Metabolic and Cardiovascular' },
    { id: 4, code: 'RESPIRATORY', name: '????', englishName: 'Respiratory' },
    { id: 5, code: 'INFECTIOUS_DISEASE', name: '?????', englishName: 'Infectious Disease' },
    { id: 6, code: 'NEUROSCIENCE', name: '????', englishName: 'Neuroscience' },
  ]
  // ?? program/project ????? study?? code ??????????
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
      if (!currentUser) throw new Error('????')
      return currentUser
    },
    async login(credentials) {
      const account = users.find(
        (item) =>
          item.username === credentials.username &&
          item.password === credentials.password,
      )
      if (!account) throw new Error('???????')
      const { password: _password, ...user } = account
      currentUser = user
      return user
    },
    async logout() {
      currentUser = undefined
    },
    async getPipelineOverview() {
      // ? projectCode ?? study ? project??? TA code ?? ? area
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
              (teamAssignments.get(`${studyId}|${roleCode}`) ?? []).map(nameOf).filter(Boolean).join('?')
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
            }
          }),
        }
        const taCode = first.therapeuticAreaCode ?? 'OTHER'
        const taName = first.therapeuticAreaName ?? '??'
        const entry = byArea.get(taCode)
        if (entry) entry.projects.push(project)
        else byArea.set(taCode, { name: taName, projects: [project] })
      }
      const areas: OverviewArea[] = [...byArea.entries()].map(([code, { name, projects }]) => ({
        therapeuticAreaCode: code,
        therapeuticAreaName: name,
        projects,
      }))
      return { title: '??????', areas }
    },
    async listStudies(query = {}) {
      const nameOf = (userId: number) => users[userId - 1]?.displayName ?? ''
      const roleNames = (studyId: number, roleCode: string) =>
        (teamAssignments.get(`${studyId}|${roleCode}`) ?? []).map(nameOf).filter(Boolean).join(' / ')
      const all = demoStudies.map((study) => {
        const milestones = mockMilestones.get(study.id) ?? buildDemoMilestones(study.id, study.code)
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
        if (query.milestoneStatus && study.currentStatus !== query.milestoneStatus) return false
        return true
      })
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      const total = all.length
      const totalPages = Math.max(1, Math.ceil(total / pageSize))
      return {
        data: all.slice((page - 1) * pageSize, page * pageSize),
        total,
        page,
        pageSize,
        totalPages,
      }
    },
    async listRisks(query = {}) {
      const keyword = query.query?.trim().toLowerCase() ?? ''
      const base = mockRisks.filter(({ risk }) =>
        (!keyword || [risk.riskCode, risk.description, risk.ownerName, risk.programCode]
          .some(value => value.toLowerCase().includes(keyword))) &&
        (!query.functionCode || risk.functionCode === query.functionCode))
      const filtered = base.filter(({ risk }) =>
        (!query.status || risk.status === query.status) &&
        (!query.level || risk.level === query.level) &&
        (!query.studyId || risk.studyId === query.studyId) &&
        (!query.ownerUserId || risk.ownerUserId === query.ownerUserId) &&
        (!query.overdueOnly || risk.overdueActionCount > 0))
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      filtered.forEach(item => syncRiskTracking(item))
      return {
        data: filtered.slice((page - 1) * pageSize, page * pageSize).map(item => item.risk),
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
      if (!risk) throw new Error('?????')
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
      const score = input.assessment.impact * input.assessment.likelihood * input.assessment.detectability
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
        const score = input.assessment.impact * input.assessment.likelihood * input.assessment.detectability
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
    async listMonthlyReports() {
      return []
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
          if (!line.editable) throw new Error('????????????')
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
      throw new Error('????????: ' + reportId)
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
      throw new Error('?????????: ' + entryId)
    },
    async deleteMonthlyEntry(entryId) {
      await delay(120)
      for (const page of mockMonthlyPages.values()) {
        for (const line of page.functionLines) {
          const index = line.entries.findIndex((item) => item.entryId === entryId)
          if (index >= 0) {
            if (!line.editable) throw new Error('????????????')
            line.entries.splice(index, 1)
            return structuredClone(page)
          }
        }
      }
      throw new Error('?????????: ' + entryId)
    },
    async getMonthlyReportHistory(studyId, functionLineId, month) {
      await delay(150)
      const page = getMockMonthlyPage(studyId, month)
      const line = page.functionLines.find((item) => item.functionLineId === functionLineId)
      if (!line) throw new Error('??????')
      // ?? 2 ???minusMonths ?????????? 2026-01 ? 2025-12 / 2025-11?
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
          content: `??????${line.functionName} ? ${mo} ???????`,
          updatedBy: author,
          updatedAt: `${mo}-15T10:00:00Z`,
          editable: false,
        },
        {
          entryId: 9100 + functionLineId * 10 + salt,
          entryDate: `${mo}-22`,
          content: `??????${line.functionName} ? ${mo} ???????`,
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
      anchor.download = `??????_${query.startDate}_${query.endDate}.${format}`
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
      if (!study) throw new Error('Study ???')
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
          throw new Error('???????????????????')
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
          updatedAt: study.updatedAt,
        }
      }).filter((row) => !keyword || [
        row.studyCode, row.therapeuticAreaCode, row.therapeuticAreaName, row.programCode,
      ].some((value) => value.toLowerCase().includes(keyword)))
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 10
      const totalItems = all.length
      return {
        data: all.slice((page - 1) * pageSize, page * pageSize),
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
      return programs.filter((item) => !query || [item.code, item.productName]
        .some((value) => value.toLowerCase().includes(query)))
    },
    async createProgram(input) {
      if (programs.some((item) => item.code === input.code)) throw new Error('Program ?????')
      const program: PipelineProgram = {
        id: nextProgramId++, code: input.code, productName: input.productName,
        moa: input.moa ?? null, sourceCode: input.sourceCode,
        sourceLabel: sourceLabel(input.sourceCode),
        originCode: input.originCode, originLabel: originLabel(input.originCode),
        projectCount: 0, studyCount: 0, updatedAt: now(),
      }
      programs.push(program)
      return program
    },
    async updateProgram(id, input) {
      const program = programs.find((item) => item.id === id)
      if (!program) throw new Error('Program ???')
      Object.assign(program, input, { updatedAt: now() })
      return program
    },
    async deleteProgram(id) {
      const index = programs.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Program ???')
      if (programs[index].projectCount) throw new Error('Program ???? Project?????')
      programs.splice(index, 1)
    },
    async listProjects(programId, keyword = '') {
      const query = keyword.trim().toLowerCase()
      return projects.filter((item) => (!programId || item.programId === programId) &&
        (!query || item.code.toLowerCase().includes(query)))
    },
    async createProject(input) {
      if (projects.some((item) => item.code === input.code)) throw new Error('Project ?????')
      const program = programs.find((item) => item.id === input.programId)
      if (!program) throw new Error('Program ???')
      const project: PipelineProject = {
        id: nextProjectId++, code: input.code, programId: input.programId,
        programCode: program.code, indication: input.indication, therapeuticAreaId: 99,
        therapeuticAreaCode: input.therapeuticAreaCode,
        therapeuticAreaName: therapeuticAreas.find((item) => item.code === input.therapeuticAreaCode)?.name ?? input.therapeuticAreaCode,
        studyCount: 0, updatedAt: now(),
      }
      projects.push(project)
      program.projectCount++
      return project
    },
    async updateProject(id, input) {
      const project = projects.find((item) => item.id === id)
      if (!project) throw new Error('Project ???')
      Object.assign(project, input, { updatedAt: now() })
      return project
    },
    async deleteProject(id) {
      const index = projects.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Project ???')
      if (projects[index].studyCount) throw new Error('Project ???? Study?????')
      projects.splice(index, 1)
    },
    async createStudyConfig(input) {
      const project = projects.find((item) => item.id === input.projectId)
      if (!project) throw new Error('Project ???')
      demoStudies.push({ id: nextStudyId++, code: input.code,
        indication: project.indication, phase: input.phase, status: 'ACTIVE', statusLabel: '???',
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
      if (!study || !project) throw new Error('Study ? Project ???')
      study.phase = input.phaseStatusCode
      study.projectCode = project.code
      study.programCode = project.programCode
      study.therapeuticAreaCode = project.therapeuticAreaCode
      study.therapeuticAreaName = project.therapeuticAreaName
      study.indication = project.indication
      return mustFind(
        (await this.listPipelineConfig({ page: 1, pageSize: 500 })).data,
        (item) => item.studyId === id,
        'Study 配置不存在',
      )
    },
    async deleteStudyConfig(id) {
      const index = demoStudies.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Study ???')
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
        visibleStudyCount: user.roles.includes('ADMIN') ? 3 : (user.roles.includes('USER') ? 2 : 1),
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
        throw new Error('??????')
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
      if (!account) throw new Error('?????')
      if (account.password !== input.currentPassword) {
        throw new Error('???????')
      }
      if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,128}$/.test(input.newPassword)) {
        throw new Error('????? 8 ??????????????????')
      }
      if (input.currentPassword === input.newPassword) {
        throw new Error('????????????')
      }
      account.password = input.newPassword
    },
    async resetPassword(id: number) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('?????')
      users[index].password = 'Hd123456'
    },
    async updateUser(id: number, input: UpdateUserInput) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('?????')
      users[index].displayName = input.displayName
    },
    async deleteUser(id: number) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('?????')
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
      const page = filters.page ?? 1
      const pageSize = filters.pageSize ?? 10
      const start = (page - 1) * pageSize
      return {
        data: filtered.slice(start, start + pageSize),
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
        throw new Error('????????????')
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
      if (!role) throw new Error('?????')
      role.roleDescription = input.roleDescription
      role.dataScopeMode = input.dataScopeMode
      role.status = input.status ?? role.status
      role.permissionCodes = [...input.permissionCodes]
      role.updatedAt = new Date().toISOString()
      return { role, invalidatedUserCount: role.assignedUserCount, currentSessionInvalidated: false }
    },
    async deleteRole(roleId) {
      const index = roles.findIndex((role) => role.id === roleId)
      if (index < 0) throw new Error('?????')
      if (roles[index].systemRole) throw new Error('????????')
      if (roles[index].assignedUserCount) throw new Error('????????????')
      roles.splice(index, 1)
    },
    async getMilestones(studyId) {
      await delay(200)
      const data = mockMilestones.get(studyId)
      if (!data) throw new Error('Study ???????????')
      return structuredClone(data)
    },
    async updateMilestone(studyId, milestoneCode, input) {
      await delay(200)
      const page = mockMilestones.get(studyId)
      if (!page) throw new Error('Study ???')
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
      throw new Error('????????: ' + milestoneCode)
    },
    async getStageProjection(studyId) {
      await delay(100)
      const page = mockMilestones.get(studyId)
      if (!page) throw new Error('Study ???')
      for (const group of page.groups) {
        for (const node of group.nodes) {
          if (node.status === 'IN_PROGRESS') {
            return { currentStageCode: group.stageCode, currentStageName: group.stageName,
              currentMilestoneCode: node.milestoneCode, currentMilestoneName: node.milestoneName,
              statusText: '???' }
          }
        }
      }
      // Check if all completed
      const allCompleted = page.groups.every(g => g.nodes.every(n => n.status === 'COMPLETED'))
      if (allCompleted) {
        return { currentStageCode: '', currentStageName: '', currentMilestoneCode: '', currentMilestoneName: '', statusText: '???' }
      }
      // Find first not-started
      for (const group of page.groups) {
        for (const node of group.nodes) {
          if (node.status === 'NOT_STARTED') {
            return { currentStageCode: group.stageCode, currentStageName: group.stageName,
              currentMilestoneCode: node.milestoneCode, currentMilestoneName: node.milestoneName,
              statusText: '???' }
          }
        }
      }
      return { currentStageCode: '', currentStageName: '', currentMilestoneCode: '', currentMilestoneName: '', statusText: '' }
    },
  }
}

function buildMockMonthlyExport(
  query: MonthlyExportQuery,
  therapeuticAreas: TherapeuticArea[],
  programs: PipelineProgram[],
): MonthlyExportReport {
  if (!query.startDate || !query.endDate) {
    throw new Error('????????????')
  }
  if (query.endDate < query.startDate) {
    throw new Error('????????????')
  }
  let scoped = [...demoStudies]
  let scopeLabels = ['????']
  if (query.scopeType === 'TA') {
    if (!query.taIds?.length) throw new Error('???????????')
    const selected = therapeuticAreas.filter((area) => query.taIds!.includes(area.id))
    const codes = new Set(selected.map((area) => area.code))
    scoped = demoStudies.filter((study) => codes.has(study.therapeuticAreaCode ?? ''))
    scopeLabels = selected.map((area) => area.name || area.code)
  } else if (query.scopeType === 'PROGRAM') {
    if (!query.programIds?.length) throw new Error('??????? Program')
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
  // ???????? ? ????? frontier ????????????
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
