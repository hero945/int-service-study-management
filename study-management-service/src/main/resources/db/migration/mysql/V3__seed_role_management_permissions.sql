INSERT IGNORE INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('role', 'role.page.view', 'View role management', 'PAGE', 'view', 'ACTIVE', 80, 'system', 'system'),
    ('role', 'role.create', 'Create roles', 'OPERATION', 'create', 'ACTIVE', 81, 'system', 'system'),
    ('role', 'role.update', 'Update roles', 'OPERATION', 'update', 'ACTIVE', 82, 'system', 'system'),
    ('role', 'role.delete', 'Delete roles', 'OPERATION', 'delete', 'ACTIVE', 83, 'system', 'system');

INSERT IGNORE INTO hd_plt_role_permission(
    role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'role.page.view', 'role.create', 'role.update', 'role.delete')
WHERE r.role_name = 'ADMIN' AND r.sys_deleted = 0 AND p.sys_deleted = 0;
