<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const navItems = computed(() => [
  { label: '管线总览', icon: '▦', to: '/pipeline' },
  { label: '研究 Study 列表', icon: '▤', to: '/studies' },
  { label: '风险管理', icon: '⚠', to: '/risks', badge: '1' },
  ...(user.value?.permissions.includes('team.page.view')
    ? [{ label: '团队矩阵', icon: '◫', to: '/team' }]
    : []),
  ...(user.value?.permissions.includes('config.page.view')
    ? [{ label: '管线配置', icon: '⚙', to: '/config' }]
    : []),
  { label: '月报导出', icon: '⭳', to: '/reports' },
  ...(user.value?.permissions.includes('account.page.view')
    ? [{ label: '账号管理', icon: '⚷', to: '/accounts' }]
    : []),
  ...(user.value?.permissions.includes('role.page.view')
    ? [{ label: '角色权限管理', icon: '⌘', to: '/roles' }]
    : []),
])

async function logout() {
  await session.logout()
  await router.replace('/login')
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
          <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar-user">
        <span class="sidebar-caption">当前登录</span>
        <div class="user-summary">
          <span class="avatar">{{ userInitials }}</span>
          <span>
            <strong>{{ user?.displayName }}</strong>
            <small>{{ user?.title || roleLabel }}</small>
          </span>
        </div>
        <button class="logout-button" type="button" @click="logout">退出登录</button>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageSubtitle }}</p>
        </div>
        <div class="topbar-actions">
          <div class="topbar-user">
            <span class="avatar">{{ userInitials }}</span>
            <span><strong>{{ user?.displayName }}</strong><small>{{ roleLabel }}</small></span>
          </div>
        </div>
      </header>
      <RouterView />
    </main>
  </div>
</template>
