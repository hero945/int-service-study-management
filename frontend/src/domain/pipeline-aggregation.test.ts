import { describe, expect, it } from 'vitest'
import type { Study } from '../api/types'
import type { CellStudy } from './pipeline-aggregation'
import { furthestPhaseOf, getProjectCell, groupByProject, displaySubNodeLabel } from './pipeline-aggregation'

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
      phase: 'PHASE_3_1',
      code: 'HDM2001-301',
      plName: '张伟',
      pmName: '李静',
      productName: 'HDM2001',
      updatedAt: '2026-06-15T00:00:00',
    })]
    // 无 Phase 1 → 监管列空；无本列 Study → 普通列也空
    expect(getProjectCell(studies, 'PRE_IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'PHASE_1')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'PHASE_2')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('ordinary columns without an own-phase study stay empty even if a later phase exists', () => {
    const studies = [ms({ phase: 'PHASE_2', statusLabel: '进行中', statusTone: 'positive' })]
    expect(getProjectCell(studies, 'PRE_IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'PHASE_1')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('marks phases without a matching study as — (empty/gray)', () => {
    const studies = [ms({ phase: 'PHASE_2', statusLabel: '进行中' })]
    expect(getProjectCell(studies, 'PRE_3')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'PHASE_3_1')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'PHASE_3_2')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('own-phase column shows full sub-status matching filter options', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      mainStageLabel: 'Protocol',
      subStatusLabel: '方案摘要定稿',
    })]
    const cell = getProjectCell(studies, 'PHASE_2')
    expect(cell).toMatchObject({ label: '方案摘要定稿', tone: 'blue', subText: 'Protocol' })
  })

  it('keeps full milestone sub-status text including stage prefix', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      mainStageLabel: 'Pre3',
      subStatusLabel: 'Pre3 反馈-数统',
    })]
    expect(getProjectCell(studies, 'PHASE_2')).toMatchObject({
      label: 'Pre3 反馈-数统',
      subText: 'Pre3',
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
      mainStageLabel: 'Enrollment',
      subStatusLabel: 'LPO',
      currentPhaseCompleted: true,
    })]
    expect(getProjectCell(studies, 'PHASE_2')).toMatchObject({
      label: '已完成', tone: 'green', subText: 'PHASE_2',
    })
  })

  it('PreIND-only project: PreIND shows full sub-status, later phases are —', () => {
    const studies = [ms({
      phase: 'PRE_IND',
      mainStageLabel: 'PreIND',
      subStatusLabel: 'PreIND 反馈-药学',
    })]
    expect(getProjectCell(studies, 'PRE_IND')).toMatchObject({
      label: 'PreIND 反馈-药学', subText: 'PreIND', tone: 'blue',
    })
    expect(getProjectCell(studies, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'PHASE_1')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('each ordinary column follows its own study milestone; later phases do not force earlier 已完成', () => {
    const phase1 = ms({
      id: 11,
      phase: 'PHASE_1',
      mainStageCode: 'Enrollment',
      mainStageLabel: 'Enrollment',
      subStatusLabel: 'LPI',
    })
    const phase31 = ms({
      id: 31,
      phase: 'PHASE_3_1',
      mainStageCode: 'Data_Report',
      mainStageLabel: 'Data & Report',
      subStatusLabel: 'DBL',
    })
    const studies = [
      phase1,
      phase31,
      ms({ phase: 'PHASE_3_2', mainStageLabel: 'NDA/BLA', subStatusLabel: 'NDA/BLA 递交' }),
    ]
    // 监管列不变：PreIND/IND ← Phase 1；PRE-3 ← Phase 3-1（已过 Pre3）
    expect(getProjectCell(studies, 'PRE_IND')).toMatchObject({
      label: '已完成', tone: 'green', subText: 'PRE_IND', studyId: 11,
    })
    expect(getProjectCell(studies, 'IND')).toMatchObject({
      label: '已完成', tone: 'green', studyId: 11,
    })
    expect(getProjectCell(studies, 'PRE_3')).toMatchObject({
      label: '已完成', tone: 'green', studyId: 31,
    })
    // 普通列：Phase 3-1 仍进行中（DBL），不因存在 Phase 3-2 而变「已完成」
    expect(getProjectCell(studies, 'PHASE_3_1')).toMatchObject({
      label: 'DBL', tone: 'blue', subText: 'Data & Report', studyId: 31,
    })
    expect(getProjectCell(studies, 'PHASE_1')).toMatchObject({
      label: 'LPI', tone: 'blue', subText: 'Enrollment', studyId: 11,
    })
    expect(getProjectCell(studies, 'PHASE_2')).toMatchObject({ label: '—', tone: 'empty' })
    const cell = getProjectCell(studies, 'PHASE_3_2')
    expect(cell).toMatchObject({ label: 'NDA/BLA 递交', subText: 'NDA/BLA', tone: 'blue' })
  })

  it('takes the latest study at the own phase for that column', () => {
    const studies = [
      ms({ phase: 'PHASE_1', statusLabel: '旧', updatedAt: '2026-01-01T00:00:00' }),
      ms({ phase: 'PHASE_1', statusLabel: '新', updatedAt: '2026-07-01T00:00:00' }),
    ]
    expect(getProjectCell(studies, 'PHASE_1').label).toBe('新')
  })

  it('PreIND/IND follow Phase 1 milestones; PRE-3 does not use Phase 1', () => {
    const phase1 = ms({
      id: 42,
      phase: 'PHASE_1',
      code: 'HDM-P1',
      mainStageCode: 'PreIND',
      mainStageLabel: 'PreIND',
      subStatusLabel: 'PreIND 反馈-药学',
      productName: 'HDM',
    })
    const studies = [
      phase1,
      ms({
        phase: 'PHASE_2',
        mainStageCode: 'Enrollment',
        mainStageLabel: 'Enrollment',
        subStatusLabel: 'FPI',
      }),
    ]
    expect(getProjectCell(studies, 'PRE_IND')).toMatchObject({
      label: 'PreIND 反馈-药学',
      subText: 'PreIND',
      tone: 'blue',
      studyId: 42,
    })
    expect(getProjectCell(studies, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
    // 无 Phase 3-1 / PRE_3 Study → PRE-3 为空
    expect(getProjectCell(studies, 'PRE_3')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('PRE-3 follows Phase 3-1 study Pre3 milestones and links that study', () => {
    const phase31 = ms({
      id: 77,
      phase: 'PHASE_3_1',
      mainStageCode: 'Pre3',
      mainStageLabel: 'Pre3',
      subStatusLabel: 'Pre3 反馈-数统',
    })
    const studies = [
      ms({
        id: 11,
        phase: 'PHASE_1',
        mainStageCode: 'Enrollment',
        mainStageLabel: 'Enrollment',
        subStatusLabel: 'LPI',
      }),
      phase31,
    ]
    expect(getProjectCell(studies, 'PRE_3')).toMatchObject({
      label: 'Pre3 反馈-数统',
      subText: 'Pre3',
      tone: 'blue',
      studyId: 77,
    })
    // Phase 1 已过 Pre3 不应用来填 PRE-3
    expect(getProjectCell(studies, 'PRE_IND').studyId).toBe(11)
  })

  it('Phase 1 at Pre3 completes PreIND/IND but PRE-3 stays empty without Phase 3-1', () => {
    const phase1 = ms({
      id: 7,
      phase: 'PHASE_1',
      mainStageCode: 'Pre3',
      mainStageLabel: 'Pre3',
      subStatusLabel: 'Pre3 反馈-数统',
      preindCompleted: true,
      indCompleted: true,
    })
    expect(getProjectCell([phase1], 'PRE_IND')).toMatchObject({
      label: '已完成', tone: 'green', studyId: 7,
    })
    expect(getProjectCell([phase1], 'IND')).toMatchObject({
      label: '已完成', tone: 'green', studyId: 7,
    })
    // 无 Phase 3-1 / PRE_3 Study → PRE-3 为空
    expect(getProjectCell([phase1], 'PRE_3')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('Phase 3-1 past Pre3 marks PRE-3 completed and linked to Phase 3-1', () => {
    const phase31 = ms({
      id: 9,
      phase: 'PHASE_3_1',
      mainStageCode: 'Protocol',
      mainStageLabel: 'Protocol',
      subStatusLabel: '方案定稿',
    })
    expect(getProjectCell([phase31], 'PRE_3')).toMatchObject({
      label: '已完成', tone: 'green', studyId: 9,
    })
  })
})

describe('furthestPhaseOf', () => {
  it('returns the most advanced phase of the project', () => {
    const studies: CellStudy[] = [
      ms({ phase: 'PHASE_1' }),
      ms({ phase: 'PHASE_3_1' }),
    ]
    expect(furthestPhaseOf(studies)).toBe('PHASE_3_1')
  })
})
