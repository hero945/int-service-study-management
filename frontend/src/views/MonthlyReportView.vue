<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiClient } from '../api/client'
import { formatApiError } from '../api/errors'
import type { MonthlyReport } from '../api/types'
import PageState from '../components/PageState.vue'

const reports = ref<MonthlyReport[]>([])
const loading = ref(true)
const error = ref('')
const month = ref(new Date().toISOString().slice(0, 7))

async function load() {
  loading.value = true
  try {
    reports.value = await apiClient.listMonthlyReports(month.value)
  } catch (reason) {
    error.value = formatApiError(reason, '月度汇报加载失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar"><label>汇报月份 <input v-model="month" type="month" @change="load"></label><button class="primary-button" type="button">＋ 填写月报</button></div>
    <PageState :loading :error :empty="!reports.length" empty-title="本月尚无汇报" empty-description="后端月报接口接通后，可按 Study 和部门填写月度进展。">
      <div class="data-card"><table class="data-table"><thead><tr><th>Study</th><th>月份</th><th>部门</th><th>汇报内容</th><th>更新人</th><th>更新时间</th></tr></thead><tbody><tr v-for="report in reports" :key="`${report.studyCode}-${report.month}-${report.functionCode}`"><td>{{ report.studyCode }}</td><td>{{ report.month }}</td><td>{{ report.functionName }}</td><td>{{ report.content }}</td><td>{{ report.updatedBy }}</td><td>{{ report.updatedAt }}</td></tr></tbody></table></div>
    </PageState>
  </section>
</template>
