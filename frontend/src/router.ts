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
import RolePermissionManagementView from './views/RolePermissionManagementView.vue'
import MilestoneView from './views/MilestoneView.vue'
import MonthlyReportFillView from './views/MonthlyReportFillView.vue'

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
          meta: {
            title: '风险管理',
            subtitle: '识别、评估和跟踪项目风险',
            requiredPermission: 'risk.page.view',
          },
        },
        {
          path: 'milestones/:studyId',
          name: 'milestones',
          component: MilestoneView,
          meta: {
            title: '里程碑',
            subtitle: 'Study 里程碑跟踪',
            requiredPermission: 'pipeline.page.view',
          },
        },
        {
          path: 'studies/:studyId/monthly-report',
          name: 'monthly-report-fill',
          component: MonthlyReportFillView,
          meta: {
            title: '月报填写',
            subtitle: '按功能线填写研究月度进展',
            requiredPermission: 'monthly.read',
          },
        },
        {
          path: 'team',
          name: 'team',
          component: TeamMatrixView,
          meta: {
            title: '团队矩阵',
            subtitle: 'Study × 项目角色分工',
            requiredPermission: 'team.page.view',
          },
        },
        {
          path: 'config',
          name: 'config',
          component: PipelineConfigView,
          meta: {
            title: '管线配置',
            subtitle: '维护 Program、Project 与 Study 实体关系',
            requiredPermission: 'config.page.view',
          },
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
            requiredPermission: 'account.page.view',
          },
        },
        {
          path: 'roles',
          name: 'roles',
          component: RolePermissionManagementView,
          meta: {
            title: '角色权限管理',
            subtitle: '维护角色、数据范围与页面操作权限',
            requiredPermission: 'role.page.view',
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
  const requiredPermission = to.meta.requiredPermission
  if (
    typeof requiredPermission === 'string' &&
    !user.permissions.includes(requiredPermission)
  ) return { name: 'pipeline' }
  return true
})
