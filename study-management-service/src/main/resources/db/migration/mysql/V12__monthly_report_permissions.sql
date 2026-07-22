-- 月报填写权限：注册 monthly.read / monthly.create / monthly.update
-- 角色策略与 risk 模块一致：ADMIN 全量、USER 三权限、VIEWER 仅 read。
INSERT IGNORE INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('monthly', 'monthly.read', '查询月报', 'DATA', 'read', 'ACTIVE', 90, 'system', 'system'),
    ('monthly', 'monthly.create', '新增月报进展', 'DATA', 'create', 'ACTIVE', 91, 'system', 'system'),
    ('monthly', 'monthly.update', '修改月报进展', 'DATA', 'update', 'ACTIVE', 92, 'system', 'system');

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'monthly.read', 'monthly.create', 'monthly.update')
WHERE r.role_name = 'ADMIN' AND r.sys_deleted = 0 AND p.sys_deleted = 0;

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'monthly.read', 'monthly.create', 'monthly.update')
WHERE r.role_name = 'USER' AND r.sys_deleted = 0 AND p.sys_deleted = 0;

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN ('monthly.read')
WHERE r.role_name = 'VIEWER' AND r.sys_deleted = 0 AND p.sys_deleted = 0;
