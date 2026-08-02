<script setup lang="ts">
import { computed, toRef } from 'vue'
import { useRouter } from 'vue-router'
import type { OverviewProject, OverviewStudy } from '../api/types'
import { toneForStatus } from '../domain/pipeline-status'
import { useEscapeClose } from '../composables/useEscapeClose'
import { session } from '../session'

const props = defineProps<{
  open: boolean
  project: OverviewProject | null
  areaName: string
}>()

const emit = defineEmits<{
  close: []
  'select-study': [study: OverviewStudy]
}>()

useEscapeClose(toRef(props, 'open'), () => emit('close'))

const router = useRouter()
const canReadMilestone = computed(() =>
  session.currentUser.value?.permissions.includes('milestone.read') ?? false,
)

const studies = computed(() => props.project?.studies ?? [])
const studyCount = computed(() => studies.value.length)
const headerMeta = computed(() => {
  const project = props.project
  if (!project) return ''
  const mid = project.moa || project.productName || ''
  return [props.areaName, mid, `${studyCount.value} 个 Study`].filter(Boolean).join(' · ')
})
const title = computed(() => {
  if (!props.project) return ''
  return props.project.productName || props.project.code
})

function openMilestones(studyId: number) {
  router.push(`/milestones/${studyId}`)
}

function milestoneStage(study: OverviewStudy) {
  return study.mainStageLabel || study.phase || '—'
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="drawer-overlay project-studies-overlay"
      @click.self="emit('close')"
    >
      <aside
        class="study-drawer project-studies-drawer"
        role="dialog"
        aria-modal="true"
        :aria-label="title || 'Project Study 列表'"
      >
        <header class="drawer-header project-studies-header">
          <div>
            <p class="project-studies-meta">{{ headerMeta }}</p>
            <h2 class="project-studies-title">{{ title }}</h2>
            <p v-if="project" class="project-studies-sub mono">{{ project.code }}</p>
          </div>
          <button class="drawer-close" type="button" aria-label="关闭" @click="emit('close')">×</button>
        </header>

        <div class="drawer-body project-studies-body">
          <p v-if="!studies.length" class="project-studies-empty">该 Project 下暂无 Study</p>
          <article
            v-for="study in studies"
            :key="study.id"
            class="project-study-card"
            @click="emit('select-study', study)"
          >
            <div class="project-study-card__top">
              <strong class="mono">{{ study.code }}</strong>
              <button
                v-if="canReadMilestone"
                class="project-study-ms-btn"
                type="button"
                @click.stop="openMilestones(study.id)"
              >里程碑</button>
            </div>
            <p class="project-study-card__indication">{{ project?.indication || '—' }}</p>
            <div class="project-study-card__meta">
              <span class="project-study-tag project-study-tag--stage">{{ milestoneStage(study) }}</span>
              <span
                v-if="study.subStatusLabel"
                class="project-study-tag project-study-tag--node"
              >{{ study.subStatusLabel }}</span>
              <span
                class="project-study-tag"
                :class="`chip-tone--${toneForStatus(study.statusTone)}`"
              >{{ study.statusLabel || study.status }}</span>
            </div>
          </article>
        </div>
      </aside>
    </div>
  </Teleport>
</template>

<style scoped>
.project-studies-overlay {
  /* 低于 Study 详情抽屉（.drawer-overlay 用 --z-drawer），保证从列表打开详情时详情在上层 */
  z-index: var(--z-backdrop);
}
.project-studies-header {
  align-items: flex-start;
}
.project-studies-meta {
  margin: 0 0 6px;
  color: var(--muted);
  font-size: 12px;
}
.project-studies-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--body);
  line-height: 1.25;
}
.project-studies-sub {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 12px;
}
.project-studies-body {
  padding: 18px 22px 24px;
  display: grid;
  gap: 10px;
  align-content: start;
}
.project-studies-empty {
  margin: 24px 0;
  text-align: center;
  color: var(--muted);
  font-size: 12.5px;
}
.project-study-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--surface);
  cursor: pointer;
  transition: background .12s ease, border-color .12s ease;
}
.project-study-card:hover {
  background: var(--hover-surface);
  border-color: #d7e3f5;
}
.project-study-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.project-study-card__top strong {
  font-size: 13px;
  font-weight: 600;
  color: var(--body);
}
.project-study-ms-btn {
  flex-shrink: 0;
  border: 1px solid var(--accent);
  background: var(--surface);
  color: var(--accent);
  border-radius: 6px;
  padding: 4px 9px;
  font: inherit;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.project-study-ms-btn:hover {
  background: #f3f7ff;
}
.project-study-card__indication {
  margin: 0;
  color: var(--secondary);
  font-size: 12.5px;
  line-height: 1.4;
}
.project-study-card__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
}
.project-study-tag {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  box-sizing: border-box;
  padding: 3px 9px;
  border-radius: 6px;
  border: 1px solid transparent;
  font-size: 11.5px;
  font-weight: 600;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.project-study-tag--stage {
  background: #f3f5f8;
  color: #5b6472;
  border-color: #e4e7ec;
  font-weight: 500;
}
.project-study-tag--node {
  background: #f3f7ff;
  color: #2a5088;
  border-color: #d5e2f5;
}
</style>
