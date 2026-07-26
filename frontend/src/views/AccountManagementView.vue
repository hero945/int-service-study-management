<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { apiClient } from '../api/client'
import type {
  CreateUserInput,
  PlatformRole,
  PlatformUser,
} from '../api/types'
import ListPagination, { DEFAULT_PAGE_SIZE } from '../components/ListPagination.vue'
import PageState from '../components/PageState.vue'
import { session } from '../session'

const users = ref<PlatformUser[]>([])
const roles = ref<PlatformRole[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const notice = ref('')
const keyword = ref('')
const roleFilter = ref('')
const page = ref(1)
const pageSize = ref(DEFAULT_PAGE_SIZE)
const totalItems = ref(0)
const totalPages = ref(1)

const dialogOpen = ref(false)
const formError = ref('')
const assignDialogOpen = ref(false)
const assignUserId = ref<number | null>(null)
const selectedRoleCodes = ref<string[]>([])
const toggleConfirmOpen = ref(false)
const toggleTargetId = ref<number | null>(null)
const toggleTargetName = ref('')
const toggleTargetEnabled = ref(true)
const resetConfirmOpen = ref(false)
const resetTargetId = ref<number | null>(null)
const resetTargetName = ref('')

let noticeTimer: ReturnType<typeof setTimeout> | undefined

const form = ref({
  username: '',
  displayName: '',
  roleCodes: [] as string[],
})

const DEFAULT_PASSWORD = 'Hd123456'

const userPermissions = computed(() => session.currentUser.value?.permissions ?? [])
const canCreate = computed(() => userPermissions.value.includes('account.create'))
const canUpdate = computed(() => userPermissions.value.includes('account.update'))
const canAssignRoles = computed(() => userPermissions.value.includes('account.assignRole'))

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
  noticeTimer = setTimeout(() => { notice.value = '' }, 5000)
}

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [userPage, rolePage] = await Promise.all([
      apiClient.listUsers({
        keyword: keyword.value,
        roleCode: roleFilter.value || undefined,
        page: page.value,
        pageSize: pageSize.value,
      }),
      apiClient.listRoles({ page: 1, pageSize: 100 }),
    ])
    users.value = userPage.data
    page.value = userPage.page
    pageSize.value = userPage.pageSize
    totalItems.value = userPage.totalItems
    totalPages.value = Math.max(userPage.totalPages, 1)
    roles.value = rolePage.data
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '数据加载失败'
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  page.value = 1
  await loadData()
}

const searchTimer = ref<ReturnType<typeof setTimeout>>()
function onKeywordInput(value: string) {
  keyword.value = value
  if (searchTimer.value) clearTimeout(searchTimer.value)
  searchTimer.value = setTimeout(() => {
    page.value = 1
    void loadData()
  }, 300)
}

function changePage(next: number) {
  page.value = next
  void loadData()
}

function changePageSize(nextSize: number) {
  pageSize.value = nextSize
  page.value = 1
  void loadData()
}

function openCreateDialog() {
  form.value = { username: '', displayName: '', roleCodes: [] }
  formError.value = ''
  dialogOpen.value = true
}

function closeDialog() {
  dialogOpen.value = false
  formError.value = ''
}

async function submitForm() {
  formError.value = ''
  saving.value = true
  try {
    if (!form.value.username || !form.value.displayName || form.value.roleCodes.length === 0) {
      formError.value = '请完整填写所有必填字段'
      saving.value = false
      return
    }
    const input: CreateUserInput = {
      username: form.value.username,
      displayName: form.value.displayName,
      roleCodes: form.value.roleCodes,
    }
    await apiClient.createUser(input)
    showNotice('账号创建成功')
    closeDialog()
    await loadData()
  } catch (reason) {
    formError.value = reason instanceof Error ? reason.message : '保存失败'
  } finally {
    saving.value = false
  }
}

function openAssignDialog(user: PlatformUser) {
  assignUserId.value = user.id
  selectedRoleCodes.value = [...user.roles]
  assignDialogOpen.value = true
}

function closeAssignDialog() {
  assignDialogOpen.value = false
  assignUserId.value = null
  selectedRoleCodes.value = []
  formError.value = ''
}

async function submitAssignRoles() {
  if (assignUserId.value === null) return
  formError.value = ''
  saving.value = true
  try {
    await apiClient.assignRoles(assignUserId.value, { roleCodes: selectedRoleCodes.value })
    showNotice('角色分配已更新')
    closeAssignDialog()
    await loadData()
  } catch (reason) {
    formError.value = reason instanceof Error ? reason.message : '角色分配失败'
  } finally {
    saving.value = false
  }
}

function confirmToggle(user: PlatformUser) {
  toggleTargetId.value = user.id
  toggleTargetName.value = user.displayName
  toggleTargetEnabled.value = user.enabled
  toggleConfirmOpen.value = true
}

function closeToggleConfirm() {
  toggleConfirmOpen.value = false
  toggleTargetId.value = null
  toggleTargetName.value = ''
}

function confirmResetPassword(user: PlatformUser) {
  resetTargetId.value = user.id
  resetTargetName.value = user.displayName
  resetConfirmOpen.value = true
}

function closeResetConfirm() {
  resetConfirmOpen.value = false
  resetTargetId.value = null
  resetTargetName.value = ''
}

async function executeResetPassword() {
  if (resetTargetId.value === null) return
  saving.value = true
  try {
    await apiClient.resetPassword(resetTargetId.value)
    showNotice(`已将 ${resetTargetName.value} 的密码重置为 ${DEFAULT_PASSWORD}`)
    closeResetConfirm()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '重置密码失败'
  } finally {
    saving.value = false
  }
}

async function executeToggle() {
  if (toggleTargetId.value === null) return
  saving.value = true
  try {
    await apiClient.updateUser(toggleTargetId.value, {
      displayName: toggleTargetName.value,
      enabled: !toggleTargetEnabled.value,
    })
    showNotice(toggleTargetEnabled.value ? '账号已停用' : '账号已启用')
    closeToggleConfirm()
    await loadData()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '操作失败'
  } finally {
    saving.value = false
  }
}

function toggleRoleForAssign(roleCode: string) {
  const idx = selectedRoleCodes.value.indexOf(roleCode)
  if (idx >= 0) {
    selectedRoleCodes.value.splice(idx, 1)
  } else {
    selectedRoleCodes.value.push(roleCode)
  }
}

function dataScopeLabel(dataScope: string): string {
  return dataScope === 'ALL' ? '全部 Study' : '指定 Study'
}

function visibleScopeLabel(user: PlatformUser): string {
  if (user.dataScope === 'ALL') return '全部 Study'
  return `${user.visibleStudyCount} 个 Study`
}

onMounted(() => loadData())
onUnmounted(() => {
  if (noticeTimer) clearTimeout(noticeTimer)
  if (searchTimer.value) clearTimeout(searchTimer.value)
})
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar">
      <div class="toolbar-filters">
        <div class="inline-search">
          <input
            v-model="keyword"
            type="search"
            placeholder="搜索姓名或登录邮箱…"
            @input="onKeywordInput(($event.target as HTMLInputElement).value)"
          />
        </div>
        <select v-model="roleFilter" aria-label="按角色筛选" @change="doSearch">
          <option value="">全部角色</option>
          <option
            v-for="role in roles"
            :key="role.roleCode"
            :value="role.roleCode"
          >{{ role.roleDescription ?? role.roleCode }}</option>
        </select>
      </div>
      <div class="toolbar-actions">
        <button
          v-if="canCreate"
          class="primary-button"
          type="button"
          @click="openCreateDialog"
        >＋ 新增账号</button>
      </div>
    </div>

    <div v-if="notice" class="role-notice">{{ notice }}</div>

    <PageState :loading :error :empty="!users.length">
      <div class="data-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>登录邮箱</th>
              <th>角色</th>
              <th>范围模式</th>
              <th>可见范围</th>
              <th>状态</th>
              <th style="width: 200px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td class="strong">{{ user.displayName }}</td>
              <td class="mono">{{ user.username }}</td>
              <td>
                <span
                  v-for="(desc, i) in user.roleDescriptions"
                  :key="user.roles[i]"
                  class="status-chip status-chip--blue"
                  style="margin-right: 4px;"
                >{{ desc }}</span>
                <span
                  v-if="!user.roleDescriptions.length"
                  v-for="role in user.roles"
                  :key="role"
                  class="status-chip status-chip--blue"
                  style="margin-right: 4px;"
                >{{ role }}</span>
              </td>
              <td>
                <span
                  class="status-chip"
                  :class="user.dataScope === 'ALL' ? 'status-chip--green' : 'status-chip--orange'"
                >{{ dataScopeLabel(user.dataScope) }}</span>
              </td>
              <td class="mono">{{ visibleScopeLabel(user) }}</td>
              <td>
                <span
                  class="status-chip"
                  :class="user.enabled ? 'status-chip--green' : 'status-chip--red'"
                >{{ user.enabled ? '启用' : '停用' }}</span>
              </td>
              <td class="role-actions">
                <div class="action-links">
                  <button
                    v-if="canAssignRoles"
                    type="button"
                    class="link-button"
                    @click="openAssignDialog(user)"
                  >分配角色</button>
                  <button
                    v-if="canUpdate"
                    type="button"
                    class="link-button"
                    @click="confirmResetPassword(user)"
                  >重置密码</button>
                  <button
                    v-if="canUpdate"
                    type="button"
                    class="link-button"
                    :class="user.enabled ? 'link-button--danger' : ''"
                    @click="confirmToggle(user)"
                  >{{ user.enabled ? '停用' : '启用' }}</button>
                </div>
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
      aria-label="账号列表分页"
      @update:page="changePage"
      @update:page-size="changePageSize"
    />

    <!-- 新增账号弹窗 -->
    <Teleport to="body">
      <div v-if="dialogOpen" class="dialog-backdrop" @click.self="closeDialog">
        <div class="role-dialog" style="width: min(520px, 100%);">
          <header>
            <div>
              <h2>新增账号</h2>
              <p>创建新的平台登录账号</p>
            </div>
            <button type="button" @click="closeDialog" aria-label="关闭">&#x2715;</button>
          </header>
          <div class="role-form-grid">
            <label class="role-form-wide">
              登录邮箱
              <input
                v-model="form.username"
                type="email"
                placeholder="例如 user@eastchinapharm.com"
              />
            </label>
            <label class="role-form-wide">
              显示姓名
              <input
                v-model="form.displayName"
                type="text"
                placeholder="例如 张三"
                maxlength="100"
              />
            </label>
            <label class="role-form-wide">
              登录密码
              <input
                type="text"
                :value="DEFAULT_PASSWORD"
                readonly
                title="初始密码由系统自动赋予，管理员不可修改"
              />
              <small style="margin-top: 4px; color: var(--muted); font-size: 11px;">
                系统默认赋予初始密码，管理员不可修改；用户登录后可自行修改密码
              </small>
            </label>
            <label class="role-form-wide">
              角色
              <div class="role-check-options">
                <label
                  v-for="role in roles"
                  :key="role.roleCode"
                >
                  <input
                    type="checkbox"
                    :value="role.roleCode"
                    :checked="form.roleCodes.includes(role.roleCode)"
                    @change="(e: Event) => {
                      const cb = e.target as HTMLInputElement
                      if (cb.checked) form.roleCodes.push(role.roleCode)
                      else form.roleCodes = form.roleCodes.filter(c => c !== role.roleCode)
                    }"
                  />
                  {{ role.roleDescription ?? role.roleCode }}
                </label>
              </div>
            </label>
          </div>
          <div v-if="formError" class="form-error" style="margin: 0 22px 14px;">{{ formError }}</div>
          <footer>
            <button class="secondary-button" type="button" @click="closeDialog">取消</button>
            <button class="primary-button" type="button" :disabled="saving" @click="submitForm">
              {{ saving ? '保存中…' : '创建账号' }}
            </button>
          </footer>
        </div>
      </div>

      <!-- 分配角色弹窗 -->
      <div v-if="assignDialogOpen" class="dialog-backdrop" @click.self="closeAssignDialog">
        <div class="role-dialog" style="width: min(480px, 100%);">
          <header>
            <div>
              <h2>分配角色</h2>
              <p>选择账号的角色权限</p>
            </div>
            <button type="button" @click="closeAssignDialog" aria-label="关闭">&#x2715;</button>
          </header>
          <div style="padding: 18px 22px;">
            <div class="role-check-options">
              <label
                v-for="role in roles"
                :key="role.roleCode"
              >
                <input
                  type="checkbox"
                  :value="role.roleCode"
                  :checked="selectedRoleCodes.includes(role.roleCode)"
                  @change="toggleRoleForAssign(role.roleCode)"
                />
                {{ role.roleDescription ?? role.roleCode }}
              </label>
            </div>
          </div>
          <div v-if="formError" class="form-error" style="margin: 0 22px 14px;">{{ formError }}</div>
          <footer>
            <button class="secondary-button" type="button" @click="closeAssignDialog">取消</button>
            <button
              class="primary-button"
              type="button"
              :disabled="saving || selectedRoleCodes.length === 0"
              @click="submitAssignRoles"
            >{{ saving ? '保存中…' : '确认分配' }}</button>
          </footer>
        </div>
      </div>

      <!-- 停用/启用确认弹窗 -->
      <div v-if="toggleConfirmOpen" class="dialog-backdrop" @click.self="closeToggleConfirm">
        <div class="role-dialog" style="width: min(400px, 100%);">
          <header>
            <div>
              <h2>{{ toggleTargetEnabled ? '确认停用' : '确认启用' }}</h2>
              <p>{{ toggleTargetEnabled ? '停用后该账号将无法登录系统' : '启用后该账号将恢复登录权限' }}</p>
            </div>
            <button type="button" @click="closeToggleConfirm" aria-label="关闭">&#x2715;</button>
          </header>
          <div style="padding: 18px 22px;">
            <p style="margin: 0; color: #3b424e; font-size: 13px;">
              确定要{{ toggleTargetEnabled ? '停用' : '启用' }}账号 <strong>{{ toggleTargetName }}</strong> 吗？
            </p>
          </div>
          <footer>
            <button class="secondary-button" type="button" @click="closeToggleConfirm">取消</button>
            <button
              class="primary-button"
              type="button"
              :style="toggleTargetEnabled ? 'background: #ef4444; border-color: #ef4444;' : ''"
              :disabled="saving"
              @click="executeToggle"
            >{{ saving ? '处理中…' : (toggleTargetEnabled ? '确认停用' : '确认启用') }}</button>
          </footer>
        </div>
      </div>

      <!-- 重置密码确认弹窗 -->
      <div v-if="resetConfirmOpen" class="dialog-backdrop" @click.self="closeResetConfirm">
        <div class="role-dialog" style="width: min(400px, 100%);">
          <header>
            <div>
              <h2>确认重置密码</h2>
              <p>密码将恢复为系统初始密码</p>
            </div>
            <button type="button" @click="closeResetConfirm" aria-label="关闭">&#x2715;</button>
          </header>
          <div style="padding: 18px 22px;">
            <p style="margin: 0; color: #3b424e; font-size: 13px;">
              确定将账号 <strong>{{ resetTargetName }}</strong> 的密码重置为
              <strong class="mono">{{ DEFAULT_PASSWORD }}</strong> 吗？
              该用户的现有登录会话将被强制退出。
            </p>
          </div>
          <footer>
            <button class="secondary-button" type="button" @click="closeResetConfirm">取消</button>
            <button
              class="primary-button"
              type="button"
              :disabled="saving"
              @click="executeResetPassword"
            >{{ saving ? '处理中…' : '确认重置' }}</button>
          </footer>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.action-links {
  display: flex;
  align-items: center;
  gap: 12px;
}
.link-button {
  border: 0;
  background: transparent;
  color: var(--accent);
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}
.link-button:hover {
  text-decoration: underline;
}
.link-button--danger {
  color: var(--red-text);
}
.link-button:focus-visible {
  outline: 2px solid var(--accent);
  outline-offset: 2px;
}
</style>
