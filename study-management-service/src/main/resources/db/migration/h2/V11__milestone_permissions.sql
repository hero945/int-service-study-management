-- 里程碑编辑权限：注册 milestone.update 并授权给 ADMIN / USER（H2 测试库）
-- VIEWER 保持只读，与 risk 模块的角色授权策略一致。
INSERT INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('milestone', 'milestone.update', '修改里程碑', 'DATA', 'update', 'ACTIVE', 80, 'system', 'system');

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system' FROM hd_plt_role r JOIN hd_plt_permission p ON
  p.permission_code = 'milestone.update'
WHERE r.role_name = 'ADMIN';

INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system' FROM hd_plt_role r JOIN hd_plt_permission p ON
  p.permission_code = 'milestone.update'
WHERE r.role_name = 'USER';
