ALTER TABLE hd_plt_program ADD COLUMN version INT NOT NULL DEFAULT 1;
ALTER TABLE hd_plt_project ADD COLUMN version INT NOT NULL DEFAULT 1;
ALTER TABLE hd_plt_study ADD COLUMN version INT NOT NULL DEFAULT 1;

ALTER TABLE hd_plt_program DROP CONSTRAINT IF EXISTS uk_hd_plt_program_code;
ALTER TABLE hd_plt_program DROP CONSTRAINT IF EXISTS uk_hd_plt_program_product;
ALTER TABLE hd_plt_project DROP CONSTRAINT IF EXISTS uk_hd_plt_project_code;
ALTER TABLE hd_plt_study DROP CONSTRAINT IF EXISTS uk_hd_plt_study_code;

ALTER TABLE hd_plt_program ADD UNIQUE (program_code, version);
ALTER TABLE hd_plt_program ADD UNIQUE (product_name, version);
ALTER TABLE hd_plt_project ADD UNIQUE (project_code, version);
ALTER TABLE hd_plt_study ADD UNIQUE (study_code, version);
