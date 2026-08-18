import { nextTick, watch, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export interface MilestoneStageItem {
  code: string
  name: string
}

export function resolveMilestoneStage(
  queryStage: string | undefined,
  stageCodes: string[],
  fallbackStage?: string,
): string {
  if (queryStage && stageCodes.includes(queryStage)) return queryStage
  if (fallbackStage && stageCodes.includes(fallbackStage)) return fallbackStage
  return stageCodes[0] ?? ''
}

export function milestoneScrollOffset(
  rowTop: number,
  scrollerTop: number,
  scrollTop: number,
  headerHeight: number,
): number {
  return Math.max(0, scrollTop + (rowTop - scrollerTop) - headerHeight)
}

export function scrollMilestoneRowIntoView(row: HTMLElement) {
  const scroller = row.closest('.milestone-card')
  if (!(scroller instanceof HTMLElement)) {
    row.scrollIntoView({ block: 'start', inline: 'nearest' })
    return
  }
  const header = scroller.querySelector('thead')
  const headerHeight = header instanceof HTMLElement ? header.getBoundingClientRect().height : 0
  const nextTop = milestoneScrollOffset(
    row.getBoundingClientRect().top,
    scroller.getBoundingClientRect().top,
    scroller.scrollTop,
    headerHeight,
  )
  scroller.scrollTo({ top: nextTop, behavior: 'smooth' })
}

export function useMilestoneStageFocus(
  stages: Ref<MilestoneStageItem[]>,
  options?: {
    fallbackStage?: Ref<string | undefined>
    focusMilestoneCode?: Ref<string | undefined>
    ready?: Ref<boolean>
  },
) {
  const route = useRoute()
  const router = useRouter()

  const stageCodes = () => stages.value.map((stage) => stage.code)

  const activeStage = () => resolveMilestoneStage(
    String(route.query.stage ?? ''),
    stageCodes(),
    options?.fallbackStage?.value,
  )

  function scrollToStage(code: string, milestoneCode?: string) {
    const nodeRow = milestoneCode
      ? document.querySelector<HTMLElement>(`[data-milestone-code="${CSS.escape(milestoneCode)}"]`)
      : null
    const row = nodeRow ?? document.querySelector<HTMLElement>(`[data-stage-code="${CSS.escape(code)}"]`)
    if (!row) return false
    scrollMilestoneRowIntoView(row)
    row.classList.add('milestone-stage-row--flash')
    window.setTimeout(() => row.classList.remove('milestone-stage-row--flash'), 1200)
    const navItem = document.querySelector<HTMLElement>(`[data-stage-nav="${CSS.escape(code)}"]`)
    navItem?.scrollIntoView({ block: 'nearest', inline: 'center' })
    return true
  }

  async function selectStage(code: string) {
    await router.replace({ query: { ...route.query, stage: code } })
    await nextTick()
    scrollToStage(code)
  }

  watch(
    () => [
      String(route.query.stage ?? ''),
      stageCodes().join(','),
      options?.fallbackStage?.value ?? '',
      options?.focusMilestoneCode?.value ?? '',
      options?.ready?.value ?? true,
    ],
    async ([query, codesJoined, fallback, milestoneCode, ready]) => {
      if (!ready) return
      const codes = codesJoined ? String(codesJoined).split(',') : []
      if (!codes.length) return
      const resolved = resolveMilestoneStage(
        String(query),
        codes,
        String(fallback) || undefined,
      )
      if (!resolved) return
      if (String(query) !== resolved) {
        await router.replace({ query: { ...route.query, stage: resolved } })
        return
      }
      await nextTick()
      const currentNode = resolved === fallback ? String(milestoneCode) : ''
      scrollToStage(resolved, currentNode || undefined)
    },
    { immediate: true, flush: 'post' },
  )

  return { activeStage, selectStage, scrollToStage }
}
