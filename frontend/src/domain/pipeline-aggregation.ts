import {
  PHASE_TAGS,
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
    const index = PHASE_TAGS.indexOf(phase)
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
  return {
    ...cell,
    tipStage: stage,
    tipStatus: status,
    tipUpdated: formatTipMonth(study?.updatedAt),
    tipOwner: formatTipOwner(study),
    explanation: explanation || (study ? tipRefLine(study) : undefined),
  }
}

/**
 * 监管列 → 里程碑主阶段 + 取数 Study 的 Phase。
 * PreIND / IND ← Phase 1 study 的 PreIND / IND 里程碑；
 * PRE-3 ← Phase 3-1 study 的 Pre3 里程碑。
 */
const REGULATORY_COLUMN_SOURCE: Partial<
  Record<PipelinePhase, { stageCode: string; sourcePhase: PipelinePhase }>
> = {
  PreIND: { stageCode: 'PreIND', sourcePhase: 'Phase 1' },
  IND: { stageCode: 'IND', sourcePhase: 'Phase 1' },
  'PRE-3': { stageCode: 'Pre3', sourcePhase: 'Phase 3-1' },
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
  const subStatus = source.subStatusLabel?.trim()
  if (subStatus) {
    const stage = source.mainStageLabel ?? stageCode
    return withTip(
      {
        label: subStatus,
        tone: 'blue',
        clickable: true,
        studyId: source.id,
        subText: source.mainStageLabel ?? undefined,
      },
      stage,
      subStatus,
      source,
    )
  }
  const fallback = source.mainStageLabel ?? source.statusLabel
  return withTip(
    {
      label: fallback,
      tone: 'blue',
      clickable: true,
      studyId: source.id,
    },
    source.mainStageLabel ?? stageCode,
    fallback,
    source,
  )
}

/**
 * 单元格取数与状态（对齐参考样式：早于当前阶段=已完成 / 等于=当前进度 / 晚于=—）。
 *
 * PreIND / IND：优先按 Phase 1 study 的 PreIND / IND 里程碑展示并链接该 study。
 * PRE-3：优先按 Phase 3-1 study 的 Pre3 里程碑展示并链接该 study。
 * 其余列以 project 最靠后阶段（furthestPhaseOf）为基准：
 *   - 目标列 < 当前阶段 → "已完成"（绿），上方灰色 caption = 列阶段名 targetPhase
 *   - 目标列 = 当前阶段 → pill = 里程碑子状态全文；上方灰色 = 主节点名
 *   - 目标列 > 当前阶段 → "—"（灰）
 */
export function getProjectCell(studies: CellStudy[], targetPhase: PipelinePhase): ProjectCell {
  const regulatory = getRegulatoryMilestoneCell(studies, targetPhase)
  if (regulatory) return regulatory

  const currentPhase = furthestPhaseOf(studies)
  if (!currentPhase) {
    return { label: '—', tone: 'empty', clickable: false }
  }
  const targetIndex = PHASE_TAGS.indexOf(targetPhase)
  const currentIndex = PHASE_TAGS.indexOf(currentPhase)
  const fillSource = furthestStudy(studies)

  // 早于当前阶段 → 已完成，上方显示列阶段名
  if (targetIndex < currentIndex) {
    const own = studies.find((s) => normalizePhase(s.phase) === targetPhase)
    const tipStudy = own ?? fillSource
    const backfill = !own && fillSource
      ? `${targetPhase} 实际无项目，由 ${currentPhase} 回填`
      : undefined
    return withTip(
      {
        label: '已完成',
        tone: 'green',
        clickable: true,
        studyId: tipStudy?.id,
        subText: targetPhase,
      },
      targetPhase,
      '已完成',
      tipStudy,
      backfill,
    )
  }
  // 晚于当前阶段 → 尚未到达
  if (targetIndex > currentIndex) {
    return { label: '—', tone: 'empty', clickable: false }
  }
  // 等于当前阶段 → 当前研究的里程碑进度
  const study = furthestStudy(studies)
  if (!study) {
    return { label: '—', tone: 'empty', clickable: false }
  }
  // 当前阶段已完成 → 绿底「已完成」，上方显示列阶段名
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
  // 进行中：pill = 完整子状态（与筛选框一致）；上方灰色 = 主节点名
  const subStatus = study.subStatusLabel?.trim()
  if (subStatus) {
    const stage = study.mainStageLabel ?? targetPhase
    return withTip(
      {
        label: subStatus,
        tone: 'blue',
        clickable: true,
        studyId: study.id,
        subText: study.mainStageLabel ?? undefined,
      },
      stage,
      subStatus,
      study,
    )
  }
  // 无子节点时回退主节点/statusLabel，且不重复设 caption
  const fallback = study.mainStageLabel ?? study.statusLabel
  return withTip(
    {
      label: fallback,
      tone: 'blue',
      clickable: true,
      studyId: study.id,
    },
    study.mainStageLabel ?? targetPhase,
    fallback,
    study,
  )
}
