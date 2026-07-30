export const REQUEST_ID_HEADER = 'X-Request-ID'
export const SYSTEM_ERROR_MESSAGE = '系统暂时不可用，请稍后重试'
export const CLIENT_NETWORK_ERROR_CODE = 'CLIENT_NETWORK_ERROR'

export interface ApiErrorResponse {
  code: string
  message: string
  details?: Record<string, string>
  timestamp?: string
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
    readonly details?: Record<string, string>,
    readonly requestId?: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export function shortRequestId(requestId: string): string {
  return requestId.replace(/-/g, '').slice(0, 8).toUpperCase()
}

export function isSystemError(error: ApiError): boolean {
  return (
    error.code === 'INTERNAL_ERROR'
    || error.code === CLIENT_NETWORK_ERROR_CODE
    || error.status >= 500
    || error.status === 0
  )
}

function enrichBusinessMessage(error: ApiError): string | undefined {
  if (error.status === 409 && error.details) {
    const counts = Object.entries(error.details)
      .filter(([, value]) => value !== '0')
      .map(([key, value]) => `${key}: ${value}`)
      .join('，')
    return `${error.message}${counts ? `（${counts}）` : ''}`
  }
  return undefined
}

function formatSystemMessage(requestId?: string): string {
  return requestId
    ? `${SYSTEM_ERROR_MESSAGE}（错误编号：${shortRequestId(requestId)}）`
    : SYSTEM_ERROR_MESSAGE
}

export function formatApiError(reason: unknown, contextFallback?: string): string {
  if (reason instanceof ApiError) {
    if (isSystemError(reason)) {
      return formatSystemMessage(reason.requestId)
    }
    return enrichBusinessMessage(reason)
      ?? (reason.message?.trim() ? reason.message : undefined)
      ?? contextFallback
      ?? SYSTEM_ERROR_MESSAGE
  }
  if (import.meta.env.DEV && reason instanceof Error) {
    console.debug('[api] unexpected client error', reason)
  }
  return SYSTEM_ERROR_MESSAGE
}

export function createClientNetworkError(cause?: unknown): ApiError {
  if (import.meta.env.DEV && cause instanceof Error) {
    console.debug('[api] network or parse failure', cause)
  }
  return new ApiError(SYSTEM_ERROR_MESSAGE, 0, CLIENT_NETWORK_ERROR_CODE)
}

export function parseApiErrorResponse(
  response: Response,
  body: ApiErrorResponse | undefined,
): ApiError {
  const requestId = response.headers.get(REQUEST_ID_HEADER) ?? undefined
  return new ApiError(
    body?.message ?? SYSTEM_ERROR_MESSAGE,
    response.status,
    body?.code,
    body?.details,
    requestId,
  )
}

export function parseJsonBody(text: string, contentType: string | null): ApiErrorResponse | undefined {
  if (!text) {
    return undefined
  }
  const looksJson = contentType?.includes('json') || text.trimStart().startsWith('{')
  if (!looksJson) {
    return undefined
  }
  try {
    return JSON.parse(text) as ApiErrorResponse
  } catch (cause) {
    throw createClientNetworkError(cause)
  }
}
