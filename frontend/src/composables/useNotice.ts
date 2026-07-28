import { onUnmounted, ref, type Ref } from 'vue'

export interface UseNoticeReturn {
  notice: Ref<string>
  /** 展示通知，4s（可配置）后自动消失；重复调用会重置计时 */
  showNotice: (message: string) => void
  hideNotice: () => void
}

/**
 * 顶部通知条状态，替换各视图里复制的 showNotice/hideNotice + setTimeout 样板。
 */
export function useNotice(timeoutMs = 4000): UseNoticeReturn {
  const notice = ref('')
  let timer: ReturnType<typeof setTimeout> | undefined

  function hideNotice() {
    notice.value = ''
    if (timer) {
      clearTimeout(timer)
      timer = undefined
    }
  }

  function showNotice(message: string) {
    hideNotice()
    notice.value = message
    timer = setTimeout(() => {
      notice.value = ''
      timer = undefined
    }, timeoutMs)
  }

  onUnmounted(hideNotice)

  return { notice, showNotice, hideNotice }
}
