import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PageState from './PageState.vue'

describe('PageState', () => {
  it('renders a content-shaped loading skeleton with an accessible label', () => {
    const wrapper = mount(PageState, { props: { loading: true } })

    expect(wrapper.get('[aria-busy="true"]').attributes('aria-label')).toBe('正在加载数据')
    expect(wrapper.findAll('.state-panel__skeleton-row')).toHaveLength(4)
  })

  it('offers a retry action for recoverable errors', async () => {
    const wrapper = mount(PageState, {
      props: { error: '数据加载失败', retryable: true },
    })

    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('retry')).toHaveLength(1)
  })
})
