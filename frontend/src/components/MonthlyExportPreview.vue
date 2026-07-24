<script setup lang="ts">
import type { MonthlyExportReport } from '../api/types'

defineProps<{
  report: MonthlyExportReport
}>()

function formatGeneratedAt(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function formatMd(value: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)
  return match ? `${match[2]}-${match[3]}` : value
}

function statusClass(status: string) {
  if (status === '进行中') return 'st-active'
  if (status === '已完成') return 'st-done'
  return 'st-prep'
}

function scoreClass(level: string) {
  if (level === 'HIGH') return 'score-high'
  if (level === 'MEDIUM') return 'score-med'
  return 'score-low'
}

function groupTitle(taCode: string, taName: string) {
  if (taName && taCode && taName !== taCode) return `${taName} · ${taCode}`
  return taName || taCode || '未分类'
}
</script>

<template>
  <article class="export-sheet" id="monthly-export-print-root">
    <header class="export-masthead">
      <h1>临床研发管线月度报告</h1>
    </header>
    <div class="export-meta">
      <div class="export-meta-right">生成于 {{ formatGeneratedAt(report.meta.generatedAt) }}</div>
      <div>汇报时间段 <strong>{{ report.meta.startDate }} 至 {{ report.meta.endDate }}</strong></div>
      <div>导出范围 · {{ report.meta.scopeLabels.join('、') }}</div>
    </div>

    <div class="export-body">
      <section class="export-sec">
        <div class="export-sec-h"><span class="export-sec-num">01</span><h2>Study 汇总</h2></div>
        <div class="export-metrics export-metrics--4">
          <div class="export-metric"><div class="lbl">总数</div><div class="val">{{ report.summary.total }}</div></div>
          <div class="export-metric"><div class="lbl">未开始</div><div class="val">{{ report.summary.notStarted }}</div></div>
          <div class="export-metric"><div class="lbl">进行中</div><div class="val">{{ report.summary.inProgress }}</div></div>
          <div class="export-metric"><div class="lbl">已完成</div><div class="val">{{ report.summary.completed }}</div></div>
        </div>
        <div class="export-metric-note">
          有填报 Study {{ report.summary.reportedStudyCount }} · 未关闭风险 {{ report.summary.openRiskCount }}
        </div>
      </section>

      <section class="export-sec">
        <div class="export-sec-h"><span class="export-sec-num">02</span><h2>管线快照</h2></div>
        <p v-if="!report.snapshotGroups.length" class="export-empty">当前范围内暂无 Study。</p>
        <template v-else>
          <div v-for="group in report.snapshotGroups" :key="`${group.taCode}-${group.taName}`">
            <div class="export-ta-bar">{{ groupTitle(group.taCode, group.taName) }}</div>
            <table class="export-table">
              <thead>
                <tr>
                  <th>Program</th>
                  <th>Study</th>
                  <th>适应症</th>
                  <th>阶段</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in group.rows" :key="row.studyCode">
                  <td class="mono">{{ row.programCode }}</td>
                  <td class="mono">{{ row.studyCode }}</td>
                  <td>{{ row.indication }}</td>
                  <td>{{ row.phase }}</td>
                  <td :class="statusClass(row.projectStatus)">{{ row.projectStatus }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </section>

      <section class="export-sec">
        <div class="export-sec-h"><span class="export-sec-num">03</span><h2>月报进展</h2></div>
        <p v-if="!report.progress.length" class="export-empty">所选时间段内暂无月报进展。</p>
        <div v-for="(item, index) in report.progress" :key="`${item.studyCode}-${item.entryDate}-${index}`" class="export-prog">
          <div class="export-prog-date">{{ formatMd(item.entryDate) }}</div>
          <div>
            <div class="export-prog-study">{{ item.studyCode }}</div>
            <div class="export-prog-meta">{{ item.programCode }} · {{ item.taName }}</div>
            <div class="export-prog-line">
              <span class="export-pill">{{ item.functionCode }}</span>
              {{ item.content }}
            </div>
          </div>
        </div>
      </section>

      <section class="export-sec">
        <div class="export-sec-h"><span class="export-sec-num">04</span><h2>未关闭风险</h2></div>
        <p v-if="!report.openRisks.length" class="export-empty">当前范围内无未关闭风险。</p>
        <div v-for="(risk, index) in report.openRisks" :key="risk.riskCode" class="export-risk">
          <div class="export-risk-idx">{{ String(index + 1).padStart(2, '0') }}</div>
          <div>
            <div class="mono export-risk-code">{{ risk.riskCode }}</div>
            <div class="export-risk-desc">{{ risk.description }}</div>
            <div class="export-risk-meta">{{ risk.programCode }} · {{ risk.ownerName }}</div>
          </div>
          <div class="export-risk-score" :class="scoreClass(risk.level)">{{ risk.score }}</div>
        </div>
      </section>
    </div>

    <footer class="export-foot">临床研发平台 · 月报导出 · 机密</footer>
  </article>
</template>
