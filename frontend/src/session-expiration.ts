interface SessionExpiredHandlerDependencies {
  isInitialized(): boolean
  isLoginPage(): boolean
  currentRoute(): string
  invalidate(): void
  redirectToLogin(redirect: string): void | Promise<unknown>
}

export function createSessionExpiredHandler(
  dependencies: SessionExpiredHandlerDependencies,
) {
  let redirecting = false

  return () => {
    if (!dependencies.isInitialized() || dependencies.isLoginPage() || redirecting) return

    redirecting = true
    dependencies.invalidate()
    try {
      void Promise.resolve(dependencies.redirectToLogin(dependencies.currentRoute()))
        .finally(() => {
          redirecting = false
        })
    } catch {
      redirecting = false
    }
  }
}
