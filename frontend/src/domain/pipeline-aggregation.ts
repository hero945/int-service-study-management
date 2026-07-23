import {
  PHASE_TAGS,
  normalizePhase,
  toneForStatus,
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
}

export interface ProjectCell {
  label: string
  tone: PipelineTone
  explanation?: string
  clickable: boolean
  studyId?: number
  /** 副文本：主状态（stage 名，灰色，仅在当前阶段列且未完成时显示） */
  subText?: string
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
 * 单元格取数与状态（对齐参考样式：早于当前阶段=已完成 / 等于=当前进度 / 晚于=—）。
 *
 * 以 project 推进到的最靠后阶段（furthestPhaseOf）作为"当前阶段"基准：
 *   - 目标列 < 当前阶段 → "已完成"（绿）：项目已越过该阶段
 *   - 目标列 = 当前阶段 → 该研究的里程碑【子状态】作为主文案，【主状态】作为灰色副文本
 *   - 目标列 > 当前阶段 → "—"（灰）：尚未到达
 *
 * 关键：主文案永远是里程碑子状态（节点名，如 "LPI 入组"），不出现"进行中"；
 *       灰色副文本才是主状态（stage 名，如 "Enrollment"）；完成态（currentPhaseCompleted）
 *       才显示"已完成"（绿）且不带副文本。
 */
export function getProjectCell(studies: CellStudy[], targetPhase: PipelinePhase): ProjectCell {
  const currentPhase = furthestPhaseOf(studies)
  if (!currentPhase) {
    return { label: '—', tone: 'empty', clickable: false }
  }
  const targetIndex = PHASE_TAGS.indexOf(targetPhase)
  const currentIndex = PHASE_TAGS.indexOf(currentPhase)

  // 早于当前阶段 → 已完成
  if (targetIndex < currentIndex) {
    return { label: '已完成', tone: 'green', clickable: true }
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
  // 当前阶段已完成（里程碑末节点 actual_end 非空）→ 绿底"已完成"，不带副文本
  if (study.currentPhaseCompleted || study.globallyCompleted) {
    return { label: '已完成', tone: 'green', clickable: true, studyId: study.id }
  }
  // 否则主文案=里程碑子状态（节点名），副文本=主状态（stage 名，灰色）
  const subStatus = study.subStatusLabel
  if (subStatus) {
    return {
      label: subStatus,
      tone: 'blue',
      clickable: true,
      studyId: study.id,
      subText: study.mainStageLabel ?? undefined,
    }
  }
  // 无里程碑子状态（数据缺口）时回退到主状态/stage 名，避免出现"进行中"
  const fallback = study.mainStageLabel ?? study.statusLabel
  return {
    label: fallback,
    tone: 'blue',
    clickable: true,
    studyId: study.id,
  }
}
