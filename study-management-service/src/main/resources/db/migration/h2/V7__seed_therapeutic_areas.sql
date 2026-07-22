INSERT INTO hd_plt_therapeutic_area(
    area_code, area_name, english_name, sort_order, status_code, sys_create_by, sys_update_by)
SELECT 'ONCOLOGY', '肿瘤', 'Oncology', 10, 'ACTIVE', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'ONCOLOGY');

INSERT INTO hd_plt_therapeutic_area(
    area_code, area_name, english_name, sort_order, status_code, sys_create_by, sys_update_by)
SELECT 'AUTOIMMUNE', '自身免疫', 'Autoimmune Disease', 20, 'ACTIVE', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'AUTOIMMUNE');

INSERT INTO hd_plt_therapeutic_area(
    area_code, area_name, english_name, sort_order, status_code, sys_create_by, sys_update_by)
SELECT 'METABOLIC_CARDIOVASCULAR', '代谢与心血管', 'Metabolic and Cardiovascular', 30, 'ACTIVE', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'METABOLIC_CARDIOVASCULAR');

INSERT INTO hd_plt_therapeutic_area(
    area_code, area_name, english_name, sort_order, status_code, sys_create_by, sys_update_by)
SELECT 'RESPIRATORY', '呼吸系统', 'Respiratory', 40, 'ACTIVE', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'RESPIRATORY');

INSERT INTO hd_plt_therapeutic_area(
    area_code, area_name, english_name, sort_order, status_code, sys_create_by, sys_update_by)
SELECT 'INFECTIOUS_DISEASE', '感染性疾病', 'Infectious Disease', 50, 'ACTIVE', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'INFECTIOUS_DISEASE');

INSERT INTO hd_plt_therapeutic_area(
    area_code, area_name, english_name, sort_order, status_code, sys_create_by, sys_update_by)
SELECT 'NEUROSCIENCE', '神经科学', 'Neuroscience', 60, 'ACTIVE', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'NEUROSCIENCE');
