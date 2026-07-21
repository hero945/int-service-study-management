<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiClient } from '../api/client'
import type { PipelineConfig } from '../api/types'
import PageState from '../components/PageState.vue'

const rows = ref<PipelineConfig[]>([])
const loading = ref(true)
const error = ref('')
onMounted(async () => {
  try {
    rows.value = await apiClient.listPipelineConfig()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '管线配置加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar"><span>Phase Status 决定项目在管线总览中的起始阶段</span><button class="primary-button" type="button">＋ 新增配置</button></div>
    <PageState :loading :error :empty="!rows.length">
      <div class="data-card"><table class="data-table"><thead><tr><th>Source</th><th>Origin</th><th>Product</th><th>MOA</th><th>Program</th><th>Indication</th><th>Project</th><th>TA</th><th>Study No.</th><th>项目情况</th><th>Phase Status</th></tr></thead><tbody><tr v-for="row in rows" :key="row.key"><td>{{ row.source }}</td><td>{{ row.origin }}</td><td>{{ row.product }}</td><td>{{ row.moa }}</td><td class="mono">{{ row.program }}</td><td>{{ row.indication }}</td><td>{{ row.project }}</td><td>{{ row.therapeuticArea }}</td><td class="mono">{{ row.studyCode }}</td><td>{{ row.projectStatus }}</td><td><span class="status-chip status-chip--blue">{{ row.phaseStatus }}</span></td></tr></tbody></table></div>
    </PageState>
  </section>
</template>
