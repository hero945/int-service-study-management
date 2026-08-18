import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RiskManagementView from './RiskManagementView.vue'

const { listRisks, getRiskFormOptions } = vi.hoisted(() => ({
  listRisks: vi.fn(), getRiskFormOptions: vi.fn(),
}))

vi.mock('../api/client', () => ({ apiClient: { listRisks, getRiskFormOptions } }))
vi.mock('../session', () => ({
  session: { currentUser: ref({
    username: 'admin@example.com', displayName: '管理员', roles: ['ADMIN'],
    permissions: ['risk.page.view', 'risk.read', 'risk.create'], dataScope: 'ALL',
  }) },
}))
vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
}))
vi.mock('../components/RiskEditorDrawer.vue', () => ({
  default: { props: ['open', 'riskCode'], template: '<div v-if="open" data-testid="risk-drawer">{{ riskCode || "new" }}</div>' },
}))

const page = {
  data: [{ riskCode: 'RSK-2026-000018', studyId: 3, studyCode: 'HDM1005-302',
    programCode: 'HDM1005', projectCode: 'HDM1005-3', functionCode: 'RA',
    functionName: '注册', description: '监管沟通窗口可能影响计划节点', ownerUserId: 2,
    ownerName: '张伟', score: 48, level: 'HIGH', status: 'OPEN', actionCount: 1,
    openActionCount: 1, overdueActionCount: 0, nextPlannedDate: '2026-08-15',
    version: 0, updatedAt: '2026-07-22T09:00:00Z' }],
  stats: { total: 1, open: 1, high: 1, medium: 0 },
  pagination: { page: 1, pageSize: 10, totalItems: 1, totalPages: 1 },
}

describe('RiskManagementView', () => {
  beforeEach(() => {
    listRisks.mockReset().mockResolvedValue(page)
    getRiskFormOptions.mockReset().mockResolvedValue({
      studies: [],
      functions: [{ id: 1, code: 'RA', name: '注册' }],
      owners: [],
      scoringRule: { id: 1, lowMax: 12, mediumMax: 36 },
    })
  })

  it('renders risk statistics and opens the selected risk', async () => {
    const wrapper = mount(RiskManagementView, { attachTo: document.body })
    await vi.waitFor(() => expect(listRisks).toHaveBeenCalled())
    await vi.waitFor(() => expect(wrapper.text()).toContain('HDM1005-302'))
    expect(wrapper.text()).toContain('高危')
    expect(wrapper.text()).not.toContain('RSK-2026-000018')
    await wrapper.get('tbody tr').trigger('click')
    expect(wrapper.get('[data-testid="risk-drawer"]').text()).toBe('RSK-2026-000018')
    wrapper.unmount()
  })

  it('groups Program and Project cells by project across risk rows', async () => {
    listRisks.mockResolvedValue({
      ...page,
      data: [
        page.data[0],
        {
          ...page.data[0],
          riskCode: 'RSK-2026-000019',
          studyCode: 'HDM1005-303',
          description: '第二条同项目风险',
        },
        {
          ...page.data[0],
          riskCode: 'RSK-2026-000020',
          studyCode: 'HDM2001-101',
          programCode: 'HDM2001',
          projectCode: 'HDM2001-1',
          description: '另一项目风险',
        },
      ],
    })
    const wrapper = mount(RiskManagementView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('HDM1005-303'))
    const merged = wrapper.findAll('td.project-group-cell')
    expect(merged).toHaveLength(4)
    expect(merged[0].attributes('rowspan')).toBe('2')
    expect(merged[1].attributes('rowspan')).toBe('2')
    expect(merged[2].attributes('rowspan')).toBe('1')
    expect(wrapper.text()).toContain('HDM1005-3')
    expect(wrapper.text()).toContain('HDM2001-1')
    wrapper.unmount()
  })

  it('shows the score calculation rule from form-options scoringRule', async () => {
    getRiskFormOptions.mockResolvedValue({
      studies: [],
      functions: [],
      owners: [],
      scoringRule: { id: 9, lowMax: 20, mediumMax: 50 },
    })
    const wrapper = mount(RiskManagementView, { attachTo: document.body })
    await vi.waitFor(() => expect(wrapper.find('.risk-score').exists()).toBe(true))
    await wrapper.get('.risk-score').trigger('mouseenter')
    const tip = document.getElementById('risk-score-rule-tip')
    expect(tip?.textContent).toContain('评分计算规则')
    expect(tip?.textContent).toContain('影响程度 a')
    expect(tip?.textContent).toContain('≤20 低风险')
    expect(tip?.textContent).toContain('21–50 中风险')
    expect(tip?.textContent).toContain('≥51 高危')
    await wrapper.get('.risk-score').trigger('mouseleave')
    expect(document.getElementById('risk-score-rule-tip')).toBeNull()
    wrapper.unmount()
  })

  it('applies the Open quick filter through the API boundary', async () => {
    const wrapper = mount(RiskManagementView)
    await vi.waitFor(() => expect(listRisks).toHaveBeenCalledTimes(1))
    const openCard = wrapper.findAll('.risk-stats button').find(button => button.text().includes('未关闭'))!
    await openCard.trigger('click')
    await vi.waitFor(() => expect(listRisks).toHaveBeenLastCalledWith(expect.objectContaining({
      status: 'OPEN', page: 1,
    })))
  })
})
