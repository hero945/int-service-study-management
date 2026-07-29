ALTER TABLE hd_plt_audit_log ADD COLUMN group_type VARCHAR(32);
ALTER TABLE hd_plt_audit_log ADD COLUMN group_id BIGINT;
ALTER TABLE hd_plt_audit_log ADD COLUMN group_code VARCHAR(128);

CREATE INDEX idx_hd_plt_audit_group
  ON hd_plt_audit_log(
    module_code, group_type, group_id, group_code, scope_study_id, occurred_time, id);

UPDATE hd_plt_audit_log a
SET
  group_type = 'MILESTONE_STAGE',
  group_code = (
    SELECT m.stage_code
    FROM hd_plt_study_milestone m
    WHERE m.id = a.target_id),
  scope_study_id = COALESCE(
    scope_study_id,
    (SELECT m.study_id
     FROM hd_plt_study_milestone m
     WHERE m.id = a.target_id))
WHERE module_code = 'MILESTONE'
  AND target_table = 'hd_plt_study_milestone'
  AND group_type IS NULL
  AND EXISTS (
    SELECT 1
    FROM hd_plt_study_milestone m
    WHERE m.id = a.target_id);

UPDATE hd_plt_audit_log a
SET
  group_type = 'MONTHLY_FUNCTION',
  group_id = (
    SELECT r.id
    FROM hd_plt_monthly_report_entry e
    JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
    WHERE e.id = a.target_id),
  group_code = (
    SELECT r.function_line_code_snapshot
    FROM hd_plt_monthly_report_entry e
    JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
    WHERE e.id = a.target_id),
  scope_study_id = COALESCE(
    scope_study_id,
    (SELECT r.study_id
     FROM hd_plt_monthly_report_entry e
     JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
     WHERE e.id = a.target_id))
WHERE module_code = 'MONTHLY'
  AND target_table = 'hd_plt_monthly_report_entry'
  AND group_type IS NULL
  AND EXISTS (
    SELECT 1
    FROM hd_plt_monthly_report_entry e
    JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
    WHERE e.id = a.target_id);
