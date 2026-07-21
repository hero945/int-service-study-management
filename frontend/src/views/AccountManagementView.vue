<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiClient } from '../api/client'
import type { PlatformUser } from '../api/types'
import PageState from '../components/PageState.vue'

const users = ref<PlatformUser[]>([])
const loading = ref(true)
const error = ref('')
onMounted(async () => {
  try {
    users.value = await apiClient.listUsers()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '账号列表加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar"><span>密码不会在页面或接口中返回</span><button class="primary-button" type="button">＋ 新增账号</button></div>
    <PageState :loading :error :empty="!users.length">
      <div class="data-card"><table class="data-table"><thead><tr><th>姓名</th><th>账号</th><th>角色</th><th>状态</th></tr></thead><tbody><tr v-for="user in users" :key="user.id"><td class="strong">{{ user.displayName }}</td><td class="mono">{{ user.username }}</td><td><span class="status-chip status-chip--blue">{{ user.roles.join('、') }}</span></td><td>{{ user.enabled ? '启用' : '停用' }}</td></tr></tbody></table></div>
    </PageState>
  </section>
</template>
