-- 里程碑查询权限：注册 milestone.read（H2 测试库）
-- 角色策略与 mysql V18 一致：ADMIN/USER 有 read+update，VIEWER 仅 read。
INSERT INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('milestone', 'milestone.read', '查询里程碑', 'DATA', 'read', 'ACTIVE', 79, 'system', 'system');

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r JOIN hd_plt_permission p ON
  p.permission_code = 'milestone.read'
WHERE r.role_name = 'ADMIN';

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r JOIN hd_plt_permission p ON
  p.permission_code = 'milestone.read'
WHERE r.role_name = 'USER';

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r JOIN hd_plt_permission p ON
  p.permission_code = 'milestone.read'
WHERE r.role_name = 'VIEWER';
