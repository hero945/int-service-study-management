ALTER TABLE hd_plt_study
    ADD COLUMN team_version BIGINT UNSIGNED NOT NULL DEFAULT 0
    COMMENT '团队矩阵乐观锁版本' AFTER indication_description_snapshot;

INSERT INTO hd_plt_function_line(
    function_code, function_name, sort_order, status_code, sys_create_by, sys_update_by)
VALUES
    ('PM', '项目管理', 1, 'ACTIVE', 'system', 'system'),
    ('RA', '注册', 2, 'ACTIVE', 'system', 'system'),
    ('CM', '临床医学', 3, 'ACTIVE', 'system', 'system'),
    ('CP', '临床药理', 4, 'ACTIVE', 'system', 'system'),
    ('PV', '药物警戒', 5, 'ACTIVE', 'system', 'system'),
    ('TM', '试验管理', 6, 'ACTIVE', 'system', 'system'),
    ('CO', '临床运营', 7, 'ACTIVE', 'system', 'system'),
    ('LAB', '中心实验室', 8, 'ACTIVE', 'system', 'system'),
    ('SUPPLY', '供应保障', 9, 'ACTIVE', 'system', 'system'),
    ('CTA', '临床试验协调', 10, 'ACTIVE', 'system', 'system'),
    ('ST', '生物统计', 11, 'ACTIVE', 'system', 'system'),
    ('PG', '统计编程', 12, 'ACTIVE', 'system', 'system'),
    ('DM', '数据管理', 13, 'ACTIVE', 'system', 'system'),
    ('MW', '医学写作', 14, 'ACTIVE', 'system', 'system'),
    ('NC', '非临床', 15, 'ACTIVE', 'system', 'system'),
    ('CMC', '药学CMC', 16, 'ACTIVE', 'system', 'system'),
    ('IP', '药品管理', 17, 'ACTIVE', 'system', 'system');

INSERT INTO hd_plt_team_role(
    role_code, role_name, function_line_id, is_project_lead, can_manage_team,
    sort_order, status_code, sys_create_by, sys_update_by)
SELECT 'PL', 'PL 项目负责人', f.id, 1, 1, 1, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PM'
UNION ALL
SELECT 'APL', 'APL 副项目负责人', f.id, 1, 0, 2, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PM'
UNION ALL
SELECT 'PM', 'PM 项目经理', f.id, 1, 1, 3, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PM'
UNION ALL
SELECT 'APM', 'APM 副项目经理', f.id, 1, 0, 4, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PM'
UNION ALL
SELECT 'RA_SPONSOR', 'RA Sponsor', f.id, 0, 0, 5, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'RA'
UNION ALL
SELECT 'RA_MANAGER', 'RA Manager', f.id, 0, 0, 6, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'RA'
UNION ALL
SELECT 'RA_SPECIALIST', 'RA Specialist', f.id, 0, 0, 7, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'RA'
UNION ALL
SELECT 'RA_CMC', 'RA CMC', f.id, 0, 0, 8, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'RA'
UNION ALL
SELECT 'CM_SPONSOR', 'CM Sponsor', f.id, 0, 0, 9, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CM'
UNION ALL
SELECT 'CM', 'CM', f.id, 0, 0, 10, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CM'
UNION ALL
SELECT 'CP_SPONSOR', 'CP Sponsor', f.id, 0, 0, 11, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CP'
UNION ALL
SELECT 'CP', 'CP', f.id, 0, 0, 12, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CP'
UNION ALL
SELECT 'PV_SPONSOR', 'PV Sponsor', f.id, 0, 0, 13, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PV'
UNION ALL
SELECT 'PVP', 'PVP', f.id, 0, 0, 14, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PV'
UNION ALL
SELECT 'PVO', 'PVO', f.id, 0, 0, 15, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PV'
UNION ALL
SELECT 'TM_SPONSOR', 'TM Sponsor', f.id, 0, 0, 16, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'TM'
UNION ALL
SELECT 'TM', 'TM', f.id, 0, 0, 17, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'TM'
UNION ALL
SELECT 'CO_SPONSOR', 'CO Sponsor', f.id, 0, 0, 18, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CO'
UNION ALL
SELECT 'CTM', 'CTM', f.id, 0, 0, 19, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CO'
UNION ALL
SELECT 'ACTM', 'ACTM', f.id, 0, 0, 20, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CO'
UNION ALL
SELECT 'LAB', 'Lab', f.id, 0, 0, 21, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'LAB'
UNION ALL
SELECT 'LAB_BACKUP', 'Lab backup', f.id, 0, 0, 22, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'LAB'
UNION ALL
SELECT 'SUPPLY', 'Supply', f.id, 0, 0, 23, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'SUPPLY'
UNION ALL
SELECT 'SUPPLY_BACKUP', 'Supply backup', f.id, 0, 0, 24, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'SUPPLY'
UNION ALL
SELECT 'CTA_PROCESS', 'CTA process', f.id, 0, 0, 25, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CTA'
UNION ALL
SELECT 'CTA_TMF', 'CTA TMF', f.id, 0, 0, 26, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CTA'
UNION ALL
SELECT 'ST_SPONSOR', 'ST Sponsor', f.id, 0, 0, 27, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'ST'
UNION ALL
SELECT 'ST', 'ST', f.id, 0, 0, 28, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'ST'
UNION ALL
SELECT 'PG_SPONSOR', 'PG Sponsor', f.id, 0, 0, 29, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PG'
UNION ALL
SELECT 'PG', 'PG', f.id, 0, 0, 30, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'PG'
UNION ALL
SELECT 'DM_SPONSOR', 'DM Sponsor', f.id, 0, 0, 31, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'DM'
UNION ALL
SELECT 'DM', 'DM', f.id, 0, 0, 32, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'DM'
UNION ALL
SELECT 'MW', 'MW', f.id, 0, 0, 33, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'MW'
UNION ALL
SELECT 'NC_CONTACT', 'NC-contact', f.id, 0, 0, 34, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'NC'
UNION ALL
SELECT 'NC_PK', 'NC-PK', f.id, 0, 0, 35, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'NC'
UNION ALL
SELECT 'NC_PD', 'NC-PD', f.id, 0, 0, 36, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'NC'
UNION ALL
SELECT 'NC_TOX', 'NC-TOX', f.id, 0, 0, 37, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'NC'
UNION ALL
SELECT 'CMC_PL', 'CMC-PL', f.id, 0, 0, 38, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CMC'
UNION ALL
SELECT 'CMC_PM', 'CMC-PM', f.id, 0, 0, 39, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CMC'
UNION ALL
SELECT 'CMC_DS', 'CMC-DS', f.id, 0, 0, 40, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CMC'
UNION ALL
SELECT 'CMC_DP', 'CMC-DP', f.id, 0, 0, 41, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CMC'
UNION ALL
SELECT 'CMC_OA', 'CMC-OA', f.id, 0, 0, 42, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CMC'
UNION ALL
SELECT 'CMC_RA', 'CMC-RA', f.id, 0, 0, 43, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'CMC'
UNION ALL
SELECT 'IP', 'IP', f.id, 0, 0, 44, 'ACTIVE', 'system', 'system'
FROM hd_plt_function_line f WHERE f.function_code = 'IP';

INSERT INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('team', 'team.page.view', '查看团队矩阵', 'PAGE', 'view', 'ACTIVE', 60, 'system', 'system'),
    ('team', 'team.edit_mode', '进入团队编辑模式', 'PAGE_OPERATION', 'edit_mode', 'ACTIVE', 61, 'system', 'system'),
    ('team', 'team.update', '更新团队分配', 'DATA', 'update', 'ACTIVE', 62, 'system', 'system');

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'team.page.view', 'team.edit_mode', 'team.update')
WHERE r.role_name = 'ADMIN';

