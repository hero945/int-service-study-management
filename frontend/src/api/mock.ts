import type { ApiClient } from './client'
import type { CurrentUser, Study } from './types'

const users: Array<CurrentUser & { password: string }> = [
  {
    username: 'chen@eastchinapharm.com',
    displayName: '陈研发',
    title: '系统管理员',
    role: 'ADMIN',
    password: '1234',
  },
  {
    username: 'zhangwei@eastchinapharm.com',
    displayName: '张伟',
    title: '项目负责人 · PL',
    role: 'USER',
    password: '1234',
  },
  {
    username: 'liuyang@eastchinapharm.com',
    displayName: '刘洋',
    title: '质量观察员',
    role: 'VIEWER',
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
        role: user.role,
        enabled: true,
      }))
    },
  }
}
