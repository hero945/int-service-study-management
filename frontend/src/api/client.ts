import type {
  AuditLogPage,
  AuditLogQuery,
  AssignRolesInput,
  ChangePasswordInput,
  CreateUserInput,
  CsrfToken,
  CurrentUser,
  LoginCredentials,
  CreateStudyConfigInput,
  PipelineConfigPage,
  PipelineConfigQuery,
  PipelineConfigRow,
  PipelineProgram,
  PipelineProject,
  PipelineOverview,
  PlatformUser,
  UserListQuery,
  UserPage,
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
  ProjectMilestonePage,
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
  StudyListQuery,
  StudyPage,
  StudyConfigInput,
  StudyDeletePreview,
  TeamMatrixBatchInput,
  TeamMatrixBatchResult,
  TeamMatrixPage,
  TeamMatrixQuery,
  TherapeuticArea,
  UpdateUserInput,
} from './types'
import { createMockApiClient } from './mock'
import {
  createClientNetworkError,
  parseApiErrorResponse,
  parseJsonBody,
} from './errors'

export { ApiError, formatApiError } from './errors'
export type { ApiErrorResponse } from './errors'

export interface ApiClient {
  listAuditLogs(query: AuditLogQuery): Promise<AuditLogPage>
  getCurrentUser(): Promise<CurrentUser>
  login(credentials: LoginCredentials): Promise<CurrentUser>
  logout(): Promise<void>
  getPipelineOverview(): Promise<PipelineOverview>
  listStudies(query?: StudyListQuery): Promise<StudyPage>
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
  getProjectMilestones(studyId: number): Promise<ProjectMilestonePage>
  updateProjectMilestone(studyId: number, milestoneCode: string, input: MilestoneUpdateInput): Promise<ProjectMilestonePage>
  getProjectStageProjection(studyId: number): Promise<StageProjection>
  getMonthlyReports(studyId: number, month: string): Promise<MonthlyReportPage>
  createMonthlyEntry(reportId: number, input: MonthlyEntryCreateInput): Promise<MonthlyReportPage>
  updateMonthlyEntry(entryId: number, input: MonthlyEntryUpdateInput): Promise<MonthlyReportPage>
  deleteMonthlyEntry(entryId: number): Promise<MonthlyReportPage>
  getMonthlyReportHistory(studyId: number, functionLineId: number, month: string): Promise<FunctionLineHistory>
  previewMonthlyExport(query: MonthlyExportQuery): Promise<MonthlyExportReport>
  downloadMonthlyExport(query: MonthlyExportQuery, format: MonthlyExportFormat): Promise<void>
  listTeamMatrix(query?: TeamMatrixQuery): Promise<TeamMatrixPage>
  getStudyTeam(studyId: number): Promise<TeamMatrixPage>
  replaceTeamAssignments(input: TeamMatrixBatchInput): Promise<TeamMatrixBatchResult>
  listPipelineConfig(query?: PipelineConfigQuery): Promise<PipelineConfigPage>
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
  getStudyDeletePreview(id: number): Promise<StudyDeletePreview>
  deleteStudyConfig(id: number): Promise<void>
  listUsers(query?: UserListQuery): Promise<UserPage>
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

type UnauthorizedHandler = () => void

let unauthorizedHandler: UnauthorizedHandler | undefined

export function setUnauthorizedHandler(handler: UnauthorizedHandler | undefined) {
  unauthorizedHandler = handler
}

type QueryValue = string | number | boolean | null | undefined

/** 统一构建查询串：跳过 undefined/null/空字符串/false，数组展开为重复参数，true 序列化为 'true' */
function toSearchParams(query: Record<string, QueryValue | QueryValue[]>): string {
  const parameters = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value == null || value === '' || value === false) continue
    if (Array.isArray(value)) {
      for (const item of value) {
        if (item != null && item !== '') parameters.append(key, String(item))
      }
      continue
    }
    parameters.set(key, String(value))
  }
  return parameters.toString()
}

/** 带 ? 前缀的查询串；无参数时返回空串 */
function querySuffix(query: Record<string, QueryValue | QueryValue[]>): string {
  const stringified = toSearchParams(query)
  return stringified ? `?${stringified}` : ''
}

function toExportParams(query: MonthlyExportQuery, format?: MonthlyExportFormat): string {
  return toSearchParams({
    startDate: query.startDate,
    endDate: query.endDate,
    scopeType: query.scopeType,
    taIds: query.taIds,
    programIds: query.programIds,
    format,
  })
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
    let response: Response
    try {
      response = await fetch(url, { ...options, headers })
    } catch (cause) {
      throw createClientNetworkError(cause)
    }
    const text = await response.text()
    const data = parseJsonBody(text, response.headers.get('content-type'))
    if (!response.ok) {
      if (response.status === 401) unauthorizedHandler?.()
      throw parseApiErrorResponse(response, data)
    }
    if (!text) {
      return undefined as T
    }
    if (data !== undefined) {
      return data as T
    }
    try {
      return JSON.parse(text) as T
    } catch (cause) {
      throw createClientNetworkError(cause)
    }
  }

  const refreshCsrf = async () => {
    csrf = await request<CsrfToken>('/api/v1/platform/auth/csrf')
  }

  return {
    listAuditLogs: (query) =>
      request<AuditLogPage>(`/api/v1/audit-logs?${toSearchParams({
        moduleCode: query.moduleCode,
        subjectType: query.subjectType,
        subjectId: query.subjectId,
        scopeStudyId: query.scopeStudyId,
        groupType: query.groupType,
        groupId: query.groupId,
        groupCode: query.groupCode,
        resultCode: query.resultCode,
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 20,
      })}`),
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
    listStudies: (query = {}) =>
      request<StudyPage>(`/api/v1/clinical-pipeline/studies?${toSearchParams({
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 10,
        therapeuticArea: query.therapeuticArea,
        program: query.program,
        product: query.product,
        studyCode: query.studyCode,
        milestoneStatus: query.milestoneStatus,
      })}`),
    listRisks: (query = {}) =>
      request<RiskPage>(`/api/v1/risk-management/risks?${toSearchParams({
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 10,
        sortBy: query.sortBy ?? 'updatedAt',
        sortOrder: query.sortOrder ?? 'desc',
        query: query.query,
        functionCode: query.functionCode,
        status: query.status,
        level: query.level,
        studyId: query.studyId,
        ownerUserId: query.ownerUserId,
        overdueOnly: query.overdueOnly,
      })}`),
    getRisk: (riskCode) => request<RiskDetail>(
      `/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}`),
    getRiskFormOptions: (studyId) => request<RiskFormOptions>(
      `/api/v1/risk-management/form-options${querySuffix({ studyId })}`),
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
      return request(`/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}${querySuffix({ expectedVersion })}`, { method: 'DELETE' })
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
      return request<RiskDetail>(`/api/v1/risk-management/risks/${encodeURIComponent(riskCode)}/actions/${actionId}${querySuffix({ expectedVersion })}`, { method: 'DELETE' })
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
    getProjectMilestones: (studyId) =>
      request<ProjectMilestonePage>(`/api/v1/studies/${studyId}/project-milestones`),
    async updateProjectMilestone(studyId, milestoneCode, input) {
      await refreshCsrf()
      return request<ProjectMilestonePage>(`/api/v1/studies/${studyId}/project-milestones/${encodeURIComponent(milestoneCode)}`, {
        method: 'PUT', body: JSON.stringify(input),
      })
    },
    getProjectStageProjection: (studyId) =>
      request<StageProjection>(`/api/v1/studies/${studyId}/project-milestones/stage-projection`),
    getMonthlyReports: (studyId, month) =>
      request<MonthlyReportPage>(
        `/api/v1/studies/${studyId}/monthly-reports?${toSearchParams({ month })}`,
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
        `/api/v1/studies/${studyId}/monthly-reports/history?${toSearchParams({ functionLineId, month })}`),
    previewMonthlyExport: (query) =>
      request<MonthlyExportReport>(`/api/v1/reports/monthly/preview?${toExportParams(query)}`),
    async downloadMonthlyExport(query, format) {
      let response: Response
      try {
        response = await fetch(
          `/api/v1/reports/monthly/export?${toExportParams(query, format)}`,
        )
      } catch (cause) {
        throw createClientNetworkError(cause)
      }
      if (!response.ok) {
        const text = await response.text()
        let body
        try {
          body = parseJsonBody(text, response.headers.get('content-type'))
        } catch (error) {
          throw error
        }
        if (response.status === 401) unauthorizedHandler?.()
        throw parseApiErrorResponse(response, body)
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
    listTeamMatrix: (query = {}) =>
      request<TeamMatrixPage>(`/api/v1/team-matrix?${toSearchParams({
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 10,
        studyQuery: query.studyQuery,
        roleQuery: query.roleQuery,
      })}`),
    getStudyTeam: (studyId) =>
      request<TeamMatrixPage>(`/api/v1/studies/${studyId}/team`),
    async replaceTeamAssignments(input) {
      await refreshCsrf()
      return request<TeamMatrixBatchResult>('/api/v1/team-matrix/assignments', {
        method: 'PUT',
        body: JSON.stringify(input),
      })
    },
    listPipelineConfig: (query = {}) =>
      request<PipelineConfigPage>(`/api/v1/clinical-pipeline/pipeline-config?${toSearchParams({
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 10,
        keyword: query.keyword,
      })}`),
    listTherapeuticAreas: () =>
      request<TherapeuticArea[]>('/api/v1/clinical-pipeline/therapeutic-areas'),
    listPrograms: (keyword = '') =>
      request<PipelineProgram[]>(`/api/v1/clinical-pipeline/programs${querySuffix({ keyword })}`),
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
    listProjects: (programId, keyword = '') =>
      request<PipelineProject[]>(`/api/v1/clinical-pipeline/projects${querySuffix({ programId, keyword })}`),
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
    async getStudyDeletePreview(id) {
      return request<StudyDeletePreview>(`/api/v1/clinical-pipeline/studies/${id}/delete-preview`)
    },
    async deleteStudyConfig(id) {
      await refreshCsrf()
      await request<void>(`/api/v1/clinical-pipeline/studies/${id}`, { method: 'DELETE' })
    },
    listUsers: (query = {}) =>
      request<UserPage>(`/api/v1/platform/users?${toSearchParams({
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 10,
        keyword: query.keyword,
        roleCode: query.roleCode,
      })}`),
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
    listRoles: (filters = {}) =>
      request<RolePage>(`/api/v1/platform/roles?${toSearchParams({
        page: filters.page ?? 1,
        pageSize: filters.pageSize ?? 10,
        keyword: filters.keyword,
        status: filters.status,
      })}`),
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
