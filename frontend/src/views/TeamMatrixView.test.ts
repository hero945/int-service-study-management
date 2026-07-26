import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import TeamMatrixView from './TeamMatrixView.vue'

const { listTeamMatrix, replaceTeamAssignments, listUsers } = vi.hoisted(() => ({
  listTeamMatrix: vi.fn(),
  replaceTeamAssignments: vi.fn(),
  listUsers: vi.fn(),
}))

vi.mock('../api/client', () => ({
  apiClient: {
    listTeamMatrix,
    replaceTeamAssignments,
    listUsers,
  },
  ApiError: class ApiError extends Error {
    constructor(message: string, readonly status: number, readonly code?: string) {
      super(message)
    }
  },
}))

vi.mock('../session', () => ({
  session: {
    currentUser: ref({
      username: 'admin@example.com',
      displayName: '管理员',
      roles: ['ADMIN'],
      permissions: ['team.page.view', 'team.edit_mode', 'team.update'],
      dataScope: 'ALL',
    }),
  },
}))

const matrix = {
  studies: [{
    studyId: 11,
    studyCode: 'HDM1005-302',
    indication: '非小细胞肺癌',
    statusCode: 'ACTIVE',
    statusLabel: '进行中',
    currentStatus: 'IND 获批',
    version: 0,
  }],
  roles: [{
    roleCode: 'PL',
    roleName: 'PL 项目负责人',
    functionCode: 'PM',
    functionName: '项目管理',
  }],
  assignments: [],
  totalRoles: 44,
  pagination: { page: 1, pageSize: 10, totalItems: 1, totalPages: 1 },
}

describe('TeamMatrixView', () => {
  beforeEach(() => {
    listTeamMatrix.mockReset().mockResolvedValue(matrix)
    replaceTeamAssignments.mockReset().mockResolvedValue({
      studies: [{ studyId: 11, version: 1 }],
    })
    listUsers.mockReset().mockResolvedValue({
      data: [{
        id: 21,
        username: 'member@example.com',
        displayName: '张伟',
        roles: ['USER'],
        roleDescriptions: ['普通成员'],
        dataScope: 'ASSIGNED_STUDY',
        visibleStudyCount: 0,
        enabled: true,
      }],
      page: 1,
      pageSize: 100,
      totalItems: 1,
      totalPages: 1,
    })
  })

  it('renders the role by Study matrix and saves staged member changes', async () => {
    const wrapper = mount(TeamMatrixView)
    await vi.waitFor(() => expect(listTeamMatrix).toHaveBeenCalled())
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('HDM1005-302')
    expect(wrapper.text()).toContain('PL 项目负责人')

    await wrapper.get('[data-testid="edit-team"]').trigger('click')
    await wrapper.get('[data-testid="add-member-11-PL"]').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.get('[data-testid="member-option-21"]').trigger('click')
    await wrapper.get('[data-testid="save-team"]').trigger('click')

    expect(replaceTeamAssignments).toHaveBeenCalledWith({
      studies: [{
        studyId: 11,
        expectedVersion: 0,
        roles: [{ roleCode: 'PL', userIds: [21] }],
      }],
    })
  })

  it('cancels staged changes without calling the API', async () => {
    const wrapper = mount(TeamMatrixView)
    await vi.waitFor(() => expect(listTeamMatrix).toHaveBeenCalled())
    await wrapper.vm.$nextTick()

    await wrapper.get('[data-testid="edit-team"]').trigger('click')
    await wrapper.get('[data-testid="add-member-11-PL"]').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.get('[data-testid="member-option-21"]').trigger('click')
    await wrapper.get('[data-testid="cancel-team"]').trigger('click')

    expect(replaceTeamAssignments).not.toHaveBeenCalled()
    expect(wrapper.text()).not.toContain('张伟')
  })
})
