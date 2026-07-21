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

export interface StatusMetric {
  status: string
  label: string
  tone: 'neutral' | 'positive' | 'warning' | 'info'
  count: number
}

export interface PipelineOverview {
  title: string
  total: number
  statuses: StatusMetric[]
}

export interface Study {
  id: number
  code: string
  name: string
  indication: string
  phase: string
  status: string
  statusLabel: string
  statusTone: string
  ownerName: string
  startDate: string | null
  updatedAt: string
  therapeuticArea?: string
  therapeuticAreaEn?: string
  product?: string
  program?: string
  project?: string
  moa?: string
  source?: string
  origin?: string
}

export interface Risk {
  id: string
  studyCode: string
  program: string
  functionName: string
  description: string
  owner: string
  severity: '低' | '中' | '高'
  status: 'Open' | 'Monitoring' | 'Closed'
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

export interface TeamAssignment {
  studyCode: string
  project: string
  roleCode: string
  roleName: string
  department: string
  members: string[]
}

export interface PipelineConfig {
  key: string
  source: string
  origin: string
  product: string
  moa: string
  program: string
  indication: string
  project: string
  therapeuticArea: string
  studyCode: string
  projectStatus: string
  phaseStatus: string
}

export interface PlatformUser {
  id: number
  username: string
  displayName: string
  roles: RoleCode[]
  enabled: boolean
}

export interface LoginCredentials {
  username: string
  password: string
}
