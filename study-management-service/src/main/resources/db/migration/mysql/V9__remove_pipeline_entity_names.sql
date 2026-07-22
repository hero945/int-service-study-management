ALTER TABLE hd_plt_study
    DROP INDEX idx_hd_plt_study_program_name,
    DROP INDEX idx_hd_plt_study_project_name,
    DROP COLUMN study_name,
    DROP COLUMN program_name_snapshot,
    DROP COLUMN project_name_snapshot;

ALTER TABLE hd_plt_monthly_report
    DROP COLUMN program_name_snapshot,
    DROP COLUMN project_name_snapshot;

ALTER TABLE hd_plt_risk
    DROP COLUMN program_name_snapshot,
    DROP COLUMN project_name_snapshot;

ALTER TABLE hd_plt_project DROP COLUMN project_name;
ALTER TABLE hd_plt_program DROP COLUMN program_name;
