import { readonly, ref } from 'vue'
import { apiClient } from './api/client'
import type { CurrentUser, LoginCredentials } from './api/types'

const currentUser = ref<CurrentUser>()
const initialized = ref(false)

async function restore() {
  if (initialized.value) return currentUser.value
  try {
    currentUser.value = await apiClient.getCurrentUser()
  } catch {
    currentUser.value = undefined
  } finally {
    initialized.value = true
  }
  return currentUser.value
}

async function login(credentials: LoginCredentials) {
  currentUser.value = await apiClient.login(credentials)
  initialized.value = true
  return currentUser.value
}

async function logout() {
  await apiClient.logout()
  invalidate()
}

function invalidate() {
  currentUser.value = undefined
  initialized.value = true
}

export const session = {
  currentUser: readonly(currentUser),
  initialized: readonly(initialized),
  restore,
  login,
  logout,
  invalidate,
}
