<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { firstAllowedHome } from '../router'
import { safeRedirectPath } from '../login-redirect'
import { session } from '../session'

const route = useRoute()
const router = useRouter()
const credentials = reactive({ username: '', password: '' })
const errorMessage = ref('')
const submitting = ref(false)
const showPassword = ref(false)
const capsLockOn = ref(false)
const passwordInput = ref<HTMLInputElement>()
const isMockMode = import.meta.env.VITE_API_MODE === 'mock'

function trackCapsLock(event: KeyboardEvent) {
  capsLockOn.value = event.getModifierState?.('CapsLock') ?? false
}

async function submit() {
  errorMessage.value = ''
  submitting.value = true
  try {
    const user = await session.login(credentials)
    const redirect = safeRedirectPath(route.query.redirect)
    await router.replace(redirect ?? { name: firstAllowedHome(user.permissions) })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
    passwordInput.value?.focus()
    passwordInput.value?.select()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <form class="login-card" aria-labelledby="login-title" @submit.prevent="submit">
      <div class="brand-mark" aria-hidden="true">研</div>
      <h1 id="login-title">临床研发平台</h1>
      <p class="login-subtitle">PIPELINE OPS · 登录</p>

      <label for="username">邮箱账号</label>
      <input
        id="username"
        v-model.trim="credentials.username"
        name="username"
        type="email"
        inputmode="email"
        autocomplete="username"
        placeholder="you@company.com"
        autofocus
        required
        :disabled="submitting"
      >

      <label for="password">密码</label>
      <div class="password-field">
        <input
          id="password"
          ref="passwordInput"
          v-model="credentials.password"
          name="password"
          :type="showPassword ? 'text' : 'password'"
          autocomplete="current-password"
          placeholder="请输入密码"
          required
          :disabled="submitting"
          @keydown="trackCapsLock"
          @keyup="trackCapsLock"
          @blur="capsLockOn = false"
        >
        <button
          type="button"
          class="password-toggle"
          :aria-label="showPassword ? '隐藏密码' : '显示密码'"
          :aria-pressed="showPassword"
          :disabled="submitting"
          @click="showPassword = !showPassword"
        >
          <svg v-if="showPassword" viewBox="0 0 20 20" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M2.5 2.5l15 15" />
            <path d="M8.2 4.2A8.6 8.6 0 0 1 10 4c4.5 0 7.5 3.4 8.7 6a12.7 12.7 0 0 1-2.4 3.3M5 5.6A11.9 11.9 0 0 0 1.3 10c1.2 2.6 4.2 6 8.7 6 1 0 2-.2 2.8-.5" />
            <path d="M8 8.3a2.9 2.9 0 0 0 4 4" />
          </svg>
          <svg v-else viewBox="0 0 20 20" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M1.3 10C2.5 7.4 5.5 4 10 4s7.5 3.4 8.7 6c-1.2 2.6-4.2 6-8.7 6S2.5 12.6 1.3 10Z" />
            <circle cx="10" cy="10" r="2.9" />
          </svg>
        </button>
      </div>
      <p v-if="capsLockOn" class="caps-lock-hint" role="status">大写锁定已开启（Caps Lock）</p>

      <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>
      <p v-if="isMockMode" class="demo-accounts">
        演示模式可使用：<br>
        <span>chen@eastchinapharm.com · 管理员</span><br>
        <span>zhangwei@eastchinapharm.com · 成员（PL）</span><br>
        <span>liuyang@eastchinapharm.com · 只读</span><br>
        密码均为 <strong>1234</strong>
      </p>

      <button class="primary-button login-button" type="submit" :disabled="submitting">
        {{ submitting ? '正在登录…' : '登录' }}
      </button>

      <p class="login-footer">临床研发平台 · 仅限内部使用</p>
    </form>
  </main>
</template>
