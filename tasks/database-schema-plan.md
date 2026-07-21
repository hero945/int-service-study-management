# Implementation Plan: 完整 PRD MySQL 建表语句

## 范围

根据 `docs/database/完整PRD数据库设计规格.md` 生成一份从空库执行的 MySQL 8 建表脚本，不替换现有 MVP Flyway 基线，不修改 Java 或前端。

## 顺序

1. 创建认证、角色权限和技术表。
2. 创建基础配置、Program、Project、Study及团队表。
3. 创建里程碑、月报、风险与审计表。
4. 静态检查表数、命名、通用字段、索引、唯一键和敏感数据。

初始化管理员、权限字典和风险规则属于下一步数据确认，不在本建表文件中写入未经确认的业务值。

## 验收

- 生成 `docs/database/hd_plt_full_schema.sql`。
- 25张表均使用 `hd_plt_` 前缀。
- 业务表具有已确认的系统字段和逻辑删除字段。
- 不包含租户表、`tenant_id`、`CONSTRAINT`、`FOREIGN KEY` 或 `CHECK`。
- 不覆盖现有 `V1__baseline.sql`。
- 在没有可用 MySQL 服务时明确报告未执行真实建库验证。
- 在本机MySQL 8.0.46空库实际创建25张表并核验表数、字符集及已移除结构。
- 同步PRD、技术设计、ADR、运行与运维文档，同时保留旧MVP/Flyway未适配边界。

## 风险

- 当前代码仍依赖试验性表结构；本次SQL是目标设计，不代表现有Java已完成适配。
- Spring Session 自定义表名前缀需要同步配置 `spring.session.jdbc.table-name`。
- Phase Status、里程碑定义和 Project 状态实时汇总计算由未来 Java 实现保证。
