# MyBatis-Plus 迁移执行清单

> 状态：已完成，并在2026-07-21继续切换到 `hd_plt_*` 目标模型；后续验证以
> `docs/验证记录_v1.0.md` 为准。

## Phase 0：基线

- [x] 运行并记录 `mvn test`
- [x] 运行并记录 `node --test tests\backend-module-boundaries.test.js`
- [x] 固化三个 JDBC Repository 的排序、上限、空结果、重复键和更新语义

## Phase 1：依赖与装配

- [x] 在父 POM 锁定实施时确认的 MyBatis-Plus 版本
- [x] Repository 模块引入 Spring Boot 3 starter
- [x] 使用分页时显式引入 `mybatis-plus-jsqlparser`
- [x] 检查依赖树，排除重复 MyBatis/MyBatis-Spring 版本
- [x] 增加 Mapper 扫描和 MySQL 分页配置
- [x] 验证 Flyway、Spring Session 和测试上下文仍可启动

## Phase 2：逐端口迁移

- [x] 建立 `SystemSettingEntity` 和 `SystemSettingMapper`
- [x] 用 MyBatis-Plus 适配器替换 `JdbcSettingRepository`
- [x] 验证 Setting 筛选、排序、100 条上限和更新语义
- [x] 建立 `UserAccountEntity` 和 `UserAccountMapper`
- [x] 用 MyBatis-Plus 适配器替换 `JdbcUserAccountRepository`
- [x] 验证登录查询、列表排序、500 条上限和创建用户
- [x] 建立 `StudyEntity` 和 `StudyMapper`
- [x] 用 MyBatis-Plus 适配器替换 `JdbcStudyRepository`
- [x] 验证列表排序、500 条上限、计数、创建和重复 code 异常
- [x] 确认 Repository 模块业务代码不再使用 `JdbcClient`

## Phase 3：边界与回归

- [x] 增加 MyBatis-Plus 不得进入 Domain/Manager/API 的边界检查
- [x] 补齐 Repository 行为测试
- [x] 运行 `mvn test`
- [x] 运行 `node --test tests\backend-module-boundaries.test.js`
- [ ] 在隔离的 MySQL 8.4 上执行 Flyway 和应用启动
- [ ] 对登录、账号、Study、概览和 Setting 执行 HTTP 冒烟验证
- [ ] 检查重复键、时间字段、分页 SQL和稳定排序
- [x] 检查最终 `git diff`，确认未覆盖用户现有无关改动

> 2026-07-21 验证边界：本机 `MySQL80` 服务正在运行，但项目默认
> `study_app` 账号连接被拒绝，且本机无 Docker 命令；因此真实 MySQL 8.4
> 启动与 HTTP 冒烟仍未完成，不能以 H2 测试替代。

## Phase 4：后续独立评审

- [ ] 评审 API 级分页契约
- [ ] 按业务并发风险评审基于 `sys_update_time`、状态条件或专项字段的并发控制方案
- [ ] 评审并分业务切片迁移 25 张 `hd_plt_*` 目标表
- [ ] 评审后端数据范围过滤与 SQL 注入防护
- [ ] 评审慢查询指标、执行计划和索引治理
