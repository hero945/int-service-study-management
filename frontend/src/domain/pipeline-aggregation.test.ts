import type { RegulatoryStatus } from '../api/types'
import { describe, expect, it } from 'vitest'
import type { Study } from '../api/types'
import type { CellStudy } from './pipeline-aggregation'
import { furthestPhaseOf, getProjectCell, getProjectPhaseCells, groupByProject, displaySubNodeLabel, hasChipContent } from './pipeline-aggregation'

let seq = 0
const study = (init: Partial<Study> & { phase: string }): Study => ({
  id: ++seq,
  code: `S${seq}`,
  indication: '适应症',
  status: 'ACTIVE',
  statusLabel: '进行中',
  statusTone: 'positive',
  ownerName: '张伟',
  startDate: null,
  updatedAt: '2026-07-17T00:00:00',
  ...init,
})

/** 创建带里程碑状态的 study（用于测试新逻辑） */
const ms = (init: Partial<CellStudy> & { phase: string }): CellStudy => ({
  id: init.id ?? ++seq,
  phase: init.phase,
  statusLabel: init.statusLabel || '进行中',
  statusTone: init.statusTone || 'positive',
  updatedAt: init.updatedAt || '2026-07-17T00:00:00',
  mainStageCode: init.mainStageCode || null,
  mainStageLabel: init.mainStageLabel || null,
  subStatusLabel: init.subStatusLabel || null,
  preindCompleted: init.preindCompleted ?? false,
  indCompleted: init.indCompleted ?? false,
  globallyCompleted: init.globallyCompleted ?? false,
  currentPhaseCompleted: init.currentPhaseCompleted ?? false,
  code: init.code,
  ownerName: init.ownerName,
  plName: init.plName,
  pmName: init.pmName,
  productName: init.productName,
})

const regulatory = (init: Partial<RegulatoryStatus>): RegulatoryStatus => ({
  mainStageCode: init.mainStageCode ?? null,
  mainStageLabel: init.mainStageLabel ?? null,
  subStatusLabel: init.subStatusLabel ?? null,
  preindCompleted: init.preindCompleted ?? false,
  indCompleted: init.indCompleted ?? false,
  pre3Completed: init.pre3Completed ?? false,
  prendaCompleted: init.prendaCompleted ?? false,
  ndaCompleted: init.ndaCompleted ?? false,
})

describe('groupByProject', () => {
  it('keeps one row per project and merges studies under it', () => {
    const groups = groupByProject([
      study({ code: 'A-001', projectCode: 'P1', phase: 'PHASE_1' }),
      study({ code: 'A-002', projectCode: 'P1', phase: 'PHASE_2' }),
      study({ code: 'B-001', projectCode: 'P2', phase: 'PHASE_1' }),
    ])
    expect(groups).toHaveLength(2)
    expect(groups.find((g) => g.projectCode === 'P1')?.studies).toHaveLength(2)
  })
})

describe('getProjectCell', () => {
  it('does not backfill earlier columns from a later phase study', () => {
    const studies = [ms({
      phase: 'PHASE_3',
      code: 'HDM2001-301',
      plName: '张伟',
      pmName: '李静',
      productName: 'HDM2001',
      updatedAt: '2026-06-15T00:00:00',
    })]
    // 无 Phase 1 → 监管列空；无本列 Study → 普通列也空
    expect(getProjectCell(studies, undefined, 'PRE_IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, undefined, 'PHASE_1')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, undefined, 'PHASE_2')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('ordinary columns without an own-phase study stay empty even if a later phase exists', () => {
    const studies = [ms({ phase: 'PHASE_2', statusLabel: '进行中', statusTone: 'positive' })]
    expect(getProjectCell(studies, undefined, 'PRE_IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, undefined, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, undefined, 'PHASE_1')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('marks phases without a matching study as — (empty/gray)', () => {
    const studies = [ms({ phase: 'PHASE_2', statusLabel: '进行中' })]
    expect(getProjectCell(studies, undefined, 'PRE_3')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, undefined, 'PHASE_3')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, undefined, 'PHASE_3')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('own-phase column shows full sub-status matching filter options', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      code: 'PROTO-001',
      mainStageLabel: 'Protocol',
      subStatusLabel: '方案摘要定稿',
    })]
    const cell = getProjectCell(studies, undefined, 'PHASE_2')
    expect(cell).toMatchObject({ label: '方案摘要定稿', tone: 'blue', subText: 'PROTO-001' })
  })

  it('keeps full milestone sub-status text including stage prefix', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      code: 'PRE3-001',
      mainStageLabel: 'Pre3',
      subStatusLabel: 'Pre3 反馈-数统',
    })]
    expect(getProjectCell(studies, undefined, 'PHASE_2')).toMatchObject({
      label: 'Pre3 反馈-数统',
      subText: 'PRE3-001',
      tone: 'blue',
    })
  })

  it('displaySubNodeLabel helper can still strip prefixes when needed elsewhere', () => {
    expect(displaySubNodeLabel('IA 数据冻结', 'IA')).toBe('数据冻结')
    expect(displaySubNodeLabel('NDA/BLA 递交', 'NDA/BLA')).toBe('递交')
    expect(displaySubNodeLabel('FPI', 'Enrollment')).toBe('FPI')
    expect(displaySubNodeLabel('方案摘要定稿', 'Protocol')).toBe('方案摘要定稿')
  })

  it('shows 已完成 on own-phase column when that phase milestone is completed', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      code: 'COMP-001',
      mainStageLabel: 'Enrollment',
      subStatusLabel: 'LPO',
      currentPhaseCompleted: true,
    })]
    expect(getProjectCell(studies, undefined, 'PHASE_2')).toMatchObject({
      label: '已完成', tone: 'green', subText: 'COMP-001',
    })
  })

  it('PreIND-only project: PreIND shows full sub-status, later phases are —', () => {
    const studies = [ms({
      phase: 'PRE_IND',
      mainStageLabel: 'PreIND',
      subStatusLabel: 'PreIND 反馈-药学',
    })]
    const reg = regulatory({
      mainStageCode: 'PreIND',
      mainStageLabel: 'PreIND',
      subStatusLabel: 'PreIND 反馈-药学',
    })
    expect(getProjectCell(studies, reg, 'PRE_IND')).toMatchObject({
      label: 'PreIND 反馈-药学', subText: 'PreIND', tone: 'blue',
    })
    expect(getProjectCell(studies, reg, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, reg, 'PHASE_1')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('each ordinary column follows its own study milestone; multiple studies at the same phase are shown together', () => {
    const phase1 = ms({
      id: 11,
      code: 'PH1-001',
      phase: 'PHASE_1',
      mainStageCode: 'Enrollment',
      mainStageLabel: 'Enrollment',
      subStatusLabel: 'LPI',
    })
    const phase31 = ms({
      id: 31,
      code: 'PH3A-001',
      phase: 'PHASE_3',
      mainStageCode: 'Data_Report',
      mainStageLabel: 'Data & Report',
      subStatusLabel: 'DBL',
    })
    const phase32 = ms({
      id: 32,
      code: 'PH3B-001',
      phase: 'PHASE_3',
      mainStageLabel: 'NDA/BLA',
      subStatusLabel: 'NDA/BLA 递交',
    })
    const studies = [phase1, phase31, phase32]
    const reg = regulatory({
      mainStageCode: 'NDA_BLA',
      mainStageLabel: 'NDA/BLA',
      subStatusLabel: 'NDA/BLA 获批',
      preindCompleted: true,
      indCompleted: true,
      pre3Completed: true,
      prendaCompleted: true,
      ndaCompleted: true,
    })
    // 监管列：直接读取 project 维度状态
    expect(getProjectCell(studies, reg, 'PRE_IND')).toMatchObject({
      label: '已完成', tone: 'green', subText: 'PRE_IND', clickable: false,
    })
    expect(getProjectCell(studies, reg, 'IND')).toMatchObject({
      label: '已完成', tone: 'green', clickable: false,
    })
    expect(getProjectCell(studies, reg, 'PRE_3')).toMatchObject({
      label: '已完成', tone: 'green', clickable: false,
    })
    expect(getProjectCell(studies, reg, 'PRE_NDA')).toMatchObject({
      label: '已完成', tone: 'green', clickable: false,
    })
    expect(getProjectCell(studies, reg, 'NDA')).toMatchObject({
      label: '已完成', tone: 'green', clickable: false,
    })
    // 普通列：各自阶段的 Study 独立显示；Ph3 列同时列出两条 Study
    expect(getProjectCell(studies, reg, 'PHASE_1')).toMatchObject({
      label: 'LPI', tone: 'blue', subText: 'PH1-001', studyId: 11,
    })
    expect(getProjectCell(studies, reg, 'PHASE_2')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, reg, 'PHASE_3')).toMatchObject({
      label: 'DBL', tone: 'blue', subText: 'PH3A-001', studyId: 31,
    })
    const phase3Cells = getProjectPhaseCells(studies, reg, 'PHASE_3')
    expect(phase3Cells).toHaveLength(2)
    expect(phase3Cells[0]).toMatchObject({ label: 'DBL', subText: 'PH3A-001', studyId: 31, tone: 'blue' })
    expect(phase3Cells[1]).toMatchObject({ label: 'NDA/BLA 递交', subText: 'PH3B-001', studyId: 32, tone: 'blue' })
  })

  it('returns all studies at the phase sorted by code', () => {
    const studies = [
      ms({ phase: 'PHASE_1', code: 'B-001', statusLabel: '旧', updatedAt: '2026-01-01T00:00:00' }),
      ms({ phase: 'PHASE_1', code: 'A-001', statusLabel: '新', updatedAt: '2026-07-01T00:00:00' }),
    ]
    const result = getProjectPhaseCells(studies, undefined, 'PHASE_1')
    expect(result).toHaveLength(2)
    expect(result[0].subText).toBe('A-001')
    expect(result[1].subText).toBe('B-001')
    expect(result[0].label).toBe('新')
    expect(result[1].label).toBe('旧')
  })

  it('keeps later clinical columns empty when studies are only in PHASE_1', () => {
    const studies = [
      ms({ phase: 'PHASE_1', code: 'A-001', currentPhaseCompleted: true }),
      ms({ phase: 'PHASE_1', code: 'A-002', mainStageLabel: 'Enrollment', subStatusLabel: 'FPI' }),
    ]
    expect(getProjectPhaseCells(studies, undefined, 'PHASE_1')).toHaveLength(2)
    expect(getProjectPhaseCells(studies, undefined, 'PHASE_2')).toHaveLength(0)
    expect(getProjectPhaseCells(studies, undefined, 'PHASE_3')).toHaveLength(0)
    expect(getProjectPhaseCells(studies, undefined, 'PHASE_3')).toHaveLength(0)
    expect(getProjectCell(studies, undefined, 'PRE_IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, undefined, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('falls back to statusLabel when milestone fields are empty strings', () => {
    const studies = [ms({
      phase: 'PHASE_1',
      mainStageLabel: '',
      subStatusLabel: '',
      statusLabel: '计划中',
    })]
    expect(getProjectCell(studies, undefined, 'PHASE_1')).toMatchObject({
      label: '计划中',
      tone: 'blue',
    })
  })

  it('shows — instead of an empty chip when no milestone or status text exists', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      mainStageLabel: '',
      subStatusLabel: '',
      statusLabel: '   ',
    })]
    expect(getProjectCell(studies, undefined, 'PHASE_2')).toMatchObject({
      label: '—',
      tone: 'empty',
      clickable: false,
    })
  })

  it('hasChipContent is false for blue tone with blank label', () => {
    expect(hasChipContent({ label: '  ', tone: 'blue', clickable: true })).toBe(false)
    expect(hasChipContent({ label: '进行中', tone: 'blue', clickable: true })).toBe(true)
  })

  it('regulatory columns read from project-level status and do not link to a study', () => {
    const reg = regulatory({
      mainStageCode: 'PreIND',
      mainStageLabel: 'PreIND',
      subStatusLabel: 'PreIND 反馈-药学',
    })
    const studies = [
      ms({
        id: 42,
        phase: 'PHASE_1',
        code: 'HDM-P1',
        mainStageCode: 'PreIND',
        mainStageLabel: 'PreIND',
        subStatusLabel: 'PreIND 反馈-药学',
        productName: 'HDM',
      }),
      ms({
        phase: 'PHASE_2',
        mainStageCode: 'Enrollment',
        mainStageLabel: 'Enrollment',
        subStatusLabel: 'FPI',
      }),
    ]
    expect(getProjectCell(studies, reg, 'PRE_IND')).toMatchObject({
      label: 'PreIND 反馈-药学',
      subText: 'PreIND',
      tone: 'blue',
      clickable: false,
    })
    expect(getProjectCell(studies, reg, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, reg, 'PRE_3')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('PRE-3 reads project Pre3 status; earlier regulatory columns stay completed', () => {
    const reg = regulatory({
      mainStageCode: 'Pre3',
      mainStageLabel: 'Pre3',
      subStatusLabel: 'Pre3 反馈-数统',
      preindCompleted: true,
      indCompleted: true,
    })
    const studies = [
      ms({
        id: 11,
        phase: 'PHASE_1',
        mainStageCode: 'Enrollment',
        mainStageLabel: 'Enrollment',
        subStatusLabel: 'LPI',
      }),
      ms({
        id: 77,
        phase: 'PHASE_3',
        mainStageCode: 'Pre3',
        mainStageLabel: 'Pre3',
        subStatusLabel: 'Pre3 反馈-数统',
      }),
    ]
    expect(getProjectCell(studies, reg, 'PRE_3')).toMatchObject({
      label: 'Pre3 反馈-数统',
      subText: 'Pre3',
      tone: 'blue',
      clickable: false,
    })
    expect(getProjectCell(studies, reg, 'PRE_IND')).toMatchObject({ label: '已完成', tone: 'green' })
    expect(getProjectCell(studies, reg, 'IND')).toMatchObject({ label: '已完成', tone: 'green' })
  })

  it('completed regulatory flags show 已完成 regardless of current study phase', () => {
    const reg = regulatory({
      mainStageCode: 'PreIND',
      mainStageLabel: 'PreIND',
      subStatusLabel: 'PreIND 反馈-药学',
      preindCompleted: true,
      indCompleted: true,
    })
    const phase1 = ms({
      id: 7,
      phase: 'PHASE_1',
      mainStageCode: 'Enrollment',
      mainStageLabel: 'Enrollment',
      subStatusLabel: 'LPI',
    })
    expect(getProjectCell([phase1], reg, 'PRE_IND')).toMatchObject({
      label: '已完成', tone: 'green', clickable: false,
    })
    expect(getProjectCell([phase1], reg, 'IND')).toMatchObject({
      label: '已完成', tone: 'green', clickable: false,
    })
    // PRE-3 尚未到达且未完成 → 空
    expect(getProjectCell([phase1], reg, 'PRE_3')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('regulatory column marks completed when the project has passed that stage', () => {
    const reg = regulatory({
      mainStageCode: 'Protocol',
      mainStageLabel: 'Protocol',
      subStatusLabel: '方案定稿',
      pre3Completed: true,
    })
    const phase31 = ms({
      id: 9,
      phase: 'PHASE_3',
      mainStageCode: 'Protocol',
      mainStageLabel: 'Protocol',
      subStatusLabel: '方案定稿',
    })
    expect(getProjectCell([phase31], reg, 'PRE_3')).toMatchObject({
      label: '已完成', tone: 'green', clickable: false,
    })
  })
})

describe('furthestPhaseOf', () => {
  it('returns the most advanced phase of the project', () => {
    const studies: CellStudy[] = [
      ms({ phase: 'PHASE_1' }),
      ms({ phase: 'PHASE_3' }),
    ]
    expect(furthestPhaseOf(studies)).toBe('PHASE_3')
  })
})
