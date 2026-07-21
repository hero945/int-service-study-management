import { createRouter, createWebHistory } from 'vue-router'
import { session } from './session'
import LoginView from './views/LoginView.vue'
import AppShell from './layout/AppShell.vue'
import PipelineOverviewView from './views/PipelineOverviewView.vue'
import StudyListView from './views/StudyListView.vue'
import MonthlyReportView from './views/MonthlyReportView.vue'
import RiskManagementView from './views/RiskManagementView.vue'
import TeamMatrixView from './views/TeamMatrixView.vue'
import PipelineConfigView from './views/PipelineConfigView.vue'
import MonthlyExportView from './views/MonthlyExportView.vue'
import AccountManagementView from './views/AccountManagementView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/',
      component: AppShell,
      children: [
        { path: '', redirect: '/pipeline' },
        {
          path: 'pipeline',
          name: 'pipeline',
          component: PipelineOverviewView,
          meta: { title: '管线总览', subtitle: '按治疗领域和项目查看研发阶段' },
        },
        {
          path: 'studies',
          name: 'studies',
          component: StudyListView,
          meta: { title: '研究 Study 列表', subtitle: '研究项目主数据与当前状态' },
        },
        {
          path: 'monthly',
          name: 'monthly',
          component: MonthlyReportView,
          meta: { title: '研究月度汇报', subtitle: '按部门维护研究月度进展' },
        },
        {
          path: 'risks',
          name: 'risks',
          component: RiskManagementView,
          meta: { title: '风险管理', subtitle: '识别、评估和跟踪项目风险' },
        },
        {
          path: 'team',
          name: 'team',
          component: TeamMatrixView,
          meta: { title: '团队矩阵', subtitle: '项目角色与部门成员分工' },
        },
        {
          path: 'config',
          name: 'config',
          component: PipelineConfigView,
          meta: { title: '管线配置', subtitle: '维护管线展示与阶段映射' },
        },
        {
          path: 'reports',
          name: 'reports',
          component: MonthlyExportView,
          meta: { title: '月报导出', subtitle: '汇总并导出月度进展' },
        },
        {
          path: 'accounts',
          name: 'accounts',
          component: AccountManagementView,
          meta: {
            title: '账号管理',
            subtitle: '平台账号和角色管理',
            requiresAdmin: true,
          },
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const user = await session.restore()
  if (to.meta.public) {
    return user ? { name: 'pipeline' } : true
  }
  if (!user) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.meta.requiresAdmin && user.role !== 'ADMIN') return { name: 'pipeline' }
  return true
})
