-- 临床研发管线管理系统完整 PRD 建表语句
-- 目标版本：MySQL 8.x
-- 使用方式：先创建并选择目标数据库，再执行本文件。
-- 注意：本文件是全新空库目标结构，不兼容当前试验性 V1__baseline.sql。
-- 按已确认口径：单组织、不含租户字段、不使用数据库关系及检查约束；
-- 跨表关系、状态编码和日期规则由Java服务校验。

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE TABLE hd_plt_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户内部主键',
    email VARCHAR(254) NOT NULL COMMENT '登录邮箱，应用层统一转为小写',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码单向哈希，禁止保存明文',
    display_name VARCHAR(100) NOT NULL COMMENT '姓名',
    status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    COMMENT '状态：PENDING/ACTIVE/LOCKED/DISABLED',
    must_change_password TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '下次登录必须修改密码',
    failed_login_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME(6) NULL COMMENT '临时锁定截止时间',
    password_changed_time DATETIME(6) NULL COMMENT '最近密码修改时间',
    last_login_time DATETIME(6) NULL COMMENT '最近成功登录时间',
    security_stamp CHAR(36) NOT NULL COMMENT '安全版本标识，改密或停用时更新以撤销会话',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_user_email (email),
    KEY idx_hd_plt_user_status (status_code, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台账号';

CREATE TABLE hd_plt_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色内部主键',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_description VARCHAR(500) NULL COMMENT '角色说明',
    data_scope_mode VARCHAR(32) NOT NULL DEFAULT 'ASSIGNED_STUDY'
    COMMENT '数据范围：ALL/ASSIGNED_STUDY',
    status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    is_system_role TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否系统预置角色',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

CREATE TABLE hd_plt_permission (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限内部主键',
    module_code VARCHAR(64) NOT NULL COMMENT '模块编码',
    permission_code VARCHAR(128) NOT NULL COMMENT '稳定权限编码，如risk.update',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：PAGE/OPERATION/DATA',
    action_code VARCHAR(32) NOT NULL COMMENT '动作编码，如view/read/create/update/delete/export',
    permission_description VARCHAR(500) NULL COMMENT '权限说明',
    status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_permission_code (permission_code),
    KEY idx_hd_plt_permission_tree (module_code, permission_type, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限字典';

CREATE TABLE hd_plt_user_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户角色关系主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_user_role (user_id, role_id),
    KEY idx_hd_plt_user_role_role (role_id, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系';

CREATE TABLE hd_plt_role_permission (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色权限关系主键',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_role_permission (role_id, permission_id),
    KEY idx_hd_plt_role_permission_permission (permission_id, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关系';

CREATE TABLE hd_plt_password_reset_token (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '重置记录主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    token_hash CHAR(64) NOT NULL COMMENT '一次性令牌SHA-256哈希，禁止保存原始令牌',
    expires_time DATETIME(6) NOT NULL COMMENT '过期时间',
    used_time DATETIME(6) NULL COMMENT '使用时间',
    revoked_time DATETIME(6) NULL COMMENT '撤销时间',
    create_ip VARCHAR(45) NULL COMMENT '申请IP',
    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_password_reset_hash (token_hash),
    KEY idx_hd_plt_password_reset_user (user_id, expires_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='密码重置令牌';

CREATE TABLE hd_plt_login_attempt (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '登录尝试主键',
    user_id BIGINT UNSIGNED NULL COMMENT '已识别用户ID',
    login_email VARCHAR(254) NOT NULL COMMENT '本次登录邮箱',
    ip_address VARCHAR(45) NOT NULL COMMENT '客户端IP',
    user_agent VARCHAR(500) NULL COMMENT '客户端User-Agent',
    result_code VARCHAR(20) NOT NULL COMMENT '结果：SUCCESS/FAILED/LOCKED',
    failure_reason_code VARCHAR(64) NULL COMMENT '失败原因编码，不记录密码',
    attempt_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '尝试时间',
    PRIMARY KEY (id),
    KEY idx_hd_plt_login_attempt_email (login_email, attempt_time),
    KEY idx_hd_plt_login_attempt_ip (ip_address, attempt_time),
    KEY idx_hd_plt_login_attempt_user (user_id, attempt_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录尝试记录';

CREATE TABLE hd_plt_spring_session (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(254) NULL,
    PRIMARY KEY (PRIMARY_ID),
    UNIQUE KEY uk_hd_plt_spring_session_id (SESSION_ID),
    KEY idx_hd_plt_spring_session_expiry (EXPIRY_TIME),
    KEY idx_hd_plt_spring_session_principal (PRINCIPAL_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Spring Session';

CREATE TABLE hd_plt_spring_session_attributes (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Spring Session属性';

CREATE TABLE hd_plt_system_setting (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置主键',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '值类型：STRING/NUMBER/BOOLEAN/JSON',
    config_description VARCHAR(500) NULL COMMENT '配置说明',
    public_visible TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否允许未登录页面读取',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_system_setting_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

CREATE TABLE hd_plt_therapeutic_area (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '治疗领域主键',
    area_code VARCHAR(64) NOT NULL COMMENT '治疗领域稳定编码',
    area_name VARCHAR(100) NOT NULL COMMENT '治疗领域名称',
    english_name VARCHAR(200) NULL COMMENT '英文名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_therapeutic_area_code (area_code),
    KEY idx_hd_plt_therapeutic_area_order (status_code, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='TA治疗领域';

CREATE TABLE hd_plt_function_line (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '功能线主键',
    function_code VARCHAR(64) NOT NULL COMMENT '功能线稳定编码',
    function_name VARCHAR(100) NOT NULL COMMENT '功能线名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_function_line_code (function_code),
    KEY idx_hd_plt_function_line_order (status_code, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='功能线';

CREATE TABLE hd_plt_team_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '团队角色主键',
    role_code VARCHAR(64) NOT NULL COMMENT '团队角色稳定编码',
    role_name VARCHAR(100) NOT NULL COMMENT '团队角色名称',
    function_line_id BIGINT UNSIGNED NULL COMMENT '所属功能线ID',
    is_project_lead TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否项目负责人角色',
    can_manage_team TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否可维护团队矩阵',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_team_role_code (role_code),
    KEY idx_hd_plt_team_role_function (function_line_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Study团队角色';

CREATE TABLE hd_plt_program (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Program内部主键',
    program_code VARCHAR(64) NOT NULL COMMENT 'Program稳定业务编码，创建后不可修改',
    program_name VARCHAR(200) NOT NULL COMMENT 'Program名称',
    product_name VARCHAR(200) NOT NULL COMMENT '产品或化合物名称，一个产品对应一个Program',
    moa VARCHAR(500) NULL COMMENT 'MOA作用机制',
    source_code VARCHAR(32) NULL
    COMMENT '来源编码：SELF_DEVELOPED/IN_LICENSE/COOPERATION，由Java映射显示名称',
    origin_code VARCHAR(32) NULL
    COMMENT '国内外编码：DOMESTIC/IMPORTED，由Java映射显示名称',
    status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_program_code (program_code),
    UNIQUE KEY uk_hd_plt_program_product (product_name),
    KEY idx_hd_plt_program_status (status_code, sort_order),
    KEY idx_hd_plt_program_source (source_code, sys_deleted),
    KEY idx_hd_plt_program_origin (origin_code, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Program项目集及产品';

CREATE TABLE hd_plt_project (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Project内部主键',
    project_code VARCHAR(64) NOT NULL COMMENT 'Project稳定业务编码，创建后不可修改',
    project_name VARCHAR(200) NOT NULL COMMENT 'Project名称',
    program_id BIGINT UNSIGNED NOT NULL COMMENT '所属Program ID',
    indication_description VARCHAR(500) NOT NULL COMMENT '适应症文本描述，不做配置化',
    therapeutic_area_id BIGINT UNSIGNED NOT NULL COMMENT 'TA治疗领域ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '同TA内管线展示顺序',
    project_description TEXT NULL COMMENT '项目说明',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_project_code (project_code),
    KEY idx_hd_plt_project_program (program_id, sys_deleted),
    KEY idx_hd_plt_project_indication (indication_description(191), sys_deleted),
    KEY idx_hd_plt_project_area_order (therapeutic_area_id, sort_order, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Project项目';

CREATE TABLE hd_plt_study (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Study内部主键',
    study_code VARCHAR(64) NOT NULL COMMENT 'Study No.稳定业务编码，创建后不可修改',
    study_name VARCHAR(200) NOT NULL COMMENT 'Study名称',
    phase_status_code VARCHAR(32) NULL
    COMMENT 'Java固定映射的Phase Status编码，如PHASE_2',
    planned_start_date DATE NULL COMMENT '计划开始日期',
    planned_end_date DATE NULL COMMENT '计划结束日期',
    actual_start_date DATE NULL COMMENT '实际开始日期',
    actual_end_date DATE NULL COMMENT '实际结束日期',
    study_description TEXT NULL COMMENT 'Study说明',
    program_id BIGINT UNSIGNED NOT NULL COMMENT 'Program ID',
    program_code_snapshot VARCHAR(64) NOT NULL COMMENT '创建Study时的Program编码快照',
    program_name_snapshot VARCHAR(200) NOT NULL COMMENT '创建Study时的Program名称快照',
    product_name_snapshot VARCHAR(200) NOT NULL COMMENT '创建Study时的产品名称快照',
    moa_snapshot VARCHAR(500) NULL COMMENT '创建Study时的MOA快照',
    source_code_snapshot VARCHAR(32) NULL COMMENT '创建Study时的来源编码快照',
    origin_code_snapshot VARCHAR(32) NULL COMMENT '创建Study时的国内外编码快照',
    project_id BIGINT UNSIGNED NOT NULL COMMENT 'Project ID',
    project_code_snapshot VARCHAR(64) NOT NULL COMMENT '创建Study时的Project编码快照',
    project_name_snapshot VARCHAR(200) NOT NULL COMMENT '创建Study时的Project名称快照',
    therapeutic_area_id BIGINT UNSIGNED NOT NULL COMMENT 'TA治疗领域ID',
    therapeutic_area_code_snapshot VARCHAR(64) NOT NULL COMMENT 'TA编码快照',
    therapeutic_area_name_snapshot VARCHAR(100) NOT NULL COMMENT 'TA名称快照',
    indication_description_snapshot VARCHAR(500) NOT NULL COMMENT '创建Study时的适应症文本快照',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_study_code (study_code),
    KEY idx_hd_plt_study_program (program_id, sys_deleted),
    KEY idx_hd_plt_study_project (project_id, sys_deleted),
    KEY idx_hd_plt_study_area (therapeutic_area_id, sys_deleted),
    KEY idx_hd_plt_study_indication (indication_description_snapshot(191), sys_deleted),
    KEY idx_hd_plt_study_source (source_code_snapshot, sys_deleted),
    KEY idx_hd_plt_study_origin (origin_code_snapshot, sys_deleted),
    KEY idx_hd_plt_study_phase (phase_status_code, sys_deleted),
    KEY idx_hd_plt_study_program_name (program_name_snapshot, sys_deleted),
    KEY idx_hd_plt_study_product_name (product_name_snapshot, sys_deleted),
    KEY idx_hd_plt_study_project_name (project_name_snapshot, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Study研究及筛选快照';

CREATE TABLE hd_plt_team_assignment (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '团队分配主键',
    study_id BIGINT UNSIGNED NOT NULL COMMENT 'Study ID，同时决定数据可见范围',
    team_role_id BIGINT UNSIGNED NOT NULL COMMENT 'Study团队角色ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '团队成员账号ID',
    function_line_id BIGINT UNSIGNED NULL COMMENT '分配时团队角色对应功能线ID',
    team_role_code_snapshot VARCHAR(64) NOT NULL COMMENT '团队角色编码快照',
    team_role_name_snapshot VARCHAR(100) NOT NULL COMMENT '团队角色名称快照',
    function_line_code_snapshot VARCHAR(64) NULL COMMENT '功能线编码快照',
    function_line_name_snapshot VARCHAR(100) NULL COMMENT '功能线名称快照',
    user_email_snapshot VARCHAR(254) NOT NULL COMMENT '成员邮箱快照',
    user_name_snapshot VARCHAR(100) NOT NULL COMMENT '成员姓名快照',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_team_assignment (study_id, team_role_id, user_id),
    KEY idx_hd_plt_team_assignment_user (user_id, sys_deleted, study_id),
    KEY idx_hd_plt_team_assignment_study (study_id, sys_deleted),
    KEY idx_hd_plt_team_assignment_role (team_role_id, sys_deleted),
    KEY idx_hd_plt_team_assignment_function (study_id, function_line_id, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Study团队分配及数据范围';

CREATE TABLE hd_plt_study_milestone (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Study里程碑主键',
    study_id BIGINT UNSIGNED NOT NULL COMMENT 'Study ID',
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
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_study_milestone (study_id, milestone_code),
    KEY idx_hd_plt_study_milestone_stage (study_id, stage_code, sys_deleted),
    KEY idx_hd_plt_study_milestone_update (study_id, sys_update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Study里程碑节点数据';

CREATE TABLE hd_plt_monthly_report (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '月报应填项主键',
    study_id BIGINT UNSIGNED NOT NULL COMMENT 'Study ID',
    report_month DATE NOT NULL COMMENT '报告月份，固定保存当月1日',
    function_line_id BIGINT UNSIGNED NOT NULL COMMENT '应填功能线ID',
    function_line_code_snapshot VARCHAR(64) NOT NULL COMMENT '生成月份时的功能线编码快照',
    function_line_name_snapshot VARCHAR(100) NOT NULL COMMENT '生成月份时的功能线名称快照',
    study_code_snapshot VARCHAR(64) NOT NULL COMMENT 'Study No.快照',
    program_code_snapshot VARCHAR(64) NOT NULL COMMENT 'Program编码快照',
    program_name_snapshot VARCHAR(200) NOT NULL COMMENT 'Program名称快照',
    product_name_snapshot VARCHAR(200) NOT NULL COMMENT '产品名称快照',
    project_code_snapshot VARCHAR(64) NOT NULL COMMENT 'Project编码快照',
    project_name_snapshot VARCHAR(200) NOT NULL COMMENT 'Project名称快照',
    therapeutic_area_code_snapshot VARCHAR(64) NOT NULL COMMENT 'TA编码快照',
    therapeutic_area_name_snapshot VARCHAR(100) NOT NULL COMMENT 'TA名称快照',
    indication_description_snapshot VARCHAR(500) NOT NULL COMMENT '生成月份时的适应症文本快照',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_monthly_report (study_id, report_month, function_line_id),
    KEY idx_hd_plt_monthly_report_month (report_month, sys_deleted),
    KEY idx_hd_plt_monthly_report_study_month (study_id, report_month, sys_deleted),
    KEY idx_hd_plt_monthly_report_function (function_line_id, report_month, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Study月度应填功能线快照';

CREATE TABLE hd_plt_monthly_report_entry (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '月报进展明细主键',
    monthly_report_id BIGINT UNSIGNED NOT NULL COMMENT '月报应填项ID',
    entry_date DATE NOT NULL COMMENT '本次进展对应日期',
    sequence_no INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '同一应填项内展示顺序',
    progress_content TEXT NOT NULL COMMENT '本次独立进展内容',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_monthly_report_entry_order
    (monthly_report_id, sequence_no),
    KEY idx_hd_plt_monthly_report_entry_date
    (monthly_report_id, entry_date, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='月报多次独立进展';

CREATE TABLE hd_plt_risk_rule_version (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '风险规则版本主键',
    version_no INT UNSIGNED NOT NULL COMMENT '规则版本号',
    version_name VARCHAR(100) NOT NULL COMMENT '规则版本名称',
    low_risk_max_score INT UNSIGNED NOT NULL COMMENT '低风险最高分',
    medium_risk_max_score INT UNSIGNED NOT NULL COMMENT '中风险最高分，高于此值为高危',
    status_code VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/ACTIVE/RETIRED',
    effective_from DATETIME(6) NULL COMMENT '生效时间',
    effective_to DATETIME(6) NULL COMMENT '失效时间',
    approved_by VARCHAR(254) NULL COMMENT '批准人邮箱',
    approved_time DATETIME(6) NULL COMMENT '批准时间',
    version_description VARCHAR(500) NULL COMMENT '版本说明',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_risk_rule_version (version_no),
    KEY idx_hd_plt_risk_rule_effective (status_code, effective_from, effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风险评分阈值版本';

CREATE TABLE hd_plt_risk (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '风险内部主键',
    risk_code VARCHAR(64) NOT NULL COMMENT '后端生成的稳定风险编号',
    study_id BIGINT UNSIGNED NOT NULL COMMENT '关联Study ID',
    study_code_snapshot VARCHAR(64) NOT NULL COMMENT 'Study No.快照',
    program_id BIGINT UNSIGNED NOT NULL COMMENT 'Program ID',
    program_code_snapshot VARCHAR(64) NOT NULL COMMENT 'Program编码快照',
    program_name_snapshot VARCHAR(200) NOT NULL COMMENT 'Program名称快照',
    project_id BIGINT UNSIGNED NOT NULL COMMENT 'Project ID',
    project_code_snapshot VARCHAR(64) NOT NULL COMMENT 'Project编码快照',
    project_name_snapshot VARCHAR(200) NOT NULL COMMENT 'Project名称快照',
    function_line_id BIGINT UNSIGNED NOT NULL COMMENT '风险归属功能线ID',
    function_line_code_snapshot VARCHAR(64) NOT NULL COMMENT '功能线编码快照',
    function_line_name_snapshot VARCHAR(100) NOT NULL COMMENT '功能线名称快照',
    owner_user_id BIGINT UNSIGNED NOT NULL COMMENT '风险Owner账号ID，必须为Study团队成员',
    owner_email_snapshot VARCHAR(254) NOT NULL COMMENT 'Owner邮箱快照',
    owner_name_snapshot VARCHAR(100) NOT NULL COMMENT 'Owner姓名快照',
    risk_description TEXT NOT NULL COMMENT '风险描述',
    registered_date DATE NOT NULL COMMENT '风险登记日期',
    status_code VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN/CLOSED',
    closed_time DATETIME(6) NULL COMMENT '最近关闭时间',
    close_reason TEXT NULL COMMENT '最近关闭原因',
    latest_assessment_id BIGINT UNSIGNED NULL COMMENT '最新风险评估ID',
    current_score INT UNSIGNED NULL COMMENT '最新风险总分，列表筛选冗余',
    current_level_code VARCHAR(20) NULL COMMENT '最新等级：LOW/MEDIUM/HIGH',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_risk_code (risk_code),
    KEY idx_hd_plt_risk_study_status (study_id, status_code, sys_deleted),
    KEY idx_hd_plt_risk_program (program_id, status_code, sys_deleted),
    KEY idx_hd_plt_risk_project (project_id, status_code, sys_deleted),
    KEY idx_hd_plt_risk_function_status (function_line_id, status_code, sys_deleted),
    KEY idx_hd_plt_risk_level (current_level_code, current_score, sys_deleted),
    KEY idx_hd_plt_risk_owner (owner_user_id, status_code, sys_deleted),
    KEY idx_hd_plt_risk_latest_assessment (latest_assessment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风险主记录及最新评分';

CREATE TABLE hd_plt_risk_assessment (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '风险评估主键',
    risk_id BIGINT UNSIGNED NOT NULL COMMENT '风险ID',
    assessment_no INT UNSIGNED NOT NULL COMMENT '该风险内评估序号',
    rule_version_id BIGINT UNSIGNED NOT NULL COMMENT '使用的风险规则版本ID',
    impact_score TINYINT UNSIGNED NOT NULL COMMENT '影响程度：1-5',
    likelihood_score TINYINT UNSIGNED NOT NULL COMMENT '发生可能性：1-5',
    detectability_score TINYINT UNSIGNED NOT NULL COMMENT '可探测性：1-5',
    total_score INT UNSIGNED NOT NULL COMMENT '三因子乘积：1-125',
    risk_level_code VARCHAR(20) NOT NULL COMMENT '等级快照：LOW/MEDIUM/HIGH',
    assessment_reason TEXT NULL COMMENT '首次评估或重新评估原因',
    assessed_by VARCHAR(254) NOT NULL COMMENT '评估人邮箱',
    assessed_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '评估时间',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hd_plt_risk_assessment_no (risk_id, assessment_no),
    KEY idx_hd_plt_risk_assessment_time (risk_id, assessed_time),
    KEY idx_hd_plt_risk_assessment_rule (rule_version_id, assessed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风险评估历史快照';

CREATE TABLE hd_plt_risk_action (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '风险措施主键',
    risk_id BIGINT UNSIGNED NOT NULL COMMENT '风险ID',
    action_type_code VARCHAR(32) NOT NULL DEFAULT 'CONTROL' COMMENT '措施类型编码',
    action_description TEXT NOT NULL COMMENT '措施内容',
    owner_user_id BIGINT UNSIGNED NOT NULL COMMENT '措施责任人账号ID，必须为Study团队成员',
    owner_email_snapshot VARCHAR(254) NOT NULL COMMENT '责任人邮箱快照',
    owner_name_snapshot VARCHAR(100) NOT NULL COMMENT '责任人姓名快照',
    planned_date DATE NULL COMMENT '计划完成日期',
    completed_date DATE NULL COMMENT '实际完成日期',
    status_code VARCHAR(20) NOT NULL DEFAULT 'OPEN'
    COMMENT '状态：OPEN/IN_PROGRESS/COMPLETED/CANCELLED',
    completion_note TEXT NULL COMMENT '完成或取消说明',
    sys_create_by VARCHAR(254) NOT NULL COMMENT '创建人邮箱',
    sys_update_by VARCHAR(254) NOT NULL COMMENT '更新人邮箱',
    sys_create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    sys_update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    sys_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    KEY idx_hd_plt_risk_action_status (risk_id, status_code, sys_deleted),
    KEY idx_hd_plt_risk_action_owner (owner_user_id, status_code, sys_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风险控制措施';

CREATE TABLE hd_plt_audit_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审计日志主键',
    operator_user_id BIGINT UNSIGNED NULL COMMENT '操作人用户ID',
    operator_email VARCHAR(254) NOT NULL COMMENT '操作人邮箱快照',
    action_code VARCHAR(64) NOT NULL COMMENT '动作编码',
    target_table VARCHAR(128) NOT NULL COMMENT '目标表名',
    target_id BIGINT UNSIGNED NULL COMMENT '目标记录ID',
    request_id VARCHAR(64) NULL COMMENT '请求链路ID',
    ip_address VARCHAR(45) NULL COMMENT '客户端IP',
    operation_reason VARCHAR(1000) NULL COMMENT '高影响操作原因',
    before_data JSON NULL COMMENT '变更前字段快照，禁止写入密码和令牌',
    after_data JSON NULL COMMENT '变更后字段快照，禁止写入密码和令牌',
    result_code VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '结果：SUCCESS/FAILED/DENIED',
    occurred_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '发生时间',
    PRIMARY KEY (id),
    KEY idx_hd_plt_audit_target (target_table, target_id, occurred_time),
    KEY idx_hd_plt_audit_operator (operator_user_id, occurred_time),
    KEY idx_hd_plt_audit_action (action_code, occurred_time),
    KEY idx_hd_plt_audit_request (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='只追加的关键操作审计日志';
