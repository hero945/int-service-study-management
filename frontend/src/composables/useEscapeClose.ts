import { watch, type Ref } from 'vue'

/**
 * 弹层打开期间按 Esc 触发 close。
 * 多个弹层叠放时（如 Study 详情抽屉叠在 Project 抽屉上）只关闭最上层：
 * 所有实例共用一个 window 监听和一个栈，Esc 只调用栈顶。
 */
const closeStack: Array<() => void> = []
let listenerAttached = false

function onKeydown(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  closeStack[closeStack.length - 1]?.()
}

export function useEscapeClose(open: Ref<boolean>, close: () => void): void {
  watch(open, (isOpen, _prev, onCleanup) => {
    if (!isOpen) return
    closeStack.push(close)
    if (!listenerAttached) {
      window.addEventListener('keydown', onKeydown)
      listenerAttached = true
    }
    onCleanup(() => {
      const index = closeStack.indexOf(close)
      if (index !== -1) closeStack.splice(index, 1)
      if (!closeStack.length && listenerAttached) {
        window.removeEventListener('keydown', onKeydown)
        listenerAttached = false
      }
    })
  }, { immediate: true })
}
