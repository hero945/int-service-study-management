ALTER TABLE hd_plt_audit_log
  ADD COLUMN group_type VARCHAR(32) NULL AFTER scope_study_id,
  ADD COLUMN group_id BIGINT UNSIGNED NULL AFTER group_type,
  ADD COLUMN group_code VARCHAR(128) NULL AFTER group_id,
  ADD KEY idx_hd_plt_audit_group
    (module_code, group_type, group_id, group_code, scope_study_id, occurred_time, id);

UPDATE hd_plt_audit_log a
JOIN hd_plt_study_milestone m
  ON m.id = a.target_id
SET
  a.group_type = 'MILESTONE_STAGE',
  a.group_code = m.stage_code,
  a.scope_study_id = COALESCE(a.scope_study_id, m.study_id)
WHERE a.module_code = 'MILESTONE'
  AND a.target_table = 'hd_plt_study_milestone'
  AND a.group_type IS NULL;

UPDATE hd_plt_audit_log a
JOIN hd_plt_monthly_report_entry e
  ON e.id = a.target_id
JOIN hd_plt_monthly_report r
  ON r.id = e.monthly_report_id
SET
  a.group_type = 'MONTHLY_FUNCTION',
  a.group_id = r.id,
  a.group_code = r.function_line_code_snapshot,
  a.scope_study_id = COALESCE(a.scope_study_id, r.study_id)
WHERE a.module_code = 'MONTHLY'
  AND a.target_table = 'hd_plt_monthly_report_entry'
  AND a.group_type IS NULL;
