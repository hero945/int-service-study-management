import { computed, type ComputedRef } from 'vue'
import type { PermissionCode } from '../api/types'
import { session } from '../session'

export interface UsePermissionsReturn {
  /** 当前用户权限码列表（未登录为空数组） */
  permissions: ComputedRef<readonly PermissionCode[]>
  /** 生成一个权限检查 computed：`const canCreate = can('account.create')` */
  can: (code: PermissionCode) => ComputedRef<boolean>
}

/**
 * 统一的权限检查入口，替换各视图里重复的
 * `computed(() => session.currentUser.value?.permissions ?? [])` 样板。
 */
export function usePermissions(): UsePermissionsReturn {
  const permissions = computed(() => session.currentUser.value?.permissions ?? [])
  const can = (code: PermissionCode) => computed(() => permissions.value.includes(code))
  return { permissions, can }
}
