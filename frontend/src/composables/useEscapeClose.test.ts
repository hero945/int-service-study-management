import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, ref } from 'vue'
import { useEscapeClose } from './useEscapeClose'

function setup(initialOpen = false) {
  const open = ref(initialOpen)
  const close = vi.fn(() => { open.value = false })
  const wrapper = mount(defineComponent({
    setup() {
      useEscapeClose(open, close)
      return () => null
    },
  }))
  return { open, close, wrapper }
}

function pressEscape() {
  window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
}

describe('useEscapeClose', () => {
  let wrapper: ReturnType<typeof setup>['wrapper'] | undefined
  afterEach(() => { wrapper?.unmount(); wrapper = undefined })

  it('打开时按 Esc 触发 close', async () => {
    const s = setup()
    wrapper = s.wrapper
    pressEscape()
    expect(s.close).not.toHaveBeenCalled()
    s.open.value = true
    await s.wrapper.vm.$nextTick()
    pressEscape()
    expect(s.close).toHaveBeenCalledTimes(1)
  })

  it('关闭后移除监听', async () => {
    const s = setup(true)
    wrapper = s.wrapper
    s.open.value = false
    await s.wrapper.vm.$nextTick()
    pressEscape()
    expect(s.close).not.toHaveBeenCalled()
  })

  it('卸载后移除监听', async () => {
    const s = setup(true)
    wrapper = s.wrapper
    s.wrapper.unmount()
    wrapper = undefined
    pressEscape()
    expect(s.close).not.toHaveBeenCalled()
  })

  it('非 Escape 键不触发', async () => {
    const s = setup(true)
    wrapper = s.wrapper
    window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }))
    expect(s.close).not.toHaveBeenCalled()
  })

  it('叠放时只关闭最上层', async () => {
    const bottom = setup(true)
    const top = setup(true)
    wrapper = top.wrapper
    await bottom.wrapper.vm.$nextTick()
    pressEscape()
    expect(top.close).toHaveBeenCalledTimes(1)
    expect(bottom.close).not.toHaveBeenCalled()
    // 顶层关闭后，下一次 Esc 才轮到下层
    await top.wrapper.vm.$nextTick()
    pressEscape()
    expect(bottom.close).toHaveBeenCalledTimes(1)
    bottom.wrapper.unmount()
  })
})
