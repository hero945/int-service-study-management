import {
  CLINICAL_PHASE_CODES,
  normalizePhase,
  type PipelinePhase,
  type PipelineTone,
  type Study,
} from './pipeline-status'
import type { RegulatoryStatus } from '../api/types'

/** 管线总览的一行：一个 project 聚合其下所有 study */
export interface ProjectGroup {
  projectCode: string
  programCode: string
  therapeuticAreaCode: string
  therapeuticAreaName: string
  productName: string
  moa: string
  sourceCode: string
  originCode: string
  indication: string
  studies: Study[]
}

/** 按 projectCode 聚合 study：一个 project 一行，同 project 多 study 合并 */
export function groupByProject(studies: Study[]): ProjectGroup[] {
  const map = new Map<string, Study[]>()
  for (const study of studies) {
    const key = study.projectCode ?? study.code
    const list = map.get(key)
    if (list) list.push(study)
    else map.set(key, [study])
  }
  return [...map.entries()].map(([projectCode, list]) => {
    const first = list[0]
    return {
      projectCode,
      programCode: first.programCode ?? '',
      therapeuticAreaCode: first.therapeuticAreaCode ?? '',
      therapeuticAreaName: first.therapeuticAreaName ?? '',
      productName: first.productName ?? '',
      moa: first.moa ?? '',
      sourceCode: first.sourceCode ?? '',
      originCode: first.originCode ?? '',
      indication: first.indication ?? '',
      studies: list,
    }
  })
}

/** 按治疗领域分组 project 行（保持 project 间相对顺序） */
export interface AreaGroup {
  therapeuticAreaName: string
  projects: ProjectGroup[]
}

export function groupByTherapeuticArea(projects: ProjectGroup[]): AreaGroup[] {
  const map = new Map<string, ProjectGroup[]>()
  for (const project of projects) {
    const key = project.therapeuticAreaName || '其他'
    const list = map.get(key)
    if (list) list.push(project)
    else map.set(key, [project])
  }
  return [...map.entries()].map(([therapeuticAreaName, grouped]) => ({
    therapeuticAreaName,
    projects: grouped,
  }))
}

/** 单元格取数所需的最小 study 字段（前端 Study 与后端 OverviewStudy 均满足） */
export interface CellStudy {
  id: number
  code?: string
  phase: string
  statusLabel: string
  statusTone: string
  updatedAt: string
  mainStageCode: string | null
  mainStageLabel: string | null
  subStatusLabel: string | null
  preindCompleted: boolean
  indCompleted: boolean
  globallyCompleted: boolean
  currentPhaseCompleted: boolean
  ownerName?: string
  plName?: string
  pmName?: string
  productName?: string
  openRiskCount?: number
}

export interface ProjectCell {
  label: string
  tone: PipelineTone
  /** 悬浮框副文案：回填说明或 Study · Product */
  explanation?: string
  clickable: boolean
  studyId?: number
  /** 副文本：主状态（stage 名，灰色，仅在当前阶段列且未完成时显示） */
  subText?: string
  tipStage?: string
  tipStatus?: string
  tipUpdated?: string
  tipOwner?: string
  /** 该单元格对应 Study 的未关闭风险数 */
  openRiskCount?: number
}

/** 该 project 下阶段最靠后的 study（作为"当前阶段"基准）。
 *  同阶段多个 study 时取 updatedAt 最新者，保证当前列展示最新进度。 */
function furthestStudy(studies: CellStudy[]): CellStudy | undefined {
  let best: CellStudy | undefined
  let bestIndex = -1
  let bestUpdated = ''
  for (const s of studies) {
    const phase = normalizePhase(s.phase)
    if (!phase) continue
    const index = CLINICAL_PHASE_CODES.indexOf(phase)
    const updated = s.updatedAt ?? ''
    if (index > bestIndex || (index === bestIndex && updated > bestUpdated)) {
      bestIndex = index
      bestUpdated = updated
      best = s
    }
  }
  return best
}

/** 一组 study 当前推进到的最靠后阶段（用于阶段筛选与单元格基准） */
export function furthestPhaseOf(studies: CellStudy[]): PipelinePhase | undefined {
  const study = furthestStudy(studies)
  return study ? normalizePhase(study.phase) : undefined
}

/**
 * 去掉子节点文案里拼上的主节点英文名（如 "IA 数据冻结" → "数据冻结"）。
 * 管线总览 pill 已与筛选框对齐、保留完整子状态文案；此函数供其它展示场景复用。
 */
export function displaySubNodeLabel(
  subStatusLabel: string,
  mainStageLabel: string | null | undefined,
): string {
  const label = subStatusLabel.trim()
  const prefix = mainStageLabel?.trim()
  if (!prefix || !label) return label
  if (label === prefix) return label
  if (label.startsWith(`${prefix} `)) {
    const rest = label.slice(prefix.length + 1).trim()
    return rest || label
  }
  return label
}

export function formatTipMonth(updatedAt: string | null | undefined): string {
  if (!updatedAt) return '—'
  return updatedAt.slice(0, 7) || '—'
}

export function formatTipOwner(study: CellStudy | undefined): string {
  if (!study) return '—'
  const parts = [study.plName, study.pmName].map((v) => v?.trim()).filter(Boolean)
  if (parts.length) return parts.join(' / ')
  return study.ownerName?.trim() || '—'
}

function tipRefLine(study: CellStudy): string {
  const code = study.code?.trim()
  const product = study.productName?.trim()
  if (code && product) return `${code} · ${product}`
  return code || product || ''
}

function withTip(
  cell: ProjectCell,
  stage: string,
  status: string,
  study: CellStudy | undefined,
  explanation?: string,
): ProjectCell {
  const openRiskCount =
    status === '已完成' ? 0 : (study?.openRiskCount ?? 0)
  return {
    ...cell,
    tipStage: stage,
    tipStatus: status,
    tipUpdated: formatTipMonth(study?.updatedAt),
    tipOwner: formatTipOwner(study),
    explanation: explanation || (study ? tipRefLine(study) : undefined),
    ...(openRiskCount > 0 ? { openRiskCount } : {}),
  }
}

function trimmedOrUndefined(value: string | null | undefined): string | undefined {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

function emptyCell(): ProjectCell {
  return { label: '—', tone: 'empty', clickable: false }
}

/** 是否应在管线总览渲染 pill（有 tone 且有可见文案） */
export function hasChipContent(cell: ProjectCell): boolean {
  return cell.tone !== 'empty' && Boolean(trimmedOrUndefined(cell.label))
}

function ensureRenderableCell(cell: ProjectCell): ProjectCell {
  if (!hasChipContent(cell)) return emptyCell()
  return cell
}

/** 里程碑子状态 → 主节点 → Study 基础状态；空字符串视为缺失。 */
function resolveDisplayLabel(study: CellStudy): string | undefined {
  return trimmedOrUndefined(study.subStatusLabel)
    ?? trimmedOrUndefined(study.mainStageLabel)
    ?? trimmedOrUndefined(study.statusLabel)
}

const REGULATORY_STAGE_BY_PHASE: Partial<Record<PipelinePhase, string>> = {
  PRE_IND: 'PreIND',
  IND: 'IND',
  PRE_3: 'Pre3',
  PRE_NDA: 'PreNDA_BLA',
  NDA: 'NDA_BLA',
}

/** 将 mainStage code/label 归一到可比较的排序索引（与 MilestoneDefinition 一致） */
function milestoneStageRank(codeOrLabel: string | null | undefined): number {
  if (!codeOrLabel) return -1
  const aliases: Record<string, number> = {
    PreIND: 0,
    IND: 1,
    Pre3: 2,
    Protocol: 3,
    SSU: 4,
    Enrollment: 5,
    IA: 6,
    Data_Report: 7,
    'Data & Report': 7,
    PreNDA_BLA: 8,
    'PreNDA/BLA': 8,
    NDA_BLA: 9,
    'NDA/BLA': 9,
  }
  return aliases[codeOrLabel.trim()] ?? -1
}

function completedRegulatoryCell(
  targetPhase: PipelinePhase,
  stageLabel: string,
): ProjectCell {
  return {
    label: '已完成',
    tone: 'green',
    clickable: false,
    subText: targetPhase,
  }
}

const CLINICAL_PHASES: PipelinePhase[] = ['PHASE_1', 'PHASE_2', 'PHASE_3']

/**
 * PreIND / IND / PRE-3 / PRE-NDA / NDA 列：读取 project 维度的监管里程碑状态。
 */
function getRegulatoryMilestoneCell(
  regulatory: RegulatoryStatus | undefined,
  targetPhase: PipelinePhase,
): ProjectCell | null {
  const stageCode = REGULATORY_STAGE_BY_PHASE[targetPhase]
  if (!stageCode || !regulatory) return null

  const targetRank = milestoneStageRank(stageCode)
  const currentRank = milestoneStageRank(regulatory.mainStageCode)

  const completedMap: Partial<Record<PipelinePhase, keyof RegulatoryStatus>> = {
    PRE_IND: 'preindCompleted',
    IND: 'indCompleted',
    PRE_3: 'pre3Completed',
    PRE_NDA: 'prendaCompleted',
    NDA: 'ndaCompleted',
  }
  const completed = completedMap[targetPhase] ? regulatory[completedMap[targetPhase]!] : false

  // 已完成：明确标记 或 当前阶段已越过目标阶段
  if (completed || (targetRank >= 0 && currentRank > targetRank)) {
    return completedRegulatoryCell(targetPhase, stageCode)
  }

  // 尚未到达该阶段
  if (currentRank < targetRank) {
    return { label: '—', tone: 'empty', clickable: false }
  }

  // 正在该阶段：显示子节点
  const subStatus = trimmedOrUndefined(regulatory.subStatusLabel)
  if (subStatus) {
    return {
      label: subStatus,
      tone: 'blue',
      clickable: false,
      subText: trimmedOrUndefined(regulatory.mainStageLabel) ?? stageCode,
    }
  }

  return { label: '—', tone: 'empty', clickable: false }
}

function studyToProjectCell(study: CellStudy, targetPhase: PipelinePhase): ProjectCell {
  if (study.currentPhaseCompleted || study.globallyCompleted) {
    return withTip(
      {
        label: '已完成',
        tone: 'green',
        clickable: true,
        studyId: study.id,
        subText: study.code,
      },
      targetPhase,
      '已完成',
      study,
    )
  }
  const subStatus = trimmedOrUndefined(study.subStatusLabel)
  if (subStatus) {
    const stage = trimmedOrUndefined(study.mainStageLabel) ?? targetPhase
    return withTip(
      {
        label: subStatus,
        tone: 'blue',
        clickable: true,
        studyId: study.id,
        subText: study.code,
      },
      stage,
      subStatus,
      study,
    )
  }
  const fallback = resolveDisplayLabel(study)
  if (!fallback) return emptyCell()
  return ensureRenderableCell(withTip(
    {
      label: fallback,
      tone: 'blue',
      clickable: true,
      studyId: study.id,
      subText: study.code,
    },
    trimmedOrUndefined(study.mainStageLabel) ?? targetPhase,
    fallback,
    study,
  ))
}

/**
 * 返回某一列的全部单元格。
 *
 * 监管列（PRE_IND / IND / PRE_3 / PRE_NDA / NDA）返回 0/1 个 cell。
 * 临床列（PHASE_1 / 2 / 3）返回该阶段全部 Study 对应的 cell，按 study.code 升序排列。
 */
export function getProjectPhaseCells(
  studies: CellStudy[],
  regulatory: RegulatoryStatus | undefined,
  targetPhase: PipelinePhase,
): ProjectCell[] {
  const regulatoryCell = getRegulatoryMilestoneCell(regulatory, targetPhase)
  if (regulatoryCell) {
    return [ensureRenderableCell(regulatoryCell)]
  }

  if (!CLINICAL_PHASES.includes(targetPhase)) {
    return []
  }

  return studies
    .filter((s) => normalizePhase(s.phase) === targetPhase)
    .sort((a, b) => (a.code ?? '').localeCompare(b.code ?? ''))
    .map((study) => studyToProjectCell(study, targetPhase))
}

/**
 * 单元格取数与状态（兼容旧调用：返回该列第一个 cell）。
 *
 * PRE_IND / IND / PRE_3（监管列）：逻辑不变，见 getRegulatoryMilestoneCell。
 * 普通列（PHASE_1 / 2 / 3）：
 *   只认本列对应 Phase 的 Study + 真实里程碑；
 *   不因「后面还有更大阶段」而把本列回填成「已完成」。
 *   - 无该 Phase 的 Study → "—"（灰）
 *   - 有 Study 且阶段/全局已完成 → "已完成"（绿）
 *   - 有 Study 进行中 → pill = 里程碑子状态全文（蓝）；上方灰色 = Study 编号
 */
export function getProjectCell(
  studies: CellStudy[],
  regulatory: RegulatoryStatus | undefined,
  targetPhase: PipelinePhase,
): ProjectCell {
  return getProjectPhaseCells(studies, regulatory, targetPhase)[0] ?? emptyCell()
}
