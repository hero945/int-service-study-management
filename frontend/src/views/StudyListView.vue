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
import { formatDateTimeSeconds } from '../domain/date-format'
import { groupByProject } from '../domain/pipeline-aggregation'
import { useClientSort } from '../composables/useClientSort'
import { usePagedList } from '../composables/usePagedList'
import { usePermissions } from '../composables/usePermissions'
import { useResizableColumns } from '../composables/useResizableColumns'

const router = useRouter()
const { can } = usePermissions()
const studyCols = useResizableColumns('study-list-v3', {
  ta: 140, program: 150, project: 160, studyNo: 150, plPm: 140, updatedAt: 160, actions: 130,
})
const canReadMonthly = can('monthly.read')
const canReadMilestone = can('milestone.read')
const canRegister = can('project.milestone.read')

const filters = reactive({ ta: '', program: '', project: '', studyCode: '' })

const {
  result, loading, error, page, pageSize,
  load, applyFilters, changePage, changePageSize,
} = usePagedList({
  filters,
  errorMessage: '研究数据加载失败',
  fetcher: (q) => apiClient.listStudies({
    therapeuticArea: q.ta || undefined,
    program: q.program || undefined,
    project: q.project || undefined,
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
} = useClientSort({ items: studies, initialKey: 'project', initialDirection: 'asc' })

registerStudySortColumns([
  { key: 'ta', resolver: (s) => s.therapeuticAreaName || s.therapeuticArea || s.therapeuticAreaCode, type: 'string' },
  { key: 'program', resolver: (s) => s.programCode || s.program, type: 'string' },
  { key: 'project', resolver: (s) => s.projectCode || s.project, type: 'string' },
  { key: 'studyNo', resolver: (s) => s.code, type: 'string' },
  { key: 'plPm', resolver: (s) => plPmLabel(s), type: 'string' },
  { key: 'updatedAt', resolver: (s) => s.updatedAt, type: 'date' },
])

const projectGroups = computed(() => groupByProject(sortedStudies.value))
const pageProjectCount = computed(() => projectGroups.value.length)

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
          <span class="filter-field__label">Project</span>
          <input v-model.trim="filters.project" type="text" class="filter-input" placeholder="输入 Project 编号">
        </label>
        <label class="filter-field">
          <span class="filter-field__label">Study</span>
          <input v-model.trim="filters.studyCode" type="text" class="filter-input" placeholder="输入 Study 编号">
        </label>
        <button class="secondary-button" type="submit">搜索</button>
      </div>
      <span class="filter-count">{{ pageProjectCount }} 个项目 · {{ total }} 个研究</span>
    </form>

    <PageState :loading :error retryable :empty="!studies.length" @retry="load">
      <div class="data-card">
        <table class="data-table" :style="studyCols.tableStyle()">
          <thead><tr>
            <th v-bind="studySortHeader('ta')" :style="studyCols.fillColStyle('ta')">TA<span class="col-resizer" @pointerdown="studyCols.startResize('ta', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('program')" :style="studyCols.fillColStyle('program')">Program (MOA)<span class="col-resizer" @pointerdown="studyCols.startResize('program', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('project')" :style="studyCols.fillColStyle('project')">Project (Indication)<span class="col-resizer" @pointerdown="studyCols.startResize('project', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('studyNo')" :style="studyCols.fillColStyle('studyNo')">Study No.<span class="col-resizer" @pointerdown="studyCols.startResize('studyNo', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('plPm')" :style="studyCols.fillColStyle('plPm')">PL/PM<span class="col-resizer" @pointerdown="studyCols.startResize('plPm', $event)" @click.stop></span></th>
            <th v-bind="studySortHeader('updatedAt')" :style="studyCols.fillColStyle('updatedAt')" title="该研究记录最近一次保存的时间">更新时间<span class="col-resizer" @pointerdown="studyCols.startResize('updatedAt', $event)" @click.stop></span></th>
            <th :style="studyCols.fillColStyle('actions')">操作<span class="col-resizer" @pointerdown="studyCols.startResize('actions', $event)" @click.stop></span></th>
          </tr></thead>
          <tbody>
            <template v-for="group in projectGroups" :key="group.projectCode">
              <tr
                v-for="(study, index) in group.studies"
                :key="study.id"
                class="study-row--clickable"
                @click="openDrawer(study)"
              >
                <td
                  v-if="index === 0"
                  class="project-group-cell"
                  :rowspan="group.studies.length"
                  @click.stop
                >
                  <span class="area-dot" :class="areaDotClass(group.therapeuticAreaCode)"></span>{{ group.therapeuticAreaName || group.therapeuticAreaCode || '—' }}
                </td>
                <td
                  v-if="index === 0"
                  class="project-group-cell project-group-id"
                  :rowspan="group.studies.length"
                  @click.stop
                >
                  <strong class="mono">{{ group.programCode || '—' }}</strong>
                  <small>{{ group.moa || '—' }}</small>
                </td>
                <td
                  v-if="index === 0"
                  class="project-group-cell project-group-id"
                  :rowspan="group.studies.length"
                  @click.stop
                >
                  <span class="project-group-id__title">
                    <strong class="mono">{{ group.projectCode || '—' }}</strong>
                    <button
                      v-if="canRegister"
                      class="link-button"
                      @click="goProjectMilestones(group.studies[0].id)"
                    >注册</button>
                  </span>
                  <small>{{ group.indication || '—' }}</small>
                </td>
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
                <td>{{ plPmLabel(study) || '—' }}</td>
                <td class="mono">{{ formatDateTimeSeconds(study.updatedAt) }}</td>
                <td>
                  <div class="actions">
                    <button
                      v-if="canReadMilestone"
                      class="link-button"
                      @click.stop="goMilestones(study.id)"
                    >里程碑</button>
                    <button v-if="canReadMonthly" class="link-button" @click.stop="goMonthlyReport(study.id)">月报</button>
                  </div>
                </td>
              </tr>
            </template>
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
