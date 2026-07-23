<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { OverviewProject, OverviewStudy } from '../api/types'
import { toneForStatus } from '../domain/pipeline-status'

const props = defineProps<{
  open: boolean
  project: OverviewProject | null
  areaName: string
}>()

const emit = defineEmits<{
  close: []
  'select-study': [study: OverviewStudy]
}>()

const router = useRouter()

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
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="drawer-overlay project-studies-overlay"
      @click.self="emit('close')"
      @keydown.escape="emit('close')"
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
            <div class="project-study-card__main">
              <strong class="mono">{{ study.code }}</strong>
              <p>{{ project?.indication || '—' }}</p>
              <small>
                {{ study.mainStageLabel || study.phase || '—' }}
                <template v-if="study.subStatusLabel"> · {{ study.subStatusLabel }}</template>
              </small>
            </div>
            <div class="project-study-card__side">
              <span
                class="status-chip"
                :class="`status-chip--${toneForStatus(study.statusTone)}`"
              >{{ study.statusLabel || study.status }}</span>
              <button
                class="text-button"
                type="button"
                @click.stop="openMilestones(study.id)"
              >里程碑</button>
            </div>
          </article>
        </div>
      </aside>
    </div>
  </Teleport>
</template>

<style scoped>
.project-studies-overlay {
  z-index: 900;
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
  font-size: 22px;
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
  padding: 16px 20px 24px;
  display: grid;
  gap: 10px;
  align-content: start;
}
.project-studies-empty {
  margin: 24px 0;
  text-align: center;
  color: var(--muted);
  font-size: 13px;
}
.project-study-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: var(--surface);
  cursor: pointer;
  transition: background .12s ease, border-color .12s ease;
}
.project-study-card:hover {
  background: var(--hover-surface);
  border-color: #d7e3f5;
}
.project-study-card__main strong {
  display: block;
  font-size: 14px;
  color: var(--body);
}
.project-study-card__main p {
  margin: 6px 0 0;
  color: var(--secondary);
  font-size: 12.5px;
}
.project-study-card__main small {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 11.5px;
}
.project-study-card__side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  flex-shrink: 0;
}
</style>
