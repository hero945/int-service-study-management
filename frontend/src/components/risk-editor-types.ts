import type { RiskActionInput, RiskStatus } from '../api/types'

/** 风险基本信息 + 评估表单的可变状态（RiskEditorDrawer 与各子组件共享的 reactive 对象） */
export interface RiskFormState {
  studyId: number
  functionLineId: number
  ownerUserId: number
  description: string
  registeredDate: string
  status: RiskStatus
  statusReason: string
  impact: number
  likelihood: number
  detectability: number
  assessmentReason: string
}

/** 措施编辑表单状态（与 RiskActionInput 同形） */
export type RiskActionFormState = RiskActionInput

/** 正在编辑的已保存措施的定位信息；undefined 表示在编辑待保存措施 */
export interface RiskActionEditorState {
  id?: number
  version?: number
  fromStatus?: string
}
