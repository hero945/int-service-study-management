import type { ApiClient } from './client'
import type {
  AssignRolesInput,
  CreateUserInput,
  CurrentUser,
  PipelineProgram,
  PipelineProject,
  PlatformPermission,
  PlatformRole,
  Study,
  TeamMatrixAssignment,
  TeamMatrixRole,
  TherapeuticArea,
  UpdateUserInput,
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
      'config.page.view',
      'config.create',
      'config.update',
      'config.delete',
      'account.page.view',
      'account.create',
      'platform.setting.read',
      'platform.setting.update',
      'role.page.view',
      'role.create',
      'role.update',
      'role.delete',
      'team.page.view',
      'team.edit_mode',
      'team.update',
    ],
    dataScope: 'ALL',
    password: '1234',
  },
  {
    username: 'zhangwei@eastchinapharm.com',
    displayName: '张伟',
    title: '项目负责人 · PL',
    roles: ['USER'],
    permissions: ['pipeline.page.view', 'study.read'],
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

const teamRoles: TeamMatrixRole[] = [
  ['PL', 'PL 项目负责人', 'PM', '项目管理'],
  ['APL', 'APL 副项目负责人', 'PM', '项目管理'],
  ['PM', 'PM 项目经理', 'PM', '项目管理'],
  ['APM', 'APM 副项目经理', 'PM', '项目管理'],
  ['RA_SPONSOR', 'RA Sponsor', 'RA', '注册'],
  ['RA_MANAGER', 'RA Manager', 'RA', '注册'],
  ['RA_SPECIALIST', 'RA Specialist', 'RA', '注册'],
  ['RA_CMC', 'RA CMC', 'RA', '注册'],
  ['CM_SPONSOR', 'CM Sponsor', 'CM', '临床医学'],
  ['CM', 'CM', 'CM', '临床医学'],
  ['CP_SPONSOR', 'CP Sponsor', 'CP', '临床药理'],
  ['CP', 'CP', 'CP', '临床药理'],
  ['PV_SPONSOR', 'PV Sponsor', 'PV', '药物警戒'],
  ['PVP', 'PVP', 'PV', '药物警戒'],
  ['PVO', 'PVO', 'PV', '药物警戒'],
  ['TM_SPONSOR', 'TM Sponsor', 'TM', '试验管理'],
  ['TM', 'TM', 'TM', '试验管理'],
  ['CO_SPONSOR', 'CO Sponsor', 'CO', '临床运营'],
  ['CTM', 'CTM', 'CO', '临床运营'],
  ['ACTM', 'ACTM', 'CO', '临床运营'],
  ['LAB', 'Lab', 'LAB', '中心实验室'],
  ['LAB_BACKUP', 'Lab backup', 'LAB', '中心实验室'],
  ['SUPPLY', 'Supply', 'SUPPLY', '供应保障'],
  ['SUPPLY_BACKUP', 'Supply backup', 'SUPPLY', '供应保障'],
  ['CTA_PROCESS', 'CTA process', 'CTA', '临床试验协调'],
  ['CTA_TMF', 'CTA TMF', 'CTA', '临床试验协调'],
  ['ST_SPONSOR', 'ST Sponsor', 'ST', '生物统计'],
  ['ST', 'ST', 'ST', '生物统计'],
  ['PG_SPONSOR', 'PG Sponsor', 'PG', '统计编程'],
  ['PG', 'PG', 'PG', '统计编程'],
  ['DM_SPONSOR', 'DM Sponsor', 'DM', '数据管理'],
  ['DM', 'DM', 'DM', '数据管理'],
  ['MW', 'MW', 'MW', '医学写作'],
  ['NC_CONTACT', 'NC-contact', 'NC', '非临床'],
  ['NC_PK', 'NC-PK', 'NC', '非临床'],
  ['NC_PD', 'NC-PD', 'NC', '非临床'],
  ['NC_TOX', 'NC-TOX', 'NC', '非临床'],
  ['CMC_PL', 'CMC-PL', 'CMC', '药学CMC'],
  ['CMC_PM', 'CMC-PM', 'CMC', '药学CMC'],
  ['CMC_DS', 'CMC-DS', 'CMC', '药学CMC'],
  ['CMC_DP', 'CMC-DP', 'CMC', '药学CMC'],
  ['CMC_OA', 'CMC-OA', 'CMC', '药学CMC'],
  ['CMC_RA', 'CMC-RA', 'CMC', '药学CMC'],
  ['IP', 'IP', 'IP', '药品管理'],
].map(([roleCode, roleName, functionCode, functionName]) => ({
  roleCode, roleName, functionCode, functionName,
}))

export function createMockApiClient(): ApiClient {
  let currentUser: CurrentUser | undefined
  const teamVersions = new Map(demoStudies.map((study) => [study.id, 0]))
  const teamAssignments = new Map<string, number[]>([
    [`${demoStudies[0].id}|PL`, [2]],
    [`${demoStudies[1].id}|PM`, [2]],
  ])
  const permissions: PlatformPermission[] = [
    ['pipeline', 'pipeline.page.view', '查看管线总览', 'PAGE', 'view'],
    ['study', 'study.read', '查看 Study', 'ACTION', 'read'],
    ['config', 'config.page.view', '查看管线配置', 'PAGE', 'view'],
    ['config', 'config.create', '维护管线配置', 'ACTION', 'create'],
    ['config', 'config.update', '修改管线配置', 'ACTION', 'update'],
    ['config', 'config.delete', '删除管线配置', 'ACTION', 'delete'],
    ['account', 'account.page.view', '查看账号管理', 'PAGE', 'view'],
    ['account', 'account.create', '新增账号', 'ACTION', 'create'],
    ['role', 'role.page.view', '查看角色权限管理', 'PAGE', 'view'],
    ['role', 'role.create', '新增角色', 'ACTION', 'create'],
    ['role', 'role.update', '编辑角色权限', 'ACTION', 'update'],
    ['role', 'role.delete', '删除角色', 'ACTION', 'delete'],
    ['team', 'team.page.view', '查看团队矩阵', 'PAGE', 'view'],
    ['team', 'team.edit_mode', '进入团队编辑模式', 'PAGE_OPERATION', 'edit_mode'],
    ['team', 'team.update', '更新团队分配', 'ACTION', 'update'],
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
      permissionCodes: ['pipeline.page.view', 'study.read'],
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
  const now = () => new Date().toISOString()
  let nextProgramId = 4
  let nextProjectId = 4
  let nextStudyId = 4
  const programs: PipelineProgram[] = demoStudies.map((study, index) => ({
    id: index + 1,
    code: study.program ?? '',
    name: `${study.program ?? ''} Program`,
    productName: study.product ?? '',
    moa: study.moa ?? null,
    sourceCode: study.source === '合作' ? 'COOPERATION' : 'SELF_DEVELOPED',
    sourceLabel: study.source ?? '',
    originCode: 'DOMESTIC',
    originLabel: study.origin ?? '',
    projectCount: 1,
    studyCount: 1,
    updatedAt: study.updatedAt,
  }))
  const projects: PipelineProject[] = demoStudies.map((study, index) => ({
    id: index + 1,
    code: study.project ?? '',
    name: `${study.project ?? ''} Project`,
    programId: index + 1,
    programCode: study.program ?? '',
    indication: study.indication,
    therapeuticAreaId: index + 1,
    therapeuticAreaCode: study.therapeuticAreaEn?.toUpperCase().replaceAll(' ', '_') ?? 'OTHER',
    therapeuticAreaName: study.therapeuticArea ?? '',
    studyCount: 1,
    updatedAt: study.updatedAt,
  }))
  const therapeuticAreas: TherapeuticArea[] = [
    { id: 1, code: 'ONCOLOGY', name: '肿瘤', englishName: 'Oncology' },
    { id: 2, code: 'AUTOIMMUNE', name: '自身免疫', englishName: 'Autoimmune Disease' },
    { id: 3, code: 'METABOLIC_CARDIOVASCULAR', name: '代谢与心血管', englishName: 'Metabolic and Cardiovascular' },
    { id: 4, code: 'RESPIRATORY', name: '呼吸系统', englishName: 'Respiratory' },
    { id: 5, code: 'INFECTIOUS_DISEASE', name: '感染性疾病', englishName: 'Infectious Disease' },
    { id: 6, code: 'NEUROSCIENCE', name: '神经科学', englishName: 'Neuroscience' },
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
    async listTeamMatrix(query = {}) {
      const studyQuery = query.studyQuery?.trim().toLowerCase() ?? ''
      const roleQuery = query.roleQuery?.trim().toLowerCase() ?? ''
      const page = query.page ?? 1
      const pageSize = query.pageSize ?? 20
      const filteredStudies = demoStudies.filter(study =>
        !studyQuery ||
        study.code.toLowerCase().includes(studyQuery) ||
        study.indication.toLowerCase().includes(studyQuery))
      const roles = teamRoles.filter(role =>
        !roleQuery ||
        role.roleCode.toLowerCase().includes(roleQuery) ||
        role.roleName.toLowerCase().includes(roleQuery) ||
        (role.functionName ?? '').toLowerCase().includes(roleQuery))
      const studies = filteredStudies.slice((page - 1) * pageSize, page * pageSize)
        .map(study => ({
          studyId: study.id,
          studyCode: study.code,
          indication: study.indication,
          statusCode: study.status,
          statusLabel: study.statusLabel,
          version: teamVersions.get(study.id) ?? 0,
        }))
      const assignments: TeamMatrixAssignment[] = []
      for (const study of studies) {
        for (const role of roles) {
          const ids = teamAssignments.get(`${study.studyId}|${role.roleCode}`) ?? []
          const members = ids.map(id => users[id - 1]).filter(Boolean).map((member, index) => ({
            userId: ids[index],
            email: member.username,
            displayName: member.displayName,
            enabled: true,
          }))
          if (members.length) assignments.push({
            studyId: study.studyId,
            roleCode: role.roleCode,
            members,
          })
        }
      }
      return {
        studies,
        roles,
        assignments,
        totalRoles: roles.length,
        pagination: {
          page,
          pageSize,
          totalItems: filteredStudies.length,
          totalPages: Math.max(1, Math.ceil(filteredStudies.length / pageSize)),
        },
      }
    },
    async replaceTeamAssignments(input) {
      for (const study of input.studies) {
        const currentVersion = teamVersions.get(study.studyId) ?? 0
        if (currentVersion !== study.expectedVersion) {
          throw new Error('团队矩阵已被其他用户修改，请刷新后重试')
        }
        for (const role of study.roles) {
          teamAssignments.set(`${study.studyId}|${role.roleCode}`, [...role.userIds])
        }
        teamVersions.set(study.studyId, currentVersion + 1)
      }
      return {
        studies: input.studies.map(study => ({
          studyId: study.studyId,
          version: teamVersions.get(study.studyId) ?? study.expectedVersion,
        })),
      }
    },
    async listPipelineConfig() {
      return demoStudies.map((study) => {
        const project = projects.find((item) => item.code === study.project)!
        const program = programs.find((item) => item.id === project.programId)!
        return {
        studyId: study.id,
        studyCode: study.code,
        studyName: study.name,
        phaseStatusCode: study.phase.toUpperCase().replaceAll(' ', '_'),
        phaseStatusLabel: study.phase,
        projectId: project.id,
        projectCode: project.code,
        projectName: project.name,
        indication: project.indication,
        therapeuticAreaCode: project.therapeuticAreaCode,
        therapeuticAreaName: project.therapeuticAreaName,
        programId: program.id,
        programCode: program.code,
        programName: program.name,
        productName: program.productName,
        moa: program.moa,
        sourceCode: program.sourceCode,
        sourceLabel: program.sourceLabel,
        originCode: program.originCode,
        originLabel: program.originLabel,
        updatedAt: study.updatedAt,
      }})
    },
    async listTherapeuticAreas() {
      return therapeuticAreas
    },
    async listPrograms(keyword = '') {
      const query = keyword.trim().toLowerCase()
      return programs.filter((item) => !query || [item.code, item.name, item.productName]
        .some((value) => value.toLowerCase().includes(query)))
    },
    async createProgram(input) {
      if (programs.some((item) => item.code === input.code)) throw new Error('Program 编码已存在')
      const program: PipelineProgram = {
        id: nextProgramId++, code: input.code, name: input.code, productName: input.productName,
        moa: input.moa ?? null, sourceCode: input.sourceCode,
        sourceLabel: input.sourceCode === 'SELF_DEVELOPED' ? '自研' : input.sourceCode === 'IN_LICENSE' ? '引进' : '合作',
        originCode: input.originCode, originLabel: input.originCode === 'DOMESTIC' ? '国产' : '进口',
        projectCount: 0, studyCount: 0, updatedAt: now(),
      }
      programs.push(program)
      return program
    },
    async updateProgram(id, input) {
      const program = programs.find((item) => item.id === id)
      if (!program) throw new Error('Program 不存在')
      Object.assign(program, input, { updatedAt: now() })
      return program
    },
    async previewProgramRename(id) {
      const program = programs.find((item) => item.id === id)
      if (!program) throw new Error('Program 不存在')
      return { projectCount: program.projectCount, studyCount: program.studyCount, expectedUpdatedAt: program.updatedAt }
    },
    async deleteProgram(id) {
      const index = programs.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Program 不存在')
      if (programs[index].projectCount) throw new Error('Program 仍有关联 Project，不能删除')
      programs.splice(index, 1)
    },
    async listProjects(programId, keyword = '') {
      const query = keyword.trim().toLowerCase()
      return projects.filter((item) => (!programId || item.programId === programId) &&
        (!query || item.code.toLowerCase().includes(query) || item.name.toLowerCase().includes(query)))
    },
    async createProject(input) {
      if (projects.some((item) => item.code === input.code)) throw new Error('Project 编码已存在')
      const program = programs.find((item) => item.id === input.programId)
      if (!program) throw new Error('Program 不存在')
      const project: PipelineProject = {
        id: nextProjectId++, code: input.code, name: input.code, programId: input.programId,
        programCode: program.code, indication: input.indication, therapeuticAreaId: 99,
        therapeuticAreaCode: input.therapeuticAreaCode,
        therapeuticAreaName: therapeuticAreas.find((item) => item.code === input.therapeuticAreaCode)?.name ?? input.therapeuticAreaCode,
        studyCount: 0, updatedAt: now(),
      }
      projects.push(project)
      program.projectCount++
      return project
    },
    async updateProject(id, input) {
      const project = projects.find((item) => item.id === id)
      if (!project) throw new Error('Project 不存在')
      Object.assign(project, input, { updatedAt: now() })
      return project
    },
    async previewProjectRename(id) {
      const project = projects.find((item) => item.id === id)
      if (!project) throw new Error('Project 不存在')
      return { projectCount: 0, studyCount: project.studyCount, expectedUpdatedAt: project.updatedAt }
    },
    async deleteProject(id) {
      const index = projects.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Project 不存在')
      if (projects[index].studyCount) throw new Error('Project 仍有关联 Study，不能删除')
      projects.splice(index, 1)
    },
    async createStudyConfig(input) {
      const project = projects.find((item) => item.id === input.projectId)
      if (!project) throw new Error('Project 不存在')
      demoStudies.push({ id: nextStudyId++, code: input.code, name: input.name,
        indication: project.indication, phase: input.phase, status: 'ACTIVE', statusLabel: '进行中',
        statusTone: 'info', ownerName: '', startDate: null, updatedAt: now(),
        program: project.programCode, project: project.code,
        therapeuticArea: project.therapeuticAreaName,
        product: programs.find((item) => item.id === project.programId)?.productName })
      project.studyCount++
    },
    async updateStudyConfig(id, input) {
      const study = demoStudies.find((item) => item.id === id)
      const project = projects.find((item) => item.id === input.projectId)
      if (!study || !project) throw new Error('Study 或 Project 不存在')
      study.name = input.name
      study.phase = input.phaseStatusCode
      study.project = project.code
      study.program = project.programCode
      return (await this.listPipelineConfig()).find((item) => item.studyId === id)!
    },
    async deleteStudyConfig(id) {
      const index = demoStudies.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('Study 不存在')
      demoStudies.splice(index, 1)
    },
    async listUsers(keyword = '', roleCode = '') {
      let filtered = users.map((user, index) => ({
        id: index + 1,
        username: user.username,
        displayName: user.displayName,
        roles: user.roles,
        roleDescriptions: user.roles.map(r => {
          switch (r) {
            case 'ADMIN': return '系统管理员'
            case 'USER': return '项目负责人'
            case 'VIEWER': return '只读成员'
            default: return r
          }
        }),
        dataScope: user.dataScope,
        visibleStudyCount: user.roles.includes('ADMIN') ? 3 : (user.roles.includes('USER') ? 2 : 1),
        enabled: true,
      }))
      if (keyword) {
        const lower = keyword.toLowerCase()
        filtered = filtered.filter(u =>
          u.displayName.toLowerCase().includes(lower) ||
          u.username.toLowerCase().includes(lower))
      }
      if (roleCode) {
        filtered = filtered.filter(u => u.roles.includes(roleCode))
      }
      return filtered
    },
    async createUser(input: CreateUserInput) {
      if (users.some(u => u.username === input.username)) {
        throw new Error('用户名已存在')
      }
      users.push({
        username: input.username,
        displayName: input.displayName,
        title: '',
        roles: input.roleCodes,
        permissions: [],
        dataScope: 'ALL',
        password: input.password,
      })
    },
    async updateUser(id: number, input: UpdateUserInput) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('用户不存在')
      users[index].displayName = input.displayName
    },
    async deleteUser(id: number) {
      const index = id - 1
      if (index < 0 || index >= users.length) throw new Error('用户不存在')
      users.splice(index, 1)
    },
    async assignRoles(_id: number, _input: AssignRolesInput) {
      // mock: no-op, just return
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
