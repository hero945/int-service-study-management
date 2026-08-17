import { nextTick, watch, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export interface MilestoneStageItem {
  code: string
  name: string
}

export function useMilestoneStageFocus(stages: Ref<MilestoneStageItem[]>) {
  const route = useRoute()
  const router = useRouter()

  const activeStage = () => String(route.query.stage ?? '')

  function scrollToStage(code: string) {
    const row = document.querySelector<HTMLElement>(`[data-stage-code="${CSS.escape(code)}"]`)
    row?.scrollIntoView({ block: 'start', inline: 'nearest' })
    row?.classList.add('milestone-stage-row--flash')
    window.setTimeout(() => row?.classList.remove('milestone-stage-row--flash'), 1200)
    const navItem = document.querySelector<HTMLElement>(`[data-stage-nav="${CSS.escape(code)}"]`)
    navItem?.scrollIntoView({ block: 'nearest', inline: 'center' })
  }

  async function selectStage(code: string) {
    await router.replace({ query: { ...route.query, stage: code } })
    await nextTick()
    scrollToStage(code)
  }

  watch(
    () => [route.query.stage, stages.value.map((s) => s.code).join(',')],
    async ([stage]) => {
      if (!stage) return
      await nextTick()
      scrollToStage(String(stage))
    },
    { immediate: true },
  )

  return { activeStage, selectStage, scrollToStage }
}
