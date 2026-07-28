import type { Study } from '../api/types'

/** PL / PM 合并展示（'张三 / 李四'，缺一则只显示有的） */
export function plPmLabel(study: Pick<Study, 'plName' | 'pmName'>): string {
  return [study.plName, study.pmName].filter(Boolean).join(' / ')
}
