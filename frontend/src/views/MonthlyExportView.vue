<script setup lang="ts">
const month = new Date().toISOString().slice(0, 7)
</script>

<template>
  <section class="page-content">
    <div class="report-layout">
      <div class="data-card report-card">
        <h2>月报导出</h2>
        <p>选择汇报月份与导出范围，后端汇总接口接通后可生成项目月报。</p>
        <label>汇报月份<input type="month" :value="month"></label>
        <label>导出范围<select><option>全部可见项目</option><option>按治疗领域</option><option>按 Program</option></select></label>
        <button class="primary-button" type="button">生成月报</button>
      </div>
      <div class="state-panel"><strong>导出预览</strong><span>等待接入月报聚合与文件下载接口。</span></div>
    </div>
  </section>
</template>
