import { describe, expect, it } from 'vitest'
import type { Study } from '../api/types'
import type { CellStudy } from './pipeline-aggregation'
import { furthestPhaseOf, getProjectCell, groupByProject } from './pipeline-aggregation'

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
  id: ++seq,
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
  it('marks all phases earlier than current as 已完成 (green)', () => {
    const studies = [ms({ phase: 'PHASE_2', statusLabel: '进行中', statusTone: 'positive' })]
    // current = Phase 2 → PreIND / IND / Phase 1 are earlier
    expect(getProjectCell(studies, 'PreIND')).toMatchObject({ label: '已完成', tone: 'green' })
    expect(getProjectCell(studies, 'IND')).toMatchObject({ label: '已完成', tone: 'green' })
    expect(getProjectCell(studies, 'Phase 1')).toMatchObject({ label: '已完成', tone: 'green' })
  })

  it('marks all phases later than current as — (empty/gray)', () => {
    const studies = [ms({ phase: 'PHASE_2', statusLabel: '进行中' })]
    expect(getProjectCell(studies, 'PRE-3')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'Phase 3-1')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'Phase 3-2')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('current column shows milestone sub-status as label with main status as gray sub-text', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      mainStageLabel: 'IA',
      subStatusLabel: 'IA 数据冻结',
    })]
    const cell = getProjectCell(studies, 'Phase 2')
    expect(cell).toMatchObject({ label: 'IA 数据冻结', tone: 'blue', subText: 'IA' })
  })

  it('shows 已完成 on the current column when current phase completed', () => {
    const studies = [ms({
      phase: 'PHASE_2',
      mainStageLabel: 'Enrollment',
      subStatusLabel: 'LPO',
      currentPhaseCompleted: true,
    })]
    expect(getProjectCell(studies, 'Phase 2')).toMatchObject({ label: '已完成', tone: 'green' })
  })

  it('PreIND-only project: PreIND is current (sub-status), later phases are —', () => {
    const studies = [ms({
      phase: 'PRE_IND',
      mainStageLabel: 'PreIND',
      subStatusLabel: 'PreIND 反馈-药学',
    })]
    expect(getProjectCell(studies, 'PreIND')).toMatchObject({ label: 'PreIND 反馈-药学', subText: 'PreIND', tone: 'blue' })
    expect(getProjectCell(studies, 'IND')).toMatchObject({ label: '—', tone: 'empty' })
    expect(getProjectCell(studies, 'Phase 1')).toMatchObject({ label: '—', tone: 'empty' })
  })

  it('deep program shows all earlier phases 已完成 and only the furthest as current sub-status', () => {
    const studies = [
      ms({ phase: 'PHASE_1', mainStageLabel: 'Enrollment', subStatusLabel: 'LPI' }),
      ms({ phase: 'PHASE_3_1', mainStageLabel: 'Data & Report', subStatusLabel: 'DBL' }),
      ms({ phase: 'PHASE_3_2', mainStageLabel: 'NDA/BLA', subStatusLabel: 'NDA/BLA 递交' }),
    ]
    // current = Phase 3-2
    expect(getProjectCell(studies, 'PreIND')).toMatchObject({ label: '已完成', tone: 'green' })
    expect(getProjectCell(studies, 'Phase 3-1')).toMatchObject({ label: '已完成', tone: 'green' })
    const cell = getProjectCell(studies, 'Phase 3-2')
    expect(cell).toMatchObject({ label: 'NDA/BLA 递交', subText: 'NDA/BLA', tone: 'blue' })
  })

  it('takes the latest study at the current phase for the equal column', () => {
    const studies = [
      ms({ phase: 'PHASE_1', statusLabel: '旧', updatedAt: '2026-01-01T00:00:00' }),
      ms({ phase: 'PHASE_1', statusLabel: '新', updatedAt: '2026-07-01T00:00:00' }),
    ]
    expect(getProjectCell(studies, 'Phase 1').label).toBe('新')
  })
})

describe('furthestPhaseOf', () => {
  it('returns the most advanced phase of the project', () => {
    const studies: CellStudy[] = [
      ms({ phase: 'PHASE_1' }),
      ms({ phase: 'PHASE_3_1' }),
    ]
    expect(furthestPhaseOf(studies)).toBe('Phase 3-1')
  })
})
