<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { firstAllowedHome } from '../router'
import { safeRedirectPath } from '../login-redirect'
import { session } from '../session'
import { formatApiError } from '../api/errors'

const route = useRoute()
const router = useRouter()
const credentials = reactive({ username: '', password: '' })
const errorMessage = ref('')
const submitting = ref(false)
const showPassword = ref(false)
const capsLockOn = ref(false)
const passwordInput = ref<HTMLInputElement>()
const isMockMode = import.meta.env.VITE_API_MODE === 'mock'
const heroImageUrl = `${import.meta.env.BASE_URL}brand/patient-centered-research-hero.jpg`
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
  } catch (error) {
    errorMessage.value = formatApiError(error, '登录失败')
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
          <div class="login-panel-ambient" aria-hidden="true">
            <img :src="brandMarkUrl" alt="">
          </div>
          <div class="login-card">
            <span class="login-card-accent" aria-hidden="true"></span>
            <header class="login-card-header">
              <p>PIPELINE OPS</p>
              <h1 id="login-title">登录研发管理平台</h1>
              <span>使用华东医药内部账号继续</span>
            </header>

            <form class="login-form" aria-labelledby="login-title" @submit.prevent="submit">
              <label for="username">邮箱账号</label>
              <div class="login-input-wrap">
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
                  :disabled="submitting"
                >
              </div>

              <label for="password">密码</label>
              <div class="password-field login-input-wrap">
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
                <span>{{ submitting ? '正在登录…' : '登录' }}</span>
                <svg v-if="!submitting" viewBox="0 0 20 20" aria-hidden="true">
                  <path d="m7 4 6 6-6 6M3 10h10" />
                </svg>
              </button>

              <p class="login-footer">华东医药 · 仅限内部授权用户使用</p>
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
  --brand-blue: #176bb5;
  --brand-blue-dark: #104f88;
  --brand-blue-pale: #e9f3fb;
  --brand-gray: #7c858d;
  --brand-green: #78ad43;
  position: relative;
  isolation: isolate;
  display: grid;
  grid-template-columns: minmax(0, 1.16fr) minmax(420px, .84fr);
  min-height: 100svh;
  overflow: hidden;
  background: #f7f9fc;
  color: #172635;
}

.brand-watermark {
  position: absolute;
  z-index: -1;
  display: block;
  overflow: hidden;
  width: clamp(280px, 28vw, 430px);
  aspect-ratio: 1;
  opacity: .065;
  pointer-events: none;
}

.brand-watermark img {
  width: auto;
  max-width: none;
  height: 100%;
}

.brand-watermark--top {
  top: clamp(-230px, -15vw, -150px);
  right: 26%;
  transform: rotate(8deg);
}

.brand-watermark--bottom {
  bottom: clamp(-220px, -13vw, -120px);
  left: clamp(-160px, -8vw, -80px);
  opacity: .05;
  filter: grayscale(1);
  transform: rotate(-10deg);
}

.login-culture {
  position: relative;
  z-index: 1;
  display: flex;
  min-width: 0;
  flex-direction: column;
  padding: clamp(28px, 4vw, 58px) clamp(32px, 5vw, 78px);
  background:
    linear-gradient(90deg, rgba(255, 255, 255, .86), rgba(255, 255, 255, .42)),
    radial-gradient(circle at 58% 58%, rgba(23, 107, 181, .06), transparent 42%);
}

.login-brand-header {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 42px;
}

.login-brand-logo {
  display: block;
  width: 178px;
  height: auto;
}

.login-product-name {
  padding-left: 18px;
  border-left: 1px solid #d7dde3;
  color: #586674;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: .08em;
}

.login-culture-content {
  display: flex;
  width: min(100%, 790px);
  flex: 1;
  flex-direction: column;
  justify-content: center;
  padding-top: clamp(28px, 4vh, 52px);
}

.culture-copy {
  position: relative;
  z-index: 2;
}

.culture-eyebrow {
  margin: 0 0 14px;
  color: var(--brand-blue);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .2em;
}

.culture-copy h1 {
  margin: 0;
  color: #142b3f;
  font-family: "Noto Serif SC", "Songti SC", "STSong", serif;
  font-size: clamp(38px, 4vw, 58px);
  font-weight: 650;
  line-height: 1.2;
  letter-spacing: .025em;
}

.culture-mission {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 18px 0 0;
  color: #344a5f;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: .08em;
}

.culture-mission span,
.culture-vision-label {
  flex: none;
  color: var(--brand-blue);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .14em;
}

.culture-mission span {
  padding: 5px 9px;
  border: 1px solid rgba(23, 107, 181, .24);
  background: rgba(255, 255, 255, .72);
}

.culture-visual {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 172px;
  align-items: center;
  gap: clamp(18px, 2.5vw, 38px);
  min-height: 300px;
  margin-top: 6px;
}

.culture-illustration {
  position: relative;
  min-width: 0;
  margin: 0;
}

.culture-illustration > img {
  display: block;
  width: 100%;
  max-height: 370px;
  object-fit: contain;
  mix-blend-mode: multiply;
}

.culture-illustration figcaption {
  position: absolute;
  bottom: 18px;
  left: 0;
  display: grid;
  gap: 5px;
  max-width: 280px;
  padding: 12px 15px 12px 17px;
  border-left: 3px solid var(--brand-green);
  background: rgba(255, 255, 255, .9);
  box-shadow: 0 10px 28px rgba(26, 56, 82, .08);
  backdrop-filter: blur(8px);
}

.culture-illustration figcaption span {
  color: var(--brand-gray);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: .16em;
}

.culture-illustration figcaption strong {
  color: #263c4f;
  font-size: 13px;
  font-weight: 650;
  letter-spacing: .04em;
}

.culture-values {
  position: relative;
  padding-left: 20px;
  border-left: 1px solid #dfe5ea;
}

.culture-values > p {
  margin: 0 0 10px;
  color: #82909c;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .14em;
}

.culture-values ul {
  display: grid;
  gap: 2px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.culture-values li {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 46px;
  border-bottom: 1px solid #e3e8ec;
  color: #263c4f;
  font-family: "Noto Serif SC", "Songti SC", "STSong", serif;
  font-size: 20px;
  font-weight: 650;
}

.culture-values li:last-child {
  border-bottom: 0;
}

.culture-values li span {
  color: #9facb6;
  font-family: "IBM Plex Mono", monospace;
  font-size: 9px;
  font-weight: 500;
}

.culture-vision {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) 56px;
  align-items: center;
  gap: 15px;
  margin-top: 4px;
}

.culture-vision p {
  margin: 0;
  color: #5d6c78;
  font-size: 12px;
  line-height: 1.6;
  letter-spacing: .04em;
}

.culture-vision-line {
  width: 56px;
  height: 1px;
  background: linear-gradient(90deg, var(--brand-blue), transparent);
}

.login-form-panel {
  position: relative;
  z-index: 2;
  display: grid;
  min-width: 0;
  place-items: center;
  margin-left: -32px;
  padding: clamp(32px, 5vw, 72px);
  border-radius: 34px 0 0 34px;
  background:
    radial-gradient(circle at 100% 0, rgba(23, 107, 181, .07), transparent 36%),
    rgba(241, 245, 248, .78);
  border-left: 1px solid rgba(219, 226, 232, .8);
  box-shadow: -24px 0 58px rgba(39, 66, 88, .08);
}

.login-card {
  position: relative;
  width: min(440px, 100%);
  padding: clamp(34px, 4vw, 52px);
  overflow: hidden;
  border: 1px solid rgba(218, 225, 231, .9);
  border-radius: 3px;
  background: rgba(255, 255, 255, .96);
  box-shadow: 0 24px 70px rgba(32, 57, 79, .14);
}

.login-card-accent {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--brand-blue) 0 62%, #8c969f 62% 78%, var(--brand-green) 78%);
}

.login-card-header {
  margin-bottom: 30px;
}

.login-card-header p {
  margin: 0 0 12px;
  color: var(--brand-blue);
  font-size: 10px;
  font-weight: 750;
  letter-spacing: .22em;
}

.login-card-header h2 {
  margin: 0;
  color: #172635;
  font-size: 27px;
  font-weight: 680;
  letter-spacing: .02em;
}

.login-card-header span {
  display: block;
  margin-top: 9px;
  color: #7b8791;
  font-size: 12px;
}

.login-form label {
  display: block;
  margin: 0 0 6px;
  color: #546572;
  font-size: 11px;
  font-weight: 650;
  letter-spacing: .06em;
}

.login-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 50px;
  margin-bottom: 20px;
  border-bottom: 1px solid #cfd8df;
  transition: border-color .16s ease, box-shadow .16s ease;
}

.login-input-wrap:focus-within {
  border-color: var(--brand-blue);
  box-shadow: 0 1px 0 var(--brand-blue);
}

.login-input-wrap > svg {
  width: 20px;
  height: 20px;
  flex: none;
  margin-right: 12px;
  fill: none;
  stroke: #7f93a3;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 1.5;
}

.login-input-wrap:focus-within > svg {
  stroke: var(--brand-blue);
}

.login-input-wrap input {
  width: 100%;
  height: 50px;
  margin: 0;
  padding: 0 38px 0 0;
  border: 0;
  border-radius: 0;
  outline: 0;
  background: transparent;
  box-shadow: none;
  color: #1c2d3b;
  font-size: 13px;
}

.login-input-wrap input:focus {
  border: 0;
  box-shadow: none;
}

.login-input-wrap input::placeholder {
  color: #a1acb5;
}

.login-input-wrap input:disabled {
  background: transparent;
}

.password-field {
  margin-bottom: 20px;
}

.password-toggle {
  right: 0;
}

.caps-lock-hint,
.form-error {
  margin: -9px 0 12px;
}

.demo-accounts {
  margin: 0 0 12px;
  padding: 10px 12px;
  border: 1px solid #e3e9ee;
  background: #f7f9fb;
  font-size: 10px;
  line-height: 1.6;
}

.login-button {
  display: flex;
  width: 100%;
  min-height: 50px;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 6px;
  border: 1px solid var(--brand-blue);
  border-radius: 2px;
  background: var(--brand-blue);
  box-shadow: 0 9px 22px rgba(23, 107, 181, .2);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: .16em;
  transition: background .16s ease, border-color .16s ease, box-shadow .16s ease, transform .16s ease;
}

.login-button svg {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 1.6;
}

.login-button:hover:not(:disabled) {
  border-color: var(--brand-blue-dark);
  background: var(--brand-blue-dark);
  box-shadow: 0 12px 28px rgba(23, 107, 181, .26);
  transform: translateY(-1px);
}

.login-button:focus-visible {
  outline: 2px solid var(--brand-blue);
  outline-offset: 3px;
}

.login-footer {
  margin: 22px 0 0;
  padding-top: 16px;
  border-top: 1px solid #e7ebef;
  color: #939da5;
  font-size: 10px;
  text-align: center;
  letter-spacing: .06em;
}

@media (max-width: 1100px) {
  .login-page {
    grid-template-columns: minmax(0, 1fr) minmax(390px, .8fr);
  }

  .login-culture {
    padding-right: 38px;
    padding-left: 38px;
  }

  .culture-visual {
    grid-template-columns: minmax(0, 1fr) 142px;
    gap: 18px;
  }

  .culture-values li {
    min-height: 42px;
    font-size: 18px;
  }
}

@media (min-width: 901px) and (max-height: 780px) {
  .login-culture {
    padding-top: 28px;
    padding-bottom: 28px;
  }

  .login-culture-content {
    padding-top: 20px;
  }

  .culture-copy h1 {
    font-size: clamp(34px, 3.4vw, 48px);
  }

  .culture-mission {
    margin-top: 12px;
  }

  .culture-visual {
    min-height: 252px;
  }

  .culture-illustration > img {
    max-height: 286px;
  }

  .culture-values li {
    min-height: 38px;
    font-size: 17px;
  }

  .login-card {
    padding-top: 36px;
    padding-bottom: 34px;
  }

  .login-card-header {
    margin-bottom: 22px;
  }

  .login-input-wrap,
  .password-field {
    margin-bottom: 16px;
  }
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .brand-watermark--top {
    top: -180px;
    right: -90px;
  }

  .login-culture {
    padding: 30px clamp(20px, 7vw, 64px) 34px;
  }

  .login-culture-content {
    width: 100%;
    padding-top: 38px;
  }

  .culture-copy h1 {
    font-size: clamp(36px, 8vw, 52px);
  }

  .culture-visual {
    grid-template-columns: minmax(0, 1fr) 170px;
    min-height: 280px;
  }

  .culture-illustration > img {
    max-height: 320px;
  }

  .login-form-panel {
    margin-left: 0;
    padding: 48px 20px 60px;
    border-radius: 0;
    border-top: 1px solid #dfe5ea;
    border-left: 0;
    box-shadow: none;
  }
}

@media (max-width: 600px) {
  .login-brand-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .login-brand-logo {
    width: 162px;
  }

  .login-product-name {
    padding-left: 0;
    border-left: 0;
    font-size: 11px;
  }

  .culture-eyebrow {
    line-height: 1.6;
  }

  .culture-copy h1 {
    font-size: clamp(32px, 11vw, 44px);
  }

  .culture-mission {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
    font-size: 15px;
  }

  .culture-visual {
    grid-template-columns: 1fr;
    gap: 12px;
    margin-top: 18px;
  }

  .culture-illustration > img {
    max-height: 250px;
  }

  .culture-illustration figcaption {
    position: static;
    margin-top: -8px;
  }

  .culture-values {
    padding: 16px 0 0;
    border-top: 1px solid #dfe5ea;
    border-left: 0;
  }

  .culture-values ul {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .culture-values li {
    align-items: flex-start;
    flex-direction: column;
    gap: 3px;
    min-height: auto;
    padding: 8px 0;
    border-bottom: 0;
    font-size: 17px;
  }

  .culture-vision {
    grid-template-columns: 1fr;
    gap: 7px;
    margin-top: 22px;
  }

  .culture-vision-line {
    display: none;
  }

  .login-card {
    padding: 34px 22px 28px;
  }

  .login-card-header h2 {
    font-size: 23px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-button,
  .login-input-wrap {
    transition: none;
  }
}

/* Image-led login composition */
.login-page {
  grid-template-columns: minmax(0, 1.2fr) minmax(430px, .8fr);
  background: #f8fafc;
}

.login-visual {
  position: relative;
  z-index: 1;
  display: grid;
  min-width: 0;
  min-height: 100svh;
  place-items: center;
  overflow: hidden;
  padding: clamp(72px, 8vw, 116px) clamp(34px, 5vw, 76px) clamp(38px, 5vw, 72px);
  background:
    radial-gradient(circle at 48% 50%, rgba(23, 107, 181, .065), transparent 42%),
    linear-gradient(135deg, rgba(255, 255, 255, .96), rgba(246, 249, 252, .84));
}

.login-visual::before,
.login-visual::after {
  position: absolute;
  border: 1px solid rgba(23, 107, 181, .07);
  border-radius: 50%;
  content: "";
  pointer-events: none;
}

.login-visual::before {
  width: min(70vw, 700px);
  aspect-ratio: 1;
}

.login-visual::after {
  width: min(54vw, 540px);
  aspect-ratio: 1;
}

.visual-brand-logo {
  position: absolute;
  top: clamp(30px, 4.5vw, 58px);
  left: clamp(32px, 5vw, 76px);
  z-index: 2;
  display: block;
  width: clamp(172px, 15vw, 218px);
  height: auto;
}

.login-hero-figure {
  position: relative;
  z-index: 1;
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  margin: 0;
}

.login-hero-figure img {
  display: block;
  width: min(100%, 720px);
  max-height: min(74svh, 700px);
  object-fit: contain;
  filter: drop-shadow(0 22px 36px rgba(31, 73, 108, .09));
  mix-blend-mode: multiply;
}

.login-form-panel {
  overflow: hidden;
  margin-left: -42px;
  border: 0;
  border-radius: 72px 0 0 72px;
  background:
    linear-gradient(150deg, rgba(239, 246, 251, .92), rgba(226, 237, 245, .78)),
    #edf4f8;
  box-shadow: -24px 0 64px rgba(41, 70, 94, .09);
}

.login-panel-ambient {
  position: absolute;
  z-index: 0;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.login-panel-ambient::before {
  position: absolute;
  top: -18%;
  right: -62%;
  width: min(860px, 88vw);
  aspect-ratio: 1;
  border: 1px solid rgba(255, 255, 255, .74);
  border-radius: 50%;
  background: rgba(255, 255, 255, .28);
  box-shadow: inset 0 0 90px rgba(255, 255, 255, .6);
  content: "";
}

.login-panel-ambient img {
  position: absolute;
  top: 54%;
  left: -28%;
  width: min(700px, 70vw);
  max-width: none;
  opacity: .25;
  filter: blur(12px) saturate(.9);
  mix-blend-mode: multiply;
  transform: translateY(-50%) rotate(-8deg) scale(1.12);
}

.login-card {
  z-index: 1;
  overflow: visible;
  border: 1px solid rgba(255, 255, 255, .82);
  border-radius: 24px;
  background: rgba(255, 255, 255, .84);
  box-shadow:
    0 30px 80px rgba(44, 73, 98, .13),
    inset 0 1px 0 rgba(255, 255, 255, .92);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
}

.login-card-accent {
  top: -1px;
  right: 54px;
  left: 54px;
  height: 3px;
  border-radius: 0 0 999px 999px;
  opacity: .68;
}

.login-card-header h1 {
  margin: 0;
  color: #172635;
  font-size: 27px;
  font-weight: 680;
  letter-spacing: .02em;
}

.login-input-wrap {
  border-bottom-color: rgba(154, 173, 188, .44);
}

.login-button {
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(23, 107, 181, .18);
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    min-height: 540px;
    padding: 92px 32px 64px;
  }

  .login-hero-figure img {
    width: min(100%, 560px);
    max-height: 430px;
  }

  .login-form-panel {
    min-height: 760px;
    margin: -38px 0 0;
    border-radius: 56px 56px 0 0;
  }

  .login-panel-ambient::before {
    top: -32%;
    right: -28%;
  }

  .login-panel-ambient img {
    left: -22%;
    width: 720px;
  }
}

@media (max-width: 600px) {
  .login-visual {
    min-height: 420px;
    padding: 84px 20px 54px;
  }

  .visual-brand-logo {
    top: 28px;
    left: 26px;
    width: 158px;
  }

  .login-hero-figure img {
    width: min(100%, 360px);
    max-height: 310px;
  }

  .login-form-panel {
    min-height: 700px;
    padding-top: 68px;
    border-radius: 44px 44px 0 0;
  }

  .login-card {
    border-radius: 20px;
  }

  .login-card-header h1 {
    font-size: 23px;
  }
}

/* Public-service portal layout */
.login-page {
  display: grid;
  grid-template-rows: 88px minmax(0, 1fr) 78px;
  grid-template-columns: 1fr;
  background: #eaf2ff;
}

.portal-header {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, .06), transparent 36%),
    linear-gradient(90deg, #2457bc, #315fca 52%, #2457bc);
  box-shadow: 0 2px 0 rgba(112, 211, 255, .7);
}

.portal-header::after {
  position: absolute;
  right: 0;
  bottom: 7px;
  left: 0;
  height: 1px;
  background: rgba(150, 224, 255, .58);
  content: "";
}

.portal-header-inner,
.portal-footer-inner {
  display: flex;
  width: min(1280px, calc(100% - 64px));
  align-items: center;
  margin: 0 auto;
}

.portal-header-inner {
  width: 100%;
  gap: 16px;
  margin: 0;
  padding: 0 clamp(24px, 3vw, 56px);
}

.portal-header-inner img {
  width: 46px;
  height: 42px;
  padding: 4px;
  border-radius: 9px;
  background: transparent;
  object-fit: contain;
  mix-blend-mode: screen;
}

.portal-header-inner span {
  padding-left: 16px;
  border-left: 1px solid rgba(255, 255, 255, .36);
  color: #fff;
  font-size: 19px;
  font-weight: 650;
  letter-spacing: .08em;
}

.portal-stage {
  position: relative;
  z-index: 1;
  min-height: 0;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 29% 52%, rgba(255, 255, 255, .98) 0%, rgba(255, 255, 255, .92) 38%, transparent 66%),
    radial-gradient(circle at 54% 20%, rgba(255, 255, 255, .96), transparent 42%),
    radial-gradient(circle at 92% 58%, rgba(113, 155, 240, .22), transparent 34%),
    linear-gradient(110deg, #d9e8ff 0%, #f8fbff 51%, #dce8ff 100%);
}

.portal-stage::before,
.portal-stage::after {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, .26);
  content: "";
  pointer-events: none;
}

.portal-stage::before {
  top: -62%;
  left: -18%;
  width: 68vw;
  aspect-ratio: 1;
}

.portal-stage::after {
  right: -17%;
  bottom: -78%;
  width: 58vw;
  aspect-ratio: 1;
}

.portal-stage-inner {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.16fr) minmax(390px, .84fr);
  gap: clamp(36px, 5vw, 82px);
  width: min(1320px, 100%);
  height: 100%;
  align-items: center;
  margin: 0 auto;
  padding: 24px clamp(36px, 5vw, 72px);
}

.login-visual {
  min-height: 0;
  height: 100%;
  overflow: visible;
  padding: 0;
  background: transparent;
}

.login-visual::before,
.login-visual::after {
  display: none;
}

.login-hero-figure::before {
  position: absolute;
  z-index: 0;
  inset: 2% -6%;
  background: radial-gradient(
    ellipse at center,
    rgba(255, 255, 255, .98) 0%,
    rgba(255, 255, 255, .9) 58%,
    rgba(255, 255, 255, .48) 76%,
    transparent 100%
  );
  filter: blur(18px);
  content: "";
  pointer-events: none;
}

.login-hero-figure img {
  position: relative;
  z-index: 1;
  width: min(100%, 680px);
  max-height: min(62svh, 560px);
  filter: saturate(.98) contrast(.99);
  mix-blend-mode: multiply;
  -webkit-mask-image: radial-gradient(
    ellipse 74% 80% at center,
    #000 60%,
    rgba(0, 0, 0, .96) 74%,
    rgba(0, 0, 0, .58) 88%,
    transparent 100%
  );
  mask-image: radial-gradient(
    ellipse 74% 80% at center,
    #000 60%,
    rgba(0, 0, 0, .96) 74%,
    rgba(0, 0, 0, .58) 88%,
    transparent 100%
  );
}

.login-form-panel {
  min-height: 0;
  overflow: visible;
  margin: 0;
  padding: 22px;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.login-panel-ambient {
  inset: -16%;
  border-radius: 50%;
}

.login-panel-ambient::before {
  top: -28%;
  right: -48%;
  width: 720px;
  background: rgba(255, 255, 255, .18);
}

.login-panel-ambient img {
  top: 55%;
  left: -32%;
  width: 620px;
  opacity: .13;
  filter: blur(15px) saturate(.9);
}

.login-card {
  width: min(430px, 100%);
  padding: 38px 44px 34px;
  border: 1px solid rgba(255, 255, 255, .95);
  border-radius: 18px;
  background: rgba(255, 255, 255, .94);
  box-shadow: 0 20px 50px rgba(40, 72, 132, .18);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.login-card-accent {
  right: 48px;
  left: 48px;
}

.login-card-header {
  margin-bottom: 24px;
}

.login-card-header h1 {
  color: #2457b8;
  font-size: 25px;
}

.login-card-header span {
  color: #8290a1;
}

.login-input-wrap {
  border-bottom-color: rgba(125, 151, 180, .32);
}

.login-button {
  min-height: 48px;
  border-radius: 8px;
  background: #285bc4;
  box-shadow: 0 9px 22px rgba(37, 88, 192, .2);
}

.portal-footer {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, .05), transparent 38%),
    linear-gradient(90deg, #2457bc, #315fca 52%, #2457bc);
}

.portal-footer-inner {
  gap: 24px;
  color: rgba(255, 255, 255, .92);
}

.portal-footer-inner span {
  font-size: 13px;
  font-weight: 600;
}

.portal-footer-inner small {
  margin-left: auto;
  color: rgba(255, 255, 255, .7);
  font-size: 11px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-rows: 72px auto 70px;
    overflow: auto;
  }

  .portal-header-inner,
  .portal-footer-inner {
    width: min(100% - 36px, 720px);
  }

  .portal-header-inner {
    width: 100%;
    padding: 0 24px;
  }

  .portal-header-inner img {
    width: 42px;
    height: 38px;
  }

  .portal-header-inner span {
    font-size: 14px;
  }

  .portal-stage {
    overflow: visible;
  }

  .portal-stage-inner {
    grid-template-columns: 1fr;
    gap: 12px;
    height: auto;
    padding: 30px 20px 44px;
  }

  .login-visual {
    min-height: 350px;
    height: auto;
    padding: 0;
  }

  .login-hero-figure img {
    width: min(100%, 520px);
    max-height: 390px;
  }

  .login-form-panel {
    min-height: 0;
    margin: 0;
    padding: 18px 0 0;
    border-radius: 0;
  }

  .login-card {
    margin: 0 auto;
  }

  .portal-footer-inner small {
    display: none;
  }
}

@media (max-width: 600px) {
  .login-page {
    grid-template-rows: 66px auto 62px;
  }

  .portal-header::after {
    bottom: 5px;
  }

  .portal-header-inner {
    gap: 10px;
    padding: 0 18px;
  }

  .portal-header-inner img {
    width: 38px;
    height: 36px;
  }

  .portal-header-inner span {
    padding-left: 10px;
    font-size: 11px;
    letter-spacing: .03em;
  }

  .portal-stage-inner {
    padding: 22px 16px 32px;
  }

  .login-visual {
    min-height: 280px;
  }

  .login-hero-figure img {
    width: min(100%, 350px);
    max-height: 280px;
  }

  .login-card {
    padding: 32px 22px 28px;
    border-radius: 16px;
  }

  .login-card-header h1 {
    font-size: 22px;
  }

  .portal-footer-inner {
    justify-content: center;
  }

  .portal-footer-inner img {
    width: 142px;
  }

  .portal-footer-inner span {
    display: none;
  }
}
</style>
