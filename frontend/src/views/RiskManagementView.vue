<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { apiClient } from '../api/client'
import type { RiskFormOptions, RiskLevel, RiskPage, RiskStatus, RiskSummary } from '../api/types'
import ListPagination, { DEFAULT_PAGE_SIZE } from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import RiskEditorDrawer from '../components/RiskEditorDrawer.vue'
import {
  riskLevelLabel,
  riskLevelTone,
  riskScoreRuleLines,
  riskStatusLabel,
} from '../domain/risk-labels'
import { session } from '../session'

const result = ref<RiskPage>()
const formOptions = ref<RiskFormOptions>()
const loading = ref(true)
const error = ref('')
const drawerOpen = ref(false)
const selectedRiskCode = ref<string>()
const filters = reactive({
  query: '',
  functionCode: '',
  status: '' as RiskStatus | '',
  level: '' as RiskLevel | '',
  sortBy: 'updatedAt' as const,
  sortOrder: 'desc' as const,
  page: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})
const scoreTip = ref<{ left: number; top: number } | null>(null)

const permissions = computed(() => session.currentUser.value?.permissions ?? [])
const canCreate = computed(() => permissions.value.includes('risk.create'))
const functionOptions = computed(() => formOptions.value?.functions ?? [])
const scoreRuleLines = computed(() =>
  riskScoreRuleLines(formOptions.value?.scoringRule),
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [page, options] = await Promise.all([
      apiClient.listRisks({ ...filters }),
      formOptions.value ? Promise.resolve(formOptions.value) : apiClient.getRiskFormOptions(),
    ])
    result.value = page
    formOptions.value = options
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '风险数据加载失败'
  } finally {
    loading.value = false
  }
}

function applyFilters() { filters.page = 1; void load() }
function quickFilter(type: 'total' | 'open' | 'high' | 'medium') {
  filters.status = type === 'open' ? 'OPEN' : ''
  filters.level = type === 'high' ? 'HIGH' : type === 'medium' ? 'MEDIUM' : ''
  applyFilters()
}
function openCreate() { selectedRiskCode.value = undefined; drawerOpen.value = true }
function openRisk(risk: RiskSummary) { selectedRiskCode.value = risk.riskCode; drawerOpen.value = true }
function closeDrawer() { drawerOpen.value = false; selectedRiskCode.value = undefined }
async function saved() { closeDrawer(); await load() }
function changePage(page: number) {
  filters.page = page
  void load()
}

function changePageSize(pageSize: number) {
  filters.pageSize = pageSize
  filters.page = 1
  void load()
}

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

onMounted(load)
</script>

<template>
  <section class="page-content risk-page">
    <form class="page-toolbar risk-toolbar" role="search" @submit.prevent="applyFilters">
      <div class="toolbar-filters">
        <label class="inline-search"><span class="sr-only">搜索风险</span><input v-model="filters.query" type="search" placeholder="搜索编号 / 描述 / Owner / Program"></label>
        <label><span class="sr-only">功能线</span><select v-model="filters.functionCode" @change="applyFilters"><option value="">全部功能线</option><option v-for="item in functionOptions" :key="item.code" :value="item.code">{{ item.name }}</option></select></label>
        <label><span class="sr-only">风险状态</span><select v-model="filters.status" @change="applyFilters"><option value="">全部状态</option><option value="OPEN">未关闭</option><option value="CLOSED">已关闭</option></select></label>
        <label><span class="sr-only">风险等级</span><select v-model="filters.level" @change="applyFilters"><option value="">全部等级</option><option value="HIGH">高危</option><option value="MEDIUM">中风险</option><option value="LOW">低风险</option></select></label>
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

    <PageState :loading :error :empty="!result?.data.length" empty-title="暂无风险记录">
      <div class="data-card risk-table-card">
        <table class="data-table risk-table">
          <thead><tr><th>Risk ID</th><th>Study No.</th><th>Program / Project</th><th>功能线</th><th>风险描述</th><th>Owner</th><th>评分</th><th>等级</th><th>措施</th><th>Status</th></tr></thead>
          <tbody><tr v-for="risk in result?.data" :key="risk.riskCode" tabindex="0" @click="openRisk(risk)" @keydown.enter="openRisk(risk)">
            <td><button class="risk-link mono" type="button" @click.stop="openRisk(risk)">{{ risk.riskCode }}</button></td>
            <td class="mono">{{ risk.studyCode }}</td><td><strong>{{ risk.programCode }}</strong><small>{{ risk.projectCode }}</small></td><td>{{ risk.functionName }}</td><td class="risk-description">{{ risk.description }}</td><td>{{ risk.ownerName }}</td>
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
            <td><span class="status-chip" :class="`status-chip--${riskLevelTone(risk.level)}`">{{ riskLevelLabel(risk.level) }}</span></td><td><span v-if="risk.actionCount" class="status-chip status-chip--blue">含 {{ risk.actionCount }} 项</span><span v-else>—</span></td><td><span class="status-chip" :class="risk.status === 'OPEN' ? 'status-chip--orange' : 'status-chip--green'">{{ riskStatusLabel(risk.status) }}</span></td>
          </tr></tbody>
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
