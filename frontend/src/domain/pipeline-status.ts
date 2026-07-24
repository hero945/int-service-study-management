import type { Study } from '../api/types'

export const PHASE_TAGS = [
  'PreIND',
  'IND',
  'Phase 1',
  'Phase 2',
  'PRE-3',
  'Phase 3-1',
  'Phase 3-2',
] as const

export type PipelinePhase = (typeof PHASE_TAGS)[number]
export type PipelineTone = 'blue' | 'green' | 'orange' | 'red' | 'empty'

/**
 * phase code（后端/DB 契约，见 PipelineConfigManager.PHASES）→ 总览列 tag。
 * 这是阶段编码的单一真相源：后端契约固定返回 code，展示标签在此映射。
 */
export const PHASE_CODE_TO_TAG: Record<string, PipelinePhase> = {
  PRE_IND: 'PreIND',
  IND: 'IND',
  PHASE_1: 'Phase 1',
  PHASE_2: 'Phase 2',
  PRE_3: 'PRE-3',
  PHASE_3_1: 'Phase 3-1',
  PHASE_3_2: 'Phase 3-2',
}

/** 总览列 tag → phase code（由 PHASE_CODE_TO_TAG 派生） */
export const PHASE_TAG_TO_CODE = Object.fromEntries(
  Object.entries(PHASE_CODE_TO_TAG).map(([code, tag]) => [tag, code]),
) as Record<PipelinePhase, string>

/** 将后端返回的 phase code 归一为总览列 tag；未知 code 返回 undefined */
export function normalizePhase(phase: string): PipelinePhase | undefined {
  return PHASE_CODE_TO_TAG[phase]
}

/**
 * 临床阶段展示文案（与管线总览 PHASE_TAGS 同源：Arabic 数字 Phase 1 / Phase 2…）。
 * 筛选下拉、配置页、总览列共用此映射，避免 Phase I / Phase 1 两套文案。
 */
export function phaseDisplayLabel(code: string): string {
  return PHASE_CODE_TO_TAG[code] ?? code
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
