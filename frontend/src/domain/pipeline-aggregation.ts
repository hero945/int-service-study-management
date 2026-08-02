import {
  CLINICAL_PHASE_CODES,
  normalizePhase,
  type PipelinePhase,
  type PipelineTone,
  type Study,
} from './pipeline-status'

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

/**
 * 监管列 → 里程碑主阶段 + 取数 Study 的临床 phase code。
 * PRE_IND / IND ← PHASE_1 study 的 PreIND / IND 里程碑；
 * PRE_3 ← PHASE_3_1 study 的 Pre3 里程碑。
 */
const REGULATORY_COLUMN_SOURCE: Partial<
  Record<PipelinePhase, { stageCode: string; sourcePhase: PipelinePhase }>
> = {
  PRE_IND: { stageCode: 'PreIND', sourcePhase: 'PHASE_1' },
  IND: { stageCode: 'IND', sourcePhase: 'PHASE_1' },
  PRE_3: { stageCode: 'Pre3', sourcePhase: 'PHASE_3_1' },
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

function findStudyByPhase(
  studies: CellStudy[],
  phase: PipelinePhase,
): CellStudy | undefined {
  let best: CellStudy | undefined
  let bestUpdated = ''
  for (const s of studies) {
    if (normalizePhase(s.phase) !== phase) continue
    const updated = s.updatedAt ?? ''
    if (!best || updated > bestUpdated) {
      best = s
      bestUpdated = updated
    }
  }
  return best
}

function completedRegulatoryCell(
  study: CellStudy,
  targetPhase: PipelinePhase,
  stageLabel: string,
): ProjectCell {
  return withTip(
    {
      label: '已完成',
      tone: 'green',
      clickable: true,
      studyId: study.id,
      subText: targetPhase,
    },
    stageLabel,
    '已完成',
    study,
  )
}

/**
 * PreIND / IND / PRE-3 列：按约定 Phase 的 study 对应里程碑展示并链接该 study。
 * 无对应 study 时返回 null，由调用方回退到阶段相对规则。
 */
function getRegulatoryMilestoneCell(
  studies: CellStudy[],
  targetPhase: PipelinePhase,
): ProjectCell | null {
  const cfg = REGULATORY_COLUMN_SOURCE[targetPhase]
  if (!cfg) return null
  const source = findStudyByPhase(studies, cfg.sourcePhase)
  if (!source) return null
  const { stageCode } = cfg

  const targetRank = milestoneStageRank(stageCode)
  const currentRank = milestoneStageRank(source.mainStageCode ?? source.mainStageLabel)

  // 无里程碑 frontier：仅信 PreIND/IND 完成标记，否则空
  if (currentRank < 0) {
    if (stageCode === 'PreIND' && source.preindCompleted) {
      return completedRegulatoryCell(source, targetPhase, stageCode)
    }
    if (stageCode === 'IND' && source.indCompleted) {
      return completedRegulatoryCell(source, targetPhase, stageCode)
    }
    return { label: '—', tone: 'empty', clickable: false }
  }

  // 已越过该监管阶段 → 已完成
  if (currentRank > targetRank) {
    return completedRegulatoryCell(source, targetPhase, stageCode)
  }

  // 尚未到达该阶段
  if (currentRank < targetRank) {
    return { label: '—', tone: 'empty', clickable: false }
  }

  // 正在该阶段：完成标记 / 阶段末节点完成 → 已完成
  const stageDone =
    (stageCode === 'PreIND' && source.preindCompleted) ||
    (stageCode === 'IND' && source.indCompleted) ||
    source.currentPhaseCompleted ||
    source.globallyCompleted
  if (stageDone) {
    return completedRegulatoryCell(source, targetPhase, stageCode)
  }
  const subStatus = trimmedOrUndefined(source.subStatusLabel)
  if (subStatus) {
    const stage = trimmedOrUndefined(source.mainStageLabel) ?? stageCode
    return withTip(
      {
        label: subStatus,
        tone: 'blue',
        clickable: true,
        studyId: source.id,
        subText: trimmedOrUndefined(source.mainStageLabel),
      },
      stage,
      subStatus,
      source,
    )
  }
  const fallback = trimmedOrUndefined(source.mainStageLabel)
    ?? trimmedOrUndefined(source.statusLabel)
  if (!fallback) return emptyCell()
  return withTip(
    {
      label: fallback,
      tone: 'blue',
      clickable: true,
      studyId: source.id,
    },
    trimmedOrUndefined(source.mainStageLabel) ?? stageCode,
    fallback,
    source,
  )
}

/**
 * 单元格取数与状态。
 *
 * PRE_IND / IND / PRE_3（监管列）：逻辑不变，见 getRegulatoryMilestoneCell。
 * 普通列（PHASE_1 / 2 / 3_1 / 3_2，以及监管列无约定 Study 时的回退）：
 *   只认本列对应 Phase 的 Study + 真实里程碑；
 *   不因「后面还有更大阶段」而把本列回填成「已完成」。
 *   - 无该 Phase 的 Study → "—"（灰）
 *   - 有 Study 且阶段/全局已完成 → "已完成"（绿）
 *   - 有 Study 进行中 → pill = 里程碑子状态全文（蓝）；上方灰色 = 主节点名
 */
export function getProjectCell(studies: CellStudy[], targetPhase: PipelinePhase): ProjectCell {
  const regulatory = getRegulatoryMilestoneCell(studies, targetPhase)
  if (regulatory) return ensureRenderableCell(regulatory)

  const study = findStudyByPhase(studies, targetPhase)
  if (!study) {
    return { label: '—', tone: 'empty', clickable: false }
  }
  if (study.currentPhaseCompleted || study.globallyCompleted) {
    return withTip(
      {
        label: '已完成',
        tone: 'green',
        clickable: true,
        studyId: study.id,
        subText: targetPhase,
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
        subText: trimmedOrUndefined(study.mainStageLabel),
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
    },
    trimmedOrUndefined(study.mainStageLabel) ?? targetPhase,
    fallback,
    study,
  ))
}
