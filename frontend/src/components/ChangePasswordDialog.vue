<script setup lang="ts">
import { ref, toRef } from 'vue'
import { apiClient } from '../api/client'
import { formatApiError } from '../api/errors'
import { PASSWORD_RULE_HINT, validateNewPassword } from '../domain/password-rules'
import { useEscapeClose } from '../composables/useEscapeClose'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const saving = ref(false)
const error = ref('')
const form = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })

function requestClose() {
  if (saving.value) return
  emit('close')
}

useEscapeClose(toRef(props, 'open'), requestClose)

async function submit() {
  error.value = ''
  if (!form.value.currentPassword) {
    error.value = '请输入当前密码'
    return
  }
  const ruleError = validateNewPassword(form.value.newPassword)
  if (ruleError) {
    error.value = ruleError
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    error.value = '两次输入的新密码不一致'
    return
  }
  if (form.value.currentPassword === form.value.newPassword) {
    error.value = '新密码不能与当前密码相同'
    return
  }
  saving.value = true
  try {
    await apiClient.changePassword({
      currentPassword: form.value.currentPassword,
      newPassword: form.value.newPassword,
    })
    form.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
    emit('close')
  } catch (reason) {
    error.value = formatApiError(reason, '修改密码失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="dialog-backdrop" @click.self="requestClose">
      <form class="role-dialog role-dialog--sm" @submit.prevent="submit">
        <header>
          <div>
            <h2>修改密码</h2>
            <p>{{ PASSWORD_RULE_HINT }}</p>
          </div>
          <button type="button" aria-label="关闭" @click="requestClose">×</button>
        </header>
        <div class="role-form-grid role-form-grid--single">
          <label>
            当前密码
            <input v-model="form.currentPassword" type="password" autocomplete="current-password" maxlength="128">
          </label>
          <label>
            新密码
            <input v-model="form.newPassword" type="password" autocomplete="new-password" maxlength="128">
          </label>
          <label>
            确认新密码
            <input v-model="form.confirmPassword" type="password" autocomplete="new-password" maxlength="128">
          </label>
        </div>
        <p v-if="error" class="form-error dialog-form-error" role="alert">{{ error }}</p>
        <footer>
          <button class="secondary-button" type="button" @click="requestClose">取消</button>
          <button class="primary-button" type="submit" :disabled="saving">
            {{ saving ? '保存中…' : '确认修改' }}
          </button>
        </footer>
      </form>
    </div>
  </Teleport>
</template>
