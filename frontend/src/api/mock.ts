import type { ApiClient } from './client'
import type {
  AssignRolesInput,
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

const users: Array<CurrentUser & { password: string }> = [
  {
    username: 'chen@eastchinapharm.com',
    displayName: '陈研发',
    title: '系统管理员',
    roles: ['ADMIN'],
    permissions: [
      'pipeline.page.view',
      'study.read',
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
    permissions: ['pipeline.page.view', 'study.read', 'milestone.update', 'monthly.read', 'monthly.create', 'monthly.update', 'report.page.view', 'report.export', 'risk.page.view', 'risk.read', 'risk.create', 'risk.update'],
    dataScope: 'ALL',
    password: '1234',
  },
  {
    username: 'liuyang@eastchinapharm.com',
    displayName: '刘洋',
    title: '质量观察员',
    roles: ['VIEWER'],
    permissions: ['pipeline.page.view', 'study.read', 'monthly.read', 'report.page.view', 'risk.page.view', 'risk.read'],
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

// 覆盖 6 个治疗领域；同一 project 下含多个不同 phase 的 study，用于验证 byProject 聚合与回填
const studySeeds: StudySeed[] = [
  // 肿瘤
  ...expand({ indication: '晚期实体瘤', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '肿瘤', programCode: 'HDM2020', projectCode: 'HDM2020-1', productName: 'HDM2020', moa: 'ADC', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2020-001', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '张伟', startDate: '2025-03-10', updatedAt: '2026-07-15T09:20:00' },
    { code: 'HDM2020-002', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '张伟', startDate: '2025-09-01', updatedAt: '2026-07-10T14:05:00' },
  ]),
  ...expand({ indication: '非小细胞肺癌', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '肿瘤', programCode: 'HDM2020', projectCode: 'HDM2020-2', productName: 'HDM2020', moa: 'ADC', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2020-101', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '李静', startDate: '2025-06-20', updatedAt: '2026-07-08T10:30:00' },
  ]),
  ...expand({ indication: '乳腺癌', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '肿瘤', programCode: 'HDM2020', projectCode: 'HDM2020-3', productName: 'HDM2020', moa: 'ADC', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2020-201', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '王芳', startDate: '2025-11-05', updatedAt: '2026-07-01T16:40:00' },
    { code: 'HDM2020-202', phase: 'PHASE_2', status: 'PLANNED', ownerName: '王芳', startDate: '2026-08-01', updatedAt: '2026-06-28T11:00:00' },
  ]),
  ...expand({ indication: '胃癌', therapeuticAreaCode: 'ONCOLOGY', therapeuticAreaName: '肿瘤', programCode: 'HDM2031', projectCode: 'HDM2031-1', productName: 'HDM2031', moa: '单克隆抗体', sourceCode: 'IN_LICENSE', originCode: 'IMPORTED' }, [
    { code: 'HDM2031-001', phase: 'PHASE_3_1', status: 'ACTIVE', ownerName: '陈研发', startDate: '2024-05-12', updatedAt: '2026-07-12T13:10:00' },
    { code: 'HDM2031-002', phase: 'PHASE_3_2', status: 'PLANNED', ownerName: '陈研发', startDate: '2026-09-01', updatedAt: '2026-07-05T09:45:00' },
  ]),
  // 自身免疫
  ...expand({ indication: '系统性红斑狼疮', therapeuticAreaCode: 'AUTOIMMUNE', therapeuticAreaName: '自身免疫', programCode: 'HDM2015', projectCode: 'HDM2015-1', productName: 'HDM2015', moa: 'Small Molecule', sourceCode: 'COOPERATION', originCode: 'DOMESTIC' }, [
    { code: 'HDM2015-101', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '王芳', startDate: '2025-02-18', updatedAt: '2026-07-14T16:40:00' },
    { code: 'HDM2015-102', phase: 'PHASE_1', status: 'COMPLETED', ownerName: '王芳', startDate: '2023-08-01', updatedAt: '2025-12-20T10:00:00' },
  ]),
  ...expand({ indication: '类风湿关节炎', therapeuticAreaCode: 'AUTOIMMUNE', therapeuticAreaName: '自身免疫', programCode: 'HDM2015', projectCode: 'HDM2015-2', productName: 'HDM2015', moa: 'Small Molecule', sourceCode: 'COOPERATION', originCode: 'DOMESTIC' }, [
    { code: 'HDM2015-201', phase: 'IND', status: 'ACTIVE', ownerName: '李静', startDate: '2025-10-10', updatedAt: '2026-07-09T15:20:00' },
  ]),
  // 代谢与心血管
  ...expand({ indication: '2 型糖尿病', therapeuticAreaCode: 'METABOLIC_CARDIOVASCULAR', therapeuticAreaName: '代谢与心血管', programCode: 'HDM1005', projectCode: 'HDM1005-3', productName: 'HDM1005', moa: 'Peptide', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM1005-301', phase: 'PHASE_1', status: 'COMPLETED', ownerName: '李静', startDate: '2023-05-06', updatedAt: '2024-11-30T09:00:00' },
    { code: 'HDM1005-302', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '李静', startDate: '2025-01-15', updatedAt: '2026-07-12T13:10:00' },
    { code: 'HDM1005-303', phase: 'PHASE_3_1', status: 'PLANNED', ownerName: '李静', startDate: '2026-10-01', updatedAt: '2026-07-06T10:20:00' },
  ]),
  ...expand({ indication: '肥胖', therapeuticAreaCode: 'METABOLIC_CARDIOVASCULAR', therapeuticAreaName: '代谢与心血管', programCode: 'HDM1005', projectCode: 'HDM1005-5', productName: 'HDM1005', moa: 'Peptide', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM1005-501', phase: 'PRE_IND', status: 'ACTIVE', ownerName: '张伟', startDate: '2026-01-20', updatedAt: '2026-07-03T14:00:00' },
  ]),
  // 呼吸系统
  ...expand({ indication: '哮喘', therapeuticAreaCode: 'RESPIRATORY', therapeuticAreaName: '呼吸系统', programCode: 'HDM2042', projectCode: 'HDM2042-1', productName: 'HDM2042', moa: '吸入剂', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2042-001', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '刘洋', startDate: '2025-04-22', updatedAt: '2026-07-11T09:30:00' },
  ]),
  ...expand({ indication: '慢阻肺', therapeuticAreaCode: 'RESPIRATORY', therapeuticAreaName: '呼吸系统', programCode: 'HDM2042', projectCode: 'HDM2042-2', productName: 'HDM2042', moa: '吸入剂', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2042-201', phase: 'PHASE_1', status: 'PLANNED', ownerName: '刘洋', startDate: '2026-07-01', updatedAt: '2026-06-30T17:00:00' },
  ]),
  ...expand({ indication: '特发性肺纤维化', therapeuticAreaCode: 'RESPIRATORY', therapeuticAreaName: '呼吸系统', programCode: 'HDM2042', projectCode: 'HDM2042-3', productName: 'HDM2042', moa: '吸入剂', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2042-301', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '王芳', startDate: '2025-12-08', updatedAt: '2026-07-07T11:15:00' },
  ]),
  // 感染性疾病
  ...expand({ indication: '慢性乙肝', therapeuticAreaCode: 'INFECTIOUS_DISEASE', therapeuticAreaName: '感染性疾病', programCode: 'HDM2050', projectCode: 'HDM2050-1', productName: 'HDM2050', moa: '抗病毒', sourceCode: 'COOPERATION', originCode: 'IMPORTED' }, [
    { code: 'HDM2050-001', phase: 'PHASE_3_1', status: 'ACTIVE', ownerName: '陈研发', startDate: '2024-03-15', updatedAt: '2026-07-13T10:50:00' },
    { code: 'HDM2050-002', phase: 'PHASE_3_2', status: 'ACTIVE', ownerName: '陈研发', startDate: '2024-11-20', updatedAt: '2026-07-04T15:35:00' },
  ]),
  // 神经科学
  ...expand({ indication: '阿尔茨海默病', therapeuticAreaCode: 'NEUROSCIENCE', therapeuticAreaName: '神经科学', programCode: 'HDM2066', projectCode: 'HDM2066-1', productName: 'HDM2066', moa: '小分子', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2066-001', phase: 'PHASE_1', status: 'ACTIVE', ownerName: '李静', startDate: '2025-07-30', updatedAt: '2026-07-10T09:10:00' },
    { code: 'HDM2066-002', phase: 'PRE_IND', status: 'COMPLETED', ownerName: '李静', startDate: '2024-02-14', updatedAt: '2025-06-30T14:20:00' },
  ]),
  ...expand({ indication: '帕金森病', therapeuticAreaCode: 'NEUROSCIENCE', therapeuticAreaName: '神经科学', programCode: 'HDM2066', projectCode: 'HDM2066-2', productName: 'HDM2066', moa: '小分子', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2066-201', phase: 'IND', status: 'PLANNED', ownerName: '张伟', startDate: '2026-08-15', updatedAt: '2026-07-02T10:40:00' },
  ]),
  ...expand({ indication: '抑郁症', therapeuticAreaCode: 'NEUROSCIENCE', therapeuticAreaName: '神经科学', programCode: 'HDM2066', projectCode: 'HDM2066-3', productName: 'HDM2066', moa: '小分子', sourceCode: 'SELF_DEVELOPED', originCode: 'DOMESTIC' }, [
    { code: 'HDM2066-301', phase: 'PHASE_2', status: 'ACTIVE', ownerName: '王芳', startDate: '2025-05-25', updatedAt: '2026-07-09T13:25:00' },
  ]),
]

export const demoStudies: Study[] = studySeeds.map((seed, index) => ({
  ...seed,
  id: index + 1,
  statusLabel: STUDY_STATUS_META[seed.status].label,
  statusTone: STUDY_STATUS_META[seed.status].tone,
}))

// 每个 study 的里程碑总览状态（演示用，模拟里程碑推导出的【主状态/子状态/当前阶段完成】）。
// 主显示 = 子状态（节点名，如 "LPI"）；灰色副文本 = 主状态（stage 名，如 "Enrollment"）。
const mockOverviewMilestoneView: Record<string, {
  mainStageLabel: string
  subStatusLabel: string
  currentPhaseCompleted: boolean
}> = {
  'HDM2020-001': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPI', currentPhaseCompleted: false },
  'HDM2020-002': { mainStageLabel: 'IA', subStatusLabel: 'IA 数据冻结', currentPhaseCompleted: false },
  'HDM2020-101': { mainStageLabel: 'SSU', subStatusLabel: '所有中心启动', currentPhaseCompleted: false },
  'HDM2020-201': { mainStageLabel: 'Enrollment', subStatusLabel: 'FPI', currentPhaseCompleted: false },
  'HDM2020-202': { mainStageLabel: 'Protocol', subStatusLabel: '方案定稿', currentPhaseCompleted: false },
  'HDM2031-001': { mainStageLabel: 'Data & Report', subStatusLabel: 'DBL', currentPhaseCompleted: false },
  'HDM2031-002': { mainStageLabel: 'NDA/BLA', subStatusLabel: 'NDA/BLA 递交', currentPhaseCompleted: false },
  'HDM2015-101': { mainStageLabel: 'Data & Report', subStatusLabel: 'CSR初稿', currentPhaseCompleted: false },
  'HDM2015-102': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPO', currentPhaseCompleted: true },
  'HDM2015-201': { mainStageLabel: 'IND', subStatusLabel: 'IND 获批', currentPhaseCompleted: false },
  'HDM1005-301': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPO', currentPhaseCompleted: true },
  'HDM1005-302': { mainStageLabel: 'Data & Report', subStatusLabel: 'TLR定稿', currentPhaseCompleted: false },
  'HDM1005-303': { mainStageLabel: 'NDA/BLA', subStatusLabel: 'NDA/BLA 递交', currentPhaseCompleted: false },
  'HDM1005-501': { mainStageLabel: 'PreIND', subStatusLabel: 'PreIND 反馈-药学', currentPhaseCompleted: false },
  'HDM2042-001': { mainStageLabel: 'IA', subStatusLabel: 'IA 数据分析', currentPhaseCompleted: false },
  'HDM2042-201': { mainStageLabel: 'Protocol', subStatusLabel: '方案讨论会', currentPhaseCompleted: false },
  'HDM2042-301': { mainStageLabel: 'SSU', subStatusLabel: '首家中心启动', currentPhaseCompleted: false },
  'HDM2050-001': { mainStageLabel: 'Data & Report', subStatusLabel: 'CSR定稿', currentPhaseCompleted: false },
  'HDM2050-002': { mainStageLabel: 'NDA/BLA', subStatusLabel: '临床核查', currentPhaseCompleted: false },
  'HDM2066-001': { mainStageLabel: 'Enrollment', subStatusLabel: 'LPI', currentPhaseCompleted: false },
  'HDM2066-002': { mainStageLabel: 'PreIND', subStatusLabel: 'PreIND 反馈-药学', currentPhaseCompleted: true },
  'HDM2066-201': { mainStageLabel: 'IND', subStatusLabel: 'IND 递交', currentPhaseCompleted: false },
  'HDM2066-301': { mainStageLabel: 'SSU', subStatusLabel: '所有中心启动', currentPhaseCompleted: false },
}

const SOURCE_LABEL: Record<string, string> = { SELF_DEVELOPED: '自研', IN_LICENSE: '引进', COOPERATION: '合作' }
const ORIGIN_LABEL: Record<string, string> = { DOMESTIC: '国产', IMPORTED: '进口' }

let nextRiskId = 19
let nextRiskActionId = 2
const mockRisks: RiskDetail[] = [{
  risk: {
    riskCode: 'RSK-2026-000018', studyId: 3, studyCode: 'HDM1005-302',
    programCode: 'HDM1005', projectCode: 'HDM1005-3', functionCode: 'RA',
    functionName: '注册', description: '监管沟通窗口可能影响计划节点',
    ownerUserId: 2, ownerName: '张伟', score: 48, level: 'HIGH', status: 'OPEN',
    actionCount: 1, version: 0, updatedAt: '2026-07-22T09:00:00Z',
  },
  registeredDate: '2026-07-15', closeReason: '',
  assessments: [{ id: 1, number: 1, impact: 4, likelihood: 4, detectability: 3,
    score: 48, level: 'HIGH', reason: '首次评估', assessedBy: '张伟',
    assessedAt: '2026-07-15T09:00:00Z' }],
  actions: [{ id: 1, description: '每周跟踪监管沟通材料', ownerUserId: 2,
    ownerName: '张伟', plannedDate: '2026-08-15', completedDate: null,
    status: 'IN_PROGRESS', completionNote: '', version: 0 }],
}]

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

// ── Mock 里程碑数据 ──

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
        { milestoneCode: 'PreIND-0', milestoneName: 'PreIND 递交', planV1Date: d(-180), planV2Date: d(-175), actualStartDate: d(-178), actualEndDate: d(-176), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-1', milestoneName: 'PreIND 反馈-临床医学', planV1Date: d(-150), planV2Date: d(-145), actualStartDate: d(-148), actualEndDate: d(-140), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-2', milestoneName: 'PreIND 反馈-数统', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-142), actualEndDate: d(-138), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-3', milestoneName: 'PreIND 反馈-临床药理', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-140), actualEndDate: d(-135), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-4', milestoneName: 'PreIND 反馈-非临床', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-138), actualEndDate: d(-130), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'PreIND-5', milestoneName: 'PreIND 反馈-药学', planV1Date: d(-148), planV2Date: d(-143), actualStartDate: d(-135), actualEndDate: d(-125), status: 'COMPLETED', deviationNote: null },
      ] as MilestoneNode[]},
      { stageCode: 'IND', stageName: 'IND', nodes: [
        { milestoneCode: 'IND-0', milestoneName: 'IND 递交', planV1Date: d(-100), planV2Date: d(-95), actualStartDate: d(-98), actualEndDate: d(-96), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-1', milestoneName: 'IND 形审发补', planV1Date: d(-80), planV2Date: d(-78), actualStartDate: d(-82), actualEndDate: d(-75), status: 'COMPLETED', deviationNote: 'CDE要求补充稳定性数据' },
        { milestoneCode: 'IND-2', milestoneName: 'IND 形审补正', planV1Date: d(-60), planV2Date: d(-58), actualStartDate: d(-62), actualEndDate: d(-55), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-3', milestoneName: 'IND 受理', planV1Date: d(-50), planV2Date: d(-48), actualStartDate: d(-52), actualEndDate: d(-46), status: 'COMPLETED', deviationNote: null },
        { milestoneCode: 'IND-4', milestoneName: 'IND 获批', planV1Date: d(-30), planV2Date: d(-28), actualStartDate: d(-30), actualEndDate: null, status: 'IN_PROGRESS', deviationNote: null },
      ] as MilestoneNode[]},
      { stageCode: 'Pre3', stageName: 'Pre3', nodes: [
        { milestoneCode: 'Pre3-0', milestoneName: 'Pre3 递交', planV1Date: d(60), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-1', milestoneName: 'Pre3 反馈-临床医学', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-2', milestoneName: 'Pre3 反馈-数统', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-3', milestoneName: 'Pre3 反馈-临床药理', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-4', milestoneName: 'Pre3 反馈-非临床', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Pre3-5', milestoneName: 'Pre3 反馈-药学', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] as MilestoneNode[]},
      { stageCode: 'Protocol', stageName: 'Protocol', nodes: [
        { milestoneCode: 'Protocol-0', milestoneName: '方案摘要定稿', planV1Date: d(30), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Protocol-1', milestoneName: '方案讨论会', planV1Date: d(90), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Protocol-2', milestoneName: '方案定稿', planV1Date: d(150), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] as MilestoneNode[]},
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
      ] as MilestoneNode[]},
      { stageCode: 'Enrollment', stageName: 'Enrollment', nodes: [
        { milestoneCode: 'Enrollment-0', milestoneName: 'FPI', planV1Date: d(360), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Enrollment-1', milestoneName: 'LPI', planV1Date: d(720), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Enrollment-2', milestoneName: 'LPO', planV1Date: d(730), planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] as MilestoneNode[]},
      { stageCode: 'IA', stageName: 'IA', nodes: [
        { milestoneCode: 'IA-0', milestoneName: 'IA 数据冻结', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'IA-1', milestoneName: 'IA 数据分析', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] as MilestoneNode[]},
      { stageCode: 'Data_Report', stageName: 'Data & Report', nodes: [
        { milestoneCode: 'Data_Report-0', milestoneName: 'DBL', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-1', milestoneName: 'TLR初稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-2', milestoneName: 'TLR定稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-3', milestoneName: 'TFL初稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-4', milestoneName: 'TFL定稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-5', milestoneName: 'CSR初稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-6', milestoneName: 'CSR定稿', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
        { milestoneCode: 'Data_Report-7', milestoneName: '中心关闭', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
      ] as MilestoneNode[]},
      { stageCode: 'PreNDA_BLA', stageName: 'PreNDA/BLA', nodes: [
        notStarted('PreNDA_BLA-0', 'PreNDA 递交'),
        notStarted('PreNDA_BLA-1', 'PreNDA 反馈-临床医学'),
        notStarted('PreNDA_BLA-2', 'PreNDA 反馈-数统'),
        notStarted('PreNDA_BLA-3', 'PreNDA 反馈-临床药理'),
        notStarted('PreNDA_BLA-4', 'PreNDA 反馈-非临床'),
        notStarted('PreNDA_BLA-5', 'PreNDA 反馈-药学'),
      ] as MilestoneNode[]},
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
      ] as MilestoneNode[]},
    ],
  }
}

// Populate with full SSU, PreNDA/BLA, NDA/BLA nodes for demo
;(() => {
  const demo = buildDemoMilestones(3, 'HDM1005-302')
  // SSU nodes
  demo.groups[4].nodes = [
    { milestoneCode: 'SSU-0', milestoneName: '组长单位立项递交', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-1', milestoneName: '组长单位立项获批', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-2', milestoneName: '组长单位伦理递交', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-3', milestoneName: '组长单位伦理获批', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-4', milestoneName: '组长单位合同签署', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-5', milestoneName: '首家中心启动', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-6', milestoneName: '组长单位启动', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-7', milestoneName: '所有中心启动', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-8', milestoneName: '人遗递交', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-9', milestoneName: '人遗批准', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-10', milestoneName: 'CDE 平台登记', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'SSU-11', milestoneName: 'ClinicalTrial 登记', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
  ]
  // PreNDA/BLA nodes
  demo.groups[8].nodes = [
    { milestoneCode: 'PreNDA_BLA-0', milestoneName: 'PreNDA 递交', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-1', milestoneName: 'PreNDA 反馈-临床医学', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-2', milestoneName: 'PreNDA 反馈-数统', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-3', milestoneName: 'PreNDA 反馈-临床药理', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-4', milestoneName: 'PreNDA 反馈-非临床', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'PreNDA_BLA-5', milestoneName: 'PreNDA 反馈-药学', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
  ]
  // NDA/BLA nodes
  demo.groups[9].nodes = [
    { milestoneCode: 'NDA_BLA-0', milestoneName: 'NDA/BLA 递交', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-1', milestoneName: 'NDA/BLA 形审发补', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-2', milestoneName: 'NDA/BLA 形审补正', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-3', milestoneName: 'NDA/BLA 受理', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-4', milestoneName: '临床核查', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-5', milestoneName: '药学核查', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-6', milestoneName: 'NDA/BLA 发补', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-7', milestoneName: 'NDA/BLA 补正', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
    { milestoneCode: 'NDA_BLA-8', milestoneName: 'NDA/BLA 获批', planV1Date: null, planV2Date: null, actualStartDate: null, actualEndDate: null, status: 'NOT_STARTED', deviationNote: null },
  ]
  mockMilestones.set(3, demo)
  // Also for study ID 1 and 2
  mockMilestones.set(1, buildDemoMilestones(1, 'HDM1005-101'))
  mockMilestones.set(2, buildDemoMilestones(2, 'HDM1005-201'))

})()

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


// ── Mock 月报填写数据 ──
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
      line(101, 3, 'CM', '临床医学', true, [
        [1, '06', '完成 PreIND 反馈临床问题回复并归档。', me, '09:30:00'],
        [2, '18', '与 CDE 沟通临床开发计划，确认关键终点设置。', me, '10:00:00'],
      ]),
      line(102, 11, 'ST', '生物统计', true, [
        [3, '10', '完成样本量估算初稿，待内部统计评审。', me, '14:00:00'],
      ]),
      line(103, 2, 'RA', '注册', false, [
        [4, '15', 'PreIND 申请资料已递交，等待受理。', 'wangfang@eastchinapharm.com', '11:00:00'],
      ]),
    ],
    2: [
      line(201, 2, 'RA', '注册', true, [
        [5, '08', 'IND 形审补正资料准备中。', me, '09:00:00'],
        [6, '21', '与监管确认核查时间表。', me, '16:00:00'],
      ]),
      line(202, 3, 'CM', '临床医学', false, [
        [7, '12', '更新研究者手册临床章节。', 'lijing@eastchinapharm.com', '10:30:00'],
      ]),
    ],
    3: [
      line(301, 7, 'CO', '临床运营', false, [
        [8, '09', 'FPI 后首例受试者随访完成。', 'lijing@eastchinapharm.com', '13:00:00'],
      ]),
      line(302, 13, 'DM', '数据管理', false, []),
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
    if (!page) throw new Error('Study 不存在')
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
    ['pipeline', 'pipeline.page.view', '查看管线总览', 'PAGE', 'view'],
    ['study', 'study.read', '查看 Study', 'ACTION', 'read'],
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
      roleDescription: '普通业务成员',
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
      roleDescription: '只读成员',
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
              preindCompleted: false,
              indCompleted: false,
              globallyCompleted: false,
              currentPhaseCompleted: mv?.currentPhaseCompleted ?? false,
              startDate: s.startDate,
              updatedAt: s.updatedAt,
              plName: roleNames(s.id, 'PL'),
              pmName: roleNames(s.id, 'PM'),
            }
          }),
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
    async listStudies() {
      const nameOf = (userId: number) => users[userId - 1]?.displayName ?? ''
      const roleNames = (studyId: number, roleCode: string) =>
        (teamAssignments.get(`${studyId}|${roleCode}`) ?? []).map(nameOf).filter(Boolean).join('、')
      return demoStudies.map((study) => {
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
      })
    },
    async listRisks(query = {}) {
      const keyword = query.query?.trim().toLowerCase() ?? ''
      const base = mockRisks.filter(({ risk }) =>
        (!keyword || [risk.riskCode, risk.description, risk.ownerName, risk.programCode]
          .some(value => value.toLowerCase().includes(keyword))) &&
        (!query.functionCode || risk.functionCode === query.functionCode))
      const filtered = base.filter(({ risk }) =>
        (!query.status || risk.status === query.status) &&
        (!query.level || risk.level === query.level))
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 20
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
          { id: 3, code: 'CLINICAL', name: '临床运营' },
        ] : [],
        owners: studyId ? users.map((item, index) => ({ id: index + 1,
          email: item.username, displayName: item.displayName })) : [],
      }
    },
    async createRisk(input) {
      const study = demoStudies.find(item => item.id === input.studyId)!
      const options = await this.getRiskFormOptions(input.studyId)
      const fn = options.functions.find(item => item.id === input.functionLineId)!
      const owner = options.owners.find(item => item.id === input.ownerUserId)!
      const score = input.assessment.impact * input.assessment.likelihood * input.assessment.detectability
      const level = score <= 12 ? 'LOW' : score <= 36 ? 'MEDIUM' : 'HIGH'
      const now = new Date().toISOString()
      const detail: RiskDetail = {
        risk: { riskCode: `RSK-${new Date().getFullYear()}-${String(nextRiskId++).padStart(6, '0')}`,
          studyId: study.id, studyCode: study.code, programCode: study.programCode ?? '',
          projectCode: study.projectCode ?? '', functionCode: fn.code, functionName: fn.name,
          description: input.description, ownerUserId: owner.id, ownerName: owner.displayName,
          score, level, status: 'OPEN', actionCount: input.actions.length, version: 0, updatedAt: now },
        registeredDate: input.registeredDate ?? now.slice(0, 10), closeReason: '',
        assessments: [{ id: Date.now(), number: 1, ...input.assessment, score, level,
          reason: input.assessment.reason ?? '', assessedBy: currentUser?.displayName ?? '', assessedAt: now }],
        actions: input.actions.map(action => ({ id: nextRiskActionId++, description: action.description,
          ownerUserId: action.ownerUserId,
          ownerName: options.owners.find(item => item.id === action.ownerUserId)?.displayName ?? '',
          plannedDate: action.plannedDate ?? null, completedDate: action.completedDate ?? null,
          status: action.status ?? 'OPEN', completionNote: action.completionNote ?? '', version: 0 })),
      }
      mockRisks.unshift(detail)
      return structuredClone(detail)
    },
    async updateRisk(riskCode, input) {
      const detail = mockRisks.find(item => item.risk.riskCode === riskCode)
      if (!detail) throw new Error('风险不存在')
      if (detail.risk.version !== input.expectedVersion) throw new Error('风险已被其他用户修改，请刷新后重试')
      const options = await this.getRiskFormOptions(input.studyId)
      const study = demoStudies.find(item => item.id === input.studyId)!
      const fn = options.functions.find(item => item.id === input.functionLineId)!
      const owner = options.owners.find(item => item.id === input.ownerUserId)!
      Object.assign(detail.risk, { studyId: study.id, studyCode: study.code,
        programCode: study.programCode ?? '', projectCode: study.projectCode ?? '',
        functionCode: fn.code, functionName: fn.name, ownerUserId: owner.id,
        ownerName: owner.displayName, description: input.description, status: input.status,
        version: detail.risk.version + 1, updatedAt: new Date().toISOString() })
      detail.registeredDate = input.registeredDate ?? detail.registeredDate
      detail.closeReason = input.statusReason ?? ''
      if (input.assessment) {
        const score = input.assessment.impact * input.assessment.likelihood * input.assessment.detectability
        const level = score <= 12 ? 'LOW' : score <= 36 ? 'MEDIUM' : 'HIGH'
        Object.assign(detail.risk, { score, level })
        detail.assessments.unshift({ id: Date.now(), number: detail.assessments.length + 1,
          ...input.assessment, score, level, reason: input.assessment.reason ?? '',
          assessedBy: currentUser?.displayName ?? '', assessedAt: new Date().toISOString() })
      }
      return structuredClone(detail)
    },
    async deleteRisk(riskCode, expectedVersion) {
      const index = mockRisks.findIndex(item => item.risk.riskCode === riskCode)
      if (index < 0 || mockRisks[index].risk.version !== expectedVersion) throw new Error('风险不存在或版本已变化')
      mockRisks.splice(index, 1)
    },
    async addRiskAction(riskCode, expectedRiskVersion, action) {
      const detail = mockRisks.find(item => item.risk.riskCode === riskCode)!
      if (detail.risk.version !== expectedRiskVersion) throw new Error('风险版本已变化')
      const options = await this.getRiskFormOptions(detail.risk.studyId)
      detail.actions.push({ id: nextRiskActionId++, description: action.description,
        ownerUserId: action.ownerUserId,
        ownerName: options.owners.find(item => item.id === action.ownerUserId)?.displayName ?? '',
        plannedDate: action.plannedDate ?? null, completedDate: action.completedDate ?? null,
        status: action.status ?? 'OPEN', completionNote: action.completionNote ?? '', version: 0 })
      detail.risk.actionCount = detail.actions.length
      detail.risk.version++
      return structuredClone(detail)
    },
    async updateRiskAction(riskCode, actionId, expectedVersion, action) {
      const detail = mockRisks.find(item => item.risk.riskCode === riskCode)!
      const target = detail.actions.find(item => item.id === actionId)!
      if (target.version !== expectedVersion) throw new Error('措施版本已变化')
      Object.assign(target, action, { version: target.version + 1 })
      detail.risk.version++
      return structuredClone(detail)
    },
    async deleteRiskAction(riskCode, actionId, expectedVersion) {
      const detail = mockRisks.find(item => item.risk.riskCode === riskCode)!
      const index = detail.actions.findIndex(item => item.id === actionId && item.version === expectedVersion)
      if (index < 0) throw new Error('措施不存在或版本已变化')
      detail.actions.splice(index, 1)
      detail.risk.actionCount = detail.actions.length
      detail.risk.version++
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
      throw new Error('月报进展明细不存在: ' + entryId)
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
      throw new Error('月报进展明细不存在: ' + entryId)
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
          content: `（历史示例）${line.functionName} 在 ${mo} 的进展记录一。`,
          updatedBy: author,
          updatedAt: `${mo}-15T10:00:00Z`,
          editable: false,
        },
        {
          entryId: 9100 + functionLineId * 10 + salt,
          entryDate: `${mo}-22`,
          content: `（历史示例）${line.functionName} 在 ${mo} 的进展记录二。`,
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
      const pageSize = query.pageSize ?? 20
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
    async listPipelineConfig() {
      return demoStudies.map((study) => {
        const project = projects.find((item) => item.code === study.projectCode)!
        const program = programs.find((item) => item.id === project.programId)!
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
      }})
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
      if (programs.some((item) => item.code === input.code)) throw new Error('Program 编码已存在')
      const program: PipelineProgram = {
        id: nextProgramId++, code: input.code, productName: input.productName,
        moa: input.moa ?? null, sourceCode: input.sourceCode,
        sourceLabel: input.sourceCode === 'SELF_DEVELOPED' ? '自研' : input.sourceCode === 'IN_LICENSE' ? '引进' : '合作',
        originCode: input.originCode, originLabel: input.originCode === 'DOMESTIC' ? '国产' : '进口',
        projectCount: 0, studyCount: 0, updatedAt: now(),
      }
      programs.push(program)
      return program
    },
    async updateProgram(id, input) {
      const program = programs.find((item) => item.id === id)
      if (!program) throw new Error('Program 不存在')
      Object.assign(program, input, { updatedAt: now() })
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
      return projects.filter((item) => (!programId || item.programId === programId) &&
        (!query || item.code.toLowerCase().includes(query)))
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
        studyCount: 0, updatedAt: now(),
      }
      projects.push(project)
      program.projectCount++
      return project
    },
    async updateProject(id, input) {
      const project = projects.find((item) => item.id === id)
      if (!project) throw new Error('Project 不存在')
      Object.assign(project, input, { updatedAt: now() })
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
      study.phase = input.phaseStatusCode
      study.projectCode = project.code
      study.programCode = project.programCode
      study.therapeuticAreaCode = project.therapeuticAreaCode
      study.therapeuticAreaName = project.therapeuticAreaName
      study.indication = project.indication
      return (await this.listPipelineConfig()).find((item) => item.studyId === id)!
    },
    async deleteStudyConfig(id) {
      const index = demoStudies.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Study 不存在')
      demoStudies.splice(index, 1)
    },
    async listUsers(keyword = '', roleCode = '') {
      let filtered = users.map((user, index) => ({
        id: index + 1,
        username: user.username,
        displayName: user.displayName,
        roles: user.roles,
        roleDescriptions: user.roles.map(r => {
          switch (r) {
            case 'ADMIN': return '系统管理员'
            case 'USER': return '项目负责人'
            case 'VIEWER': return '只读成员'
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
      return filtered
    },
    async createUser(input: CreateUserInput) {
      if (users.some(u => u.username === input.username)) {
        throw new Error('用户名已存在')
      }
      users.push({
        username: input.username,
        displayName: input.displayName,
        title: '',
        roles: input.roleCodes,
        permissions: [],
        dataScope: 'ALL',
        password: input.password,
      })
    },
    async updateUser(id: number, input: UpdateUserInput) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('用户不存在')
      users[index].displayName = input.displayName
    },
    async deleteUser(id: number) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('用户不存在')
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
      const pageSize = filters.pageSize ?? 20
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
        throw new Error('角色编码已存在且不可复用')
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
      const data = mockMilestones.get(studyId)
      if (!data) throw new Error('Study 不存在或暂无里程碑数据')
      return structuredClone(data)
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
      for (const group of page.groups) {
        for (const node of group.nodes) {
          if (node.status === 'IN_PROGRESS') {
            return { currentStageCode: group.stageCode, currentStageName: group.stageName,
              currentMilestoneCode: node.milestoneCode, currentMilestoneName: node.milestoneName,
              statusText: '进行中' }
          }
        }
      }
      // Check if all completed
      const allCompleted = page.groups.every(g => g.nodes.every(n => n.status === 'COMPLETED'))
      if (allCompleted) {
        return { currentStageCode: '', currentStageName: '', currentMilestoneCode: '', currentMilestoneName: '', statusText: '已完成' }
      }
      // Find first not-started
      for (const group of page.groups) {
        for (const node of group.nodes) {
          if (node.status === 'NOT_STARTED') {
            return { currentStageCode: group.stageCode, currentStageName: group.stageName,
              currentMilestoneCode: node.milestoneCode, currentMilestoneName: node.milestoneName,
              statusText: '未开始' }
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
    throw new Error('请选择汇报开始与结束日期')
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
    if (!groupMap.has(key)) {
      groupMap.set(key, {
        taCode: study.therapeuticAreaCode ?? '',
        taName: study.therapeuticAreaName ?? '',
        rows: [],
      })
    }
    groupMap.get(key)!.rows.push({
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
      notStarted: scoped.filter((s) => deriveMockStatus(s) === '未开始').length,
      inProgress: scoped.filter((s) => deriveMockStatus(s) === '进行中').length,
      completed: scoped.filter((s) => deriveMockStatus(s) === '已完成').length,
      reportedStudyCount: new Set(progress.map((item) => item.studyCode)).size,
      openRiskCount: openRisks.length,
    },
    snapshotGroups: [...groupMap.values()],
    progress,
    openRisks,
  }
}

/** Mock approximates milestone-based export status from Study list fields. */
function deriveMockStatus(study: Study): string {
  if (study.status === 'COMPLETED' || study.statusLabel === '已完成') return '已完成'
  if (study.status === 'ACTIVE' || study.statusLabel === '进行中') return '进行中'
  return '未开始'
}

function mockExportBlob(report: MonthlyExportReport, format: MonthlyExportFormat): Blob {
  if (format === 'csv') {
    const lines = [
      '汇报开始,汇报结束,TA,Program,Study,功能线代码,功能线名称,进展日期,月报进展',
      ...report.progress.map((item) =>
        [report.meta.startDate, report.meta.endDate, item.taName, item.programCode,
          item.studyCode, item.functionCode, item.functionName, item.entryDate, item.content]
          .map((value) => `"${String(value).replaceAll('"', '""')}"`)
          .join(',')),
    ]
    return new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8' })
  }
  if (format === 'xlsx') {
    return new Blob([JSON.stringify(report)], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
  }
  const html = `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8"><title>临床研发管线月度报告</title></head>
<body><h1>临床研发管线月度报告</h1>
<p>${report.meta.startDate} 至 ${report.meta.endDate} · ${report.meta.scopeLabels.join('、')}</p>
<p>Study ${report.summary.total} · 进展 ${report.progress.length} · Open 风险 ${report.summary.openRiskCount}</p>
</body></html>`
  return new Blob([html], { type: 'text/html;charset=utf-8' })
}
