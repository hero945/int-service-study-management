INSERT INTO hd_plt_role(
    role_name, role_description, data_scope_mode, status_code, is_system_role,
    sys_create_by, sys_update_by)
VALUES
    ('ADMIN', 'System administrator', 'ALL', 'ACTIVE', 1, 'system', 'system'),
    ('USER', 'Business member', 'ALL', 'ACTIVE', 1, 'system', 'system'),
    ('VIEWER', 'Read-only member', 'ALL', 'ACTIVE', 1, 'system', 'system');

INSERT INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('pipeline', 'pipeline.page.view', 'View pipeline page', 'PAGE', 'view', 'ACTIVE', 10, 'system', 'system'),
    ('study', 'study.read', 'Read studies', 'OPERATION', 'read', 'ACTIVE', 20, 'system', 'system'),
    ('config', 'config.create', 'Create pipeline configuration', 'OPERATION', 'create', 'ACTIVE', 30, 'system', 'system'),
    ('account', 'account.page.view', 'View accounts', 'PAGE', 'view', 'ACTIVE', 40, 'system', 'system'),
    ('account', 'account.create', 'Create accounts', 'OPERATION', 'create', 'ACTIVE', 50, 'system', 'system'),
    ('setting', 'platform.setting.read', 'Read platform settings', 'OPERATION', 'read', 'ACTIVE', 60, 'system', 'system'),
    ('setting', 'platform.setting.update', 'Update platform settings', 'OPERATION', 'update', 'ACTIVE', 70, 'system', 'system');

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system' FROM hd_plt_role r CROSS JOIN hd_plt_permission p
WHERE r.role_name = 'ADMIN';
INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system' FROM hd_plt_role r JOIN hd_plt_permission p
ON p.permission_code IN ('pipeline.page.view', 'study.read', 'config.create')
WHERE r.role_name = 'USER';
INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system' FROM hd_plt_role r JOIN hd_plt_permission p
ON p.permission_code IN ('pipeline.page.view', 'study.read')
WHERE r.role_name = 'VIEWER';

INSERT INTO hd_plt_system_setting(
    config_key, config_value, value_type, config_description, public_visible,
    sys_create_by, sys_update_by)
VALUES ('platform.display-name', '临床研发管理平台', 'STRING', '平台显示名称', 1, 'system', 'system');
