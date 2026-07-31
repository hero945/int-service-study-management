<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formatApiError } from '../api/errors'
import { safeRedirectPath } from '../login-redirect'
import { firstAllowedHome } from '../navigation'
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
  } catch (reason) {
    errorMessage.value = formatApiError(reason, '登录失败')
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
        placeholder="you@eastchinapharm.com"
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

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 20px;
  background: #f5f6f8;
}
.login-card {
  width: min(380px, 100%);
  padding: 36px 32px 30px;
  border: 1px solid var(--line-soft);
  border-radius: 14px;
  background: var(--surface);
  box-shadow: 0 20px 50px rgba(28, 35, 45, .12);
}
.brand-mark {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  margin: 0 auto 17px;
  border-radius: 10px;
  background: var(--accent);
  color: var(--surface);
  font-size: 20px;
  font-weight: 700;
}
.login-card h1 { margin: 0; text-align: center; font-size: 20px; }
.login-subtitle {
  margin: 4px 0 28px;
  text-align: center;
  color: var(--muted);
  font-size: 11px;
  letter-spacing: .5px;
}
.login-card label { display: block; margin: 0 0 7px; color: var(--muted); font-size: 12px; }
.login-card input {
  width: 100%;
  height: 38px;
  margin-bottom: 16px;
  padding: 0 12px;
  border: 1px solid var(--line-input);
  border-radius: 8px;
  background: var(--surface);
  color: var(--ink);
  outline: none;
}
.login-card input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(30, 94, 214, .12); }
.login-card input:disabled { background: var(--surface-muted); color: var(--subtle); cursor: not-allowed; }
.password-field { position: relative; margin-bottom: 16px; }
.password-field input { margin-bottom: 0; padding-right: 40px; }
.password-toggle {
  position: absolute;
  top: 50%;
  right: 6px;
  transform: translateY(-50%);
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--muted);
}
.password-toggle:hover:not(:disabled) { background: var(--surface-muted); color: var(--ink-2); }
.password-toggle:focus-visible { outline: none; box-shadow: var(--shadow-focus); }
.password-toggle:disabled { cursor: not-allowed; opacity: .5; }
.caps-lock-hint { margin: -8px 0 10px; color: var(--orange-text); font-size: 12px; }
/* .form-error 保持全局共享规则（main.css），不在此重复 */
.demo-accounts {
  margin: -2px 0 8px;
  padding-top: 10px;
  border-top: 1px solid #eef0f3;
  color: var(--muted);
  font-size: 10.5px;
  line-height: 1.7;
}
.demo-accounts span { color: var(--accent); }
.login-button { width: 100%; margin-top: 8px; }
.login-footer {
  margin: 18px 0 0;
  padding-top: 14px;
  border-top: 1px solid #eef0f3;
  text-align: center;
  color: var(--subtle);
  font-size: var(--text-xs);
}
</style>
