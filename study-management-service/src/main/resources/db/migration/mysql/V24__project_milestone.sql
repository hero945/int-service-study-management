CREATE TABLE hd_plt_project_milestone (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Project里程碑主键',
    project_id BIGINT UNSIGNED NOT NULL COMMENT 'Project ID',
    stage_code VARCHAR(64) NOT NULL COMMENT 'Java定义的阶段编码',
    milestone_code VARCHAR(100) NOT NULL COMMENT 'Java定义的里程碑节点编码',
    plan_v1_date DATE NULL COMMENT 'V1.0计划日期',
    plan_v2_date DATE NULL COMMENT 'V2.0计划日期',
    actual_start_date DATE NULL COMMENT '实际开始日期',
    actual_end_date DATE NULL COMMENT '实际结束日期',
    deviation_note TEXT NULL COMMENT '提前、延迟或例外说明',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_project_milestone (project_id, milestone_code),
    KEY idx_hd_plt_project_milestone_stage (project_id, stage_code, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Project维度监管里程碑节点数据';

-- 项目监管里程碑权限
INSERT IGNORE INTO hd_plt_permission(
    module_code, permission_code, permission_name, permission_type, action_code,
    status_code, sort_order, sys_create_by, sys_update_by)
VALUES
    ('milestone', 'project.milestone.read', '查看项目监管里程碑', 'DATA', 'read', 'ACTIVE', 90, 'system', 'system'),
    ('milestone', 'project.milestone.update', '修改项目监管里程碑', 'DATA', 'update', 'ACTIVE', 100, 'system', 'system');

-- 授权给 ADMIN / USER
INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code = 'project.milestone.read'
WHERE r.role_name IN ('ADMIN', 'USER') AND r.sys_deleted = 0 AND p.sys_deleted = 0;

INSERT IGNORE INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
SELECT r.id, p.id, 'system', 'system'
FROM hd_plt_role r
JOIN hd_plt_permission p ON p.permission_code = 'project.milestone.update'
WHERE r.role_name IN ('ADMIN', 'USER') AND r.sys_deleted = 0 AND p.sys_deleted = 0;
