<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { Study } from '../api/types'
import PageState from '../components/PageState.vue'
import { session } from '../session'

const router = useRouter()
const canReadMonthly = computed(() =>
  session.currentUser.value?.permissions.includes('monthly.read') ?? false,
)

const studies = ref<Study[]>([])
const query = ref('')
const loading = ref(true)
const error = ref('')
const filtered = computed(() => studies.value.filter((study) =>
  `${study.code} ${study.indication} ${study.ownerName}`
    .toLowerCase()
    .includes(query.value.toLowerCase()),
))

function goMilestones(studyId: number) {
  router.push(`/milestones/${studyId}`)
}

function goMonthlyReport(studyId: number) {
  router.push(`/studies/${studyId}/monthly-report`)
}

onMounted(async () => {
  try {
    studies.value = await apiClient.listStudies()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '研究数据加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar">
      <label class="inline-search"><span>⌕</span><input v-model.trim="query" type="search" placeholder="搜索 Study、适应症或负责人"></label>
      <span>{{ filtered.length }} 个研究</span>
    </div>
    <PageState :loading :error :empty="!filtered.length">
      <div class="data-card">
        <table class="data-table">
          <thead><tr><th>Study No.</th><th>适应症</th><th>阶段</th><th>状态</th><th>负责人</th><th>更新时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="study in filtered" :key="study.id">
              <td class="mono strong">{{ study.code }}</td>
              <td>{{ study.indication }}</td>
              <td>{{ study.phase }}</td>
              <td><span class="status-chip status-chip--blue">{{ study.statusLabel }}</span></td>
              <td>{{ study.ownerName }}</td>
              <td>{{ study.updatedAt.slice(0, 10) }}</td>
              <td>
                <button class="text-button" type="button" @click="goMilestones(study.id)">里程碑</button>
                <button v-if="canReadMonthly" class="text-button" type="button" @click="goMonthlyReport(study.id)">月报</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </PageState>
  </section>
</template>
