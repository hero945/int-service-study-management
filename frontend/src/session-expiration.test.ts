import { describe, expect, it, vi } from 'vitest'
import { createSessionExpiredHandler } from './session-expiration'

describe('session expiration navigation', () => {
  it('clears an initialized session and preserves the current route', () => {
    const invalidate = vi.fn()
    const redirectToLogin = vi.fn()
    const handler = createSessionExpiredHandler({
      isInitialized: () => true,
      isLoginPage: () => false,
      currentRoute: () => '/config?view=project#editor',
      invalidate,
      redirectToLogin,
    })

    handler()

    expect(invalidate).toHaveBeenCalledTimes(1)
    expect(redirectToLogin).toHaveBeenCalledWith('/config?view=project#editor')
  })

  it('ignores initial session restoration and login-page authentication failures', () => {
    const invalidate = vi.fn()
    const redirectToLogin = vi.fn()
    const initialRestoreHandler = createSessionExpiredHandler({
      isInitialized: () => false,
      isLoginPage: () => false,
      currentRoute: () => '/pipeline',
      invalidate,
      redirectToLogin,
    })
    const loginPageHandler = createSessionExpiredHandler({
      isInitialized: () => true,
      isLoginPage: () => true,
      currentRoute: () => '/login',
      invalidate,
      redirectToLogin,
    })

    initialRestoreHandler()
    loginPageHandler()

    expect(invalidate).not.toHaveBeenCalled()
    expect(redirectToLogin).not.toHaveBeenCalled()
  })

  it('coalesces concurrent 401 responses into one navigation', async () => {
    let finishNavigation: (() => void) | undefined
    const redirectToLogin = vi.fn(() => new Promise<void>((resolve) => {
      finishNavigation = resolve
    }))
    const handler = createSessionExpiredHandler({
      isInitialized: () => true,
      isLoginPage: () => false,
      currentRoute: () => '/studies',
      invalidate: vi.fn(),
      redirectToLogin,
    })

    handler()
    handler()

    expect(redirectToLogin).toHaveBeenCalledTimes(1)
    finishNavigation?.()
    await Promise.resolve()
  })
})
