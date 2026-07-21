<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiClient } from '../api/client'
import type { Risk } from '../api/types'
import PageState from '../components/PageState.vue'

const risks = ref<Risk[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    risks.value = await apiClient.listRisks()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '风险数据加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar">
      <div class="toolbar-filters"><select aria-label="风险状态"><option>全部状态</option><option>Open</option><option>Monitoring</option><option>Closed</option></select><select aria-label="风险等级"><option>全部等级</option><option>高</option><option>中</option><option>低</option></select></div>
      <button class="primary-button" type="button">＋ 新增风险</button>
    </div>
    <PageState :loading :error :empty="!risks.length" empty-title="暂无风险记录">
      <div class="data-card">
        <table class="data-table">
          <thead><tr><th>风险编号</th><th>Program</th><th>Study</th><th>功能线</th><th>风险描述</th><th>责任人</th><th>等级</th><th>状态</th></tr></thead>
          <tbody><tr v-for="risk in risks" :key="risk.id"><td class="mono strong">{{ risk.id }}</td><td>{{ risk.program }}</td><td class="mono">{{ risk.studyCode }}</td><td>{{ risk.functionName }}</td><td>{{ risk.description }}</td><td>{{ risk.owner }}</td><td><span class="status-chip status-chip--orange">{{ risk.severity }}</span></td><td>{{ risk.status }}</td></tr></tbody>
        </table>
      </div>
    </PageState>
  </section>
</template>
