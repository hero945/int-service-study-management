-- Program
ALTER TABLE hd_plt_program ADD COLUMN version INT NOT NULL DEFAULT 1 COMMENT '同一 program_code 版本序号';
ALTER TABLE hd_plt_program DROP INDEX uk_hd_plt_program_code;
ALTER TABLE hd_plt_program ADD UNIQUE KEY uk_hd_plt_program_code_version (program_code, version);
ALTER TABLE hd_plt_program DROP INDEX uk_hd_plt_program_product;
ALTER TABLE hd_plt_program ADD UNIQUE KEY uk_hd_plt_program_product_version (product_name, version);

-- Project
ALTER TABLE hd_plt_project ADD COLUMN version INT NOT NULL DEFAULT 1 COMMENT '同一 project_code 版本序号';
ALTER TABLE hd_plt_project DROP INDEX uk_hd_plt_project_code;
ALTER TABLE hd_plt_project ADD UNIQUE KEY uk_hd_plt_project_code_version (project_code, version);

-- Study
ALTER TABLE hd_plt_study ADD COLUMN version INT NOT NULL DEFAULT 1 COMMENT '同一 study_code 版本序号';
ALTER TABLE hd_plt_study DROP INDEX uk_hd_plt_study_code;
ALTER TABLE hd_plt_study ADD UNIQUE KEY uk_hd_plt_study_code_version (study_code, version);
