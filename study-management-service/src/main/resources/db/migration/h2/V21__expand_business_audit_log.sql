ALTER TABLE hd_plt_audit_log ADD COLUMN module_code VARCHAR(32);
ALTER TABLE hd_plt_audit_log ADD COLUMN subject_type VARCHAR(32);
ALTER TABLE hd_plt_audit_log ADD COLUMN subject_id BIGINT;
ALTER TABLE hd_plt_audit_log ADD COLUMN subject_code VARCHAR(128);
ALTER TABLE hd_plt_audit_log ADD COLUMN scope_study_id BIGINT;
ALTER TABLE hd_plt_audit_log ADD COLUMN operator_display_name VARCHAR(100);
ALTER TABLE hd_plt_audit_log ADD COLUMN request_method VARCHAR(10);
ALTER TABLE hd_plt_audit_log ADD COLUMN request_path VARCHAR(500);
ALTER TABLE hd_plt_audit_log ADD COLUMN error_code VARCHAR(64);
ALTER TABLE hd_plt_audit_log ADD COLUMN payload_version INT NOT NULL DEFAULT 1;

CREATE INDEX idx_hd_plt_audit_subject
  ON hd_plt_audit_log(subject_type, subject_id, module_code, occurred_time, id);
CREATE INDEX idx_hd_plt_audit_module
  ON hd_plt_audit_log(module_code, occurred_time, id);
CREATE INDEX idx_hd_plt_audit_study
  ON hd_plt_audit_log(scope_study_id, occurred_time, id);

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
SET scope_study_id = (
  SELECT r.study_id
  FROM hd_plt_monthly_report_entry e
  JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
  WHERE e.id = a.target_id)
WHERE module_code = 'MONTHLY' AND target_table = 'hd_plt_monthly_report_entry';

UPDATE hd_plt_audit_log a
SET
  subject_id = CASE
    WHEN target_table = 'hd_plt_risk' THEN target_id
    ELSE (SELECT ra.risk_id FROM hd_plt_risk_action ra WHERE ra.id = a.target_id)
  END,
  subject_code = CASE
    WHEN target_table = 'hd_plt_risk'
      THEN (SELECT r.risk_code FROM hd_plt_risk r WHERE r.id = a.target_id)
    ELSE (SELECT r.risk_code FROM hd_plt_risk_action ra
          JOIN hd_plt_risk r ON r.id = ra.risk_id WHERE ra.id = a.target_id)
  END,
  scope_study_id = CASE
    WHEN target_table = 'hd_plt_risk'
      THEN (SELECT r.study_id FROM hd_plt_risk r WHERE r.id = a.target_id)
    ELSE (SELECT r.study_id FROM hd_plt_risk_action ra
          JOIN hd_plt_risk r ON r.id = ra.risk_id WHERE ra.id = a.target_id)
  END
WHERE module_code = 'RISK';

UPDATE hd_plt_audit_log
SET subject_id = target_id, scope_study_id = target_id
WHERE module_code = 'TEAM'
  AND target_table IN ('hd_plt_team_assignment', 'hd_plt_study_team_member');

INSERT INTO hd_plt_permission(
  module_code, permission_code, permission_name, permission_type, action_code,
  permission_description, status_code, sort_order, sys_create_by, sys_update_by)
VALUES (
  'audit', 'audit.read', '查看操作日志', 'OPERATION', 'read',
  '查看业务记录的操作前后快照、字段差异和失败或拒绝事件',
  'ACTIVE', 200, 'system', 'system');

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code = 'audit.read'
WHERE r.role_name = 'ADMIN';
