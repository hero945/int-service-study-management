<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChangePasswordDialog from '../components/ChangePasswordDialog.vue'
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

onMounted(() => {
  if (user.value?.permissions.includes('risk.page.view')) {
    void riskBadge.refresh()
  }
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

const navItems = computed(() => {
  const permissions = user.value?.permissions ?? []
  const items: Array<{ label: string; icon: string; to: string; badge?: string }> = []
  if (permissions.includes('pipeline.page.view')) {
    items.push({ label: '管线总览', icon: '◆', to: '/pipeline' })
  }
  if (permissions.includes('study.read')) {
    items.push({ label: '研究 Study 列表', icon: '◇', to: '/studies' })
  }
  if (permissions.includes('risk.page.view')) {
    items.push({
      label: '风险管理',
      icon: '⚠',
      to: '/risks',
      badge: riskBadge.openCount.value != null ? String(riskBadge.openCount.value) : undefined,
    })
  }
  if (permissions.includes('team.page.view')) {
    items.push({ label: '团队矩阵', icon: '▦', to: '/team' })
  }
  if (permissions.includes('config.page.view')) {
    items.push({ label: '管线配置', icon: '⚙', to: '/config' })
  }
  if (permissions.includes('report.page.view')) {
    items.push({ label: '月报导出', icon: '⭐', to: '/reports' })
  }
  if (permissions.includes('account.page.view')) {
    items.push({ label: '账号管理', icon: '♑', to: '/accounts' })
  }
  if (permissions.includes('role.page.view')) {
    items.push({ label: '角色权限管理', icon: '⌘', to: '/roles' })
  }
  return items
})
async function logout() {
  menuOpen.value = false
  await session.logout()
  await router.replace('/login')
}

function openPasswordDialog() {
  menuOpen.value = false
  passwordDialogOpen.value = true
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

    <ChangePasswordDialog :open="passwordDialogOpen" @close="passwordDialogOpen = false" />
  </div>
</template>
