<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError, apiClient } from '../api/client'
import type {
  PlatformUser,
  TeamMatrixMember,
} from '../api/types'
import PageState from '../components/PageState.vue'
import ListPagination from '../components/ListPagination.vue'
import AuditLogDrawer from '../components/AuditLogDrawer.vue'
import { usePagedList } from '../composables/usePagedList'
import { usePermissions } from '../composables/usePermissions'
import { useAuditLogDrawer } from '../composables/useAuditLogDrawer'
import { session } from '../session'

const users = ref<PlatformUser[]>([])
const saving = ref(false)
const notice = ref('')
const filters = reactive({ studyQuery: '', roleQuery: '' })
const editMode = ref(false)
const picker = ref<{ studyId: number; roleCode: string }>()
const drafts = ref(new Map<string, TeamMatrixMember[]>())

const { can } = usePermissions()
const canEditMode = can('team.edit_mode')
const canUpdateTeam = can('team.update')
const canEdit = computed(() => canEditMode.value && canUpdateTeam.value)
const canAudit = can('audit.read')
const { auditDrawer, openAllAuditLogs, openRecordAuditLogs, closeAuditLogs } =
  useAuditLogDrawer('TEAM')
const enabledUsers = computed(() => users.value.filter(user => user.enabled))
const hasChanges = computed(() => drafts.value.size > 0)

const {
  result: matrix, loading, error,
  load, applyFilters: applyListFilters,
  changePage: changeListPage, changePageSize: changeListPageSize,
} = usePagedList({
  filters,
  errorMessage: '团队矩阵加载失败',
  fetcher: (q) => apiClient.listTeamMatrix({
    studyQuery: q.studyQuery,
    roleQuery: q.roleQuery,
    page: q.page,
    pageSize: q.pageSize,
  }),
})

function cellKey(studyId: number, roleCode: string) {
  return `${studyId}|${roleCode}`
}

function originalMembers(studyId: number, roleCode: string) {
  return matrix.value?.assignments.find(assignment =>
    assignment.studyId === studyId && assignment.roleCode === roleCode)?.members ?? []
}

function membersFor(studyId: number, roleCode: string) {
  return drafts.value.get(cellKey(studyId, roleCode)) ?? originalMembers(studyId, roleCode)
}

function setDraft(studyId: number, roleCode: string, members: TeamMatrixMember[]) {
  const next = new Map(drafts.value)
  const originalIds = originalMembers(studyId, roleCode).map(member => member.userId).sort()
  const nextIds = members.map(member => member.userId).sort()
  if (JSON.stringify(originalIds) === JSON.stringify(nextIds)) {
    next.delete(cellKey(studyId, roleCode))
  } else {
    next.set(cellKey(studyId, roleCode), members)
  }
  drafts.value = next
}

// 翻页/改筛选会丢弃未保存的草稿，先退出编辑态
function applyFilters() {
  cancelEdit()
  applyListFilters()
}

function changePage(nextPage: number) {
  cancelEdit()
  changeListPage(nextPage)
}

function changePageSize(nextSize: number) {
  cancelEdit()
  changeListPageSize(nextSize)
}

async function startEdit() {
  if (!canEdit.value) return
  error.value = ''
  if (!users.value.length) {
    try {
      users.value = (await apiClient.listUsers({ page: 1, pageSize: 100 })).data
    } catch (reason) {
      error.value = reason instanceof Error ? reason.message : '平台账号加载失败'
      return
    }
  }
  editMode.value = true
}

function cancelEdit() {
  drafts.value = new Map()
  picker.value = undefined
  editMode.value = false
}

function openPicker(studyId: number, roleCode: string) {
  picker.value = { studyId, roleCode }
}

function addMember(studyId: number, roleCode: string, user: PlatformUser) {
  const current = membersFor(studyId, roleCode)
  if (current.some(member => member.userId === user.id)) return
  setDraft(studyId, roleCode, [...current, {
    userId: user.id,
    email: user.username,
    displayName: user.displayName,
    enabled: user.enabled,
  }])
  picker.value = undefined
}

function removeMember(studyId: number, roleCode: string, userId: number) {
  setDraft(
    studyId,
    roleCode,
    membersFor(studyId, roleCode).filter(member => member.userId !== userId),
  )
}

function availableUsers(studyId: number, roleCode: string) {
  const assigned = new Set(membersFor(studyId, roleCode).map(member => member.userId))
  return enabledUsers.value.filter(user => !assigned.has(user.id))
}

async function save() {
  if (!matrix.value || !hasChanges.value) {
    cancelEdit()
    return
  }
  const currentEmail = session.currentUser.value?.username
  const removesSelf = [...drafts.value.entries()].some(([key, members]) => {
    const [studyId, roleCode] = key.split('|')
    const before = originalMembers(Number(studyId), roleCode)
    return before.some(member => member.email === currentEmail) &&
      !members.some(member => member.email === currentEmail)
  })
  if (removesSelf && !window.confirm(
    '保存后你将立即失去对应 Study 的访问权限，确定继续吗？',
  )) return

  const grouped = new Map<number, Array<{ roleCode: string; userIds: number[] }>>()
  for (const [key, members] of drafts.value) {
    const [studyIdText, roleCode] = key.split('|')
    const studyId = Number(studyIdText)
    const roles = grouped.get(studyId) ?? []
    roles.push({ roleCode, userIds: members.map(member => member.userId) })
    grouped.set(studyId, roles)
  }
  const studies = [...grouped.entries()].map(([studyId, roles]) => ({
    studyId,
    expectedVersion:
      matrix.value?.studies.find(study => study.studyId === studyId)?.version ?? 0,
    roles,
  }))

  saving.value = true
  error.value = ''
  try {
    await apiClient.replaceTeamAssignments({ studies })
    notice.value = '团队矩阵已保存，成员数据范围已即时更新。'
    cancelEdit()
    await load()
  } catch (reason) {
    error.value = reason instanceof ApiError && reason.code === 'TEAM_VERSION_CONFLICT'
      ? '团队矩阵已被其他用户修改，请刷新后重新编辑。'
      : reason instanceof Error ? reason.message : '团队矩阵保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page-content team-page">
    <form class="page-toolbar team-toolbar" role="search" @submit.prevent="applyFilters">
      <button v-if="canAudit" class="secondary-button" type="button" @click="openAllAuditLogs('团队全部操作日志')">全部操作日志</button>
      <div class="toolbar-filters">
        <label>
          <span class="sr-only">搜索 Study 或适应症</span>
          <input v-model="filters.studyQuery" type="search" class="filter-input" placeholder="搜索 Study / 适应症">
        </label>
        <label>
          <span class="sr-only">搜索角色或功能线</span>
          <input v-model="filters.roleQuery" type="search" class="filter-input" placeholder="搜索角色 / 功能线">
        </label>
        <button class="secondary-button" type="submit">搜索</button>
      </div>
      <div class="team-toolbar__summary">
        <span>{{ matrix?.pagination.totalItems ?? 0 }} 个 Study · {{ matrix?.totalRoles ?? 0 }} 个角色</span>
        <template v-if="editMode">
          <button data-testid="cancel-team" class="secondary-button" type="button" :disabled="saving" @click="cancelEdit">取消</button>
          <button data-testid="save-team" class="primary-button" type="button" :disabled="saving || !hasChanges" @click="save">
            {{ saving ? '保存中…' : '保存矩阵' }}
          </button>
        </template>
        <button
          v-else-if="canEdit"
          data-testid="edit-team"
          class="primary-button"
          type="button"
          @click="startEdit"
        >
          编辑矩阵
        </button>
      </div>
    </form>

    <p v-if="notice" class="team-notice" role="status">
      {{ notice }}
      <button type="button" aria-label="关闭提示" @click="notice = ''">×</button>
    </p>
    <p v-if="editMode" class="team-edit-hint">
      修改会暂存在当前页面；点击“保存矩阵”后一次性提交。团队分配同时决定 ASSIGNED_STUDY 用户的数据范围。
    </p>

    <PageState
      :loading
      :error
      :empty="!matrix?.studies.length"
      empty-title="暂无可见 Study"
      empty-description="请调整搜索条件，或由管理员检查 Study 和团队分配。"
    >
      <div class="team-matrix-card">
        <div class="team-matrix-scroll" tabindex="0" aria-label="团队成员矩阵，可横向滚动">
          <table class="team-matrix-table">
            <thead>
              <tr>
                <th scope="col">角色 / Study</th>
                <th v-for="study in matrix?.studies" :key="study.studyId" scope="col">
                  <strong>{{ study.studyCode }}</strong>
                  <button v-if="canAudit" class="text-button" type="button" @click="openRecordAuditLogs(`${study.studyCode} 角色分配记录`, 'STUDY', study.studyId)">角色分配记录</button>
                  <span>{{ study.indication || '—' }}</span>
                  <small>{{ study.currentStatus || '—' }}</small>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="role in matrix?.roles" :key="role.roleCode">
                <th scope="row">
                  <strong>{{ role.roleName }}</strong>
                  <span>{{ role.functionName || '未分组' }}</span>
                </th>
                <td v-for="study in matrix?.studies" :key="study.studyId">
                  <div class="team-members">
                    <span
                      v-for="member in membersFor(study.studyId, role.roleCode)"
                      :key="member.userId"
                      class="team-member"
                      :class="{ 'team-member--disabled': !member.enabled }"
                    >
                      <span class="team-avatar" aria-hidden="true">{{ member.displayName.slice(-2) }}</span>
                      <span>
                        {{ member.displayName }}
                        <small v-if="!member.enabled">已停用</small>
                      </span>
                      <button
                        v-if="editMode"
                        type="button"
                        :aria-label="`移除 ${member.displayName}`"
                        @click="removeMember(study.studyId, role.roleCode, member.userId)"
                      >×</button>
                    </span>
                    <span v-if="!membersFor(study.studyId, role.roleCode).length && !editMode" class="team-empty">—</span>
                    <button
                      v-if="editMode && (picker?.studyId !== study.studyId || picker?.roleCode !== role.roleCode)"
                      :data-testid="`add-member-${study.studyId}-${role.roleCode}`"
                      class="team-add"
                      type="button"
                      :aria-label="`为 ${study.studyCode} 的 ${role.roleName} 添加成员`"
                      @click="openPicker(study.studyId, role.roleCode)"
                    >＋ 添加</button>
                    <div
                      v-if="editMode && picker?.studyId === study.studyId && picker?.roleCode === role.roleCode"
                      class="team-picker"
                    >
                      <button
                        v-for="user in availableUsers(study.studyId, role.roleCode)"
                        :key="user.id"
                        :data-testid="`member-option-${user.id}`"
                        type="button"
                        @click="addMember(study.studyId, role.roleCode, user)"
                      >
                        <strong>{{ user.displayName }}</strong>
                        <small>{{ user.username }}</small>
                      </button>
                      <span v-if="!availableUsers(study.studyId, role.roleCode).length">没有可添加的启用账号</span>
                      <button type="button" @click="picker = undefined">关闭</button>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </PageState>

    <ListPagination
      v-if="!loading && !error && matrix"
      :total="matrix.pagination.totalItems"
      :page="matrix.pagination.page"
      :page-size="matrix.pagination.pageSize"
      :total-pages="matrix.pagination.totalPages"
      aria-label="团队矩阵分页"
      @update:page="changePage"
      @update:page-size="changePageSize"
    />
    <AuditLogDrawer v-bind="auditDrawer" @close="closeAuditLogs" />
  </section>
</template>
