<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiClient } from '../api/client'
import type { TeamAssignment } from '../api/types'
import PageState from '../components/PageState.vue'

const assignments = ref<TeamAssignment[]>([])
const loading = ref(true)
const error = ref('')
onMounted(async () => {
  try {
    assignments.value = await apiClient.listTeamAssignments()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '团队矩阵加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-content">
    <div class="page-toolbar"><span>仅管理员及项目 PL/PM 可维护团队矩阵</span><button class="secondary-button" type="button">编辑矩阵</button></div>
    <PageState :loading :error :empty="!assignments.length" empty-title="暂无团队分配" empty-description="已预留 /api/v1/team-assignments 接口。">
      <div class="data-card"><table class="data-table"><thead><tr><th>Project</th><th>Study</th><th>部门</th><th>角色</th><th>成员</th></tr></thead><tbody><tr v-for="assignment in assignments" :key="`${assignment.studyCode}-${assignment.roleCode}`"><td>{{ assignment.project }}</td><td>{{ assignment.studyCode }}</td><td>{{ assignment.department }}</td><td>{{ assignment.roleName }}</td><td>{{ assignment.members.join('、') }}</td></tr></tbody></table></div>
    </PageState>
  </section>
</template>
