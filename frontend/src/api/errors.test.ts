import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  ApiError,
  CLIENT_NETWORK_ERROR_CODE,
  SYSTEM_ERROR_MESSAGE,
  formatApiError,
  shortRequestId,
} from './errors'

describe('formatApiError', () => {
  it('masks internal server errors with the unified system message', () => {
    expect(formatApiError(new ApiError('服务器内部错误', 500, 'INTERNAL_ERROR'))).toBe(
      SYSTEM_ERROR_MESSAGE,
    )
  })

  it('shows request id for system errors when available', () => {
    const requestId = '11111111-2222-3333-4444-555555555555'
    expect(formatApiError(new ApiError('服务器内部错误', 500, 'INTERNAL_ERROR', undefined, requestId)))
      .toBe(`${SYSTEM_ERROR_MESSAGE}（错误编号：${shortRequestId(requestId)}）`)
  })

  it('returns business messages for expected client errors', () => {
    expect(formatApiError(new ApiError('项目编号已存在', 409, 'STUDY_CODE_EXISTS')))
      .toBe('项目编号已存在')
  })

  it('enriches conflict details for business delete failures', () => {
    expect(formatApiError(new ApiError('Program 仍有关联数据，不能删除', 409, 'PROGRAM_IN_USE', {
      studyCount: '2',
      projectCount: '0',
    }))).toBe('Program 仍有关联数据，不能删除（studyCount: 2）')
  })

  it('does not expose native browser error messages', () => {
    expect(formatApiError(new TypeError('Failed to fetch'))).toBe(SYSTEM_ERROR_MESSAGE)
  })

  it('uses context fallback for non-system ApiError without message', () => {
    expect(formatApiError(new ApiError('', 400, 'VALIDATION_FAILED'), '字段校验失败'))
      .toBe('字段校验失败')
  })

  it('treats client network errors as system failures', () => {
    expect(formatApiError(new ApiError(SYSTEM_ERROR_MESSAGE, 0, CLIENT_NETWORK_ERROR_CODE)))
      .toBe(SYSTEM_ERROR_MESSAGE)
  })
})
