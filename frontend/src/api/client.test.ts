import { afterEach, describe, expect, it, vi } from 'vitest'
import { createHttpApiClient, setUnauthorizedHandler } from './client'
import { CLIENT_NETWORK_ERROR_CODE, SYSTEM_ERROR_MESSAGE } from './errors'

afterEach(() => {
  vi.unstubAllGlobals()
  setUnauthorizedHandler(undefined)
})

describe('HTTP API authentication failures', () => {
  it('notifies the application when a request returns 401', async () => {
    const onUnauthorized = vi.fn()
    setUnauthorizedHandler(onUnauthorized)
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ code: 'UNAUTHENTICATED', message: '请先登录' }),
      { status: 401, headers: { 'Content-Type': 'application/json' } },
    )))

    await expect(createHttpApiClient().listStudies()).rejects.toMatchObject({
      status: 401,
      code: 'UNAUTHENTICATED',
    })
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })

  it('does not treat 403 as an expired session', async () => {
    const onUnauthorized = vi.fn()
    setUnauthorizedHandler(onUnauthorized)
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ code: 'ACCESS_DENIED', message: '无权执行此操作' }),
      { status: 403, headers: { 'Content-Type': 'application/json' } },
    )))

    await expect(createHttpApiClient().listStudies()).rejects.toMatchObject({
      status: 403,
      code: 'ACCESS_DENIED',
    })
    expect(onUnauthorized).not.toHaveBeenCalled()
  })

  it('maps network failures to a client-side system error', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    await expect(createHttpApiClient().listStudies()).rejects.toMatchObject({
      status: 0,
      code: CLIENT_NETWORK_ERROR_CODE,
      message: SYSTEM_ERROR_MESSAGE,
    })
  })

  it('maps malformed json responses to a client-side system error', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      '{not-json',
      { status: 500, headers: { 'Content-Type': 'application/json' } },
    )))

    await expect(createHttpApiClient().listStudies()).rejects.toMatchObject({
      status: 0,
      code: CLIENT_NETWORK_ERROR_CODE,
      message: SYSTEM_ERROR_MESSAGE,
    })
  })

  it('preserves request id from internal server error responses', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      JSON.stringify({
        code: 'INTERNAL_ERROR',
        message: '系统暂时不可用，请稍后重试',
      }),
      {
        status: 500,
        headers: {
          'Content-Type': 'application/json',
          'X-Request-ID': '11111111-2222-3333-4444-555555555555',
        },
      },
    )))

    await expect(createHttpApiClient().listStudies()).rejects.toMatchObject({
      status: 500,
      code: 'INTERNAL_ERROR',
      requestId: '11111111-2222-3333-4444-555555555555',
    })
  })
})
