<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { Study } from '../api/types'
import ListPagination from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import StudyDetailDrawer from '../components/StudyDetailDrawer.vue'
import { TA_OPTIONS, areaDotClass } from '../domain/therapeutic-areas'
import { plPmLabel } from '../domain/study-labels'
import { formatDate } from '../domain/date-format'
import { useClientSort } from '../composables/useClientSort'
import { usePagedList } from '../composables/usePagedList'
import { usePermissions } from '../composables/usePermissions'
import { useResizableColumns } from '../composables/useResizableColumns'

const router = useRouter()
const { can } = usePermissions()
const studyCols = useResizableColumns('study-list', {
  ta: 140, program: 120, product: 120, studyNo: 160, indication: 160, plPm: 140, updatedAt: 120, actions: 180,
})
const canReadMonthly = can('monthly.read')
const canReadMilestone = can('milestone.read')
const canRegister = can('project.milestone.read')

const filters = reactive({ ta: '', program: '', product: '', studyCode: '' })

const {
  result, loading, error, page, pageSize,
  load, applyFilters, changePage, changePageSize,
} = usePagedList({
  filters,
  errorMessage: '研究数据加载失败',
  fetcher: (q) => apiClient.listStudies({
    therapeuticArea: q.ta || undefined,
    program: q.program || undefined,
    product: q.product || undefined,
    studyCode: q.studyCode || undefined,
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

function goProjectMilestones(studyId: number) {
  router.push(`/studies/${studyId}/project-milestones`)
}

function goMilestones(studyId: number) {
  router.push(`/milestones/${studyId}`)
}

function goMonthlyReport(studyId: number) {
  router.push(`/studies/${studyId}/monthly-report`)
}

watch(() => [filters.ta], applyFilters)

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
          <span class="filter-field__label">Product</span>
          <input v-model.trim="filters.product" type="text" class="filter-input" placeholder="输入产品名搜索">
        </label>
        <label class="filter-field">
          <span class="filter-field__label">Study</span>
          <input v-model.trim="filters.studyCode" type="text" class="filter-input" placeholder="输入 Study 编号">
        </label>
        <button class="secondary-button" type="submit">搜索</button>
      </div>
      <span class="filter-count">共 {{ total }} 个研究</span>
    </form>

    <PageState :loading :error retryable :empty="!studies.length" @retry="load">
      <div class="data-card">
        <table class="data-table" :style="studyCols.tableStyle()">
          <thead><tr>
            <th v-bind="studySortHeader('ta')" :style="studyCols.colStyle('ta')">TA<span class="col-resizer" @pointerdown="studyCols.startResize('ta', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('program')" :style="studyCols.colStyle('program')">Program<span class="col-resizer" @pointerdown="studyCols.startResize('program', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('product')" :style="studyCols.colStyle('product')">Product<span class="col-resizer" @pointerdown="studyCols.startResize('product', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('studyNo')" :style="studyCols.colStyle('studyNo')">Study No.<span class="col-resizer" @pointerdown="studyCols.startResize('studyNo', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('indication')" :style="studyCols.colStyle('indication')">适应症<span class="col-resizer" @pointerdown="studyCols.startResize('indication', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('plPm')" :style="studyCols.colStyle('plPm')">PL/PM<span class="col-resizer" @pointerdown="studyCols.startResize('plPm', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('updatedAt')" :style="studyCols.colStyle('updatedAt')">更新时间<span class="col-resizer" @pointerdown="studyCols.startResize('updatedAt', $event)" @click.stop></span></th>
            <th :style="studyCols.fluidColStyle('actions')">操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="study in sortedStudies" :key="study.id" class="study-row--clickable" @click="openDrawer(study)">
              <td>
                <span class="area-dot" :class="areaDotClass(study.therapeuticAreaCode)"></span>{{ study.therapeuticAreaName || study.therapeuticArea || study.therapeuticAreaCode || '—' }}
              </td>
              <td class="mono">{{ study.programCode || study.program || '—' }}</td>
              <td class="mono">{{ study.productName || study.product || '—' }}</td>
              <td class="mono strong">
                <span class="study-no-with-risk">
                  {{ study.code }}
                  <span
                    v-if="study.openRiskCount"
                    class="study-risk-badge"
                    title="Open 风险"
                  >{{ study.openRiskCount }}</span>
                </span>
              </td>
              <td>{{ study.indication }}</td>
              <td>{{ plPmLabel(study) || '—' }}</td>
              <td>{{ formatDate(study.updatedAt) }}</td>
              <td>
                <div class="actions">
                  <button
                    v-if="canRegister"
                    class="link-button"
                    @click.stop="goProjectMilestones(study.id)"
                  >注册</button>
                  <button
                    v-if="canReadMilestone"
                    class="link-button"
                    @click.stop="goMilestones(study.id)"
                  >里程碑</button>
                  <button v-if="canReadMonthly" class="link-button" @click.stop="goMonthlyReport(study.id)">月报</button>
                </div>
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
