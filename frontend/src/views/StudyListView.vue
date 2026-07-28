<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { Study, StudyPage } from '../api/types'
import ListPagination, { DEFAULT_PAGE_SIZE } from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import StudyDetailDrawer from '../components/StudyDetailDrawer.vue'
import { session } from '../session'
import { ALL_MILESTONE_SUB_STATUSES } from '../domain/milestone-filters'
import { useClientSort } from '../composables/useClientSort'

const router = useRouter()
const canReadMonthly = computed(() =>
  session.currentUser.value?.permissions.includes('monthly.read') ?? false,
)
const canReadMilestone = computed(() =>
  session.currentUser.value?.permissions.includes('milestone.read') ?? false,
)

const result = ref<StudyPage>()
const loading = ref(true)
const error = ref('')
const filters = reactive({ ta: '', program: '', status: '', page: 1, pageSize: DEFAULT_PAGE_SIZE })

const TA_OPTIONS = ['肿瘤', '自身免疫', '代谢与心血管', '呼吸系统', '感染性疾病', '神经科学']
const statusOptions = ALL_MILESTONE_SUB_STATUSES

const studies = computed(() => result.value?.data ?? [])
const total = computed(() => result.value?.total ?? 0)
const page = computed(() => result.value?.page ?? filters.page)
const pageSize = computed(() => result.value?.pageSize ?? filters.pageSize)
const totalPages = computed(() => result.value?.totalPages ?? 1)

function plPm(study: Study): string {
  return [study.plName, study.pmName].filter(Boolean).join(' / ')
}

const {
  sorted: sortedStudies,
  registerMany: registerStudySortColumns,
  toggle: toggleStudySort,
  getDirection: studySortDirection,
} = useClientSort({ items: studies })

registerStudySortColumns([
  { key: 'ta', resolver: (s) => s.therapeuticAreaName || s.therapeuticArea || s.therapeuticAreaCode, type: 'string' },
  { key: 'program', resolver: (s) => s.programCode || s.program, type: 'string' },
  { key: 'product', resolver: (s) => s.productName || s.product, type: 'string' },
  { key: 'studyNo', resolver: (s) => s.code, type: 'string' },
  { key: 'indication', resolver: (s) => s.indication, type: 'string' },
  { key: 'phase', resolver: (s) => s.currentPhase, type: 'string' },
  { key: 'status', resolver: (s) => s.currentStatus, type: 'string' },
  { key: 'plPm', resolver: (s) => plPm(s), type: 'string' },
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

async function load() {
  loading.value = true
  error.value = ''
  try {
    result.value = await apiClient.listStudies({
      therapeuticArea: filters.ta || undefined,
      program: filters.program || undefined,
      milestoneStatus: filters.status || undefined,
      page: filters.page,
      pageSize: filters.pageSize,
    })
    // Keep local page in sync if backend clamps out-of-range page.
    filters.page = result.value.page
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '研究数据加载失败'
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  filters.page = 1
  void load()
}

function changePage(next: number) {
  if (next < 1 || next > totalPages.value) return
  filters.page = next
  void load()
}

function changePageSize(nextSize: number) {
  filters.pageSize = nextSize
  filters.page = 1
  void load()
}

watch(() => [filters.ta, filters.status], applyFilters)

onMounted(load)
</script>

<template>
  <section class="page-content">
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

    <PageState :loading :error :empty="!studies.length">
      <div class="data-card">
        <table class="data-table">
          <thead><tr>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('ta') === 'asc', 'sort-desc': studySortDirection('ta') === 'desc' }" @click="toggleStudySort('ta')">TA</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('program') === 'asc', 'sort-desc': studySortDirection('program') === 'desc' }" @click="toggleStudySort('program')">Program</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('product') === 'asc', 'sort-desc': studySortDirection('product') === 'desc' }" @click="toggleStudySort('product')">Product</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('studyNo') === 'asc', 'sort-desc': studySortDirection('studyNo') === 'desc' }" @click="toggleStudySort('studyNo')">Study No.</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('indication') === 'asc', 'sort-desc': studySortDirection('indication') === 'desc' }" @click="toggleStudySort('indication')">适应症</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('phase') === 'asc', 'sort-desc': studySortDirection('phase') === 'desc' }" @click="toggleStudySort('phase')">里程碑阶段</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('status') === 'asc', 'sort-desc': studySortDirection('status') === 'desc' }" @click="toggleStudySort('status')">里程碑节点</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('plPm') === 'asc', 'sort-desc': studySortDirection('plPm') === 'desc' }" @click="toggleStudySort('plPm')">PL/PM</th>
            <th class="sortable" :class="{ 'sort-asc': studySortDirection('updatedAt') === 'asc', 'sort-desc': studySortDirection('updatedAt') === 'desc' }" @click="toggleStudySort('updatedAt')">更新时间</th>
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
              <td>{{ plPm(study) || '—' }}</td>
              <td>{{ study.updatedAt ? new Date(study.updatedAt).toLocaleDateString('zh-CN') : '—' }}</td>
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
