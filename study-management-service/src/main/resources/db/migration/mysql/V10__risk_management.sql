ALTER TABLE hd_plt_risk
    ADD COLUMN row_version BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';

ALTER TABLE hd_plt_risk_action
    ADD COLUMN row_version BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';

INSERT INTO hd_plt_risk_rule_version(
    version_no, version_name, low_risk_max_score, medium_risk_max_score,
    status_code, effective_from, approved_by, approved_time,
    version_description, sys_create_by, sys_update_by)
SELECT 1, '风险评分规则 V1', 12, 36, 'ACTIVE', CURRENT_TIMESTAMP(6),
       'system', CURRENT_TIMESTAMP(6), 'PRD v1.0 默认阈值', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM hd_plt_risk_rule_version
    WHERE version_no = 1 AND sys_deleted = 0);

INSERT IGNORE INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('risk', 'risk.page.view', '查看风险管理页面', 'PAGE', 'view', 'ACTIVE', 70, 'system', 'system'),
    ('risk', 'risk.read', '查询风险', 'DATA', 'read', 'ACTIVE', 71, 'system', 'system'),
    ('risk', 'risk.create', '新增风险', 'DATA', 'create', 'ACTIVE', 72, 'system', 'system'),
    ('risk', 'risk.update', '修改风险', 'DATA', 'update', 'ACTIVE', 73, 'system', 'system'),
    ('risk', 'risk.delete', '删除风险', 'DATA', 'delete', 'ACTIVE', 74, 'system', 'system');

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'risk.page.view', 'risk.read', 'risk.create', 'risk.update', 'risk.delete')
WHERE r.role_name = 'ADMIN' AND r.sys_deleted = 0 AND p.sys_deleted = 0;

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'risk.page.view', 'risk.read', 'risk.create', 'risk.update')
WHERE r.role_name = 'USER' AND r.sys_deleted = 0 AND p.sys_deleted = 0;

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN ('risk.page.view', 'risk.read')
WHERE r.role_name = 'VIEWER' AND r.sys_deleted = 0 AND p.sys_deleted = 0;
