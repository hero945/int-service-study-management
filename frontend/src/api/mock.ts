import type { ApiClient } from './client'
import type {
  CurrentUser,
  PlatformPermission,
  PlatformRole,
  Study,
} from './types'

const users: Array<CurrentUser & { password: string }> = [
  {
    username: 'chen@eastchinapharm.com',
    displayName: '陈研发',
    title: '系统管理员',
    roles: ['ADMIN'],
    permissions: [
      'pipeline.page.view',
      'study.read',
      'config.create',
      'account.page.view',
      'account.create',
      'platform.setting.read',
      'platform.setting.update',
      'role.page.view',
      'role.create',
      'role.update',
      'role.delete',
    ],
    dataScope: 'ALL',
    password: '1234',
  },
  {
    username: 'zhangwei@eastchinapharm.com',
    displayName: '张伟',
    title: '项目负责人 · PL',
    roles: ['USER'],
    permissions: ['pipeline.page.view', 'study.read', 'config.create'],
    dataScope: 'ALL',
    password: '1234',
  },
  {
    username: 'liuyang@eastchinapharm.com',
    displayName: '刘洋',
    title: '质量观察员',
    roles: ['VIEWER'],
    permissions: ['pipeline.page.view', 'study.read'],
    dataScope: 'ALL',
    password: '1234',
  },
]

export const demoStudies: Study[] = [
  {
    id: 1,
    code: 'HDM2020-001',
    name: 'HDM2020 项目首次人体研究',
    indication: '晚期实体瘤',
    phase: 'PreIND',
    status: 'ACTIVE',
    statusLabel: '准备中',
    statusTone: 'neutral',
    ownerName: '张伟',
    startDate: '2026-02-01',
    updatedAt: '2026-07-15T09:20:00',
    therapeuticArea: '肿瘤',
    therapeuticAreaEn: 'Oncology',
    product: 'HDM2020',
    program: 'HDM2020',
    project: 'HDM2020-1',
    moa: 'ADC',
    source: '自研',
    origin: '中国',
  },
  {
    id: 2,
    code: 'HDM2015-102',
    name: 'HDM2015 适应症探索研究',
    indication: '系统性红斑狼疮',
    phase: 'IND',
    status: 'ACTIVE',
    statusLabel: '已递交',
    statusTone: 'info',
    ownerName: '王芳',
    startDate: '2025-11-18',
    updatedAt: '2026-07-14T16:40:00',
    therapeuticArea: '自身免疫',
    therapeuticAreaEn: 'Autoimmune Disease',
    product: 'HDM2015',
    program: 'HDM2015',
    project: 'HDM2015-1',
    moa: 'Small Molecule',
    source: '合作',
    origin: '中国',
  },
  {
    id: 3,
    code: 'HDM1005-302',
    name: 'HDM1005 随机对照研究',
    indication: '2 型糖尿病',
    phase: 'Phase 1',
    status: 'ACTIVE',
    statusLabel: 'FPI',
    statusTone: 'info',
    ownerName: '李静',
    startDate: '2025-05-06',
    updatedAt: '2026-07-12T13:10:00',
    therapeuticArea: '代谢与心血管',
    therapeuticAreaEn: 'Metabolic & Cardiovascular',
    product: 'HDM1005',
    program: 'HDM1005',
    project: 'HDM1005-3',
    moa: 'Peptide',
    source: '自研',
    origin: '中国',
  },
]

export function createMockApiClient(): ApiClient {
  let currentUser: CurrentUser | undefined
  const permissions: PlatformPermission[] = [
    ['pipeline', 'pipeline.page.view', '查看管线总览', 'PAGE', 'view'],
    ['study', 'study.read', '查看 Study', 'ACTION', 'read'],
    ['config', 'config.create', '维护管线配置', 'ACTION', 'create'],
    ['account', 'account.page.view', '查看账号管理', 'PAGE', 'view'],
    ['account', 'account.create', '新增账号', 'ACTION', 'create'],
    ['role', 'role.page.view', '查看角色权限管理', 'PAGE', 'view'],
    ['role', 'role.create', '新增角色', 'ACTION', 'create'],
    ['role', 'role.update', '编辑角色权限', 'ACTION', 'update'],
    ['role', 'role.delete', '删除角色', 'ACTION', 'delete'],
  ].map(([moduleCode, permissionCode, permissionName, permissionType, actionCode], index) => ({
    id: index + 1,
    moduleCode,
    permissionCode,
    permissionName,
    permissionType,
    actionCode,
    permissionDescription: null,
    sortOrder: (index + 1) * 10,
  }))
  let nextRoleId = 4
  const roles: PlatformRole[] = [
    {
      id: 1,
      roleCode: 'ADMIN',
      roleDescription: '系统管理员',
      dataScopeMode: 'ALL',
      status: 'ACTIVE',
      systemRole: true,
      assignedUserCount: 1,
      permissionCodes: permissions.map((permission) => permission.permissionCode),
      updatedAt: '2026-07-21T09:00:00',
    },
    {
      id: 2,
      roleCode: 'USER',
      roleDescription: '普通业务成员',
      dataScopeMode: 'ALL',
      status: 'ACTIVE',
      systemRole: true,
      assignedUserCount: 1,
      permissionCodes: ['pipeline.page.view', 'study.read', 'config.create'],
      updatedAt: '2026-07-21T09:00:00',
    },
    {
      id: 3,
      roleCode: 'VIEWER',
      roleDescription: '只读成员',
      dataScopeMode: 'ASSIGNED_STUDY',
      status: 'ACTIVE',
      systemRole: true,
      assignedUserCount: 1,
      permissionCodes: ['pipeline.page.view', 'study.read'],
      updatedAt: '2026-07-21T09:00:00',
    },
  ]

  return {
    async getCurrentUser() {
      if (!currentUser) throw new Error('请先登录')
      return currentUser
    },
    async login(credentials) {
      const account = users.find(
        (item) =>
          item.username === credentials.username &&
          item.password === credentials.password,
      )
      if (!account) throw new Error('账号或密码错误')
      const { password: _password, ...user } = account
      currentUser = user
      return user
    },
    async logout() {
      currentUser = undefined
    },
    async getPipelineOverview() {
      return {
        title: '临床研发管线',
        total: demoStudies.length,
        statuses: [
          { status: 'ATTENTION', label: '需关注', tone: 'warning', count: 1 },
          { status: 'APPROVED', label: '已获批', tone: 'positive', count: 0 },
          { status: 'UPDATED', label: '本月有更新', tone: 'info', count: 3 },
        ],
      }
    },
    async listStudies() {
      return demoStudies
    },
    async listRisks() {
      return [
        {
          id: 'RSK-001',
          studyCode: 'HDM1005-302',
          program: 'HDM1005',
          functionName: '注册',
          description: '监管沟通窗口可能影响计划节点',
          owner: '王芳',
          severity: '中',
          status: 'Open',
        },
      ]
    },
    async listMonthlyReports() {
      return []
    },
    async listTeamAssignments() {
      return []
    },
    async listPipelineConfig() {
      return demoStudies.map((study) => ({
        key: `${study.program}|${study.code}`,
        source: study.source ?? '',
        origin: study.origin ?? '',
        product: study.product ?? '',
        moa: study.moa ?? '',
        program: study.program ?? '',
        indication: study.indication,
        project: study.project ?? '',
        therapeuticArea: study.therapeuticArea ?? '',
        studyCode: study.code,
        projectStatus: study.statusLabel,
        phaseStatus: study.phase,
      }))
    },
    async listUsers() {
      return users.map((user, index) => ({
        id: index + 1,
        username: user.username,
        displayName: user.displayName,
        roles: user.roles,
        enabled: true,
      }))
    },
    async listRoles(filters = {}) {
      const keyword = filters.keyword?.trim().toLowerCase() ?? ''
      const filtered = roles.filter((role) =>
        (!keyword || role.roleCode.toLowerCase().includes(keyword) ||
          role.roleDescription?.toLowerCase().includes(keyword)) &&
        (!filters.status || role.status === filters.status),
      )
      const page = filters.page ?? 1
      const pageSize = filters.pageSize ?? 20
      const start = (page - 1) * pageSize
      return {
        data: filtered.slice(start, start + pageSize),
        page,
        pageSize,
        totalItems: filtered.length,
        totalPages: Math.ceil(filtered.length / pageSize),
      }
    },
    async listPermissions() {
      return permissions
    },
    async createRole(input) {
      if (roles.some((role) => role.roleCode === input.roleCode)) {
        throw new Error('角色编码已存在且不可复用')
      }
      const role: PlatformRole = {
        id: nextRoleId++,
        roleCode: input.roleCode ?? '',
        roleDescription: input.roleDescription,
        dataScopeMode: input.dataScopeMode,
        status: 'ACTIVE',
        systemRole: false,
        assignedUserCount: 0,
        permissionCodes: [...input.permissionCodes],
        updatedAt: new Date().toISOString(),
      }
      roles.push(role)
      return role
    },
    async updateRole(roleId, input) {
      const role = roles.find((item) => item.id === roleId)
      if (!role) throw new Error('角色不存在')
      role.roleDescription = input.roleDescription
      role.dataScopeMode = input.dataScopeMode
      role.status = input.status ?? role.status
      role.permissionCodes = [...input.permissionCodes]
      role.updatedAt = new Date().toISOString()
      return { role, invalidatedUserCount: role.assignedUserCount, currentSessionInvalidated: false }
    },
    async deleteRole(roleId) {
      const index = roles.findIndex((role) => role.id === roleId)
      if (index < 0) throw new Error('角色不存在')
      if (roles[index].systemRole) throw new Error('系统角色不可删除')
      if (roles[index].assignedUserCount) throw new Error('角色仍关联用户，不能删除')
      roles.splice(index, 1)
    },
  }
}
