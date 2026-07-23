export type RoleCode = string
export type PermissionCode = string
export type DataScope = 'ALL' | 'ASSIGNED_STUDY'

export interface CurrentUser {
  username: string
  displayName: string
  roles: RoleCode[]
  permissions: PermissionCode[]
  dataScope: DataScope
  title?: string
}

export interface CsrfToken {
  headerName: string
  parameterName: string
  token: string
}

export interface OverviewStudy {
  id: number
  code: string
  phase: string
  status: string
  statusLabel: string
  statusTone: string
  mainStageCode: string | null
  mainStageLabel: string | null
  subStatusLabel: string | null
  preindCompleted: boolean
  indCompleted: boolean
  globallyCompleted: boolean
  currentPhaseCompleted: boolean
  startDate: string | null
  updatedAt: string
  plName?: string
  pmName?: string
}

export interface OverviewProject {
  id: number
  code: string
  indication: string
  programCode: string
  productName: string
  moa: string
  sourceCode: string
  originCode: string
  studies: OverviewStudy[]
}

export interface OverviewArea {
  therapeuticAreaCode: string
  therapeuticAreaName: string
  projects: OverviewProject[]
}

export interface PipelineOverview {
  title: string
  areas: OverviewArea[]
}

export interface Study {
  id: number
  code: string
  indication: string
  /** 临床试验阶段编码（PRE_IND/IND/PHASE_1/PHASE_2/PRE_3/PHASE_3_1/PHASE_3_2），与后端 hd_plt_study.phase_status_code 一致 */
  phase: string
  status: string
  statusLabel: string
  statusTone: string
  ownerName: string
  startDate: string | null
  updatedAt: string
  therapeuticArea?: string
  therapeuticAreaEn?: string
  therapeuticAreaCode?: string
  therapeuticAreaName?: string
  product?: string
  program?: string
  programCode?: string
  project?: string
  projectCode?: string
  plName?: string
  pmName?: string
  currentPhase?: string
  currentStatus?: string
  productName?: string
  moa?: string
  sourceCode?: string
  originCode?: string
}

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type RiskStatus = 'OPEN' | 'CLOSED'
export type RiskActionStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export interface RiskSummary {
  riskCode: string
  studyId: number
  studyCode: string
  programCode: string
  projectCode: string
  functionCode: string
  functionName: string
  description: string
  ownerUserId: number
  ownerName: string
  score: number
  level: RiskLevel
  status: RiskStatus
  actionCount: number
  version: number
  updatedAt: string
}

export interface RiskAssessment {
  id: number
  number: number
  impact: number
  likelihood: number
  detectability: number
  score: number
  level: RiskLevel
  reason: string
  assessedBy: string
  assessedAt: string
}

export interface RiskAction {
  id: number
  description: string
  ownerUserId: number
  ownerName: string
  plannedDate: string | null
  completedDate: string | null
  status: RiskActionStatus
  completionNote: string
  version: number
}

export interface RiskDetail {
  risk: RiskSummary
  registeredDate: string
  closeReason: string
  assessments: RiskAssessment[]
  actions: RiskAction[]
}

export interface RiskPage {
  data: RiskSummary[]
  stats: { total: number; open: number; high: number; medium: number }
  pagination: { page: number; pageSize: number; totalItems: number; totalPages: number }
}

export interface RiskQuery {
  query?: string
  functionCode?: string
  status?: RiskStatus | ''
  level?: RiskLevel | ''
  studyId?: number
  sortBy?: 'updatedAt' | 'riskCode' | 'studyCode' | 'score' | 'level' | 'registeredDate'
  sortOrder?: 'asc' | 'desc'
  page?: number
  pageSize?: number
}

export interface RiskStudyOption {
  id: number
  studyCode: string
  programCode: string
  projectCode: string
}
export interface RiskFunctionOption { id: number; code: string; name: string }
export interface RiskMemberOption { id: number; email: string; displayName: string }
export interface RiskFormOptions {
  studies: RiskStudyOption[]
  functions: RiskFunctionOption[]
  owners: RiskMemberOption[]
}
export interface RiskAssessmentInput {
  impact: number
  likelihood: number
  detectability: number
  reason?: string
}
export interface RiskActionInput {
  description: string
  ownerUserId: number
  plannedDate?: string
  completedDate?: string
  status?: RiskActionStatus
  completionNote?: string
}
export interface CreateRiskInput {
  studyId: number
  functionLineId: number
  ownerUserId: number
  description: string
  registeredDate?: string
  assessment: RiskAssessmentInput
  actions: RiskActionInput[]
}
export interface UpdateRiskInput extends Omit<CreateRiskInput, 'actions' | 'assessment'> {
  expectedVersion: number
  status: RiskStatus
  statusReason?: string
  assessment?: RiskAssessmentInput
}

// ── 里程碑 ──

export interface MilestoneNode {
  milestoneCode: string
  milestoneName: string
  planV1Date: string | null
  planV2Date: string | null
  actualStartDate: string | null
  actualEndDate: string | null
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'
  deviationNote: string | null
}

export interface StageGroup {
  stageCode: string
  stageName: string
  nodes: MilestoneNode[]
}

export interface MilestonePage {
  studyCode: string
  groups: StageGroup[]
}

export interface MilestoneUpdateInput {
  planV1Date?: string | null
  planV2Date?: string | null
  actualStartDate?: string | null
  actualEndDate?: string | null
  deviationNote?: string | null
}

export interface StageProjection {
  currentStageCode: string
  currentStageName: string
  currentMilestoneCode: string
  currentMilestoneName: string
  statusText: string
}

export interface MonthlyReport {
  studyCode: string
  month: string
  functionCode: string
  functionName: string
  content: string
  updatedBy: string
  updatedAt: string
}

// ── 月报填写（按功能线） ──

export interface MonthlyReportEntry {
  entryId: number
  entryDate: string
  content: string
  updatedBy: string
  updatedAt: string
  editable: boolean
}

export interface FunctionLineReport {
  reportId: number
  functionLineId: number
  functionCode: string
  functionName: string
  editable: boolean
  entries: MonthlyReportEntry[]
}

export interface MonthlyReportPage {
  studyId: number
  studyCode: string
  month: string
  functionLines: FunctionLineReport[]
}

export interface MonthlyHistoryMonth {
  month: string
  entries: MonthlyReportEntry[]
}

export interface FunctionLineHistory {
  functionLineId: number
  functionCode: string
  functionName: string
  months: MonthlyHistoryMonth[]
}

export interface MonthlyEntryCreateInput {
  entryDate?: string
  content?: string
}

export interface MonthlyEntryUpdateInput {
  entryDate?: string
  content?: string
}

export type MonthlyExportScopeType = 'ALL' | 'TA' | 'PROGRAM'

export interface MonthlyExportQuery {
  startDate: string
  endDate: string
  scopeType: MonthlyExportScopeType
  taIds?: number[]
  programIds?: number[]
}

export interface MonthlyExportMeta {
  startDate: string
  endDate: string
  scopeType: MonthlyExportScopeType
  scopeLabels: string[]
  generatedAt: string
}

export interface MonthlyExportSummary {
  total: number
  notStarted: number
  inProgress: number
  completed: number
  reportedStudyCount: number
  openRiskCount: number
}

export interface MonthlyExportSnapshotRow {
  programCode: string
  productName: string
  studyCode: string
  indication: string
  phase: string
  projectStatus: string
}

export interface MonthlyExportSnapshotGroup {
  taCode: string
  taName: string
  rows: MonthlyExportSnapshotRow[]
}

export interface MonthlyExportProgressItem {
  studyCode: string
  programCode: string
  taName: string
  entryDate: string
  functionCode: string
  functionName: string
  content: string
}

export interface MonthlyExportRiskItem {
  riskCode: string
  programCode: string
  description: string
  score: number
  level: string
  ownerName: string
}

export interface MonthlyExportReport {
  meta: MonthlyExportMeta
  summary: MonthlyExportSummary
  snapshotGroups: MonthlyExportSnapshotGroup[]
  progress: MonthlyExportProgressItem[]
  openRisks: MonthlyExportRiskItem[]
}

export type MonthlyExportFormat = 'html' | 'csv' | 'xlsx'

export interface TeamMatrixStudy {
  studyId: number
  studyCode: string
  indication: string
  statusCode: string
  statusLabel: string
  currentStatus: string
  version: number
}

export interface TeamMatrixRole {
  roleCode: string
  roleName: string
  functionCode: string | null
  functionName: string | null
}

export interface TeamMatrixMember {
  userId: number
  email: string
  displayName: string
  enabled: boolean
}

export interface TeamMatrixAssignment {
  studyId: number
  roleCode: string
  members: TeamMatrixMember[]
}

export interface TeamMatrixPage {
  studies: TeamMatrixStudy[]
  roles: TeamMatrixRole[]
  assignments: TeamMatrixAssignment[]
  totalRoles: number
  pagination: {
    page: number
    pageSize: number
    totalItems: number
    totalPages: number
  }
}

export interface TeamMatrixQuery {
  studyQuery?: string
  roleQuery?: string
  page?: number
  pageSize?: number
}

export interface TeamMatrixBatchInput {
  studies: Array<{
    studyId: number
    expectedVersion: number
    roles: Array<{ roleCode: string; userIds: number[] }>
  }>
}

export interface TeamMatrixBatchResult {
  studies: Array<{ studyId: number; version: number }>
}

export interface PipelineConfigRow {
  studyId: number
  studyCode: string
  phaseStatusCode: string
  projectId: number
  projectCode: string
  indication: string
  therapeuticAreaCode: string
  therapeuticAreaName: string
  programId: number
  programCode: string
  productName: string
  moa: string | null
  sourceCode: string
  sourceLabel: string
  originCode: string
  originLabel: string
  updatedAt: string
}

export interface PipelineProgram {
  id: number
  code: string
  productName: string
  moa: string | null
  sourceCode: string
  sourceLabel: string
  originCode: string
  originLabel: string
  projectCount: number
  studyCount: number
  updatedAt: string
}

export interface PipelineProject {
  id: number
  code: string
  programId: number
  programCode: string
  indication: string
  therapeuticAreaId: number
  therapeuticAreaCode: string
  therapeuticAreaName: string
  studyCount: number
  updatedAt: string
}

export interface TherapeuticArea {
  id: number
  code: string
  name: string
  englishName: string | null
}

export interface ProgramInput {
  code: string
  productName: string
  moa?: string
  sourceCode: string
  originCode: string
}

export type ProgramUpdateInput = Partial<Omit<ProgramInput, 'code'>>

export interface ProjectInput {
  code: string
  programId: number
  indication: string
  therapeuticAreaCode: string
}

export type ProjectUpdateInput = Partial<Omit<ProjectInput, 'code' | 'programId'>>

export interface StudyConfigInput {
  projectId: number
  phaseStatusCode: string
}

export interface CreateStudyConfigInput {
  code: string
  projectId: number
  phase: string
}

export interface PlatformUser {
  id: number
  username: string
  displayName: string
  roles: RoleCode[]
  roleDescriptions: string[]
  dataScope: DataScope
  visibleStudyCount: number
  enabled: boolean
}

export interface CreateUserInput {
  username: string
  displayName: string
  roleCodes: RoleCode[]
}

export interface ChangePasswordInput {
  currentPassword: string
  newPassword: string
}

export interface UpdateUserInput {
  displayName: string
  enabled: boolean
}

export interface AssignRolesInput {
  roleCodes: RoleCode[]
}

export type RoleStatus = 'ACTIVE' | 'DISABLED'

export interface PlatformRole {
  id: number
  roleCode: string
  roleDescription: string | null
  dataScopeMode: DataScope
  status: RoleStatus
  systemRole: boolean
  assignedUserCount: number
  permissionCodes: PermissionCode[]
  updatedAt: string
}

export interface PlatformPermission {
  id: number
  moduleCode: string
  permissionCode: PermissionCode
  permissionName: string
  permissionType: string
  actionCode: string | null
  permissionDescription: string | null
  sortOrder: number
}

export interface RolePage {
  data: PlatformRole[]
  page: number
  pageSize: number
  totalItems: number
  totalPages: number
}

export interface RoleInput {
  roleCode?: string
  roleDescription: string
  dataScopeMode: DataScope
  status?: RoleStatus
  permissionCodes: PermissionCode[]
}

export interface RoleUpdateResult {
  role: PlatformRole
  invalidatedUserCount: number
  currentSessionInvalidated: boolean
}

export interface LoginCredentials {
  username: string
  password: string
}
