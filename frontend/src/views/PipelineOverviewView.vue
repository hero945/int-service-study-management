<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { apiClient } from '../api/client'
import type { PipelineOverview, Study } from '../api/types'
import { PHASE_TAGS, getPipelineCell, type PipelinePhase } from '../domain/pipeline-status'

const phases = PHASE_TAGS
const overview = ref<PipelineOverview>()
const studies = ref<Study[]>([])
const loading = ref(true)
const errorMessage = ref('')
const query = ref('')
const therapeuticArea = ref('全部')

const areas = computed(() => [
  '全部',
  ...new Set(studies.value.map((study) => study.therapeuticArea).filter(Boolean)),
])
const filteredStudies = computed(() => studies.value.filter((study) => {
  const text = `${study.code} ${study.name} ${study.indication} ${study.program} ${study.project}`.toLowerCase()
  const matchesQuery = text.includes(query.value.toLowerCase())
  const matchesArea =
    therapeuticArea.value === '全部' || study.therapeuticArea === therapeuticArea.value
  return matchesQuery && matchesArea
}))
const groups = computed(() => {
  const result = new Map<string, Study[]>()
  for (const study of filteredStudies.value) {
    const key = study.therapeuticArea ?? '其他'
    result.set(key, [...(result.get(key) ?? []), study])
  }
  return [...result.entries()]
})

const phaseStatus = (study: Study, phase: PipelinePhase) => getPipelineCell(study, phase)

onMounted(async () => {
  try {
    ;[overview.value, studies.value] = await Promise.all([
      apiClient.getPipelineOverview(),
      apiClient.listStudies(),
    ])
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '管线数据加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="filter-bar">
      <label>
        <span>TA</span>
        <select v-model="therapeuticArea">
          <option v-for="area in areas" :key="area">{{ area }}</option>
        </select>
      </label>
      <label>
        <span>关键词</span>
        <input v-model.trim="query" type="search" placeholder="Program / Project / Study">
      </label>
      <div class="quick-metrics">
        <span
          v-for="metric in overview?.statuses ?? []"
          :key="metric.status"
          class="quick-chip"
          :class="`quick-chip--${metric.tone}`"
        >
          <strong>{{ metric.count }}</strong>{{ metric.label }}
        </span>
      </div>
      <span class="result-summary">{{ filteredStudies.length }} 个研究</span>
    </div>

    <div class="legend-bar">
      <span>状态图例</span>
      <span><i class="legend-dot legend-dot--blue"></i>进行中</span>
      <span><i class="legend-dot legend-dot--green"></i>已完成</span>
      <span><i class="legend-dot legend-dot--gray"></i>准备中</span>
      <span><i class="legend-dot legend-dot--red"></i>延期</span>
    </div>

    <div v-if="loading" class="state-panel" aria-live="polite">正在加载管线数据…</div>
    <div v-else-if="errorMessage" class="state-panel state-panel--error" role="alert">
      {{ errorMessage }}
    </div>
    <div v-else-if="!groups.length" class="state-panel">
      <strong>暂无匹配项目</strong>
      <span>请调整筛选条件后重试。</span>
    </div>
    <div v-else class="pipeline-table-wrap">
      <table class="pipeline-table">
        <thead>
          <tr>
            <th>Product</th>
            <th>Program (MOA)</th>
            <th>Project (Indication)</th>
            <th v-for="phase in phases" :key="phase">{{ phase }}</th>
          </tr>
        </thead>
        <tbody v-for="[area, rows] in groups" :key="area">
          <tr class="area-row">
            <td :colspan="10"><span class="area-dot"></span>{{ area }}<small>{{ rows.length }} 个项目</small></td>
          </tr>
          <tr v-for="study in rows" :key="study.id">
            <td><strong>{{ study.product || study.code }}</strong><small>{{ study.source }} · {{ study.origin }}</small></td>
            <td><strong class="mono">{{ study.program || study.code }}</strong><small>{{ study.moa }}</small></td>
            <td><strong>{{ study.project || study.name }}</strong><small>{{ study.indication }}</small></td>
            <td v-for="phase in phases" :key="phase">
              <span
                class="status-chip"
                :class="`status-chip--${phaseStatus(study, phase).tone}`"
                :title="phaseStatus(study, phase).explanation"
              >
                {{ phaseStatus(study, phase).label }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
