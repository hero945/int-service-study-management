import { onMounted, onUnmounted, reactive } from 'vue'

const STORAGE_PREFIX = 'table-col-widths:'
const MIN_WIDTH = 72

function readStored(tableId: string): Record<string, number> {
  if (typeof localStorage === 'undefined') return {}
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + tableId)
    if (!raw) return {}
    const parsed = JSON.parse(raw) as Record<string, unknown>
    const widths: Record<string, number> = {}
    for (const [key, value] of Object.entries(parsed)) {
      if (typeof value === 'number' && Number.isFinite(value) && value >= MIN_WIDTH) {
        widths[key] = value
      }
    }
    return widths
  } catch {
    return {}
  }
}

function writeStored(tableId: string, widths: Record<string, number>) {
  if (typeof localStorage === 'undefined') return
  localStorage.setItem(STORAGE_PREFIX + tableId, JSON.stringify(widths))
}

export function useResizableColumns(tableId: string, defaults: Record<string, number>) {
  const widths = reactive<Record<string, number>>({ ...defaults, ...readStored(tableId) })

  onMounted(() => {
    Object.assign(widths, defaults, readStored(tableId))
  })

  function persist() {
    writeStored(tableId, { ...widths })
  }

  function startResize(key: string, event: PointerEvent) {
    event.preventDefault()
    event.stopPropagation()
    const startX = event.clientX
    const startWidth = widths[key] ?? defaults[key] ?? MIN_WIDTH
    const target = event.currentTarget as HTMLElement | null
    target?.setPointerCapture(event.pointerId)

    function onMove(moveEvent: PointerEvent) {
      const next = Math.max(MIN_WIDTH, startWidth + (moveEvent.clientX - startX))
      widths[key] = next
    }
    function onUp() {
      window.removeEventListener('pointermove', onMove)
      window.removeEventListener('pointerup', onUp)
      persist()
    }
    window.addEventListener('pointermove', onMove)
    window.addEventListener('pointerup', onUp)
  }

  function columnWidth(key: string) {
    return widths[key] ?? defaults[key]
  }

  function colStyle(key: string) {
    const width = columnWidth(key)
    if (!width) return undefined
    const px = `${width}px`
    // maxWidth 与 width 锁死列盒，避免 table-layout:fixed 把剩余宽度摊回本列，
    // 也避免单元格内容把视觉宽度撑进邻列。
    return { width: px, minWidth: px, maxWidth: px }
  }

  function totalWidth() {
    return Object.values(widths).reduce((sum, value) => sum + value, 0)
  }

  function cssVars(prefix = 'col') {
    const vars: Record<string, string> = {}
    for (const [key, value] of Object.entries(widths)) {
      vars[`--${prefix}-${key}`] = `${value}px`
    }
    return vars
  }

  function tableStyle(prefix = 'col') {
    const total = totalWidth()
    return {
      ...cssVars(prefix),
      width: '100%',
      minWidth: `${total}px`,
    }
  }

  /** 末列只保底、不锁 maxWidth，用来吃掉容器比列宽之和多出来的空间。 */
  function fluidColStyle(key: string) {
    const width = columnWidth(key)
    return width ? { minWidth: `${width}px` } : undefined
  }

  onUnmounted(() => persist())

  return { widths, startResize, colStyle, cssVars, tableStyle, fluidColStyle, totalWidth }
}
