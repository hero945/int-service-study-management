<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { apiClient, ApiError } from '../api/client'
import type {
  MonthlyExportFormat,
  MonthlyExportQuery,
  MonthlyExportReport,
  MonthlyExportScopeType,
  PipelineProgram,
  TherapeuticArea,
} from '../api/types'
import MonthlyExportPreview from '../components/MonthlyExportPreview.vue'
import { session } from '../session'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

function firstDayOfMonthIso() {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-01`
}

const startDate = ref(firstDayOfMonthIso())
const endDate = ref(todayIso())
const scopeType = ref<MonthlyExportScopeType>('ALL')
const selectedTaIds = ref<number[]>([])
const selectedProgramIds = ref<number[]>([])
const therapeuticAreas = ref<TherapeuticArea[]>([])
const programs = ref<PipelineProgram[]>([])

const loadingOptions = ref(false)
const generating = ref(false)
const exporting = ref(false)
const error = ref('')
const report = ref<MonthlyExportReport | null>(null)

const canExport = computed(() =>
  (session.currentUser.value?.permissions ?? []).includes('report.export'))

const query = computed<MonthlyExportQuery>(() => ({
  startDate: startDate.value,
  endDate: endDate.value,
  scopeType: scopeType.value,
  taIds: scopeType.value === 'TA' ? selectedTaIds.value : [],
  programIds: scopeType.value === 'PROGRAM' ? selectedProgramIds.value : [],
}))

onMounted(async () => {
  loadingOptions.value = true
  try {
    const [areas, programList] = await Promise.all([
      apiClient.listTherapeuticAreas(),
      apiClient.listPrograms(),
    ])
    therapeuticAreas.value = areas
    programs.value = programList
  } catch (err) {
    error.value = err instanceof Error ? err.message : '加载筛选选项失败'
  } finally {
    loadingOptions.value = false
  }
})

function validate(): string | null {
  if (!startDate.value || !endDate.value) return '请选择汇报开始与结束日期'
  if (endDate.value < startDate.value) return '结束日期不能早于开始日期'
  if (scopeType.value === 'TA' && selectedTaIds.value.length === 0) {
    return '请至少选择一个治疗领域'
  }
  if (scopeType.value === 'PROGRAM' && selectedProgramIds.value.length === 0) {
    return '请至少选择一个 Program'
  }
  return null
}

async function generatePreview() {
  const message = validate()
  if (message) {
    error.value = message
    return
  }
  generating.value = true
  error.value = ''
  try {
    report.value = await apiClient.previewMonthlyExport(query.value)
  } catch (err) {
    report.value = null
    error.value = err instanceof ApiError || err instanceof Error
      ? err.message
      : '生成月报失败'
  } finally {
    generating.value = false
  }
}

async function download(format: MonthlyExportFormat) {
  if (!canExport.value) {
    error.value = '当前账号无导出权限'
    return
  }
  const message = validate()
  if (message) {
    error.value = message
    return
  }
  exporting.value = true
  error.value = ''
  try {
    await apiClient.downloadMonthlyExport(query.value, format)
  } catch (err) {
    error.value = err instanceof ApiError || err instanceof Error
      ? err.message
      : '导出失败'
  } finally {
    exporting.value = false
  }
}

function toggleId(list: number[], id: number) {
  return list.includes(id) ? list.filter((item) => item !== id) : [...list, id]
}
</script>

<template>
  <section class="page-content">
    <div class="report-layout">
      <div class="data-card report-card export-controls">
        <h2>月报导出</h2>
        <p>选择汇报时间段与导出范围，生成 Study 汇总、管线快照、月报进展与未关闭风险。</p>

        <fieldset class="export-fieldset">
          <legend>汇报时间段</legend>
          <div class="export-date-row">
            <label>
              开始日期
              <input v-model="startDate" type="date">
            </label>
            <label>
              结束日期
              <input v-model="endDate" type="date">
            </label>
          </div>
          <p class="export-hint">时间段仅用于圈选「月报进展」条目日期。</p>
        </fieldset>

        <fieldset class="export-fieldset">
          <legend>导出范围</legend>
          <label class="export-radio">
            <input v-model="scopeType" type="radio" value="ALL">
            全部项目
          </label>
          <label class="export-radio">
            <input v-model="scopeType" type="radio" value="TA">
            按照 TA
          </label>
          <div v-if="scopeType === 'TA'" class="export-checkboxes" role="group" aria-label="治疗领域">
            <label v-for="area in therapeuticAreas" :key="area.id" class="export-check">
              <input
                type="checkbox"
                :checked="selectedTaIds.includes(area.id)"
                @change="selectedTaIds = toggleId(selectedTaIds, area.id)"
              >
              {{ area.name }}
            </label>
            <p v-if="loadingOptions" class="export-hint">正在加载治疗领域…</p>
          </div>

          <label class="export-radio">
            <input v-model="scopeType" type="radio" value="PROGRAM">
            按照 Program
          </label>
          <div v-if="scopeType === 'PROGRAM'" class="export-checkboxes" role="group" aria-label="Program">
            <label v-for="program in programs" :key="program.id" class="export-check">
              <input
                type="checkbox"
                :checked="selectedProgramIds.includes(program.id)"
                @change="selectedProgramIds = toggleId(selectedProgramIds, program.id)"
              >
              <span class="mono">{{ program.code }}</span>
              <span class="export-check-meta">{{ program.productName }}</span>
            </label>
            <p v-if="loadingOptions" class="export-hint">正在加载 Program…</p>
          </div>
        </fieldset>

        <p v-if="error" class="form-error" role="alert">{{ error }}</p>

        <button class="primary-button" type="button" :disabled="generating" @click="generatePreview">
          {{ generating ? '生成中…' : '生成月报' }}
        </button>

        <div v-if="report && canExport" class="export-actions">
          <button class="secondary-button" type="button" :disabled="exporting" @click="download('html')">
            下载 HTML
          </button>
          <button class="secondary-button" type="button" :disabled="exporting" @click="download('xlsx')">
            全量 Excel
          </button>
        </div>
        <p v-else-if="report && !canExport" class="export-hint">当前账号可预览，但无文件导出权限。</p>
      </div>

      <div class="export-preview-panel">
        <div v-if="!report" class="state-panel">
          <strong>导出预览</strong>
          <span>选择条件后点击「生成月报」，右侧将显示报告预览。</span>
        </div>
        <MonthlyExportPreview v-else :report="report" />
      </div>
    </div>
  </section>
</template>
