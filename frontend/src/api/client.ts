import type {
  AssignRolesInput,
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
  Risk,
  ProgramInput,
  ProgramUpdateInput,
  ProjectInput,
  ProjectUpdateInput,
  RenameImpact,
  RoleInput,
  RolePage,
  RoleStatus,
  RoleUpdateResult,
  Study,
  StudyConfigInput,
  TeamAssignment,
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
  listRisks(): Promise<Risk[]>
  listMonthlyReports(month?: string): Promise<MonthlyReport[]>
  listTeamAssignments(): Promise<TeamAssignment[]>
  listPipelineConfig(): Promise<PipelineConfigRow[]>
  listTherapeuticAreas(): Promise<TherapeuticArea[]>
  listPrograms(keyword?: string): Promise<PipelineProgram[]>
  createProgram(input: ProgramInput): Promise<PipelineProgram>
  updateProgram(id: number, input: ProgramUpdateInput): Promise<PipelineProgram>
  previewProgramRename(id: number, newName: string): Promise<RenameImpact>
  deleteProgram(id: number): Promise<void>
  listProjects(programId?: number, keyword?: string): Promise<PipelineProject[]>
  createProject(input: ProjectInput): Promise<PipelineProject>
  updateProject(id: number, input: ProjectUpdateInput): Promise<PipelineProject>
  previewProjectRename(id: number, newName: string): Promise<RenameImpact>
  deleteProject(id: number): Promise<void>
  createStudyConfig(input: CreateStudyConfigInput): Promise<void>
  updateStudyConfig(id: number, input: StudyConfigInput): Promise<PipelineConfigRow>
  deleteStudyConfig(id: number): Promise<void>
  listUsers(keyword?: string, roleCode?: string): Promise<PlatformUser[]>
  createUser(input: CreateUserInput): Promise<void>
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
    listRisks: () => request<Risk[]>('/api/v1/risk-management/risks'),
    listMonthlyReports: (month) =>
      request<MonthlyReport[]>(
        `/api/v1/monthly-reports${month ? `?month=${encodeURIComponent(month)}` : ''}`,
      ),
    listTeamAssignments: () =>
      request<TeamAssignment[]>('/api/v1/team-assignments'),
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
    async previewProgramRename(id, newName) {
      await refreshCsrf()
      return request<RenameImpact>(`/api/v1/clinical-pipeline/programs/${id}/rename-impact`, {
        method: 'POST', body: JSON.stringify({ newName }),
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
    async previewProjectRename(id, newName) {
      await refreshCsrf()
      return request<RenameImpact>(`/api/v1/clinical-pipeline/projects/${id}/rename-impact`, {
        method: 'POST', body: JSON.stringify({ newName }),
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
