<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiClient } from '../api/client'
import { PASSWORD_RULE_HINT, validateNewPassword } from '../domain/password-rules'
import { riskBadge } from '../risk-badge'
import { session } from '../session'

const route = useRoute()
const router = useRouter()
const user = computed(() => session.currentUser.value)
const pageTitle = computed(() => String(route.meta.title ?? '临床研发平台'))
const pageSubtitle = computed(() => String(route.meta.subtitle ?? ''))
const userInitials = computed(() => user.value?.displayName.slice(-2) ?? '?')
const roleLabels: Record<string, string> = {
  ADMIN: '管理员',
  USER: '普通成员',
  VIEWER: '只读者',
}
const roleLabel = computed(() => {
  const roles = user.value?.roles ?? []
  return roles.map((role) => roleLabels[role] ?? role).join('、') || '未分配角色'
})

const menuOpen = ref(false)
const passwordDialogOpen = ref(false)
const passwordSaving = ref(false)
const passwordError = ref('')
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

onMounted(() => {
  void riskBadge.refresh()
  document.addEventListener('click', onDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})

function onDocumentClick(event: MouseEvent) {
  const target = event.target as HTMLElement | null
  if (!target?.closest('.topbar-user-menu')) {
    menuOpen.value = false
  }
}

const navItems = computed(() => [
  { label: '管线总览', icon: '◆', to: '/pipeline' },
  { label: '研究 Study 列表', icon: '◇', to: '/studies' },
  {
    label: '风险管理',
    icon: '⚠',
    to: '/risks',
    badge: riskBadge.openCount.value != null ? String(riskBadge.openCount.value) : undefined,
  },
  ...(user.value?.permissions.includes('team.page.view')
    ? [{ label: '团队矩阵', icon: '▦', to: '/team' }]
    : []),
  ...(user.value?.permissions.includes('config.page.view')
    ? [{ label: '管线配置', icon: '⚙', to: '/config' }]
    : []),
  ...(user.value?.permissions.includes('report.page.view')
    ? [{ label: '月报导出', icon: '⭳', to: '/reports' }]
    : []),
  ...(user.value?.permissions.includes('account.page.view')
    ? [{ label: '账号管理', icon: '⚑', to: '/accounts' }]
    : []),
  ...(user.value?.permissions.includes('role.page.view')
    ? [{ label: '角色权限管理', icon: '⌘', to: '/roles' }]
    : []),
])

async function logout() {
  menuOpen.value = false
  await session.logout()
  await router.replace('/login')
}

function openPasswordDialog() {
  menuOpen.value = false
  passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
  passwordError.value = ''
  passwordDialogOpen.value = true
}

function closePasswordDialog() {
  if (passwordSaving.value) return
  passwordDialogOpen.value = false
}

async function submitPasswordChange() {
  passwordError.value = ''
  if (!passwordForm.value.currentPassword) {
    passwordError.value = '请输入当前密码'
    return
  }
  const ruleError = validateNewPassword(passwordForm.value.newPassword)
  if (ruleError) {
    passwordError.value = ruleError
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }
  if (passwordForm.value.currentPassword === passwordForm.value.newPassword) {
    passwordError.value = '新密码不能与当前密码相同'
    return
  }
  passwordSaving.value = true
  try {
    await apiClient.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    })
    passwordDialogOpen.value = false
  } catch (reason) {
    passwordError.value = reason instanceof Error ? reason.message : '修改密码失败'
  } finally {
    passwordSaving.value = false
  }
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-mark brand-mark--small">研</div>
        <div>
          <strong>临床研发平台</strong>
          <span>PIPELINE OPS</span>
        </div>
      </div>

      <nav aria-label="主要功能">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-item"
        >
          <span class="nav-icon" aria-hidden="true">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
          <span v-if="item.badge != null" class="nav-badge">{{ item.badge }}</span>
        </RouterLink>
      </nav>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageSubtitle }}</p>
        </div>
        <div class="topbar-actions">
          <div class="topbar-user-menu">
            <button
              class="topbar-user"
              type="button"
              :aria-expanded="menuOpen"
              aria-haspopup="menu"
              @click.stop="menuOpen = !menuOpen"
            >
              <span class="avatar">{{ userInitials }}</span>
              <span><strong>{{ user?.displayName }}</strong><small>{{ roleLabel }}</small></span>
            </button>
            <div v-if="menuOpen" class="topbar-user-dropdown" role="menu">
              <button type="button" role="menuitem" @click="openPasswordDialog">修改密码</button>
              <button type="button" role="menuitem" @click="logout">退出登录</button>
            </div>
          </div>
        </div>
      </header>
      <RouterView />
    </main>

    <Teleport to="body">
      <div v-if="passwordDialogOpen" class="dialog-backdrop" @click.self="closePasswordDialog">
        <form class="role-dialog" style="width: min(440px, 100%);" @submit.prevent="submitPasswordChange">
          <header>
            <div>
              <h2>修改密码</h2>
              <p>{{ PASSWORD_RULE_HINT }}</p>
            </div>
            <button type="button" aria-label="关闭" @click="closePasswordDialog">×</button>
          </header>
          <div class="role-form-grid" style="grid-template-columns: 1fr;">
            <label>
              当前密码
              <input v-model="passwordForm.currentPassword" type="password" autocomplete="current-password" maxlength="128">
            </label>
            <label>
              新密码
              <input v-model="passwordForm.newPassword" type="password" autocomplete="new-password" maxlength="128">
            </label>
            <label>
              确认新密码
              <input v-model="passwordForm.confirmPassword" type="password" autocomplete="new-password" maxlength="128">
            </label>
          </div>
          <p v-if="passwordError" class="form-error" role="alert" style="margin: 0 22px 14px;">{{ passwordError }}</p>
          <footer>
            <button class="secondary-button" type="button" @click="closePasswordDialog">取消</button>
            <button class="primary-button" type="submit" :disabled="passwordSaving">
              {{ passwordSaving ? '保存中…' : '确认修改' }}
            </button>
          </footer>
        </form>
      </div>
    </Teleport>
  </div>
</template>
