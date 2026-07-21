<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { session } from '../session'

const route = useRoute()
const router = useRouter()
const credentials = reactive({ username: '', password: '' })
const errorMessage = ref('')
const submitting = ref(false)
const isMockMode = import.meta.env.VITE_API_MODE === 'mock'

async function submit() {
  errorMessage.value = ''
  submitting.value = true
  try {
    await session.login(credentials)
    const redirect =
      typeof route.query.redirect === 'string' ? route.query.redirect : '/pipeline'
    await router.replace(redirect)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
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
        autocomplete="username"
        placeholder="you@company.com"
        required
      >

      <label for="password">密码</label>
      <input
        id="password"
        v-model="credentials.password"
        name="password"
        type="password"
        autocomplete="current-password"
        placeholder="请输入密码"
        required
      >

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
    </form>
  </main>
</template>
