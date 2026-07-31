<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChangePasswordDialog from '../components/ChangePasswordDialog.vue'
import NavIcon from '../components/NavIcon.vue'
import { visibleNavigationGroups } from '../navigation'
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
const navigationOpen = ref(false)
const passwordDialogOpen = ref(false)

onMounted(() => {
  if (user.value?.permissions.includes('risk.page.view')) {
    void riskBadge.refresh()
  }
  document.addEventListener('click', onDocumentClick)
  document.addEventListener('keydown', onDocumentKeydown)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
  document.removeEventListener('keydown', onDocumentKeydown)
  document.body.classList.remove('navigation-open')
})

function onDocumentClick(event: MouseEvent) {
  const target = event.target as HTMLElement | null
  if (!target?.closest('.topbar-user-menu')) {
    menuOpen.value = false
  }
}

const navGroups = computed(() => visibleNavigationGroups(user.value?.permissions ?? []))

watch(
  () => route.fullPath,
  () => {
    navigationOpen.value = false
    menuOpen.value = false
  },
)

watch(navigationOpen, (open) => {
  document.body.classList.toggle('navigation-open', open)
})

function onDocumentKeydown(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  navigationOpen.value = false
  menuOpen.value = false
}

function badgeFor(name: string) {
  if (name !== 'risks' || riskBadge.openCount.value == null) return undefined
  return String(riskBadge.openCount.value)
}

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
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <button
      v-if="navigationOpen"
      class="navigation-backdrop"
      type="button"
      aria-label="关闭导航"
      @click="navigationOpen = false"
    ></button>

    <aside id="primary-navigation" class="sidebar" :class="{ 'sidebar--open': navigationOpen }">
      <div class="sidebar-brand">
        <span class="sidebar-brand-mark" aria-hidden="true">
          <svg viewBox="0 0 32 32">
            <path d="M16 3.5 27 9.8v12.4L16 28.5 5 22.2V9.8L16 3.5Z" />
            <path d="M10.5 18.7 16 8.5l5.5 10.2M12.6 15h6.8" />
          </svg>
        </span>
        <div class="sidebar-brand-copy">
          <strong>临床研发平台</strong>
          <span>R&amp;D PIPELINE OPS</span>
        </div>
        <button class="sidebar-close" type="button" aria-label="关闭导航" @click="navigationOpen = false">
          <svg viewBox="0 0 20 20" aria-hidden="true"><path d="m5 5 10 10M15 5 5 15" /></svg>
        </button>
      </div>

      <nav aria-label="主要功能">
        <section v-for="group in navGroups" :key="group.label" class="nav-group">
          <h2>{{ group.label }}</h2>
          <RouterLink
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
          >
            <span class="nav-icon" aria-hidden="true"><NavIcon :name="item.icon" /></span>
            <span class="nav-label">{{ item.label }}</span>
            <span v-if="badgeFor(item.name) != null" class="nav-badge">{{ badgeFor(item.name) }}</span>
          </RouterLink>
        </section>
      </nav>

      <div class="sidebar-footnote">
        <span aria-hidden="true"></span>
        内部授权访问
      </div>
    </aside>

    <main id="main-content" class="workspace" tabindex="-1">
      <header class="topbar">
        <div class="topbar-heading">
          <button
            class="navigation-trigger"
            type="button"
            aria-label="打开导航"
            aria-controls="primary-navigation"
            :aria-expanded="navigationOpen"
            @click="navigationOpen = true"
          >
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M3 5h14M3 10h14M3 15h14" /></svg>
          </button>
          <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageSubtitle }}</p>
          </div>
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
              <span class="topbar-user-copy"><strong>{{ user?.displayName }}</strong></span>
              <svg class="topbar-chevron" viewBox="0 0 16 16" aria-hidden="true"><path d="m4 6 4 4 4-4" /></svg>
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
