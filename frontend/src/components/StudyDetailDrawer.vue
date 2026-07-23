<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { apiClient } from '../api/client'
import type { Study, MilestonePage, MonthlyReportPage, RiskPage, TeamMatrixPage } from '../api/types'
import PageState from '../components/PageState.vue'

const props = defineProps<{
  open: boolean
  study: Study | null
}>()

const emit = defineEmits<{ close: [] }>()

type TabKey = 'milestone' | 'monthly' | 'team' | 'risk'
const activeTab = ref<TabKey>('milestone')

// ── per-tab state ──
const milestoneLoading = ref(false)
const milestoneError = ref('')
const milestoneData = ref<MilestonePage>()

const monthlyLoading = ref(false)
const monthlyError = ref('')
const monthlyData = ref<MonthlyReportPage>()

const teamLoading = ref(false)
const teamError = ref('')
const teamData = ref<TeamMatrixPage>()

const riskLoading = ref(false)
const riskError = ref('')
const riskData = ref<RiskPage>()

// counts for tab badges
const riskCount = computed(() => riskData.value?.data.length ?? 0)
const teamCount = computed(() => {
  const s = props.study
  if (!teamData.value || !s) return 0
  return teamData.value.assignments.filter(a => a.studyId === s.id)
    .reduce((sum, a) => sum + a.members.length, 0)
})

// current month string YYYY-MM
const currentMonth = (() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
})()

watch(() => props.open, (val) => {
  if (val && props.study) {
    activeTab.value = 'milestone'
    loadMilestone()
    loadMonthly()
    loadTeam()
    loadRisks()
  }
})

function loadMilestone() {
  if (!props.study) return
  milestoneLoading.value = true; milestoneError.value = ''
  apiClient.getMilestones(props.study.id)
    .then(d => { milestoneData.value = d })
    .catch(e => { milestoneError.value = e instanceof Error ? e.message : '里程碑加载失败' })
    .finally(() => { milestoneLoading.value = false })
}

function loadMonthly() {
  if (!props.study) return
  monthlyLoading.value = true; monthlyError.value = ''
  apiClient.getMonthlyReports(props.study.id, currentMonth)
    .then(d => { monthlyData.value = d })
    .catch(e => { monthlyError.value = e instanceof Error ? e.message : '月度进展加载失败' })
    .finally(() => { monthlyLoading.value = false })
}

async function loadTeam() {
  if (!props.study) return
  teamLoading.value = true; teamError.value = ''
  try {
    // 团队矩阵接口单次查询 pageSize 上限已提到 @Max(100)（限返回的 study 条数，成员本身不截断）。
    // study.code 是 LIKE 匹配，可能命中多个 study；翻页累积所有页（每页最多 100），
    // 按 studyId|roleCode 去重合并 assignments，确保拿全当前 study 的成员。
    const PAGE_SIZE = 100
    let page = 1
    let merged: TeamMatrixPage | null = null
    let totalPages = 1
    do {
      const d = await apiClient.listTeamMatrix({
        studyQuery: props.study.code,
        page,
        pageSize: PAGE_SIZE,
      })
      if (!merged) {
        merged = {
          studies: [...d.studies],
          roles: [...d.roles],
          assignments: [...d.assignments],
          totalRoles: d.totalRoles,
          pagination: d.pagination,
        }
      } else {
        const studyIds = new Set(merged.studies.map(s => s.studyId))
        for (const s of d.studies) if (!studyIds.has(s.studyId)) merged.studies.push(s)
        const roleCodes = new Set(merged.roles.map(r => r.roleCode))
        for (const r of d.roles) if (!roleCodes.has(r.roleCode)) merged.roles.push(r)
        const aKeys = new Set(merged.assignments.map(a => `${a.studyId}|${a.roleCode}`))
        for (const a of d.assignments) {
          const key = `${a.studyId}|${a.roleCode}`
          if (!aKeys.has(key)) { merged.assignments.push(a); aKeys.add(key) }
        }
      }
      totalPages = d.pagination.totalPages
      page++
    } while (page <= totalPages)
    teamData.value = merged
  } catch (e) {
    teamError.value = e instanceof Error ? e.message : '团队数据加载失败'
  } finally {
    teamLoading.value = false
  }
}

function loadRisks() {
  if (!props.study) return
  riskLoading.value = true; riskError.value = ''
  apiClient.listRisks({ studyId: props.study.id, pageSize: 100 })
    .then(d => { riskData.value = d })
    .catch(e => { riskError.value = e instanceof Error ? e.message : '风险数据加载失败' })
    .finally(() => { riskLoading.value = false })
}

// ── helpers ──
function msStatus(s: string) {
  return { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' }[s] ?? s
}
function msStatusClass(s: string) {
  return s === 'COMPLETED' ? 'green' : s === 'IN_PROGRESS' ? 'blue' : ''
}
function riskLevelLabel(l: string) { return { LOW: '低风险', MEDIUM: '中风险', HIGH: '高危' }[l as keyof object] ?? l }
function riskLevelTone(l: string) { return l === 'HIGH' ? 'red' : l === 'MEDIUM' ? 'orange' : 'green' }

// 团队角色模板：先渲染所有角色，再用当前 study 的后端数据填充（空角色显示「暂无成员」）
const teamRoles = computed(() => {
  if (!teamData.value || !props.study) return []
  const studyId = props.study.id
  return teamData.value.roles.map(role => {
    const assignment = teamData.value!.assignments.find(
      a => a.studyId === studyId && a.roleCode === role.roleCode)
    return {
      roleCode: role.roleCode,
      roleName: role.roleName,
      functionName: role.functionName,
      members: assignment?.members ?? [],
    }
  })
})

const teamHasMembers = computed(() => teamRoles.value.some(r => r.members.length > 0))

const tabs: { key: TabKey; label: string }[] = [
  { key: 'milestone', label: '里程碑' },
  { key: 'monthly', label: '月度进展' },
  { key: 'team', label: '团队' },
  { key: 'risk', label: '风险' },
]
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="drawer-overlay" @click.self="emit('close')" @keydown.escape="emit('close')">
      <aside class="study-drawer" role="dialog" aria-modal="true" :aria-label="study?.code ?? 'Study 详情'">
        <!-- header -->
        <header class="drawer-header">
          <div class="drawer-header__title">
            <strong v-if="study" class="mono">{{ study.code }}</strong>
            <span v-if="study">{{ study.indication || '—' }}</span>
          </div>
          <button class="drawer-close" type="button" @click="emit('close')" aria-label="关闭">×</button>
        </header>

        <div v-if="study" class="drawer-meta">
          <span v-if="study.program" class="drawer-tag">{{ study.program }}</span>
          <span v-if="study.project" class="drawer-tag drawer-tag--muted">{{ study.project }}</span>
          <span class="drawer-meta__date">最近更新 {{ study.updatedAt.slice(0, 10) }}</span>
        </div>

        <!-- tabs -->
        <nav class="drawer-tabs" role="tablist">
          <button
            v-for="tab in tabs" :key="tab.key"
            role="tab"
            :class="{ 'drawer-tab--active': activeTab === tab.key }"
            class="drawer-tab"
            type="button"
            @click="activeTab = tab.key"
          >{{ tab.label }}
            <span v-if="tab.key === 'risk' && riskCount" class="drawer-tab-badge">{{ riskCount }}</span>
            <span v-if="tab.key === 'team' && teamCount" class="drawer-tab-badge">{{ teamCount }}</span>
          </button>
        </nav>

        <!-- panels -->
        <div class="drawer-body">
          <!-- ── milestone ── -->
          <section v-show="activeTab === 'milestone'" role="tabpanel" class="drawer-panel">
            <PageState :loading="milestoneLoading" :error="milestoneError" :empty="!milestoneData?.groups.length" empty-title="暂无里程碑数据">
              <div v-if="milestoneData" class="drawer-milestone">
                <div v-for="group in milestoneData.groups" :key="group.stageCode" class="ms-group">
                  <div class="ms-group-title">{{ group.stageName }}</div>
                  <table class="data-table ms-table">
                    <thead><tr><th>Milestone</th><th>Ver 1.0</th><th>Ver 2.0</th><th>Actual Start</th><th>Actual End</th><th>Note</th></tr></thead>
                    <tbody>
                      <tr v-for="node in group.nodes" :key="node.milestoneCode">
                        <td class="ms-name">
                          <span class="milestone-dot" :class="`milestone-dot--${node.status}`"></span>
                          {{ node.milestoneName }}
                        </td>
                        <td class="mono">{{ node.planV1Date || '—' }}</td>
                        <td class="mono">{{ node.planV2Date || '—' }}</td>
                        <td class="mono">{{ node.actualStartDate || '—' }}</td>
                        <td class="mono">{{ node.actualEndDate || '—' }}</td>
                        <td>{{ node.deviationNote || '—' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </PageState>
          </section>

          <!-- ── monthly progress ── -->
          <section v-show="activeTab === 'monthly'" role="tabpanel" class="drawer-panel">
            <PageState :loading="monthlyLoading" :error="monthlyError" :empty="!monthlyData?.functionLines.length" empty-title="暂无月度进展">
              <div v-if="monthlyData" class="drawer-monthly">
                <div v-for="line in monthlyData.functionLines" :key="line.functionLineId" class="mp-line">
                  <div class="mp-line-header">
                    <span class="mp-line-code">{{ line.functionCode }}</span>
                    <span class="mp-line-name">{{ line.functionName }}</span>
                  </div>
                  <div v-if="line.entries.length" class="mp-entries">
                    <div v-for="entry in line.entries" :key="entry.entryId" class="mp-entry">
                      <span class="mp-entry-date mono">{{ entry.entryDate.slice(0, 10) }}</span>
                      <span class="mp-entry-content">{{ entry.content }}</span>
                    </div>
                  </div>
                  <p v-else class="mp-empty">本月暂无进展记录</p>
                </div>
              </div>
            </PageState>
          </section>

          <!-- ── team ── -->
          <section v-show="activeTab === 'team'" role="tabpanel" class="drawer-panel">
            <PageState :loading="teamLoading" :error="teamError" :empty="!teamHasMembers" empty-title="当前 Study 暂无团队成员">
              <div class="drawer-team">
                <div v-for="r in teamRoles" :key="r.roleCode" class="team-card">
                  <div class="team-card-head">
                    <span class="team-card-role">{{ r.roleName }}</span>
                    <span v-if="r.functionName" class="team-card-fn">{{ r.functionName }}</span>
                  </div>
                  <div v-if="r.members.length" class="team-card-users">
                    <div v-for="m in r.members" :key="m.userId" class="team-card-user">
                      <span class="team-avatar" :class="{ 'team-avatar--disabled': !m.enabled }" aria-hidden="true">{{ m.displayName.slice(-2) }}</span>
                      <span>{{ m.displayName }}</span>
                    </div>
                  </div>
                  <p v-else class="team-card-empty">暂无成员</p>
                </div>
              </div>
            </PageState>
          </section>

          <!-- ── risks ── -->
          <section v-show="activeTab === 'risk'" role="tabpanel" class="drawer-panel">
            <PageState :loading="riskLoading" :error="riskError" :empty="!riskData?.data.length" empty-title="暂无关联风险">
              <div v-if="riskData" class="drawer-risks">
                <div v-for="r in riskData.data" :key="r.riskCode" class="risk-card">
                  <div class="risk-card-top">
                    <span class="risk-card-code mono">{{ r.riskCode }}</span>
                    <span class="status-chip" :class="r.status === 'OPEN' ? 'status-chip--orange' : 'status-chip--green'">{{ r.status === 'OPEN' ? 'Open' : 'Closed' }}</span>
                  </div>
                  <p class="risk-card-desc">{{ r.description }}</p>
                  <div class="risk-card-tags">
                    <span class="risk-tag risk-tag--pri">Priority {{ r.score >= 15 ? 'High' : r.score >= 6 ? 'Medium' : 'Low' }}</span>
                    <span class="risk-tag">Probability {{ ['Low','Medium','High'][Math.min(2, Math.floor((r.score / 5) % 3))] ?? 'Low' }}</span>
                    <span class="risk-tag">Impact {{ ['Low','Medium','High'][Math.min(2, Math.floor(r.score / 25))] ?? 'Low' }}</span>
                  </div>
                  <div class="risk-card-owner">Owner：{{ r.ownerName }}</div>
                </div>
              </div>
            </PageState>
          </section>
        </div>
      </aside>
    </div>
  </Teleport>
</template>
