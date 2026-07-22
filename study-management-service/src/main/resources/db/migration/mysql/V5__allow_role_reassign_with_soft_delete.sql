-- 修复“分配角色”时报 Duplicate entry 的问题：
-- 原唯一约束 UNIQUE(user_id, role_id) 不包含 sys_deleted，软删除（sys_deleted=1）
-- 后再次分配同一角色会插入 (user_id, role_id, sys_deleted=0)，与已软删除行唯一键冲突。
-- 将唯一约束纳入 sys_deleted，使同一 (user, role) 在软删除后可重新分配为有效行。
ALTER TABLE hd_plt_user_role DROP INDEX uk_hd_plt_user_role;
ALTER TABLE hd_plt_user_role ADD UNIQUE KEY uk_hd_plt_user_role (user_id, role_id, sys_deleted);
