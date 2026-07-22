INSERT IGNORE INTO hd_plt_therapeutic_area(
    area_code, area_name, english_name, sort_order, status_code,
    sys_create_by, sys_update_by)
VALUES
    ('ONCOLOGY', '肿瘤', 'Oncology', 10, 'ACTIVE', 'system', 'system'),
    ('AUTOIMMUNE', '自身免疫', 'Autoimmune Disease', 20, 'ACTIVE', 'system', 'system'),
    ('METABOLIC_CARDIOVASCULAR', '代谢与心血管', 'Metabolic and Cardiovascular', 30, 'ACTIVE', 'system', 'system'),
    ('RESPIRATORY', '呼吸系统', 'Respiratory', 40, 'ACTIVE', 'system', 'system'),
    ('INFECTIOUS_DISEASE', '感染性疾病', 'Infectious Disease', 50, 'ACTIVE', 'system', 'system'),
    ('NEUROSCIENCE', '神经科学', 'Neuroscience', 60, 'ACTIVE', 'system', 'system');
