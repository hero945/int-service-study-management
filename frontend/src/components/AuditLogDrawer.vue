<script setup lang="ts">
import { nextTick, ref, toRef, watch } from 'vue'
import { apiClient } from '../api/client'
import { formatApiError } from '../api/errors'
import type {
  AuditLogEntry,
  AuditGroupType,
  AuditModuleCode,
  AuditResultCode,
  AuditSubjectType,
} from '../api/types'
import { useEscapeClose } from '../composables/useEscapeClose'

const props = defineProps<{
  open: boolean
  title: string
  moduleCode: AuditModuleCode
  subjectType?: AuditSubjectType
  subjectId?: number
  scopeStudyId?: number
  groupType?: AuditGroupType
  groupId?: number
  groupCode?: string
}>()
const emit = defineEmits<{ close: [] }>()

const loading = ref(false)
const error = ref('')
const entries = ref<AuditLogEntry[]>([])
const resultCode = ref<AuditResultCode | ''>('')
const page = ref(1)
const pageSize = 20
const totalItems = ref(0)
const totalPages = ref(0)
const closeButton = ref<HTMLButtonElement>()
let opener: HTMLElement | null = null
let sequence = 0

useEscapeClose(toRef(props, 'open'), close)

watch(
  () => [
    props.open,
    props.moduleCode,
    props.subjectType,
    props.subjectId,
    props.scopeStudyId,
    props.groupType,
    props.groupId,
    props.groupCode,
  ],
  async ([open]) => {
    if (!open) return
    opener = document.activeElement instanceof HTMLElement ? document.activeElement : null
    page.value = 1
    resultCode.value = ''
    await load()
    await nextTick()
    closeButton.value?.focus()
  },
)

watch(resultCode, () => {
  page.value = 1
  void load()
})

async function load() {
  if (!props.open) return
  const current = ++sequence
  loading.value = true
  error.value = ''
  try {
    const response = await apiClient.listAuditLogs({
      moduleCode: props.moduleCode,
      subjectType: props.subjectType,
      subjectId: props.subjectId,
      scopeStudyId: props.scopeStudyId,
      groupType: props.groupType,
      groupId: props.groupId,
      groupCode: props.groupCode,
      resultCode: resultCode.value || undefined,
      page: page.value,
      pageSize,
    })
    if (current !== sequence) return
    entries.value = response.data
    totalItems.value = response.totalItems
    totalPages.value = response.totalPages
  } catch (cause) {
    if (current !== sequence) return
    error.value = formatApiError(cause, '操作日志加载失败')
    entries.value = []
  } finally {
    if (current === sequence) loading.value = false
  }
}

function changePage(nextPage: number) {
  page.value = nextPage
  void load()
}

function close() {
  emit('close')
  nextTick(() => opener?.focus())
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function display(value: unknown) {
  if (value == null || value === '') return '—'
  return typeof value === 'object' ? JSON.stringify(value) : String(value)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="drawer-overlay" @mousedown.self="close">
      <aside class="audit-drawer" role="dialog" aria-modal="true" aria-labelledby="audit-title">
        <header class="audit-header">
          <div>
            <h2 id="audit-title">{{ title }}</h2>
            <p>共 {{ totalItems }} 条操作记录，按发生时间倒序展示</p>
          </div>
          <button ref="closeButton" type="button" class="drawer-close" aria-label="关闭操作日志" @click="close">×</button>
        </header>

        <div class="audit-toolbar">
          <label>
            <span>结果</span>
            <select v-model="resultCode">
              <option value="">全部</option>
              <option value="SUCCESS">成功</option>
              <option value="FAILED">失败</option>
              <option value="DENIED">拒绝</option>
            </select>
          </label>
          <button type="button" class="secondary-button" :disabled="loading" @click="load">刷新</button>
        </div>

        <div class="audit-body" aria-live="polite">
          <p v-if="loading" class="audit-state">正在加载操作日志…</p>
          <div v-else-if="error" class="audit-state audit-state--error" role="alert">
            <p>{{ error }}</p>
            <button type="button" class="secondary-button" @click="load">重试</button>
          </div>
          <p v-else-if="!entries.length" class="audit-state">暂无操作日志</p>

          <article v-for="entry in entries" v-else :key="entry.id" class="audit-event">
            <div class="audit-event__summary">
              <div>
                <span class="audit-result" :class="`audit-result--${entry.resultCode.toLowerCase()}`">
                  {{ entry.resultCode === 'SUCCESS' ? '成功' : entry.resultCode === 'FAILED' ? '失败' : '拒绝' }}
                </span>
                <strong>{{ entry.actionLabel }}</strong>
                <span v-if="entry.subjectCode" class="audit-subject">{{ entry.subjectCode }}</span>
              </div>
              <time :datetime="entry.occurredTime">{{ formatTime(entry.occurredTime) }}</time>
            </div>
            <p class="audit-operator">
              {{ entry.operatorDisplayName || entry.operatorEmail }}
              <span v-if="entry.operatorDisplayName">（{{ entry.operatorEmail }}）</span>
            </p>
            <p v-if="entry.operationReason" class="audit-reason">{{ entry.operationReason }}</p>
            <p v-if="entry.errorCode" class="audit-error">错误码：{{ entry.errorCode }}</p>

            <details>
              <summary>查看字段差异与请求信息</summary>
              <p v-if="entry.historicalSnapshotMissing" class="audit-history-note">
                历史日志未保存字段快照。
              </p>
              <table v-else-if="entry.changes.length" class="audit-change-table">
                <thead><tr><th>字段</th><th>操作前</th><th>操作后</th></tr></thead>
                <tbody>
                  <tr v-for="change in entry.changes" :key="change.fieldName">
                    <th>{{ change.fieldLabel }}</th>
                    <td>{{ display(change.beforeValue) }}</td>
                    <td>{{ display(change.afterValue) }}</td>
                  </tr>
                </tbody>
              </table>
              <p v-else class="audit-history-note">该事件没有可展示的字段变化。</p>
              <dl class="audit-request">
                <div><dt>Request ID</dt><dd>{{ entry.requestId || '—' }}</dd></div>
                <div><dt>请求</dt><dd>{{ entry.requestMethod || '—' }} {{ entry.requestPath || '—' }}</dd></div>
                <div><dt>IP</dt><dd>{{ entry.ipAddress || '—' }}</dd></div>
                <div><dt>实际修改记录</dt><dd>{{ entry.targetTable }} / {{ entry.targetId ?? '—' }}</dd></div>
              </dl>
            </details>
          </article>
        </div>

        <footer class="audit-footer">
          <button type="button" class="secondary-button" :disabled="page <= 1 || loading" @click="changePage(page - 1)">上一页</button>
          <span>第 {{ page }} / {{ Math.max(totalPages, 1) }} 页</span>
          <button type="button" class="secondary-button" :disabled="page >= Math.max(totalPages, 1) || loading" @click="changePage(page + 1)">下一页</button>
        </footer>
      </aside>
    </div>
  </Teleport>
</template>

<style scoped>
/* 遮罩/阴影/关闭按钮复用全局 .drawer-overlay / .drawer-close（z-drawer、滑入动画与其余抽屉一致） */
.audit-drawer {
  width: min(720px, 90vw);
  max-width: 100%;
  height: 100%;
  background: var(--surface);
  box-shadow: var(--shadow-drawer);
  display: flex;
  flex-direction: column;
  animation: drawer-slide-in .2s ease-out;
}
.audit-header { display: flex; justify-content: space-between; gap: 20px; padding: 18px 22px; border-bottom: 1px solid var(--line); }
.audit-header h2 { margin: 0; font-size: 18px; }
.audit-header p { margin: 6px 0 0; color: var(--muted); font-size: 12px; }
.audit-toolbar { display: flex; align-items: end; justify-content: space-between; padding: 12px 22px; border-bottom: 1px solid var(--line); }
.audit-toolbar label { display: grid; gap: 6px; font-size: 12px; color: var(--muted); }
.audit-toolbar select {
  min-width: 132px;
  height: 34px;
  padding: 0 11px;
  border: 1px solid var(--line-input);
  border-radius: 8px;
  background: var(--surface);
  color: var(--ink-2);
  font-size: 12px;
}
.audit-toolbar select:focus { border-color: var(--accent); box-shadow: var(--shadow-focus); outline: none; }
.audit-body { flex: 1; overflow: auto; padding: 18px 22px; background: #f8fafc; }
.audit-state { text-align: center; color: var(--muted); font-size: 12.5px; padding: 44px 12px; }
.audit-state--error { color: var(--red-text); }
.audit-event { background: var(--surface); border: 1px solid var(--line); border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.audit-event__summary { display: flex; justify-content: space-between; gap: 12px; }
.audit-event__summary > div { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.audit-event__summary time { color: var(--muted); font-size: 12px; white-space: nowrap; }
.audit-result { font-size: 12px; padding: 2px 7px; border-radius: 999px; }
.audit-result--success { background: var(--green-bg); color: var(--green-text); }
.audit-result--failed { background: var(--red-bg); color: var(--red-text); }
.audit-result--denied { background: var(--orange-bg); color: var(--orange-text); }
.audit-subject { color: var(--text-2); font-family: var(--font-mono); }
.audit-operator, .audit-reason, .audit-error { margin: 9px 0 0; font-size: 13px; }
.audit-error { color: var(--red-text); }
details { margin-top: 14px; border-top: 1px dashed var(--line-strong); padding-top: 12px; }
summary { cursor: pointer; color: var(--accent); }
.audit-change-table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 12.5px; }
.audit-change-table th, .audit-change-table td { border: 1px solid #eef1f4; padding: 7px; text-align: left; vertical-align: top; word-break: break-word; }
.audit-change-table thead th { background: #f7f9fb; color: #7a8493; font-size: 11px; font-weight: 600; letter-spacing: .35px; }
.audit-change-table tbody tr:hover th, .audit-change-table tbody tr:hover td { background: #f5f8fd; }
.audit-history-note { color: var(--orange-text); background: var(--orange-bg); padding: 8px; border-radius: 6px; }
.audit-request { display: grid; gap: 6px; font-size: 12px; }
.audit-request div { display: grid; grid-template-columns: 110px 1fr; }
.audit-request dt { color: var(--muted); }
.audit-request dd { margin: 0; word-break: break-all; }
.audit-footer { display: flex; justify-content: center; align-items: center; gap: 14px; padding: 14px; border-top: 1px solid var(--line); color: var(--muted); font-size: 11.5px; }
@media (max-width: 640px) {
  .audit-header, .audit-toolbar, .audit-body { padding-left: 16px; padding-right: 16px; }
  .audit-event__summary { display: block; }
  .audit-event__summary time { display: block; margin-top: 8px; }
}
</style>
