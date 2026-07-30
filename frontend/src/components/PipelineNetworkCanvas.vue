<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

interface Node {
  x: number
  y: number
  vx: number
  vy: number
  r: number
  bright: boolean
  cyan: boolean
}

interface Pulse {
  a: Node
  b: Node
  t: number
  speed: number
}

const LINK_DISTANCE = 140
const canvasRef = ref<HTMLCanvasElement>()
let ctx: CanvasRenderingContext2D | null = null
let nodes: Node[] = []
let pulses: Pulse[] = []
let rafId = 0
let width = 0
let height = 0
let dpr = 1
let lastPulseAt = 0
let resizeObserver: ResizeObserver | undefined
const pointer = { x: -9999, y: -9999, active: false }
const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)')

function randomBetween(min: number, max: number) {
  return min + Math.random() * (max - min)
}

function seedNodes() {
  const count = Math.round(Math.min(90, Math.max(30, (width * height) / 18000)))
  nodes = Array.from({ length: count }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    vx: randomBetween(-0.25, 0.25),
    vy: randomBetween(-0.25, 0.25),
    r: randomBetween(1.2, 2.4),
    bright: Math.random() < 0.12,
    cyan: Math.random() < 0.18,
  }))
  pulses = []
}

function resize() {
  const canvas = canvasRef.value
  if (!canvas || !ctx) return
  width = canvas.clientWidth
  height = canvas.clientHeight
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = Math.round(width * dpr)
  canvas.height = Math.round(height * dpr)
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  if (nodes.length === 0) seedNodes()
  if (reducedMotion.matches) drawFrame()
}

function nodeColor(node: Node, alpha: number) {
  return node.cyan ? `rgba(94, 210, 224, ${alpha})` : `rgba(120, 156, 245, ${alpha})`
}

function drawLinks() {
  if (!ctx) return
  for (let i = 0; i < nodes.length; i++) {
    for (let j = i + 1; j < nodes.length; j++) {
      const a = nodes[i]
      const b = nodes[j]
      const dx = a.x - b.x
      const dy = a.y - b.y
      const distance = Math.hypot(dx, dy)
      if (distance >= LINK_DISTANCE) continue
      const alpha = (1 - distance / LINK_DISTANCE) * 0.35
      ctx.strokeStyle = `rgba(120, 156, 245, ${alpha})`
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(a.x, a.y)
      ctx.lineTo(b.x, b.y)
      ctx.stroke()
    }
  }
  if (pointer.active) {
    for (const node of nodes) {
      const distance = Math.hypot(node.x - pointer.x, node.y - pointer.y)
      if (distance >= LINK_DISTANCE * 1.2) continue
      const alpha = (1 - distance / (LINK_DISTANCE * 1.2)) * 0.55
      ctx.strokeStyle = `rgba(148, 178, 250, ${alpha})`
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(pointer.x, pointer.y)
      ctx.lineTo(node.x, node.y)
      ctx.stroke()
    }
  }
}

function drawNodes() {
  if (!ctx) return
  for (const node of nodes) {
    if (node.bright) {
      ctx.shadowBlur = 10
      ctx.shadowColor = nodeColor(node, 0.9)
    }
    ctx.fillStyle = nodeColor(node, node.bright ? 0.95 : 0.6)
    ctx.beginPath()
    ctx.arc(node.x, node.y, node.bright ? node.r * 1.4 : node.r, 0, Math.PI * 2)
    ctx.fill()
    ctx.shadowBlur = 0
  }
}

function drawPulses(now: number) {
  if (!ctx) return
  if (now - lastPulseAt > 1600 && nodes.length > 1) {
    lastPulseAt = now
    const a = nodes[Math.floor(Math.random() * nodes.length)]
    let best: Node | undefined
    let bestDistance = Infinity
    for (const candidate of nodes) {
      if (candidate === a) continue
      const distance = Math.hypot(candidate.x - a.x, candidate.y - a.y)
      if (distance < LINK_DISTANCE && distance < bestDistance) {
        best = candidate
        bestDistance = distance
      }
    }
    if (best) pulses.push({ a, b: best, t: 0, speed: randomBetween(0.006, 0.01) })
  }
  pulses = pulses.filter((pulse) => pulse.t <= 1)
  for (const pulse of pulses) {
    pulse.t += pulse.speed
    // 整体淡入淡出，避免突然出现/消失
    const fade = Math.sin(pulse.t * Math.PI)
    const headX = pulse.a.x + (pulse.b.x - pulse.a.x) * pulse.t
    const headY = pulse.a.y + (pulse.b.y - pulse.a.y) * pulse.t
    // 渐变拖尾：从头部向后 18% 行程逐渐消散
    const tailT = Math.max(0, pulse.t - 0.18)
    const tailX = pulse.a.x + (pulse.b.x - pulse.a.x) * tailT
    const tailY = pulse.a.y + (pulse.b.y - pulse.a.y) * tailT
    const trail = ctx.createLinearGradient(tailX, tailY, headX, headY)
    trail.addColorStop(0, 'rgba(148, 178, 250, 0)')
    trail.addColorStop(1, `rgba(148, 178, 250, ${0.4 * fade})`)
    ctx.strokeStyle = trail
    ctx.lineWidth = 1.2
    ctx.beginPath()
    ctx.moveTo(tailX, tailY)
    ctx.lineTo(headX, headY)
    ctx.stroke()
    // 头部：小而柔的光点，低发光
    ctx.shadowBlur = 6
    ctx.shadowColor = `rgba(148, 178, 250, ${0.6 * fade})`
    ctx.fillStyle = `rgba(190, 208, 250, ${0.55 * fade})`
    ctx.beginPath()
    ctx.arc(headX, headY, 1.6, 0, Math.PI * 2)
    ctx.fill()
    ctx.shadowBlur = 0
  }
}

function stepNodes() {
  for (const node of nodes) {
    node.x += node.vx
    node.y += node.vy
    if (node.x < -20) node.x = width + 20
    if (node.x > width + 20) node.x = -20
    if (node.y < -20) node.y = height + 20
    if (node.y > height + 20) node.y = -20
  }
}

function drawFrame() {
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)
  drawLinks()
  drawNodes()
}

function tick(now: number) {
  stepNodes()
  drawFrame()
  drawPulses(now)
  rafId = requestAnimationFrame(tick)
}

function start() {
  if (reducedMotion.matches || rafId) return
  rafId = requestAnimationFrame(tick)
}

function stop() {
  cancelAnimationFrame(rafId)
  rafId = 0
}

function onVisibilityChange() {
  if (document.hidden) stop()
  else start()
}

function onPointerMove(event: PointerEvent) {
  const canvas = canvasRef.value
  if (!canvas) return
  const rect = canvas.getBoundingClientRect()
  pointer.x = event.clientX - rect.left
  pointer.y = event.clientY - rect.top
  pointer.active = true
}

function onPointerLeave() {
  pointer.active = false
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  if (!ctx) return
  resize()
  resizeObserver = new ResizeObserver(resize)
  resizeObserver.observe(canvas)
  window.addEventListener('pointermove', onPointerMove, { passive: true })
  window.addEventListener('pointerleave', onPointerLeave)
  document.addEventListener('visibilitychange', onVisibilityChange)
  if (reducedMotion.matches) drawFrame()
  else start()
})

onBeforeUnmount(() => {
  stop()
  resizeObserver?.disconnect()
  window.removeEventListener('pointermove', onPointerMove)
  window.removeEventListener('pointerleave', onPointerLeave)
  document.removeEventListener('visibilitychange', onVisibilityChange)
})
</script>

<template>
  <canvas ref="canvasRef" class="pipeline-network" aria-hidden="true" />
</template>
