import { onUnmounted, ref, type Ref } from 'vue'

export type NoticeType = 'info' | 'error'

export interface UseNoticeReturn {
  notice: Ref<string>
  noticeType: Ref<NoticeType>
  /** 展示顶部通知，2s（可配置）后自动消失；重复调用会重置计时 */
  showNotice: (message: string, type?: NoticeType) => void
  hideNotice: () => void
}

/**
 * 顶部通知条状态，替换各视图里复制的 showNotice/hideNotice + setTimeout 样板。
 */
export function useNotice(timeoutMs = 2000): UseNoticeReturn {
  const notice = ref('')
  const noticeType = ref<NoticeType>('info')
  let timer: ReturnType<typeof setTimeout> | undefined

  function hideNotice() {
    notice.value = ''
    if (timer) {
      clearTimeout(timer)
      timer = undefined
    }
  }

  function showNotice(message: string, type: NoticeType = 'info') {
    hideNotice()
    notice.value = message
    noticeType.value = type
    timer = setTimeout(() => {
      notice.value = ''
      noticeType.value = 'info'
      timer = undefined
    }, timeoutMs)
  }

  onUnmounted(hideNotice)

  return { notice, showNotice, hideNotice, noticeType }
}
