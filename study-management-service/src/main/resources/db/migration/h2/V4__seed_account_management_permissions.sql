INSERT INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('account', 'account.update', 'Update accounts', 'OPERATION', 'update', 'ACTIVE', 51, 'system', 'system'),
    ('account', 'account.assignRole', 'Assign roles to accounts', 'OPERATION', 'assign', 'ACTIVE', 52, 'system', 'system'),
    ('account', 'account.delete', 'Delete accounts', 'OPERATION', 'delete', 'ACTIVE', 53, 'system', 'system');

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code IN (
    'account.update', 'account.assignRole', 'account.delete')
WHERE r.role_name = 'ADMIN';
