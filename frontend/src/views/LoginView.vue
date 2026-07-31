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
const heroImageUrl = `${import.meta.env.BASE_URL}brand/patient-centered-research-hero-transparent.png`
const brandMarkUrl = `${import.meta.env.BASE_URL}brand/huadong-mark-blur-source.png`

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
    <header class="portal-header">
      <div class="portal-header-inner">
        <img class="portal-brand-mark" :src="brandMarkUrl" alt="华东医药">
        <span>研发管线管理平台</span>
      </div>
    </header>

    <section class="portal-stage">
      <div class="portal-stage-inner">
        <section class="login-visual" aria-label="以患者为中心的新药研发主视觉">
          <figure class="login-hero-figure">
            <img
              :src="heroImageUrl"
              alt="科研人员与临床医生围绕患者开展创新药研究"
              decoding="async"
            >
          </figure>
        </section>

        <section class="login-form-panel" aria-label="账号登录">
          <div class="login-card">
            <header class="login-card-header">
              <p>PIPELINE OPS</p>
              <h1 id="login-title">登录研发管理平台</h1>
              <span>使用华东医药内部账号继续</span>
            </header>

            <form class="portal-login-form" aria-labelledby="login-title" @submit.prevent="submit">
              <label for="username">邮箱账号</label>
              <div class="login-input-wrap" :class="{ 'has-error': errorMessage }">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M4 6.5h16v11H4zM4.5 7l7.5 6 7.5-6" />
                </svg>
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
                  :aria-invalid="Boolean(errorMessage)"
                  :aria-describedby="errorMessage ? 'login-error' : undefined"
                  :disabled="submitting"
                >
              </div>

              <label for="password">密码</label>
              <div class="portal-password-field login-input-wrap" :class="{ 'has-error': errorMessage }">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <rect x="5" y="10" width="14" height="10" rx="1.5" />
                  <path d="M8.5 10V7.5a3.5 3.5 0 0 1 7 0V10M12 14v2.5" />
                </svg>
                <input
                  id="password"
                  ref="passwordInput"
                  v-model="credentials.password"
                  name="password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="current-password"
                  placeholder="请输入密码"
                  required
                  :aria-invalid="Boolean(errorMessage)"
                  :aria-describedby="errorMessage ? 'login-error' : undefined"
                  :disabled="submitting"
                  @keydown="trackCapsLock"
                  @keyup="trackCapsLock"
                  @blur="capsLockOn = false"
                >
                <button
                  type="button"
                  class="portal-password-toggle"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  :aria-pressed="showPassword"
                  :disabled="submitting"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="showPassword" viewBox="0 0 20 20" aria-hidden="true">
                    <path d="M2.5 2.5l15 15M8.2 4.2A8.6 8.6 0 0 1 10 4c4.5 0 7.5 3.4 8.7 6a12.7 12.7 0 0 1-2.4 3.3M5 5.6A11.9 11.9 0 0 0 1.3 10c1.2 2.6 4.2 6 8.7 6 1 0 2-.2 2.8-.5M8 8.3a2.9 2.9 0 0 0 4 4" />
                  </svg>
                  <svg v-else viewBox="0 0 20 20" aria-hidden="true">
                    <path d="M1.3 10C2.5 7.4 5.5 4 10 4s7.5 3.4 8.7 6c-1.2 2.6-4.2 6-8.7 6S2.5 12.6 1.3 10Z" />
                    <circle cx="10" cy="10" r="2.9" />
                  </svg>
                </button>
              </div>
              <p v-if="capsLockOn" class="caps-lock-hint" role="status">大写锁定已开启（Caps Lock）</p>

              <p v-if="errorMessage" id="login-error" class="form-error" role="alert">{{ errorMessage }}</p>
              <p v-if="isMockMode" class="portal-demo-accounts">
                演示模式可使用：<br>
                <span>chen@eastchinapharm.com · 管理员</span><br>
                <span>zhangwei@eastchinapharm.com · 成员（PL）</span><br>
                <span>liuyang@eastchinapharm.com · 只读</span><br>
                密码均为 <strong>1234</strong>
              </p>

              <button class="primary-button portal-login-button" type="submit" :disabled="submitting">
                <span>{{ submitting ? '正在登录…' : '登录' }}</span>
                <svg v-if="!submitting" viewBox="0 0 20 20" aria-hidden="true">
                  <path d="m7 4 6 6-6 6M3 10h10" />
                </svg>
              </button>

              <p class="portal-login-footer">华东医药 · 仅限内部授权用户使用</p>
            </form>
          </div>
        </section>
      </div>
    </section>

    <footer class="portal-footer">
      <div class="portal-footer-inner">
        <small>内部系统 · 请妥善保管账号信息</small>
      </div>
    </footer>
  </main>
</template>

<style scoped>
.login-page {
  --login-blue: #2457bc;
  --login-blue-dark: #19479f;
  display: grid;
  grid-template-rows: 72px minmax(0, 1fr) 48px;
  grid-template-columns: 1fr;
  min-height: 100dvh;
  overflow: auto;
  background: #edf3fb;
  color: #172635;
}

.portal-header,
.portal-footer {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
}

.portal-header {
  background: var(--login-blue);
}
.portal-footer {
  background: #e9eff7;
}

.portal-header-inner,
.portal-footer-inner {
  width: min(1320px, calc(100% - 48px));
  margin: 0 auto;
}

.portal-header-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.portal-brand-mark {
  width: 42px;
  height: 38px;
  object-fit: contain;
  mix-blend-mode: screen;
}

.portal-header-inner span {
  padding-left: 14px;
  border-left: 1px solid rgba(255, 255, 255, .32);
  color: #fff;
  font-size: 17px;
  font-weight: 650;
  letter-spacing: .06em;
}

.portal-stage {
  position: relative;
  min-height: 0;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(36, 87, 188, .055) 0, rgba(36, 87, 188, .018) 18px, transparent 48px),
    radial-gradient(ellipse 44% 62% at 24% 48%, rgba(255, 255, 255, .94), transparent 74%),
    radial-gradient(ellipse 30% 52% at 82% 48%, rgba(87, 126, 201, .12), transparent 76%),
    #edf2f8;
}

.portal-stage-inner {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: clamp(48px, 5vw, 80px);
  width: min(1320px, calc(100% - 48px));
  height: 100%;
  align-items: center;
  margin: 0 auto;
  padding: clamp(28px, 4vw, 56px) 0;
}

.login-visual {
  min-width: 0;
  min-height: 0;
  display: grid;
  height: 100%;
  place-items: center;
}

.login-hero-figure {
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  margin: 0;
}

.login-hero-figure img {
  display: block;
  width: min(100%, 720px);
  max-height: min(64dvh, 560px);
  object-fit: contain;
  filter: saturate(.94) contrast(.99);
}

.login-form-panel {
  position: relative;
  min-height: 0;
  display: grid;
  place-items: center;
  margin: 0;
  padding: 20px 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.login-card {
  position: relative;
  width: min(420px, 100%);
  padding: 36px 40px 30px;
  border: 1px solid rgba(205, 217, 235, .72);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 20px 48px rgba(32, 67, 124, .1);
}

.login-card-header { margin-bottom: 24px; }
.login-card-header p {
  margin: 0 0 8px;
  color: var(--login-blue);
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: .18em;
}
.login-card-header h1 {
  margin: 0;
  color: #204f9f;
  font-size: 24px;
  font-weight: 650;
  letter-spacing: -.02em;
}
.login-card-header span {
  display: block;
  margin-top: 8px;
  color: #7a8798;
  font-size: 12px;
}

.portal-login-form label {
  display: block;
  margin: 16px 0 7px;
  color: #3c4b5e;
  font-size: 12px;
  font-weight: 600;
}

.login-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #d9e1eb;
  border-radius: 8px;
  background: #fff;
  transition: border-color .16s ease, box-shadow .16s ease;
}

.login-input-wrap:focus-within {
  border-color: var(--login-blue);
  box-shadow: 0 0 0 3px rgba(36, 87, 188, .1);
}
.login-input-wrap.has-error {
  border-color: #c55353;
  box-shadow: 0 0 0 1px rgba(197, 83, 83, .08);
}
.login-input-wrap.has-error:focus-within {
  border-color: #b64242;
  box-shadow: 0 0 0 3px rgba(182, 66, 66, .1);
}
.login-input-wrap.has-error > svg { stroke: #b64242; }

.login-input-wrap > svg {
  width: 18px;
  height: 18px;
  margin-left: 12px;
  fill: none;
  stroke: #8996a7;
  stroke-width: 1.6;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.login-input-wrap input {
  min-width: 0;
  height: 42px;
  flex: 1;
  margin: 0;
  padding: 0 12px 0 10px;
  border: 0;
  border-radius: 0;
  outline: 0;
  background: transparent;
  box-shadow: none;
  color: #1c2a3a;
  font-size: 13px;
}
.login-input-wrap input::placeholder { color: #a1aab6; }
.login-input-wrap input:disabled { cursor: not-allowed; opacity: .62; }
.portal-login-form input:focus {
  border: 0;
  outline: 0;
  box-shadow: none;
}
.portal-login-form input:-webkit-autofill,
.portal-login-form input:-webkit-autofill:hover,
.portal-login-form input:-webkit-autofill:focus {
  -webkit-text-fill-color: #1c2a3a;
  -webkit-box-shadow: 0 0 0 1000px #fff inset;
  caret-color: #1c2a3a;
}

.portal-password-toggle {
  width: 34px;
  height: 34px;
  flex: none;
  display: grid;
  margin-right: 4px;
  place-items: center;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #778599;
}
.portal-password-field { margin: 0; }
.portal-password-toggle:hover:not(:disabled) { background: #f0f4f9; color: var(--login-blue); }
.portal-password-toggle:focus-visible {
  outline: 2px solid var(--login-blue);
  outline-offset: 1px;
}
.portal-password-toggle svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.6;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.caps-lock-hint,
.form-error {
  margin: 9px 0 0;
  font-size: 11.5px;
  line-height: 1.5;
}
.caps-lock-hint { color: var(--orange-text); }
.form-error {
  padding-left: 9px;
  border-left: 2px solid #c55353;
  color: #ad3f3f;
}

.portal-demo-accounts {
  margin: 16px 0 0;
  padding: 11px 12px;
  border-left: 3px solid #b9c9e6;
  background: #f5f7fa;
  color: #657285;
  font-size: 10.5px;
  line-height: 1.65;
}
.portal-demo-accounts span { color: #315caa; }

.portal-login-button {
  width: 100%;
  min-height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  border-color: var(--login-blue);
  border-radius: 8px;
  background: var(--login-blue);
  box-shadow: 0 8px 18px rgba(36, 87, 188, .18);
  font-size: 13px;
  letter-spacing: .12em;
}
.portal-login-button:hover:not(:disabled) {
  border-color: var(--login-blue-dark);
  background: var(--login-blue-dark);
  transform: translateY(-1px);
}
.portal-login-button:active:not(:disabled) { transform: translateY(0); }
.portal-login-button svg {
  width: 17px;
  height: 17px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.6;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.portal-login-footer {
  margin: 18px 0 0;
  padding-top: 14px;
  border-top: 1px solid #e7ebf0;
  color: #919ba8;
  font-size: 10.5px;
  text-align: center;
}

.portal-footer-inner {
  display: flex;
  align-items: center;
}
.portal-footer-inner small {
  color: #748397;
  font-size: 10.5px;
}

@media (max-width: 900px) {
  .login-page { grid-template-rows: 64px auto 44px; }
  .portal-header-inner,
  .portal-footer-inner { width: min(100% - 36px, 720px); }
  .portal-header-inner span { font-size: 14px; }
  .portal-stage { overflow: visible; }
  .portal-stage-inner {
    grid-template-columns: 1fr;
    gap: 10px;
    width: min(100% - 36px, 720px);
    height: auto;
    padding: 24px 0 38px;
  }
  .login-visual { height: 320px; }
  .login-hero-figure img { max-height: 340px; }
  .login-form-panel { padding: 0; }
  .login-card { margin: 0 auto; }
}

@media (max-width: 600px) {
  .portal-header-inner { gap: 9px; }
  .portal-brand-mark { width: 36px; height: 34px; }
  .portal-header-inner span {
    padding-left: 9px;
    font-size: 11px;
    letter-spacing: .03em;
  }
  .portal-stage-inner {
    width: calc(100% - 28px);
    padding: 18px 0 28px;
  }
  .login-visual { height: 230px; }
  .login-hero-figure img { max-height: 240px; }
  .login-card { padding: 30px 22px 26px; border-radius: 13px; }
  .login-card-header h1 { font-size: 21px; }
  .portal-footer-inner { justify-content: center; }
}

@media (prefers-reduced-motion: reduce) {
  .portal-login-button,
  .login-input-wrap { transition: none; }
}
</style>
