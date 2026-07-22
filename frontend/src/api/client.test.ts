import { afterEach, describe, expect, it, vi } from 'vitest'
import { createHttpApiClient, setUnauthorizedHandler } from './client'

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
})
