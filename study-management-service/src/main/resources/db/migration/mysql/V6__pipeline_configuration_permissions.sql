INSERT IGNORE INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('config', 'config.page.view', 'View pipeline configuration', 'PAGE', 'view', 'ACTIVE', 29, 'system', 'system'),
    ('config', 'config.update', 'Update pipeline configuration', 'OPERATION', 'update', 'ACTIVE', 31, 'system', 'system'),
    ('config', 'config.delete', 'Delete pipeline configuration', 'OPERATION', 'delete', 'ACTIVE', 32, 'system', 'system');

INSERT IGNORE INTO hd_plt_role_permission(
    role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'config.page.view', 'config.update', 'config.delete')
WHERE r.role_name = 'ADMIN' AND r.sys_deleted = 0 AND p.sys_deleted = 0;

DELETE rp
FROM hd_plt_role_permission rp
JOIN hd_plt_role r ON r.id = rp.role_id
JOIN hd_plt_permission p ON p.id = rp.permission_id
WHERE r.role_name = 'USER' AND p.permission_code = 'config.create';

DELETE FROM hd_plt_spring_session_attributes;
DELETE FROM hd_plt_spring_session;
