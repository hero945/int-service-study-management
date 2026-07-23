-- 月报导出权限：注册 report.page.view / report.export
-- ADMIN/USER 两者都有；VIEWER 仅页面预览。
INSERT IGNORE INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('report', 'report.page.view', '查看月报导出页', 'PAGE', 'view', 'ACTIVE', 100, 'system', 'system'),
    ('report', 'report.export', '导出月报文件', 'DATA', 'export', 'ACTIVE', 101, 'system', 'system');

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN ('report.page.view', 'report.export')
WHERE r.role_name = 'ADMIN' AND r.sys_deleted = 0 AND p.sys_deleted = 0;

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN ('report.page.view', 'report.export')
WHERE r.role_name = 'USER' AND r.sys_deleted = 0 AND p.sys_deleted = 0;

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code = 'report.page.view'
WHERE r.role_name = 'VIEWER' AND r.sys_deleted = 0 AND p.sys_deleted = 0;
