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

const phaseAliases: Record<string, PipelinePhase> = {
  'Pre-IND': 'PreIND',
  PreIND: 'PreIND',
  IND: 'IND',
  Ph1: 'Phase 1',
  'Phase 1': 'Phase 1',
  Ph2: 'Phase 2',
  'Phase 2': 'Phase 2',
  'Pre-3': 'PRE-3',
  'PRE-3': 'PRE-3',
  'Ph3-1': 'Phase 3-1',
  'Phase 3-1': 'Phase 3-1',
  'Ph3-2': 'Phase 3-2',
  'Phase 3-2': 'Phase 3-2',
}

const statusTone: Record<string, PipelineTone> = {
  positive: 'green',
  warning: 'orange',
  danger: 'red',
  info: 'blue',
  neutral: 'blue',
}

export function normalizePhase(phase: string): PipelinePhase | undefined {
  return phaseAliases[phase]
}

export function getPipelineCell(study: Study, targetPhase: PipelinePhase) {
  const configuredPhase = normalizePhase(study.phase)
  if (!configuredPhase) {
    return { label: '—', tone: 'empty' as const, explanation: undefined }
  }

  const configuredIndex = PHASE_TAGS.indexOf(configuredPhase)
  const targetIndex = PHASE_TAGS.indexOf(targetPhase)

  if (targetIndex < configuredIndex) {
    return {
      label: '已完成',
      tone: 'green' as const,
      explanation: `${targetPhase} 实际无项目，由 ${configuredPhase} 回填`,
    }
  }
  if (targetIndex === configuredIndex) {
    return {
      label: study.statusLabel,
      tone: statusTone[study.statusTone] ?? 'blue',
      explanation: undefined,
    }
  }
  return { label: '—', tone: 'empty' as const, explanation: undefined }
}
