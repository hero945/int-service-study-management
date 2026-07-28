CREATE TABLE hd_plt_risk_status_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '风险状态变更历史主键',
    risk_id BIGINT UNSIGNED NOT NULL COMMENT '风险ID',
    from_status VARCHAR(20) NOT NULL COMMENT '变更前状态',
    to_status VARCHAR(20) NOT NULL COMMENT '变更后状态',
    reason VARCHAR(2000) NOT NULL COMMENT '变更原因',
    changed_by VARCHAR(254) NOT NULL COMMENT '操作人邮箱',
    changed_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_hd_plt_risk_status_history_risk (risk_id, changed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风险开闭状态变更历史';

CREATE TABLE hd_plt_risk_action_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '风险措施变更历史主键',
    action_id BIGINT UNSIGNED NOT NULL COMMENT '措施ID',
    risk_id BIGINT UNSIGNED NOT NULL COMMENT '风险ID',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型：CREATE/UPDATE/DELETE/REOPEN',
    from_status VARCHAR(20) NULL COMMENT '变更前措施状态',
    to_status VARCHAR(20) NULL COMMENT '变更后措施状态',
    snapshot_json TEXT NULL COMMENT '变更后字段摘要 JSON',
    reason VARCHAR(2000) NULL COMMENT '变更原因（重开等）',
    changed_by VARCHAR(254) NOT NULL COMMENT '操作人邮箱',
    changed_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_hd_plt_risk_action_history_risk (risk_id, changed_time),
    KEY idx_hd_plt_risk_action_history_action (action_id, changed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风险控制措施变更历史';
