<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { apiClient } from '../api/client'
import type {
  PlatformPermission,
  PlatformRole,
  RoleInput,
  RoleStatus,
} from '../api/types'
import ListPagination, { DEFAULT_PAGE_SIZE } from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import { session } from '../session'

const roles = ref<PlatformRole[]>([])
const permissions = ref<PlatformPermission[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const notice = ref('')
const keyword = ref('')
const status = ref<'' | RoleStatus>('')
const page = ref(1)
const pageSize = ref(DEFAULT_PAGE_SIZE)
const totalPages = ref(1)
const totalItems = ref(0)
const dialogOpen = ref(false)
const editingRole = ref<PlatformRole>()
const formError = ref('')
const form = ref<RoleInput>(emptyForm())
const collapsedModules = ref<Set<string>>(new Set())
let noticeTimer: ReturnType<typeof setTimeout> | undefined

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

const userPermissions = computed(() => session.currentUser.value?.permissions ?? [])
const canCreate = computed(() => userPermissions.value.includes('role.create'))
const canUpdate = computed(() => userPermissions.value.includes('role.update'))
const canDelete = computed(() => userPermissions.value.includes('role.delete'))
const permissionGroups = computed(() => {
  const groups = new Map<string, PlatformPermission[]>()
  for (const permission of permissions.value) {
    const group = groups.get(permission.moduleCode) ?? []
    group.push(permission)
    groups.set(permission.moduleCode, group)
  }
  return [...groups.entries()]
})

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

function hideNotice() {
  notice.value = ''
  if (noticeTimer) {
    clearTimeout(noticeTimer)
    noticeTimer = undefined
  }
}

function showNotice(message: string) {
  hideNotice()
  notice.value = message
  noticeTimer = setTimeout(() => {
    notice.value = ''
    noticeTimer = undefined
  }, 4_000)
}

async function loadRoles(targetPage = 1) {
  loading.value = true
  error.value = ''
  try {
    const result = await apiClient.listRoles({
      page: targetPage,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      status: status.value || undefined,
    })
    roles.value = result.data
    page.value = result.page
    pageSize.value = result.pageSize
    totalPages.value = Math.max(result.totalPages, 1)
    totalItems.value = result.totalItems
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '角色列表加载失败'
  } finally {
    loading.value = false
  }
}

function changePageSize(nextSize: number) {
  pageSize.value = nextSize
  void loadRoles(1)
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
    await loadRoles(page.value)
  } catch (reason) {
    formError.value = reason instanceof Error ? reason.message : '角色保存失败'
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
    await loadRoles(page.value)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '角色删除失败'
  }
}

onMounted(async () => {
  try {
    permissions.value = await apiClient.listPermissions()
    await loadRoles()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '权限字典加载失败'
    loading.value = false
  }
})

onUnmounted(hideNotice)
</script>

<template>
  <section class="page-content role-page">
    <div class="page-toolbar role-toolbar">
      <div class="toolbar-filters">
        <label class="inline-search">
          <span aria-hidden="true">⌕</span>
          <input v-model="keyword" type="search" placeholder="搜索角色编码或说明" @keyup.enter="loadRoles()">
        </label>
        <label>
          状态
          <select v-model="status" @change="loadRoles()">
            <option value="">全部</option>
            <option value="ACTIVE">启用</option>
            <option value="DISABLED">停用</option>
          </select>
        </label>
        <button class="secondary-button" type="button" @click="loadRoles()">查询</button>
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
      :empty="!roles.length"
      empty-title="暂无匹配角色"
      empty-description="可调整筛选条件，或新增一个业务角色。"
    >
      <div class="data-card role-table-card">
        <table class="data-table role-table">
          <thead><tr><th>角色</th><th>数据范围</th><th>权限</th><th>关联账号</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="role in roles" :key="role.id">
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
      @update:page="loadRoles"
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
  </section>
</template>
