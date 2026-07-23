import type {
  AssignRolesInput,
  ChangePasswordInput,
  CreateUserInput,
  CsrfToken,
  CurrentUser,
  LoginCredentials,
  MonthlyReport,
  CreateStudyConfigInput,
  PipelineConfigRow,
  PipelineProgram,
  PipelineProject,
  PipelineOverview,
  PlatformUser,
  PlatformPermission,
  PlatformRole,
  CreateRiskInput,
  RiskActionInput,
  RiskDetail,
  RiskFormOptions,
  RiskPage,
  RiskQuery,
  UpdateRiskInput,
  MilestonePage,
  MilestoneUpdateInput,
  MonthlyEntryCreateInput,
  MonthlyEntryUpdateInput,
  MonthlyReportPage,
  FunctionLineHistory,
  MonthlyExportFormat,
  MonthlyExportQuery,
  MonthlyExportReport,
  StageProjection,
  ProgramInput,
  ProgramUpdateInput,
  ProjectInput,
  ProjectUpdateInput,
  RoleInput,
  RolePage,
  RoleStatus,
  RoleUpdateResult,
  Study,
  StudyConfigInput,
  TeamMatrixBatchInput,
  TeamMatrixBatchResult,
  TeamMatrixPage,
  TeamMatrixQuery,
  TherapeuticArea,
  UpdateUserInput,
} from './types'
import { createMockApiClient } from './mock'

export interface ApiClient {
  getCurrentUser(): Promise<CurrentUser>
  login(credentials: LoginCredentials): Promise<CurrentUser>
  logout(): Promise<void>
  getPipelineOverview(): Promise<PipelineOverview>
  listStudies(): Promise<Study[]>
  listRisks(query?: RiskQuery): Promise<RiskPage>
  getRisk(riskCode: string): Promise<RiskDetail>
  getRiskFormOptions(studyId?: number): Promise<RiskFormOptions>
  createRisk(input: CreateRiskInput): Promise<RiskDetail>
  updateRisk(riskCode: string, input: UpdateRiskInput): Promise<RiskDetail>
  deleteRisk(riskCode: string, expectedVersion: number): Promise<void>
  addRiskAction(riskCode: string, expectedRiskVersion: number, action: RiskActionInput): Promise<RiskDetail>
  updateRiskAction(riskCode: string, actionId: number, expectedVersion: number, action: RiskActionInput): Promise<RiskDetail>
  deleteRiskAction(riskCode: string, actionId: number, expectedVersion: number): Promise<RiskDetail>
  getMilestones(studyId: number): Promise<MilestonePage>
  updateMilestone(studyId: number, milestoneCode: string, input: MilestoneUpdateInput): Promise<MilestonePage>
  getStageProjection(studyId: number): Promise<StageProjection>
  listMonthlyReports(month?: string): Promise<MonthlyReport[]>
  getMonthlyReports(studyId: number, month: string): Promise<MonthlyReportPage>
  createMonthlyEntry(reportId: number, input: MonthlyEntryCreateInput): Promise<MonthlyReportPage>
  updateMonthlyEntry(entryId: number, input: MonthlyEntryUpdateInput): Promise<MonthlyReportPage>
  deleteMonthlyEntry(entryId: number): Promise<MonthlyReportPage>
  getMonthlyReportHistory(studyId: number, functionLineId: number, month: string): Promise<FunctionLineHistory>
  previewMonthlyExport(query: MonthlyExportQuery): Promise<MonthlyExportReport>
  downloadMonthlyExport(query: MonthlyExportQuery, format: MonthlyExportFormat): Promise<void>
  listTeamMatrix(query?: TeamMatrixQuery): Promise<TeamMatrixPage>
  replaceTeamAssignments(input: TeamMatrixBatchInput): Promise<TeamMatrixBatchResult>
  listPipelineConfig(): Promise<PipelineConfigRow[]>
  listTherapeuticAreas(): Promise<TherapeuticArea[]>
  listPrograms(keyword?: string): Promise<PipelineProgram[]>
  createProgram(input: ProgramInput): Promise<PipelineProgram>
  updateProgram(id: number, input: ProgramUpdateInput): Promise<PipelineProgram>
  deleteProgram(id: number): Promise<void>
  listProjects(programId?: number, keyword?: string): Promise<PipelineProject[]>
  createProject(input: ProjectInput): Promise<PipelineProject>
  updateProject(id: number, input: ProjectUpdateInput): Promise<PipelineProject>
  deleteProject(id: number): Promise<void>
  createStudyConfig(input: CreateStudyConfigInput): Promise<void>
  updateStudyConfig(id: number, input: StudyConfigInput): Promise<PipelineConfigRow>
  deleteStudyConfig(id: number): Promise<void>
  listUsers(keyword?: string, roleCode?: string): Promise<PlatformUser[]>
  createUser(input: CreateUserInput): Promise<void>
  changePassword(input: ChangePasswordInput): Promise<void>
  resetPassword(id: number): Promise<void>
  updateUser(id: number, input: UpdateUserInput): Promise<void>
  deleteUser(id: number): Promise<void>
  assignRoles(id: number, input: AssignRolesInput): Promise<void>
  listRoles(filters?: { page?: number; pageSize?: number; keyword?: string; status?: RoleStatus }): Promise<RolePage>
  listPermissions(): Promise<PlatformPermission[]>
  createRole(input: RoleInput): Promise<PlatformRole>
  updateRole(roleId: number, input: RoleInput): Promise<RoleUpdateResult>
  deleteRole(roleId: number): Promise<void>
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
    readonly details?: Record<string, string>,
  ) {
    super(message)
  }
}

type UnauthorizedHandler = () => void

let unauthorizedHandler: UnauthorizedHandler | undefined

export function setUnauthorizedHandler(handler: UnauthorizedHandler | undefined) {
  unauthorizedHandler = handler
}

function toExportParams(query: MonthlyExportQuery): string {
  const parameters = new URLSearchParams()
  parameters.set('startDate', query.startDate)
  parameters.set('endDate', query.endDate)
  parameters.set('scopeType', query.scopeType)
  for (const id of query.taIds ?? []) {
    parameters.append('taIds', String(id))
  }
  for (const id of query.programIds ?? []) {
    parameters.append('programIds', String(id))
  }
  return parameters.toString()
}

export function createHttpApiClient(): ApiClient {
  let csrf: CsrfToken | undefined

  const request = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const headers = new Headers(options.headers)
    if (options.body && !(options.body instanceof URLSearchParams)) {
      headers.set('Content-Type', 'application/json')
    }
    if (csrf && options.method && options.method !== 'GET') {
      headers.set(csrf.headerName, csrf.token)
    }
    const response = await fetch(url, { ...options, headers })
    const text = await response.text()
    const data = text && response.headers.get('content-type')?.includes('json')
      ? JSON.parse(text)
      : undefined
    if (!response.ok) {
      if (response.status === 401) unauthorizedHandler?.()
      throw new ApiError(
        data?.message ?? `请求失败（${response.status}）`,
        response.status,
        data?.code,
        data?.details,
      )
    }
    return data as T
  }

  const refreshCsrf = async () => {
    csrf = await request<CsrfToken>('/api/v1/platform/auth/csrf')
  }

  return {
    getCurrentUser: () => request<CurrentUser>('/api/v1/platform/me'),
    async login(credentials) {
      await refreshCsrf()
      await request('/api/v1/platform/auth/login', {
        method: 'POST',
        body: new URLSearchParams({
          username: credentials.username,
          password: credentials.password,
        }),
      })
      await refreshCsrf()
      return request<CurrentUser>('/api/v1/platform/me')
    },
    async logout() {
      await refreshCsrf()
      await request('/api/v1/platform/auth/logout', { method: 'POST' })
    },
    getPipelineOverview: () =>
      request<PipelineOverview>('/api/v1/clinical-pipeline/overview'),
    listStudies: () => request<Study[]>('/api/v1/clinical-pipeline/studies'),
    listRisks: (query = {}) => {
      const parameters = new URLSearchParams()
      parameters.set('page', String(query.page ?? 1))
      parameters.set('pageSize', String(query.pageSize ?? 20))
      parameters.set('sortBy', query.sortBy ?? 'updatedAt')
      parameters.set('sortOrder', query.sortOrder ?? 'desc')
      if (query.query) parameters.set('query', query.query)
      if (query.functionCode) parameters.set('functionCode', query.functionCode)
      if (query.status) parameters.set('status', query.status)
      if (query.level) parameters.set('level', query.level)
      if (query.studyId) parameters.set('studyId', String(query.studyId))
      return request<RiskPage>(`/api/v1/risk-management/risks?${parameters}`)
    },
    getRisk: (riskCode) => request<RiskDetail>(
      `/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}`),
    getRiskFormOptions: (studyId) => request<RiskFormOptions>(
      `/api/v1/risk-management/form-options${studyId ? `?studyId=${studyId}` : ''}`),
    async createRisk(input) {
      await refreshCsrf()
      return request<RiskDetail>('/api/v1/risk-management/risks', {
        method: 'POST', body: JSON.stringify(input),
      })
    },
    async updateRisk(riskCode, input) {
      await refreshCsrf()
      return request<RiskDetail>(`/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}`, {
        method: 'PATCH', body: JSON.stringify(input),
      })
    },
    async deleteRisk(riskCode, expectedVersion) {
      await refreshCsrf()
      return request(`/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}?expectedVersion=${expectedVersion}`, { method: 'DELETE' })
    },
    async addRiskAction(riskCode, expectedRiskVersion, action) {
      await refreshCsrf()
      return request<RiskDetail>(`/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}/actions`, {
        method: 'POST', body: JSON.stringify({ expectedRiskVersion, action }),
      })
    },
    async updateRiskAction(riskCode, actionId, expectedVersion, action) {
      await refreshCsrf()
      return request<RiskDetail>(`/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}/actions/${actionId}`, {
        method: 'PATCH', body: JSON.stringify({ expectedVersion, action }),
      })
    },
    async deleteRiskAction(riskCode, actionId, expectedVersion) {
      await refreshCsrf()
      return request<RiskDetail>(`/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}/actions/${actionId}?expectedVersion=${expectedVersion}`, { method: 'DELETE' })
    },
    getMilestones: (studyId) =>
      request<MilestonePage>(`/api/v1/studies/${studyId}/milestones`),
    async updateMilestone(studyId, milestoneCode, input) {
      await refreshCsrf()
      return request<MilestonePage>(`/api/v1/studies/${studyId}/milestones/${encodeURIComponent(milestoneCode)}`, {
        method: 'PUT', body: JSON.stringify(input),
      })
    },
    getStageProjection: (studyId) =>
      request<StageProjection>(`/api/v1/studies/${studyId}/stage-projection`),
    listMonthlyReports: (month) =>
      request<MonthlyReport[]>(
        `/api/v1/monthly-reports${month ? `?month=${encodeURIComponent(month)}` : ''}`,
      ),
    getMonthlyReports: (studyId, month) =>
      request<MonthlyReportPage>(
        `/api/v1/studies/${studyId}/monthly-reports?month=${encodeURIComponent(month)}`,
      ),
    async createMonthlyEntry(reportId, input) {
      await refreshCsrf()
      return request<MonthlyReportPage>(`/api/v1/monthly-reports/${reportId}/entries`, {
        method: 'POST', body: JSON.stringify(input),
      })
    },
    async updateMonthlyEntry(entryId, input) {
      await refreshCsrf()
      return request<MonthlyReportPage>(`/api/v1/monthly-report-entries/${entryId}`, {
        method: 'PATCH', body: JSON.stringify(input),
      })
    },
    async deleteMonthlyEntry(entryId) {
      await refreshCsrf()
      return request<MonthlyReportPage>(`/api/v1/monthly-report-entries/${entryId}`, { method: 'DELETE' })
    },
    getMonthlyReportHistory: (studyId, functionLineId, month) =>
      request<FunctionLineHistory>(
        `/api/v1/studies/${studyId}/monthly-reports/history?functionLineId=${functionLineId}&month=${encodeURIComponent(month)}`),
    previewMonthlyExport: (query) =>
      request<MonthlyExportReport>(`/api/v1/reports/monthly/preview?${toExportParams(query)}`),
    async downloadMonthlyExport(query, format) {
      const response = await fetch(
        `/api/v1/reports/monthly/export?${toExportParams(query)}&format=${encodeURIComponent(format)}`,
      )
      if (!response.ok) {
        const text = await response.text()
        let message = `请求失败（${response.status}）`
        let code: string | undefined
        try {
          const data = text ? JSON.parse(text) : undefined
          message = data?.message ?? message
          code = data?.code
        } catch {
          /* ignore non-json */
        }
        if (response.status === 401) unauthorizedHandler?.()
        throw new ApiError(message, response.status, code)
      }
      const blob = await response.blob()
      const disposition = response.headers.get('Content-Disposition') ?? ''
      const utfMatch = /filename\*=UTF-8''([^;]+)/i.exec(disposition)
      const plainMatch = /filename="?([^";]+)"?/i.exec(disposition)
      const filename = decodeURIComponent(
        utfMatch?.[1] ?? plainMatch?.[1] ?? `研发管线月报.${format}`,
      )
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = filename
      document.body.appendChild(anchor)
      anchor.click()
      anchor.remove()
      URL.revokeObjectURL(url)
    },
    listTeamMatrix: (query = {}) => {
      const parameters = new URLSearchParams()
      parameters.set('page', String(query.page ?? 1))
      parameters.set('pageSize', String(query.pageSize ?? 20))
      if (query.studyQuery) parameters.set('studyQuery', query.studyQuery)
      if (query.roleQuery) parameters.set('roleQuery', query.roleQuery)
      return request<TeamMatrixPage>(`/api/v1/team-matrix?${parameters}`)
    },
    async replaceTeamAssignments(input) {
      await refreshCsrf()
      return request<TeamMatrixBatchResult>('/api/v1/team-matrix/assignments', {
        method: 'PUT',
        body: JSON.stringify(input),
      })
    },
    listPipelineConfig: () =>
      request<PipelineConfigRow[]>('/api/v1/clinical-pipeline/pipeline-config'),
    listTherapeuticAreas: () =>
      request<TherapeuticArea[]>('/api/v1/clinical-pipeline/therapeutic-areas'),
    listPrograms: (keyword = '') =>
      request<PipelineProgram[]>(`/api/v1/clinical-pipeline/programs${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`),
    async createProgram(input) {
      await refreshCsrf()
      return request<PipelineProgram>('/api/v1/clinical-pipeline/programs', {
        method: 'POST', body: JSON.stringify(input),
      })
    },
    async updateProgram(id, input) {
      await refreshCsrf()
      return request<PipelineProgram>(`/api/v1/clinical-pipeline/programs/${id}`, {
        method: 'PATCH', body: JSON.stringify(input),
      })
    },
    async deleteProgram(id) {
      await refreshCsrf()
      await request<void>(`/api/v1/clinical-pipeline/programs/${id}`, { method: 'DELETE' })
    },
    listProjects: (programId, keyword = '') => {
      const parameters = new URLSearchParams()
      if (programId) parameters.set('programId', String(programId))
      if (keyword) parameters.set('keyword', keyword)
      const query = parameters.toString()
      return request<PipelineProject[]>(`/api/v1/clinical-pipeline/projects${query ? `?${query}` : ''}`)
    },
    async createProject(input) {
      await refreshCsrf()
      return request<PipelineProject>('/api/v1/clinical-pipeline/projects', {
        method: 'POST', body: JSON.stringify(input),
      })
    },
    async updateProject(id, input) {
      await refreshCsrf()
      return request<PipelineProject>(`/api/v1/clinical-pipeline/projects/${id}`, {
        method: 'PATCH', body: JSON.stringify(input),
      })
    },
    async deleteProject(id) {
      await refreshCsrf()
      await request<void>(`/api/v1/clinical-pipeline/projects/${id}`, { method: 'DELETE' })
    },
    async createStudyConfig(input) {
      await refreshCsrf()
      await request<void>('/api/v1/clinical-pipeline/studies', {
        method: 'POST', body: JSON.stringify(input),
      })
    },
    async updateStudyConfig(id, input) {
      await refreshCsrf()
      return request<PipelineConfigRow>(`/api/v1/clinical-pipeline/studies/${id}`, {
        method: 'PATCH', body: JSON.stringify(input),
      })
    },
    async deleteStudyConfig(id) {
      await refreshCsrf()
      await request<void>(`/api/v1/clinical-pipeline/studies/${id}`, { method: 'DELETE' })
    },
    listUsers: (keyword = '', roleCode = '') => {
      const params = new URLSearchParams()
      if (keyword) params.set('keyword', keyword)
      if (roleCode) params.set('roleCode', roleCode)
      const qs = params.toString()
      return request<PlatformUser[]>(`/api/v1/platform/users${qs ? `?${qs}` : ''}`)
    },
    async createUser(input) {
      await refreshCsrf()
      await request<void>('/api/v1/platform/users', {
        method: 'POST',
        body: JSON.stringify(input),
      })
    },
    async changePassword(input) {
      await refreshCsrf()
      await request<void>('/api/v1/platform/me/password', {
        method: 'POST',
        body: JSON.stringify(input),
      })
    },
    async resetPassword(id) {
      await refreshCsrf()
      await request<void>(`/api/v1/platform/users/${id}/password-reset`, {
        method: 'POST',
      })
    },
    async updateUser(id, input) {
      await refreshCsrf()
      await request<void>(`/api/v1/platform/users/${id}`, {
        method: 'PATCH',
        body: JSON.stringify(input),
      })
    },
    async deleteUser(id) {
      await refreshCsrf()
      await request<void>(`/api/v1/platform/users/${id}`, { method: 'DELETE' })
    },
    async assignRoles(id, input) {
      await refreshCsrf()
      await request<void>(`/api/v1/platform/users/${id}/roles`, {
        method: 'PUT',
        body: JSON.stringify(input),
      })
    },
    listRoles: (filters = {}) => {
      const parameters = new URLSearchParams()
      parameters.set('page', String(filters.page ?? 1))
      parameters.set('pageSize', String(filters.pageSize ?? 20))
      if (filters.keyword) parameters.set('keyword', filters.keyword)
      if (filters.status) parameters.set('status', filters.status)
      return request<RolePage>(`/api/v1/platform/roles?${parameters}`)
    },
    listPermissions: () =>
      request<PlatformPermission[]>('/api/v1/platform/permissions'),
    async createRole(input) {
      await refreshCsrf()
      return request<PlatformRole>('/api/v1/platform/roles', {
        method: 'POST',
        body: JSON.stringify(input),
      })
    },
    async updateRole(roleId, input) {
      await refreshCsrf()
      return request<RoleUpdateResult>(`/api/v1/platform/roles/${roleId}`, {
        method: 'PUT',
        body: JSON.stringify(input),
      })
    },
    async deleteRole(roleId) {
      await refreshCsrf()
      await request<void>(`/api/v1/platform/roles/${roleId}`, { method: 'DELETE' })
    },
  }
}

export const apiClient: ApiClient =
  import.meta.env.VITE_API_MODE === 'mock'
    ? createMockApiClient()
    : createHttpApiClient()
