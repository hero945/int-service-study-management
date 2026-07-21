// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import RolePermissionManagementView from './RolePermissionManagementView.vue'

const apiClientMock = vi.hoisted(() => ({
  listPermissions: vi.fn(),
  listRoles: vi.fn(),
  createRole: vi.fn(),
  updateRole: vi.fn(),
  deleteRole: vi.fn(),
}))

vi.mock('../api/client', () => ({ apiClient: apiClientMock }))
vi.mock('../session', () => ({
  session: {
    currentUser: {
      value: {
        permissions: ['role.page.view', 'role.create', 'role.update', 'role.delete'],
      },
    },
  },
}))

describe('role management notices', () => {
  afterEach(() => {
    vi.useRealTimers()
    vi.clearAllMocks()
  })

  it('automatically dismisses a success notice after four seconds', async () => {
    vi.useFakeTimers()
    apiClientMock.listPermissions.mockResolvedValue([{
      id: 1,
      moduleCode: 'pipeline',
      permissionCode: 'pipeline.page.view',
      permissionName: '查看管线总览',
      permissionType: 'PAGE',
      actionCode: 'view',
      permissionDescription: null,
      sortOrder: 10,
    }])
    apiClientMock.listRoles.mockResolvedValue({
      data: [], page: 1, pageSize: 20, totalItems: 0, totalPages: 0,
    })
    apiClientMock.createRole.mockResolvedValue({ id: 4 })

    const wrapper = mount(RolePermissionManagementView, {
      global: { stubs: { Teleport: true } },
    })
    await flushPromises()

    const createButton = wrapper.findAll('button')
      .find((button) => button.text().includes('新增角色'))
    expect(createButton).toBeDefined()
    await createButton!.trigger('click')
    await wrapper.get('input[placeholder="例如 CLINICAL_LEAD"]').setValue('TEST_ROLE')
    await wrapper.get('.permission-options input[type="checkbox"]').setValue(true)
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.get('.role-notice').text()).toBe('角色已新增')
    await vi.advanceTimersByTimeAsync(3_999)
    expect(wrapper.find('.role-notice').exists()).toBe(true)
    await vi.advanceTimersByTimeAsync(1)
    expect(wrapper.find('.role-notice').exists()).toBe(false)

    wrapper.unmount()
  })
})
