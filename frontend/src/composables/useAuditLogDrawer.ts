import { reactive } from 'vue'
import type { AuditGroupType, AuditModuleCode, AuditSubjectType } from '../api/types'

export function useAuditLogDrawer(defaultModule: AuditModuleCode) {
  const auditDrawer = reactive<{
    open: boolean
    title: string
    moduleCode: AuditModuleCode
    subjectType?: AuditSubjectType
    subjectId?: number
    scopeStudyId?: number
    groupType?: AuditGroupType
    groupId?: number
    groupCode?: string
  }>({
    open: false,
    title: '全部操作日志',
    moduleCode: defaultModule,
  })

  function openAllAuditLogs(title = '全部操作日志') {
    Object.assign(auditDrawer, {
      open: true,
      title,
      moduleCode: defaultModule,
      subjectType: undefined,
      subjectId: undefined,
      scopeStudyId: undefined,
      groupType: undefined,
      groupId: undefined,
      groupCode: undefined,
    })
  }

  function openRecordAuditLogs(
    title: string,
    subjectType: AuditSubjectType,
    subjectId: number,
  ) {
    Object.assign(auditDrawer, {
      open: true,
      title,
      moduleCode: defaultModule,
      subjectType,
      subjectId,
      scopeStudyId: undefined,
      groupType: undefined,
      groupId: undefined,
      groupCode: undefined,
    })
  }

  function openGroupedAuditLogs(
    title: string,
    groupType: AuditGroupType,
    group: { scopeStudyId?: number; groupId?: number; groupCode?: string },
  ) {
    Object.assign(auditDrawer, {
      open: true,
      title,
      moduleCode: defaultModule,
      subjectType: undefined,
      subjectId: undefined,
      scopeStudyId: group.scopeStudyId,
      groupType,
      groupId: group.groupId,
      groupCode: group.groupCode,
    })
  }

  function closeAuditLogs() {
    auditDrawer.open = false
  }

  return {
    auditDrawer,
    openAllAuditLogs,
    openRecordAuditLogs,
    openGroupedAuditLogs,
    closeAuditLogs,
  }
}
