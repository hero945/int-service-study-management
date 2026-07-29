ALTER TABLE hd_plt_audit_log
  ADD COLUMN module_code VARCHAR(32) NULL AFTER action_code,
  ADD COLUMN subject_type VARCHAR(32) NULL AFTER module_code,
  ADD COLUMN subject_id BIGINT UNSIGNED NULL AFTER subject_type,
  ADD COLUMN subject_code VARCHAR(128) NULL AFTER subject_id,
  ADD COLUMN scope_study_id BIGINT UNSIGNED NULL AFTER subject_code,
  ADD COLUMN operator_display_name VARCHAR(100) NULL AFTER operator_email,
  ADD COLUMN request_method VARCHAR(10) NULL AFTER ip_address,
  ADD COLUMN request_path VARCHAR(500) NULL AFTER request_method,
  ADD COLUMN error_code VARCHAR(64) NULL AFTER operation_reason,
  ADD COLUMN payload_version INT NOT NULL DEFAULT 1 AFTER error_code,
  ADD KEY idx_hd_plt_audit_subject
    (subject_type, subject_id, module_code, occurred_time, id),
  ADD KEY idx_hd_plt_audit_module
    (module_code, occurred_time, id),
  ADD KEY idx_hd_plt_audit_study
    (scope_study_id, occurred_time, id);

UPDATE hd_plt_audit_log
SET
  module_code = CASE
    WHEN target_table = 'hd_plt_study_milestone' OR action_code LIKE 'MILESTONE%' THEN 'MILESTONE'
    WHEN target_table = 'hd_plt_monthly_report_entry' OR action_code LIKE 'MONTHLY%' THEN 'MONTHLY'
    WHEN target_table LIKE 'hd_plt_risk%' OR action_code LIKE 'RISK%' THEN 'RISK'
    WHEN target_table IN ('hd_plt_team_assignment', 'hd_plt_study_team_member')
      OR action_code LIKE 'TEAM%' THEN 'TEAM'
    WHEN target_table IN ('hd_plt_program', 'hd_plt_project', 'hd_plt_study') THEN 'CONFIG'
    WHEN target_table IN ('hd_plt_user', 'hd_plt_user_account')
      OR action_code LIKE 'USER%' OR action_code LIKE 'PASSWORD%' THEN 'ACCOUNT'
    WHEN target_table IN ('hd_plt_role', 'hd_plt_role_permission') OR action_code LIKE 'ROLE%' THEN 'ROLE'
    ELSE NULL
  END,
  subject_type = CASE
    WHEN target_table = 'hd_plt_study_milestone' OR action_code LIKE 'MILESTONE%' THEN 'MILESTONE'
    WHEN target_table = 'hd_plt_monthly_report_entry' OR action_code LIKE 'MONTHLY%' THEN 'MONTHLY_ENTRY'
    WHEN target_table LIKE 'hd_plt_risk%' OR action_code LIKE 'RISK%' THEN 'RISK'
    WHEN target_table IN ('hd_plt_team_assignment', 'hd_plt_study_team_member')
      OR action_code LIKE 'TEAM%' THEN 'STUDY'
    WHEN target_table = 'hd_plt_program' THEN 'PROGRAM'
    WHEN target_table = 'hd_plt_project' THEN 'PROJECT'
    WHEN target_table = 'hd_plt_study' THEN 'STUDY'
    WHEN target_table IN ('hd_plt_user', 'hd_plt_user_account')
      OR action_code LIKE 'USER%' OR action_code LIKE 'PASSWORD%' THEN 'USER'
    WHEN target_table IN ('hd_plt_role', 'hd_plt_role_permission') OR action_code LIKE 'ROLE%' THEN 'ROLE'
    ELSE NULL
  END,
  subject_id = target_id
WHERE module_code IS NULL;

UPDATE hd_plt_audit_log a
JOIN hd_plt_monthly_report_entry e ON e.id = a.target_id
JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
SET a.scope_study_id = r.study_id
WHERE a.module_code = 'MONTHLY' AND a.target_table = 'hd_plt_monthly_report_entry';

UPDATE hd_plt_audit_log a
LEFT JOIN hd_plt_risk direct_risk
  ON a.target_table = 'hd_plt_risk' AND direct_risk.id = a.target_id
LEFT JOIN hd_plt_risk_action risk_action
  ON a.target_table = 'hd_plt_risk_action' AND risk_action.id = a.target_id
LEFT JOIN hd_plt_risk action_risk ON action_risk.id = risk_action.risk_id
SET
  a.subject_id = COALESCE(direct_risk.id, action_risk.id),
  a.subject_code = COALESCE(direct_risk.risk_code, action_risk.risk_code),
  a.scope_study_id = COALESCE(direct_risk.study_id, action_risk.study_id)
WHERE a.module_code = 'RISK';

UPDATE hd_plt_audit_log
SET subject_id = target_id, scope_study_id = target_id
WHERE module_code = 'TEAM'
  AND target_table IN ('hd_plt_team_assignment', 'hd_plt_study_team_member');

INSERT IGNORE INTO hd_plt_permission(
  module_code, permission_code, permission_name, permission_type, action_code,
  permission_description, status_code, sort_order, sys_create_by, sys_update_by)
VALUES (
  'audit', 'audit.read', '查看操作日志', 'OPERATION', 'read',
  '查看业务记录的操作前后快照、字段差异和失败或拒绝事件',
  'ACTIVE', 200, 'system', 'system');

INSERT IGNORE INTO hd_plt_role_permission(
  role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code = 'audit.read'
WHERE r.role_name = 'ADMIN' AND r.sys_deleted = 0 AND p.sys_deleted = 0;
