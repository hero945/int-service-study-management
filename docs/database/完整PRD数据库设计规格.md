# 完整 PRD 数据库设计规格

## 1. 目标

为临床研发管线管理系统完整 PRD 提供一套从空库创建的 MySQL 8.x 数据模型。数据库覆盖账号权限、基础配置、Program、Project、Study、团队、里程碑、月报、风险和审计。

本设计已作为正式 Flyway V1 基线，不包含页面整库导入、预生成导出文件或独立人员档案。

当前状态：本脚本已在本机MySQL 8.0.46的空 `study_management` 库创建25张表并完成
结构核验，且已成为 Java 运行时使用的 Flyway V1 基线；MyBatis-Plus Repository 和
Spring Session 表名适配均已完成。

## 2. 已确认的建模规则

- 所有表使用英文小写蛇形名称，前缀统一为 `hd_plt_`。
- 数据库使用 InnoDB、`utf8mb4` 和 `utf8mb4_0900_ai_ci`。
- 业务主键使用 `BIGINT UNSIGNED AUTO_INCREMENT`。
- 业务、配置和关系表包含：
  - `sys_create_by VARCHAR(254)`
  - `sys_update_by VARCHAR(254)`
  - `sys_create_time DATETIME(6)`
  - `sys_update_time DATETIME(6)`
  - `sys_deleted TINYINT`
- `sys_create_by`、`sys_update_by` 保存小写邮箱。
- 逻辑删除只保存 `sys_deleted`；删除人、删除时间和前后值写入审计日志。
- 审计、Session、登录尝试和密码重置令牌等技术表允许使用专用生命周期字段。
- 系统按单组织建设，不建立租户表，不在业务表保存 `tenant_id`。
- 邮箱是账号唯一标识；数据库关系使用稳定的内部 `user_id`。
- 按已确认要求，建表语句不使用 `CONSTRAINT`、`FOREIGN KEY` 或 `CHECK`。
- 跨表ID存在性、状态编码、日期先后和业务归属关系由Java在事务中校验。
- 删除后的业务编码不允许被新记录复用；恢复时恢复原记录。

## 3. 权限与数据范围

- 一个用户可以拥有多个角色。
- 角色权限决定用户能执行的动作。
- 不支持用户单独追加权限、禁用权限或手工配置数据范围。
- 角色数据范围只有：
  - `ALL`：查看全部数据。
  - `ASSIGNED_STUDY`：只能查看团队矩阵中分配给自己的 Study 及其关联数据。
- `hd_plt_team_assignment` 同时表达团队分工和 Study 可见范围，不建立任务表。
- 团队分配创建后立即生效；移除时逻辑删除，不使用生效起止日期。
- 团队成员、风险 Owner 和风险措施责任人必须来自账号表。

## 4. 管线主数据

核心关系：

```text
Product 1 -- 1 Program
Program 1 -- N Project
Project 1 -- 1 Indication text
Project 1 -- N Study
```

- Product 不单独建表，产品、MOA、来源和国内外属性保存在 Program。
- 来源和国内外属性使用Java固定枚举编码，不建立配置表。
- 适应症不建立配置表；Project直接保存必填的适应症文本描述。
- Program、Project、Study 均有全局唯一、创建后不可修改的业务编码。
- `study_code` 对应页面中的 Study No.
- Phase Status 使用 Java 固定编码和映射，不建立配置表。
- Project 状态不落库、不建立配置表，由 Java 根据 Project 下属 Study 的阶段、里程碑和风险数据实时汇总计算。
- Study 保存 Program、Project、TA、适应症、产品等筛选字段的快照。
- 上级主数据修改后不自动更新已有 Study 快照。

## 5. 里程碑

- 阶段、节点、顺序和 Phase Status 映射由 Java 代码维护。
- 只建立 `hd_plt_study_milestone`。
- 一条记录表示一个 Study 的一个里程碑节点。
- 固定保存 V1、V2 计划日期、实际开始、实际结束和偏差说明。
- 节点状态由实际日期实时计算，不落库。

## 6. 月报

- `hd_plt_monthly_report` 表示一个 Study 在某月的一个应填功能线。
- 首次生成某月月报时，按当时团队功能线生成快照。
- 后续团队或功能线变化不修改历史月报快照和完成率。
- `hd_plt_monthly_report_entry` 保存当月多次独立进展汇报。
- 不需要提交、审核、退回或批准流程。
- 至少存在一条未删除且非空的进展明细时，该功能线计为已填写。

## 7. 风险

- 风险必须关联 Study、功能线和作为 Owner 的 Study 团队成员。
- 风险列表在主表保存最新分数和等级，历史评估保存在评估表。
- 风险评分为影响程度、发生可能性和可探测性的乘积。
- 风险阈值由管理员维护并版本化；规则变化不改写历史评估。
- 重新评估新增记录，不覆盖历史记录。
- 一条风险可以有多条独立措施。
- 风险关闭、重开和重新评估均写审计日志。

## 8. 导出、备份与审计

- 导出实时查询数据库生成，不预生成或存储导出文件。
- 导出操作写审计日志。
- 数据库备份使用 MySQL 全量备份、binlog 和恢复演练，不提供页面整库导入覆盖。
- `hd_plt_audit_log` 只追加，不修改、不逻辑删除。
- 密码只保存安全哈希；密码重置令牌只保存令牌哈希。
- Session 使用 Spring Session JDBC 并通过配置使用 `hd_plt_` 表名。

## 9. 表清单

### 认证与平台

- `hd_plt_user`
- `hd_plt_role`
- `hd_plt_permission`
- `hd_plt_user_role`
- `hd_plt_role_permission`
- `hd_plt_password_reset_token`
- `hd_plt_login_attempt`
- `hd_plt_spring_session`
- `hd_plt_spring_session_attributes`
- `hd_plt_system_setting`
- `hd_plt_audit_log`

### 基础配置与管线

- `hd_plt_therapeutic_area`
- `hd_plt_function_line`
- `hd_plt_team_role`
- `hd_plt_program`
- `hd_plt_project`
- `hd_plt_study`

### 业务明细

- `hd_plt_team_assignment`
- `hd_plt_study_milestone`
- `hd_plt_monthly_report`
- `hd_plt_monthly_report_entry`
- `hd_plt_risk_rule_version`
- `hd_plt_risk`
- `hd_plt_risk_assessment`
- `hd_plt_risk_action`

## 10. 验收标准

- 空 MySQL 8.x 数据库可按文件顺序创建全部 25 张表。
- 所有表名以 `hd_plt_` 开头。
- SQL中不存在 `tenant_id`、租户表、`CONSTRAINT`、`FOREIGN KEY` 或 `CHECK`。
- 邮箱、Program编码、Project编码、Study编码和风险编码具有明确唯一约束。
- 密码、Session token 和密码重置原始令牌不以明文保存。
- 月报支持同一功能线同月多条独立进展。
- 风险支持多次评估和多条措施。
- Study快照字段不随上级主数据更新。
- SQL 不包含真实账号、密码、令牌或生产连接信息。
