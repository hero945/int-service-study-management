import type {
  CsrfToken,
  CurrentUser,
  LoginCredentials,
  MonthlyReport,
  PipelineConfig,
  PipelineOverview,
  PlatformUser,
  Risk,
  Study,
  TeamAssignment,
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
  listUsers(): Promise<PlatformUser[]>
}

class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
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
      throw new ApiError(data?.message ?? `请求失败（${response.status}）`, response.status)
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
    listUsers: () => request<PlatformUser[]>('/api/v1/platform/users'),
  }
}

export const apiClient: ApiClient =
  import.meta.env.VITE_API_MODE === 'mock'
    ? createMockApiClient()
    : createHttpApiClient()
