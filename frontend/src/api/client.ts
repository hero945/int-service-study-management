import type {
  AssignRolesInput,
  CreateUserInput,
  CsrfToken,
  CurrentUser,
  LoginCredentials,
  MonthlyReport,
  PipelineConfig,
  PipelineOverview,
  PlatformUser,
  PlatformPermission,
  PlatformRole,
  Risk,
  RoleInput,
  RolePage,
  RoleStatus,
  RoleUpdateResult,
  Study,
  TeamAssignment,
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
  listPipelineConfig(): Promise<PipelineConfig[]>
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
  ) {
    super(message)
  }
}

function createHttpApiClient(): ApiClient {
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
      throw new ApiError(
        data?.message ?? `请求失败（${response.status}）`,
        response.status,
        data?.code,
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
      request<PipelineConfig[]>('/api/v1/pipeline-config'),
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
