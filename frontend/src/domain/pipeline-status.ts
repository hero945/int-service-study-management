import type { Study } from '../api/types'

/**
 * 临床 phase 唯一契约 = DB/API `phase_status_code`
 *（与后端 PipelineConfigManager.PHASES 顺序一致）。
 * 创建下拉、总览列头/筛选/聚合列 key、导出与抽屉展示均直接使用 code。
 */
export const CLINICAL_PHASE_CODES = [
  'PRE_IND',
  'IND',
  'PHASE_1',
  'PHASE_2',
  'PRE_3',
  'PHASE_3',
  'PRE_NDA',
  'NDA',
] as const

export type PipelinePhase = (typeof CLINICAL_PHASE_CODES)[number]
export type PipelineTone = 'blue' | 'green' | 'orange' | 'red' | 'empty'

/** 临床 phase DB code → 人类可读标签 */
export const PHASE_LABELS: Record<PipelinePhase, string> = {
  PRE_IND: 'Pre-IND',
  IND: 'IND',
  PHASE_1: 'Ph1',
  PHASE_2: 'Ph2',
  PRE_3: 'Pre-III',
  PHASE_3: 'Ph3',
  PRE_NDA: 'PreNDA/BLA',
  NDA: 'NDA/BLA',
}

/** 将 phase code 转为展示标签；未知或空值返回原值 */
export function phaseLabel(code: string | undefined): string {
  if (!code) return code ?? ''
  return PHASE_LABELS[code as PipelinePhase] ?? code
}

const PHASE_CODE_SET: ReadonlySet<string> = new Set(CLINICAL_PHASE_CODES)

/** 将后端返回的 phase code 归一为总览列 key；未知 code 返回 undefined */
export function normalizePhase(phase: string): PipelinePhase | undefined {
  return PHASE_CODE_SET.has(phase) ? (phase as PipelinePhase) : undefined
}

const STATUS_TONE: Record<string, PipelineTone> = {
  positive: 'blue',
  info: 'green',
  neutral: 'blue',
  warning: 'orange',
  danger: 'red',
}

/** 将后端 StudyStatus.tone 映射为总览单元格色调 */
export function toneForStatus(statusTone: string): PipelineTone {
  return STATUS_TONE[statusTone] ?? 'blue'
}

/** 来源 code → 中文标签（与 PipelineConfigManager.SOURCES、配置页字典一致） */
export const SOURCE_LABELS: Record<string, string> = {
  SELF_DEVELOPED: '自研',
  IN_LICENSE: '引进',
  COOPERATION: '合作',
}

/** 产地 code → 中文标签（与 PipelineConfigManager.ORIGINS、配置页字典一致） */
export const ORIGIN_LABELS: Record<string, string> = {
  DOMESTIC: '国产',
  IMPORTED: '进口',
}

export function sourceLabel(sourceCode: string | undefined): string {
  return sourceCode ? (SOURCE_LABELS[sourceCode] ?? sourceCode) : ''
}

export function originLabel(originCode: string | undefined): string {
  return originCode ? (ORIGIN_LABELS[originCode] ?? originCode) : ''
}

// Study 类型在此重新导出，便于聚合模块与视图统一引用
export type { Study }
