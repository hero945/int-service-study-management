<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { apiClient } from '../api/client'
import { formatApiError } from '../api/errors'
import type {
  PlatformPermission,
  PlatformRole,
  RoleInput,
  RoleStatus,
} from '../api/types'
import ListPagination from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import AuditLogDrawer from '../components/AuditLogDrawer.vue'
import { useClientSort } from '../composables/useClientSort'
import { useEscapeClose } from '../composables/useEscapeClose'
import { useNotice } from '../composables/useNotice'
import { usePagedList } from '../composables/usePagedList'
import { usePermissions } from '../composables/usePermissions'
import { useAuditLogDrawer } from '../composables/useAuditLogDrawer'

const permissions = ref<PlatformPermission[]>([])
const saving = ref(false)
const filters = reactive({ keyword: '', status: '' as '' | RoleStatus })
const dialogOpen = ref(false)
const editingRole = ref<PlatformRole>()
const formError = ref('')
const form = ref<RoleInput>(emptyForm())
const collapsedModules = ref<Set<string>>(new Set())

const MODULE_LABELS: Record<string, string> = {
  pipeline: '管线总览',
  study: '研究 Study',
  milestone: '里程碑',
  risk: '风险管理',
  team: '团队矩阵',
  config: '管线配置',
  monthly: '月报',
  report: '报告导出',
  account: '账号管理',
  role: '角色权限',
  setting: '系统设置',
}

const { can } = usePermissions()
const canCreate = can('role.create')
const canUpdate = can('role.update')
const canDelete = can('role.delete')
const canAudit = can('audit.read')
const { auditDrawer, openRecordAuditLogs, closeAuditLogs } =
  useAuditLogDrawer('ROLE')
const permissionGroups = computed(() => {
  const groups = new Map<string, PlatformPermission[]>()
  for (const permission of permissions.value) {
    const group = groups.get(permission.moduleCode) ?? []
    group.push(permission)
    groups.set(permission.moduleCode, group)
  }
  return [...groups.entries()]
})

const { notice, showNotice, hideNotice } = useNotice()

const {
  result, loading, error, page, pageSize,
  load, applyFilters, changePage, changePageSize,
} = usePagedList({
  filters,
  errorMessage: '角色列表加载失败',
  fetcher: (q) => apiClient.listRoles({
    page: q.page,
    pageSize: q.pageSize,
    keyword: q.keyword.trim() || undefined,
    status: q.status || undefined,
  }),
  onLoaded: (r) => {
    page.value = r.page
    pageSize.value = r.pageSize
  },
})

const roles = computed(() => result.value?.data ?? [])
const totalItems = computed(() => result.value?.totalItems ?? 0)
const totalPages = computed(() => Math.max(result.value?.totalPages ?? 1, 1))

const {
  sorted: sortedRoles,
  registerMany: registerRoleSortColumns,
  sortHeader: roleSortHeader,
} = useClientSort({ items: roles })

registerRoleSortColumns([
  { key: 'roleCode', resolver: (r) => r.roleCode, type: 'string' },
  { key: 'dataScope', resolver: (r) => r.dataScopeMode, type: 'string' },
  { key: 'permissions', resolver: (r) => r.permissionCodes.length, type: 'number' },
  { key: 'assignedUsers', resolver: (r) => r.assignedUserCount, type: 'number' },
  { key: 'status', resolver: (r) => (r.status === 'ACTIVE' ? '启用' : '停用'), type: 'string' },
])

function moduleLabel(moduleCode: string) {
  return MODULE_LABELS[moduleCode] ?? moduleCode
}

function isModuleExpanded(moduleCode: string) {
  return !collapsedModules.value.has(moduleCode)
}

function toggleModuleExpand(moduleCode: string) {
  const next = new Set(collapsedModules.value)
  if (next.has(moduleCode)) next.delete(moduleCode)
  else next.add(moduleCode)
  collapsedModules.value = next
}

function emptyForm(): RoleInput {
  return {
    roleCode: '',
    roleDescription: '',
    dataScopeMode: 'ASSIGNED_STUDY',
    status: 'ACTIVE',
    permissionCodes: [],
  }
}

function openCreate() {
  editingRole.value = undefined
  form.value = emptyForm()
  formError.value = ''
  dialogOpen.value = true
}

function openEdit(role: PlatformRole) {
  editingRole.value = role
  form.value = {
    roleCode: role.roleCode,
    roleDescription: role.roleDescription ?? '',
    dataScopeMode: role.dataScopeMode,
    status: role.status,
    permissionCodes: [...role.permissionCodes],
  }
  formError.value = ''
  dialogOpen.value = true
}

function closeDialog() {
  if (!saving.value) dialogOpen.value = false
}

useEscapeClose(dialogOpen, closeDialog)

function toggleGroup(group: PlatformPermission[], checked: boolean) {
  const codes = new Set(form.value.permissionCodes)
  for (const permission of group) {
    if (checked) codes.add(permission.permissionCode)
    else codes.delete(permission.permissionCode)
  }
  form.value.permissionCodes = [...codes]
}

function groupSelected(group: PlatformPermission[]) {
  return group.every((permission) => form.value.permissionCodes.includes(permission.permissionCode))
}

async function submitRole() {
  formError.value = ''
  hideNotice()
  if (!editingRole.value && !/^[A-Z][A-Z0-9_]{1,63}$/.test(form.value.roleCode ?? '')) {
    formError.value = '角色编码需为 2–64 位大写字母、数字或下划线，并以字母开头'
    return
  }
  if (!form.value.permissionCodes.length) {
    formError.value = '请至少选择一个权限'
    return
  }
  saving.value = true
  try {
    if (editingRole.value) {
      const result = await apiClient.updateRole(editingRole.value.id, form.value)
      dialogOpen.value = false
      if (result.currentSessionInvalidated) {
        window.location.replace('/login')
        return
      }
      showNotice(`角色已更新，${result.invalidatedUserCount} 个关联账号需重新登录`)
    } else {
      await apiClient.createRole(form.value)
      dialogOpen.value = false
      showNotice('角色已新增')
    }
    await load()
  } catch (reason) {
    formError.value = formatApiError(reason, '角色保存失败')
  } finally {
    saving.value = false
  }
}

async function removeRole(role: PlatformRole) {
  if (role.systemRole || role.assignedUserCount > 0) return
  if (!window.confirm(`确认删除角色 ${role.roleCode}？删除后该编码不可复用。`)) return
  hideNotice()
  try {
    await apiClient.deleteRole(role.id)
    showNotice('角色已删除')
    await load()
  } catch (reason) {
    error.value = formatApiError(reason, '角色删除失败')
  }
}

async function loadWithPermissions() {
  try {
    if (!permissions.value.length) {
      permissions.value = await apiClient.listPermissions()
    }
    await load()
  } catch (reason) {
    error.value = formatApiError(reason, '权限字典加载失败')
    loading.value = false
  }
}

onMounted(loadWithPermissions)
</script>

<template>
  <section class="page-content page-content--fill role-page">
    <div class="page-toolbar role-toolbar">
      <div class="toolbar-filters">
        <label class="inline-search">
          <span aria-hidden="true">⌕</span>
          <input v-model="filters.keyword" type="search" placeholder="搜索角色编码或说明" @keyup.enter="applyFilters">
        </label>
        <label>
          状态
          <select v-model="filters.status" @change="applyFilters">
            <option value="">全部</option>
            <option value="ACTIVE">启用</option>
            <option value="DISABLED">停用</option>
          </select>
        </label>
        <button class="secondary-button" type="button" @click="applyFilters">查询</button>
      </div>
      <div class="role-toolbar__actions">
        <span>共 {{ totalItems }} 个角色</span>
        <button v-if="canCreate" class="primary-button" type="button" @click="openCreate">＋ 新增角色</button>
      </div>
    </div>

    <p v-if="notice" class="role-notice" role="status">{{ notice }}</p>
    <PageState
      :loading
      :error
      retryable
      :empty="!roles.length"
      empty-title="暂无匹配角色"
      empty-description="可调整筛选条件，或新增一个业务角色。"
      @retry="loadWithPermissions"
    >
      <div class="data-card role-table-card">
        <table class="data-table role-table">
          <thead><tr>
            <th v-bind="roleSortHeader('roleCode')">角色</th>
            <th v-bind="roleSortHeader('dataScope')">数据范围</th>
            <th v-bind="roleSortHeader('permissions')">权限</th>
            <th v-bind="roleSortHeader('assignedUsers')">关联账号</th>
            <th v-bind="roleSortHeader('status')">状态</th>
            <th>操作</th>
            <th v-if="canAudit">操作日志</th>
          </tr></thead>
          <tbody>
            <tr v-for="role in sortedRoles" :key="role.id">
              <td>
                <div class="role-name"><strong class="mono">{{ role.roleCode }}</strong><span v-if="role.systemRole">系统</span></div>
                <small>{{ role.roleDescription || '暂无说明' }}</small>
              </td>
              <td>{{ role.dataScopeMode === 'ALL' ? '全部数据' : '已分配 Study' }}</td>
              <td><span class="permission-count">{{ role.permissionCodes.length }}</span> 项</td>
              <td>{{ role.assignedUserCount }}</td>
              <td><span :class="['status-chip', role.status === 'ACTIVE' ? 'status-chip--green' : 'status-chip--empty']">{{ role.status === 'ACTIVE' ? '启用' : '停用' }}</span></td>
              <td class="role-actions">
                <button v-if="canUpdate" type="button" @click="openEdit(role)">编辑权限</button>
                <button
                  v-if="canDelete"
                  class="danger-link"
                  type="button"
                  :disabled="role.systemRole || role.assignedUserCount > 0"
                  :title="role.systemRole ? '系统角色不可删除' : role.assignedUserCount ? '请先解除账号关联' : '删除角色'"
                  @click="removeRole(role)"
                >删除</button>
              </td>
              <td v-if="canAudit">
                <button class="text-button" type="button" @click="openRecordAuditLogs(`${role.roleCode} 操作日志`, 'ROLE', role.id)">查看</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </PageState>

    <ListPagination
      v-if="!loading && !error"
      :total="totalItems"
      :page="page"
      :page-size="pageSize"
      :total-pages="totalPages"
      aria-label="角色列表分页"
      @update:page="changePage"
      @update:page-size="changePageSize"
    />

    <Teleport to="body">
      <div v-if="dialogOpen" class="dialog-backdrop" @mousedown.self="closeDialog">
        <form class="role-dialog" role="dialog" aria-modal="true" aria-labelledby="role-dialog-title" @submit.prevent="submitRole">
          <header><div><h2 id="role-dialog-title">{{ editingRole ? '编辑角色权限' : '新增角色' }}</h2><p>权限字典由系统统一维护，此处仅为角色分配已有权限。</p></div><button type="button" aria-label="关闭" @click="closeDialog">×</button></header>
          <div class="role-form-grid">
            <label>角色编码<input v-model.trim="form.roleCode" class="mono" :disabled="!!editingRole" maxlength="64" placeholder="例如 CLINICAL_LEAD"></label>
            <label>数据范围<select v-model="form.dataScopeMode"><option value="ALL">全部数据</option><option value="ASSIGNED_STUDY">仅已分配 Study</option></select></label>
            <label class="role-form-wide">角色说明<textarea v-model.trim="form.roleDescription" maxlength="500" rows="2" placeholder="说明该角色的职责"></textarea></label>
            <label v-if="editingRole">状态<select v-model="form.status" :disabled="editingRole.systemRole"><option value="ACTIVE">启用</option><option value="DISABLED">停用</option></select></label>
          </div>
          <section class="permission-editor">
            <div class="permission-editor__title"><strong>权限分配</strong><span>已选择 {{ form.permissionCodes.length }} 项</span></div>
            <div v-for="[moduleCode, group] in permissionGroups" :key="moduleCode" class="permission-group">
              <div class="permission-group__heading">
                <button
                  class="permission-group__toggle"
                  type="button"
                  :aria-expanded="isModuleExpanded(moduleCode)"
                  :aria-label="(isModuleExpanded(moduleCode) ? '收起' : '展开') + moduleLabel(moduleCode)"
                  @click="toggleModuleExpand(moduleCode)"
                >{{ isModuleExpanded(moduleCode) ? '▾' : '▸' }}</button>
                <label>
                  <input
                    type="checkbox"
                    :checked="groupSelected(group)"
                    @change="toggleGroup(group, ($event.target as HTMLInputElement).checked)"
                  >
                  <strong>{{ moduleLabel(moduleCode) }}</strong>
                </label>
                <span>{{ group.length }} 项</span>
              </div>
              <div v-show="isModuleExpanded(moduleCode)" class="permission-options">
                <label v-for="permission in group" :key="permission.permissionCode">
                  <input v-model="form.permissionCodes" type="checkbox" :value="permission.permissionCode">
                  <span><strong>{{ permission.permissionName }}</strong></span>
                </label>
              </div>
            </div>
          </section>
          <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
          <footer><button class="secondary-button" type="button" @click="closeDialog">取消</button><button class="primary-button" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button></footer>
        </form>
      </div>
    </Teleport>
    <AuditLogDrawer v-bind="auditDrawer" @close="closeAuditLogs" />
  </section>
</template>
