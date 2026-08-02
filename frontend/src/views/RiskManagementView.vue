<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import type { RiskFormOptions, RiskLevel, RiskPage, RiskStatus, RiskSummary } from '../api/types'
import ListPagination from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import RiskEditorDrawer from '../components/RiskEditorDrawer.vue'
import { formatDateTime } from '../domain/date-format'
import {
  riskActionSummaryLabel,
  riskLevelLabel,
  riskLevelTone,
  riskScoreRuleLines,
  riskStatusLabel,
} from '../domain/risk-labels'
import { usePagedList } from '../composables/usePagedList'
import { usePermissions } from '../composables/usePermissions'
import { useServerSort } from '../composables/useServerSort'

const route = useRoute()
const router = useRouter()
const formOptions = ref<RiskFormOptions>()
const drawerOpen = ref(false)
const selectedRiskCode = ref<string>()
const filters = reactive({
  query: '',
  functionCode: '',
  status: '' as RiskStatus | '',
  level: '' as RiskLevel | '',
  overdueOnly: false,
})
const scoreTip = ref<{ left: number; top: number } | null>(null)

const { can } = usePermissions()
const canCreate = can('risk.create')
const functionOptions = computed(() => formOptions.value?.functions ?? [])
const scoreRuleLines = computed(() => riskScoreRuleLines(formOptions.value?.scoringRule))

type RiskSortKey = 'updatedAt' | 'riskCode' | 'studyCode' | 'score' | 'level' | 'registeredDate'

const {
  result, loading, error,
  load, applyFilters, changePage, changePageSize,
} = usePagedList({
  filters,
  errorMessage: '风险数据加载失败',
  fetcher: (q): Promise<RiskPage> => apiClient.listRisks({
    query: q.query || undefined,
    functionCode: q.functionCode || undefined,
    status: q.status || undefined,
    level: q.level || undefined,
    overdueOnly: q.overdueOnly || undefined,
    sortBy: sortKey.value,
    sortOrder: sortDirection.value,
    page: q.page,
    pageSize: q.pageSize,
  }),
})

const { sortKey, sortDirection, sortHeader } = useServerSort<RiskSortKey>({
  initialKey: 'updatedAt',
  initialDirection: 'desc',
  defaultDirection: (key) => (key === 'updatedAt' ? 'desc' : 'asc'),
  onChange: applyFilters,
})

const risks = computed(() => result.value?.data ?? [])

async function loadWithOptions() {
  await Promise.all([
    load(),
    formOptions.value
      ? Promise.resolve()
      : apiClient.getRiskFormOptions().then((options) => { formOptions.value = options }),
  ])
}

function quickFilter(type: 'total' | 'open' | 'high' | 'medium') {
  filters.status = type === 'open' ? 'OPEN' : ''
  filters.level = type === 'high' ? 'HIGH' : type === 'medium' ? 'MEDIUM' : ''
  applyFilters()
}
function openCreate() { selectedRiskCode.value = undefined; drawerOpen.value = true }
function openRisk(risk: RiskSummary) { selectedRiskCode.value = risk.riskCode; drawerOpen.value = true }
function closeDrawer() {
  drawerOpen.value = false
  selectedRiskCode.value = undefined
  if (route.query.riskCode) {
    const next = { ...route.query }
    delete next.riskCode
    void router.replace({ query: next })
  }
}
async function saved() { closeDrawer(); await load() }

function showScoreTip(event: FocusEvent | MouseEvent) {
  const el = event.currentTarget as HTMLElement | null
  if (!el) return
  const rect = el.getBoundingClientRect()
  scoreTip.value = {
    left: Math.round(rect.left + rect.width / 2),
    top: Math.round(rect.top - 8),
  }
}
function hideScoreTip() { scoreTip.value = null }

function openFromQuery() {
  const code = typeof route.query.riskCode === 'string' ? route.query.riskCode : ''
  if (code) {
    selectedRiskCode.value = code
    drawerOpen.value = true
  }
}

watch(() => route.query.riskCode, () => openFromQuery())
onMounted(async () => {
  await loadWithOptions()
  openFromQuery()
})
</script>

<template>
  <section class="page-content page-content--fill risk-page">
    <form class="page-toolbar risk-toolbar" role="search" @submit.prevent="applyFilters">
      <div class="toolbar-filters">
        <label class="inline-search">
          <span class="sr-only">搜索风险</span>
          <input v-model="filters.query" type="search" placeholder="搜索编号 / Study / 描述 / Owner / Program / Project">
        </label>
        <label>
          <span class="sr-only">功能线</span>
          <select v-model="filters.functionCode" @change="applyFilters">
            <option value="">全部功能线</option>
            <option v-for="item in functionOptions" :key="item.code" :value="item.code">{{ item.name }}</option>
          </select>
        </label>
        <label>
          <span class="sr-only">风险状态</span>
          <select v-model="filters.status" @change="applyFilters">
            <option value="">全部状态</option>
            <option value="OPEN">未关闭</option>
            <option value="CLOSED">已关闭</option>
          </select>
        </label>
        <label>
          <span class="sr-only">风险等级</span>
          <select v-model="filters.level" @change="applyFilters">
            <option value="">全部等级</option>
            <option value="HIGH">高危</option>
            <option value="MEDIUM">中风险</option>
            <option value="LOW">低风险</option>
          </select>
        </label>
        <label class="risk-overdue-filter">
          <input v-model="filters.overdueOnly" type="checkbox" @change="applyFilters">
          仅看逾期
        </label>
        <button class="secondary-button" type="submit">搜索</button>
      </div>
      <button v-if="canCreate" class="primary-button" type="button" @click="openCreate">＋ 新增风险</button>
    </form>

    <div class="risk-stats" aria-label="风险统计">
      <button type="button" :class="{ active: !filters.status && !filters.level }" @click="quickFilter('total')">
        <span>风险总数</span><strong>{{ result?.stats.total ?? 0 }}</strong>
      </button>
      <button type="button" :class="{ active: filters.status === 'OPEN' }" @click="quickFilter('open')">
        <span>未关闭</span><strong>{{ result?.stats.open ?? 0 }}</strong>
      </button>
      <button type="button" :class="{ active: filters.level === 'HIGH' }" @click="quickFilter('high')">
        <span>高危</span><strong class="risk-stat--red">{{ result?.stats.high ?? 0 }}</strong>
      </button>
      <button type="button" :class="{ active: filters.level === 'MEDIUM' }" @click="quickFilter('medium')">
        <span>中风险</span><strong class="risk-stat--orange">{{ result?.stats.medium ?? 0 }}</strong>
      </button>
    </div>

    <PageState :loading :error retryable :empty="!result?.data.length" empty-title="暂无风险记录" @retry="loadWithOptions">
      <div class="data-card risk-table-card">
        <table class="data-table risk-table">
          <thead>
            <tr>
              <th v-bind="sortHeader('riskCode')">Risk ID</th>
              <th v-bind="sortHeader('studyCode')">Study No.</th>
              <th>Program / Project</th>
              <th>功能线</th>
              <th>风险描述</th>
              <th>Owner</th>
              <th v-bind="sortHeader('score')">评分</th>
              <th v-bind="sortHeader('level')">等级</th>
              <th>措施</th>
              <th>Status</th>
              <th v-bind="sortHeader('updatedAt')">更新时间</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="risk in risks"
              :key="risk.riskCode"
              tabindex="0"
              @click="openRisk(risk)"
              @keydown.enter="openRisk(risk)"
              @keydown.space.prevent="openRisk(risk)"
            >
              <td>
                <button class="risk-link mono" type="button" @click.stop="openRisk(risk)">{{ risk.riskCode }}</button>
              </td>
              <td class="mono">{{ risk.studyCode }}</td>
              <td><strong>{{ risk.programCode }}</strong><small>{{ risk.projectCode }}</small></td>
              <td>{{ risk.functionName }}</td>
              <td class="risk-description">{{ risk.description }}</td>
              <td>{{ risk.ownerName }}</td>
              <td>
                <span
                  class="risk-score mono"
                  tabindex="0"
                  aria-describedby="risk-score-rule-tip"
                  @mouseenter="showScoreTip"
                  @mouseleave="hideScoreTip"
                  @focus="showScoreTip"
                  @blur="hideScoreTip"
                  @click.stop
                >{{ risk.score }}</span>
              </td>
              <td>
                <span class="status-chip" :class="`status-chip--${riskLevelTone(risk.level)}`">
                  {{ riskLevelLabel(risk.level) }}
                </span>
              </td>
              <td>
                <span
                  v-if="risk.actionCount"
                  class="status-chip"
                  :class="risk.overdueActionCount ? 'status-chip--red' : 'status-chip--blue'"
                >{{ riskActionSummaryLabel(risk) }}</span>
                <span v-else>—</span>
              </td>
              <td>
                <span class="status-chip" :class="risk.status === 'OPEN' ? 'status-chip--orange' : 'status-chip--green'">
                  {{ riskStatusLabel(risk.status) }}
                </span>
              </td>
              <td class="mono">{{ formatDateTime(risk.updatedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </PageState>

    <ListPagination
      v-if="!loading && !error && result"
      :total="result.pagination.totalItems"
      :page="result.pagination.page"
      :page-size="result.pagination.pageSize"
      :total-pages="result.pagination.totalPages"
      aria-label="风险列表分页"
      @update:page="changePage"
      @update:page-size="changePageSize"
    />

    <Teleport to="body">
      <div
        v-if="scoreTip"
        id="risk-score-rule-tip"
        class="risk-score-rule-tip"
        role="tooltip"
        :style="{ left: `${scoreTip.left}px`, top: `${scoreTip.top}px` }"
      >
        <strong>评分计算规则</strong>
        <p v-for="line in scoreRuleLines" :key="line">{{ line }}</p>
      </div>
    </Teleport>

    <RiskEditorDrawer :open="drawerOpen" :risk-code="selectedRiskCode" @close="closeDrawer" @saved="saved" />
  </section>
</template>
