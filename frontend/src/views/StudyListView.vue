<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { Study } from '../api/types'
import ListPagination from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import StudyDetailDrawer from '../components/StudyDetailDrawer.vue'
import { ALL_MILESTONE_SUB_STATUSES } from '../domain/milestone-filters'
import { TA_OPTIONS } from '../domain/therapeutic-areas'
import { plPmLabel } from '../domain/study-labels'
import { formatDate } from '../domain/date-format'
import { useClientSort } from '../composables/useClientSort'
import { usePagedList } from '../composables/usePagedList'
import { usePermissions } from '../composables/usePermissions'

const router = useRouter()
const { can } = usePermissions()
const canReadMonthly = can('monthly.read')
const canReadMilestone = can('milestone.read')

const filters = reactive({ ta: '', program: '', status: '' })
const statusOptions = ALL_MILESTONE_SUB_STATUSES

const {
  result, loading, error, page, pageSize,
  load, applyFilters, changePage, changePageSize,
} = usePagedList({
  filters,
  errorMessage: '研究数据加载失败',
  fetcher: (q) => apiClient.listStudies({
    therapeuticArea: q.ta || undefined,
    program: q.program || undefined,
    milestoneStatus: q.status || undefined,
    page: q.page,
    pageSize: q.pageSize,
  }),
  // Keep local page in sync if backend clamps out-of-range page.
  onLoaded: (r) => { page.value = r.page },
})

const studies = computed(() => result.value?.data ?? [])
const total = computed(() => result.value?.total ?? 0)
const totalPages = computed(() => result.value?.totalPages ?? 1)

const {
  sorted: sortedStudies,
  registerMany: registerStudySortColumns,
  sortHeader: studySortHeader,
} = useClientSort({ items: studies })

registerStudySortColumns([
  { key: 'ta', resolver: (s) => s.therapeuticAreaName || s.therapeuticArea || s.therapeuticAreaCode, type: 'string' },
  { key: 'program', resolver: (s) => s.programCode || s.program, type: 'string' },
  { key: 'product', resolver: (s) => s.productName || s.product, type: 'string' },
  { key: 'studyNo', resolver: (s) => s.code, type: 'string' },
  { key: 'indication', resolver: (s) => s.indication, type: 'string' },
  { key: 'phase', resolver: (s) => s.currentPhase, type: 'string' },
  { key: 'status', resolver: (s) => s.currentStatus, type: 'string' },
  { key: 'plPm', resolver: (s) => plPmLabel(s), type: 'string' },
  { key: 'updatedAt', resolver: (s) => s.updatedAt, type: 'date' },
])

const drawerOpen = ref(false)
const selectedStudy = ref<Study | null>(null)

function openDrawer(study: Study) {
  selectedStudy.value = study
  drawerOpen.value = true
}
function closeDrawer() {
  drawerOpen.value = false
  selectedStudy.value = null
}

function goMilestones(studyId: number) {
  router.push(`/milestones/${studyId}`)
}

function goMonthlyReport(studyId: number) {
  router.push(`/studies/${studyId}/monthly-report`)
}

watch(() => [filters.ta, filters.status], applyFilters)

onMounted(load)
</script>

<template>
  <section class="page-content page-content--fill">
    <form class="page-toolbar" role="search" @submit.prevent="applyFilters">
      <div class="filter-group">
        <label class="filter-field">
          <span class="filter-field__label">TA</span>
          <select v-model="filters.ta" class="filter-select">
            <option value="">全部</option>
            <option v-for="o in TA_OPTIONS" :key="o" :value="o">{{ o }}</option>
          </select>
        </label>
        <label class="filter-field">
          <span class="filter-field__label">Program</span>
          <input v-model.trim="filters.program" type="text" class="filter-input" placeholder="输入编号搜索">
        </label>
        <label class="filter-field">
          <span class="filter-field__label">里程碑节点</span>
          <select v-model="filters.status" class="filter-select filter-select--status">
            <option value="">全部</option>
            <option v-for="o in statusOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </label>
        <button class="secondary-button" type="submit">搜索</button>
      </div>
      <span class="filter-count">共 {{ total }} 个研究</span>
    </form>

    <PageState :loading :error retryable :empty="!studies.length" @retry="load">
      <div class="data-card">
        <table class="data-table">
          <thead><tr>
            <th v-bind="studySortHeader('ta')">TA</th>
            <th v-bind="studySortHeader('program')">Program</th>
            <th v-bind="studySortHeader('product')">Product</th>
            <th v-bind="studySortHeader('studyNo')">Study No.</th>
            <th v-bind="studySortHeader('indication')">适应症</th>
            <th v-bind="studySortHeader('phase')">里程碑阶段</th>
            <th v-bind="studySortHeader('status')">里程碑节点</th>
            <th v-bind="studySortHeader('plPm')">PL/PM</th>
            <th v-bind="studySortHeader('updatedAt')">更新时间</th>
            <th>操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="study in sortedStudies" :key="study.id" class="study-row--clickable" @click="openDrawer(study)">
              <td>{{ study.therapeuticAreaName || study.therapeuticArea || study.therapeuticAreaCode || '—' }}</td>
              <td class="mono">{{ study.programCode || study.program || '—' }}</td>
              <td class="mono">{{ study.productName || study.product || '—' }}</td>
              <td class="mono strong">{{ study.code }}</td>
              <td>{{ study.indication }}</td>
              <td>{{ study.currentPhase || '—' }}</td>
              <td>{{ study.currentStatus || '—' }}</td>
              <td>{{ plPmLabel(study) || '—' }}</td>
              <td>{{ formatDate(study.updatedAt) }}</td>
              <td class="actions">
                <button
                  v-if="canReadMilestone"
                  class="link-button"
                  @click.stop="goMilestones(study.id)"
                >里程碑</button>
                <button v-if="canReadMonthly" class="link-button" @click.stop="goMonthlyReport(study.id)">月报</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </PageState>

    <ListPagination
      v-if="!loading && !error"
      :total="total"
      :page="page"
      :page-size="pageSize"
      :total-pages="totalPages"
      aria-label="Study 列表分页"
      @update:page="changePage"
      @update:page-size="changePageSize"
    />

    <StudyDetailDrawer :open="drawerOpen" :study="selectedStudy" @close="closeDrawer" />
  </section>
</template>
