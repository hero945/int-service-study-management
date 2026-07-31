export type NavIconName =
  | 'pipeline'
  | 'study'
  | 'risk'
  | 'team'
  | 'export'
  | 'config'
  | 'account'
  | 'role'

export interface NavigationItem {
  name: string
  label: string
  path: string
  permission: string
  icon: NavIconName
}

export interface NavigationGroup {
  label: string
  items: NavigationItem[]
}

export const navigationGroups: NavigationGroup[] = [
  {
    label: '研发工作',
    items: [
      { name: 'pipeline', label: '管线总览', path: '/pipeline', permission: 'pipeline.page.view', icon: 'pipeline' },
      { name: 'studies', label: '研究 Study', path: '/studies', permission: 'study.read', icon: 'study' },
      { name: 'risks', label: '风险管理', path: '/risks', permission: 'risk.page.view', icon: 'risk' },
      { name: 'team', label: '团队矩阵', path: '/team', permission: 'team.page.view', icon: 'team' },
    ],
  },
  {
    label: '月报运营',
    items: [
      { name: 'reports', label: '月报导出', path: '/reports', permission: 'report.page.view', icon: 'export' },
    ],
  },
  {
    label: '平台管理',
    items: [
      { name: 'config', label: '管线配置', path: '/config', permission: 'config.page.view', icon: 'config' },
      { name: 'accounts', label: '账号管理', path: '/accounts', permission: 'account.page.view', icon: 'account' },
      { name: 'roles', label: '角色权限', path: '/roles', permission: 'role.page.view', icon: 'role' },
    ],
  },
]

export function visibleNavigationGroups(permissions: readonly string[]): NavigationGroup[] {
  const allowed = new Set(permissions)
  return navigationGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => allowed.has(item.permission)),
    }))
    .filter((group) => group.items.length > 0)
}

export function firstAllowedHome(permissions: readonly string[]): string {
  return visibleNavigationGroups(permissions)[0]?.items[0]?.name ?? 'login'
}
