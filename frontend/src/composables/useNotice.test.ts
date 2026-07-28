import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent } from 'vue'
import { useNotice } from './useNotice'

function setup(timeoutMs?: number) {
  let api: ReturnType<typeof useNotice>
  const wrapper = mount(defineComponent({
    setup() {
      api = useNotice(timeoutMs)
      return () => null
    },
  }))
  return { api: api!, wrapper }
}

describe('useNotice', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('showNotice 后 4s 自动消失', () => {
    const { api } = setup()
    api.showNotice('已保存')
    expect(api.notice.value).toBe('已保存')
    vi.advanceTimersByTime(4000)
    expect(api.notice.value).toBe('')
  })

  it('重复 showNotice 重置计时', () => {
    const { api } = setup()
    api.showNotice('第一条')
    vi.advanceTimersByTime(3000)
    api.showNotice('第二条')
    vi.advanceTimersByTime(3000)
    expect(api.notice.value).toBe('第二条')
    vi.advanceTimersByTime(1000)
    expect(api.notice.value).toBe('')
  })

  it('hideNotice 立即清除并取消计时', () => {
    const { api } = setup()
    api.showNotice('提示')
    api.hideNotice()
    expect(api.notice.value).toBe('')
    vi.advanceTimersByTime(10000)
    expect(api.notice.value).toBe('')
  })

  it('支持自定义超时', () => {
    const { api } = setup(1000)
    api.showNotice('快')
    vi.advanceTimersByTime(1000)
    expect(api.notice.value).toBe('')
  })
})
