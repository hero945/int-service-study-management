import { describe, expect, it } from 'vitest'
import { useAuditLogDrawer } from './useAuditLogDrawer'

describe('useAuditLogDrawer', () => {
  it('opens a grouped query and clears it when switching to a record query', () => {
    const { auditDrawer, openGroupedAuditLogs, openRecordAuditLogs } =
      useAuditLogDrawer('MILESTONE')

    openGroupedAuditLogs('阶段操作日志', 'MILESTONE_STAGE', {
      scopeStudyId: 12,
      groupCode: 'START_UP',
    })

    expect(auditDrawer).toMatchObject({
      open: true,
      moduleCode: 'MILESTONE',
      scopeStudyId: 12,
      groupType: 'MILESTONE_STAGE',
      groupCode: 'START_UP',
    })
    expect(auditDrawer.subjectType).toBeUndefined()
    expect(auditDrawer.subjectId).toBeUndefined()

    openRecordAuditLogs('节点操作日志', 'MILESTONE', 33)

    expect(auditDrawer).toMatchObject({
      open: true,
      moduleCode: 'MILESTONE',
      subjectType: 'MILESTONE',
      subjectId: 33,
    })
    expect(auditDrawer.scopeStudyId).toBeUndefined()
    expect(auditDrawer.groupType).toBeUndefined()
    expect(auditDrawer.groupId).toBeUndefined()
    expect(auditDrawer.groupCode).toBeUndefined()
  })

  it('uses report id and function code for a monthly function query', () => {
    const { auditDrawer, openGroupedAuditLogs } = useAuditLogDrawer('MONTHLY')

    openGroupedAuditLogs('功能线操作日志', 'MONTHLY_FUNCTION', {
      scopeStudyId: 8,
      groupId: 21,
      groupCode: 'CLINICAL',
    })

    expect(auditDrawer).toMatchObject({
      moduleCode: 'MONTHLY',
      scopeStudyId: 8,
      groupType: 'MONTHLY_FUNCTION',
      groupId: 21,
      groupCode: 'CLINICAL',
    })
  })
})
