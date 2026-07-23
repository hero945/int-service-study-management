import { readonly, ref } from 'vue'
import { apiClient } from './api/client'

/** 侧栏「风险管理」Open 风险数；新增/关闭/删除后由调用方 refresh */
const openCount = ref<number | null>(null)

async function refresh() {
  try {
    const page = await apiClient.listRisks({ page: 1, pageSize: 1 })
    openCount.value = page.stats.open
  } catch {
    openCount.value = null
  }
}

export const riskBadge = {
  openCount: readonly(openCount),
  refresh,
}
