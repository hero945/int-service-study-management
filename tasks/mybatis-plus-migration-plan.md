# MyBatis-Plus 数据访问迁移计划

> 状态：MyBatis-Plus迁移已完成；其中“保持旧表结构不变”的阶段性约束已被
> 2026-07-21确认的方案1取代。当前Java运行时以 `hd_plt_*` 和ADR-005为准。

## 1. 目标

将当前业务数据访问从 Spring `JdbcClient` 迁移到 MyBatis-Plus，同时保持现有
REST API、Manager 用例、Domain 模型和数据库结构的对外行为不变。

本次迁移后的边界如下：

```text
Service / Controller
        |
      Manager
        |
Domain Repository 端口
        |
Repository 适配器
        |
MyBatis-Plus BaseMapper + 持久化实体
        |
      MySQL
```

MyBatis-Plus 只存在于 `study-management-repository` 及其配置中。Domain、
Manager、API 和前端不得依赖 `BaseMapper`、`IPage`、`QueryWrapper`、
持久化实体或 MyBatis 注解。

## 2. 范围

### 本次迁移包含

- 为 Spring Boot 3 引入并锁定 MyBatis-Plus 依赖。
- 为 `plt_user`、`biz_study`、`plt_system_setting` 建立 Repository 内部持久化实体。
- 为三张业务表建立 `BaseMapper`。
- 用 MyBatis-Plus 重写以下 Domain 端口的 Repository 适配器：
  - `UserAccountRepository`
  - `StudyRepository`
  - `SettingRepository`
- 保持固定查询上限和排序语义：
  - Study：`updated_at DESC, id DESC`，最多 500 条。
  - User：`id ASC`，最多 500 条。
  - Setting：`config_key ASC`，最多 100 条。
- 保持数据库唯一约束到领域异常的转换，例如重复 Study 编号继续转换为
  `DuplicateStudyCodeException`。
- 使用现有 H2 MySQL 模式集成测试验证应用装配和行为，并增加真实 MySQL 验证门禁。
- 更新模块边界测试和运行文档，使 MyBatis-Plus 不会向上层泄漏。

### 本次迁移不包含

- 不修改 REST API 路径、请求或响应结构。
- 不修改现有 Flyway `V1__baseline.sql`，也不重命名现有运行表。
- 不在此次迁移中把 `biz_study` 切换到已确认的 25 张 `hd_plt_*` 目标表结构。
- 不引入 MyBatis-Plus ActiveRecord、通用 `IService`/`ServiceImpl` 或代码生成器。
- 不启用 MyBatis-Plus 自动建表；数据库结构仍由 Flyway 单独管理。
- 不替换 Spring Session JDBC。会话表属于 Spring Session 的基础设施持久化，
  继续由框架管理。
- 不在第一阶段新增软删除、租户、数据权限或乐观锁插件；这些能力必须基于明确业务规则
  和 Flyway 迁移单独实施。

## 3. 架构决策

### 3.1 依赖放置

- 在父 `pom.xml` 的 `dependencyManagement` 中导入 MyBatis-Plus BOM，统一锁定版本。
- 在 `study-management-repository/pom.xml` 中引入
  `mybatis-plus-spring-boot3-starter`。
- 如使用分页拦截器，按官方要求显式引入 `mybatis-plus-jsqlparser`。
- 不再单独引入原生 MyBatis starter，避免 MyBatis 依赖版本冲突。
- 迁移完成且确认无其他直接使用方后，从 Repository 模块移除
  `spring-boot-starter-jdbc`；Spring Session JDBC 和 Flyway 所需 JDBC 能力由各自依赖保留。

实施时先运行 `mvn dependency:tree`，确认实际只有一组 MyBatis/MyBatis-Spring 版本。
截至计划编写日，官方安装文档展示的 Spring Boot 3 starter 版本为 `3.5.17`；
真正实施前需再次核对官方发布信息并在父 POM 中显式锁定，不使用动态版本。

### 3.2 持久化实体与 Domain 模型分离

在 Repository 模块新增内部持久化实体：

- `UserAccountEntity` -> `plt_user`
- `StudyEntity` -> `biz_study`
- `SystemSettingEntity` -> `plt_system_setting`

实体只描述表字段和 MyBatis-Plus 映射，不承载领域规则。Repository 适配器负责：

```text
数据库行 <-> 持久化实体 <-> Domain record
```

`StudyStatus`、角色等值先以字符串字段落库，再在适配器中显式转换，避免 ORM 类型处理器
直接成为 Domain 契约。数据库生成的 `id`、`created_at`、`updated_at` 保持由数据库默认值
管理。

### 3.3 Mapper 与 Repository 适配器

每张表建立一个只在 Repository 模块可见的 Mapper：

- `UserAccountMapper extends BaseMapper<UserAccountEntity>`
- `StudyMapper extends BaseMapper<StudyEntity>`
- `SystemSettingMapper extends BaseMapper<SystemSettingEntity>`

现有 `Jdbc*Repository` 由 `MybatisPlus*Repository` 等价替换，仍实现 Domain 定义的端口。
简单 CRUD、计数和条件查询优先使用 `BaseMapper` 与 Lambda Wrapper。不得把客户端传入的
排序字段、列名或 SQL 片段传给 Wrapper 的动态列名或 `last()`。

列表查询使用 Repository 内部的 MyBatis-Plus 分页对象实现固定上限，但只向 Domain
返回 `List`。分页插件配置生产数据库类型为 MySQL，并设置最大单页条数 500；
MyBatis-Plus 的 `Page`/`IPage` 不得进入 Domain 或 API。

### 3.4 配置

在 Repository 模块提供 MyBatis-Plus 配置，由 Spring Boot 启动模块扫描：

- 明确扫描 Mapper 包，不扫描 Domain Repository 接口。
- 启用下划线到驼峰映射。
- 数据库主键策略为自增，并在实体主键上明确声明。
- 配置 `MybatisPlusInterceptor` 和 MySQL 分页拦截器，分页拦截器位于插件链最后。
- 不开启 SQL 标准输出，不在生产日志中打印参数、密码散列或其他敏感数据。
- 若使用 XML 自定义 SQL，资源路径使用多模块兼容的
  `classpath*:/mapper/**/*.xml`；没有复杂 SQL 时不为形式统一而新增 XML。

### 3.5 事务与异常

- Manager 继续拥有用例和事务边界；Repository 不复制业务流程。
- 保留 Spring 的数据访问异常翻译。
- Study 编号唯一约束冲突继续转换为 `DuplicateStudyCodeException`。
- 对更新配置等写操作检查受影响行数；不存在的 key 继续交由现有 Manager 语义处理，
  不在 ORM 层静默制造新配置。
- 不使用“先查是否存在、再插入”替代唯一约束，避免并发竞态。

### 3.6 Flyway 与 MyBatis-Plus 的职责

- Flyway：建表、改字段、加索引、数据修复和可审计版本升级。
- MyBatis-Plus：在既有表结构上执行参数化查询和写入。
- 禁止 ORM 启动时自动修改结构。
- H2 MySQL 模式只验证应用装配和大部分查询行为；上线前必须对 MySQL 8.4 执行真实验证，
  因为 H2 不能证明 MySQL 方言、时间精度、索引和唯一约束行为完全一致。

## 4. 实施阶段

### Phase 0：建立行为基线

**任务 0.1：锁定迁移前行为**

验收标准：

- 现有 `mvn test` 通过并记录结果。
- `node --test tests/backend-module-boundaries.test.js` 通过。
- 记录三类 Repository 的排序、数量上限、空结果、重复键和更新不存在记录的行为。

验证：

```powershell
mvn test
node --test tests\backend-module-boundaries.test.js
```

依赖：无。

可能涉及：仅测试或验证记录，不修改生产行为。

### Phase 1：依赖与最小装配

**任务 1.1：引入 MyBatis-Plus 依赖**

验收标准：

- 父 POM 显式锁定 MyBatis-Plus 版本。
- Repository 模块使用 Spring Boot 3 starter，未重复引入原生 MyBatis starter。
- `mvn dependency:tree` 中 MyBatis 与 MyBatis-Spring 无多版本冲突。

验证：

```powershell
mvn dependency:tree
mvn test
```

依赖：任务 0.1。

可能涉及：

- `pom.xml`
- `study-management-repository/pom.xml`

**任务 1.2：增加 Mapper 扫描和分页配置**

验收标准：

- Spring Boot 能发现 Repository 模块内 Mapper。
- MySQL 分页拦截器生效，最大单页 500，且未影响 Flyway 和 Spring Session。
- 测试 ApplicationContext 可成功启动。

验证：

```powershell
mvn -pl study-management-test -am test
```

依赖：任务 1.1。

可能涉及：

- Repository 模块中的 MyBatis-Plus 配置类
- `study-management-service/src/main/resources/application.yml`

### Checkpoint A：基础装配

- Maven 依赖树无冲突。
- 应用测试上下文启动成功。
- Flyway 基线与 Spring Session JDBC 未被改写。

### Phase 2：逐端口垂直迁移

**任务 2.1：迁移 Setting Repository**

先迁移读写最简单的系统配置表，用它验证实体、Mapper、分页和更新行数语义。

验收标准：

- 公共配置筛选、全部列表、按 key 查询和更新结果与 JDBC 实现一致。
- 排序和 100 条上限保持不变。
- 更新不存在的 key 不会插入新行。

验证：

```powershell
mvn -pl study-management-test -am test
```

依赖：Checkpoint A。

预计范围：3 至 5 个 Repository/测试文件。

**任务 2.2：迁移 UserAccount Repository**

验收标准：

- 登录按用户名查询、管理员列表和创建用户行为不变。
- 用户列表保持 `id ASC` 和 500 条上限。
- 密码散列不会进入日志或 API 响应。

验证：

```powershell
mvn -pl study-management-test -am test
```

依赖：任务 2.1。

预计范围：3 至 5 个 Repository/测试文件。

**任务 2.3：迁移 Study Repository**

验收标准：

- Study 列表、总数、按状态计数和创建行为不变。
- 排序仍为 `updated_at DESC, id DESC`，最多 500 条。
- 重复 `code` 仍转换为 `DuplicateStudyCodeException`。
- `StudyStatus` 的字符串映射在非法数据库值时快速失败，而不是返回错误状态。

验证：

```powershell
mvn -pl study-management-test -am test
```

依赖：任务 2.2。

预计范围：3 至 5 个 Repository/测试文件。

### Checkpoint B：等价迁移

- 三个 Domain Repository 端口均由 MyBatis-Plus 适配器实现。
- `rg "JdbcClient" study-management-repository` 无业务 Repository 命中。
- API、Manager 和 Domain 中没有 MyBatis-Plus 类型或注解。
- 现有认证、账号、Study、概览和配置集成测试全部通过。

### Phase 3：边界保护与真实数据库验证

**任务 3.1：加强模块边界测试**

验收标准：

- 自动检查 Domain、Manager 和 API 不依赖 `com.baomidou` 或 `org.apache.ibatis`。
- 自动检查 Repository 模块包含 MyBatis-Plus starter。
- 自动检查 Domain 不依赖 Repository 实现的原有规则仍通过。

验证：

```powershell
node --test tests\backend-module-boundaries.test.js
```

依赖：Checkpoint B。

**任务 3.2：补齐 Repository 行为测试**

至少覆盖：

- 空列表和空 Optional。
- 三类列表的稳定排序和最大条数。
- Setting 的 public 条件和更新不存在 key。
- Study 状态计数、数据库生成字段和重复 code。
- User 唯一用户名及认证查询。

验证：

```powershell
mvn test
```

依赖：Checkpoint B。

**任务 3.3：MySQL 8.4 端到端验证**

验收标准：

- 从空库执行 Flyway 后应用可启动。
- 账号创建/登录、Study 创建/列表/概览、Setting 查询/更新均通过真实 HTTP 请求。
- 重复键、时间字段、分页 SQL和排序在 MySQL 上符合预期。
- JAR 中使用非 mock 前端且健康探针正常。

验证命令根据本机环境二选一：

```powershell
docker compose config
docker compose build
docker compose up
```

或连接隔离的本地 MySQL 8.4 后运行应用和 HTTP 冒烟检查。没有实际执行时不得声称
MySQL 验证通过。

依赖：任务 3.1、3.2。

### Checkpoint C：迁移完成

```powershell
mvn test
node --test tests\backend-module-boundaries.test.js
```

并确认：

- 真实 MySQL 8.4 冒烟验证通过。
- `git diff` 只包含迁移所需文件。
- 未修改 `frontend/dist`、历史原型、完整 PRD 表结构或其他无关用户改动。

### Phase 4：可选后续能力，不与等价迁移捆绑

只有在 Checkpoint C 完成后再分别评审：

1. API 级分页：先在 API/Domain 定义与 MyBatis-Plus 无关的分页契约，再由 Repository
   内部适配 `IPage`。
2. 并发控制：已确认的目标表没有通用 `version` 字段；如后续出现并发覆盖风险，应先基于
   `sys_update_time`、业务状态条件或专项设计确定策略，再决定是否调整表结构，不默认启用
   `OptimisticLockerInnerInterceptor`。
3. 完整 PRD 表映射：按 25 张 `hd_plt_*` 目标表分业务切片迁移，不与 ORM 替换混做。
4. 数据权限：权限过滤必须由后端用例生成可信查询条件，不能让前端传 SQL 字段或 Wrapper。
5. 慢查询治理：以 MySQL 执行计划、索引和指标为依据，不依赖生产 SQL 全量日志。

## 5. 风险与缓解

| 风险 | 影响 | 缓解 |
|---|---|---|
| Spring Boot、MyBatis-Plus、MyBatis-Spring 版本不兼容 | 应用无法启动或运行时报方法缺失 | 使用官方 Boot 3 starter、BOM 锁版，并检查依赖树 |
| ORM 类型泄漏到 Domain/API | 模块边界被持久化框架绑死 | 独立 Entity/Mapper/Adapter，增加静态边界测试 |
| H2 通过但 MySQL 失败 | 上线后才暴露方言或约束差异 | 保留 H2 快速测试，增加 MySQL 8.4 强制验证 |
| 列表迁移后失去稳定排序或数量上限 | 页面顺序变化、数据量失控 | 为每个端口写明确的排序和上限测试 |
| 数据库默认时间字段被 ORM 写成 null | 插入失败或审计时间错误 | 明确插入策略，由数据库生成字段并做回读验证 |
| Wrapper 接收动态 SQL 片段 | SQL 注入或越权排序 | 只使用 Lambda 字段引用和服务端白名单，不接受客户端列名/SQL |
| 与完整 PRD 数据库设计同时推进 | 难以判断错误来自 ORM 还是表结构 | 先对当前运行表等价迁移，完整表映射另立任务 |
| 自动填充替代数据库审计规则 | 多写入口下时间/操作人不一致 | 当前保留数据库默认值；审计机制后续统一设计 |

## 6. 待确认事项

当前计划按以下默认判断推进，无需阻塞第一阶段：

1. “Java 与 DB 使用 MyBatis-Plus”指项目业务 Repository；Flyway 和 Spring Session
   JDBC 继续使用各自官方机制。
2. 优先迁移当前可运行的三张业务表，不立即映射
   `docs/database/hd_plt_full_schema.sql` 的完整 PRD 表。
3. 第一阶段保持现有 API 返回完整 List，但 Repository 继续执行固定上限；
   面向用户的分页 API 作为独立后续需求。

如果这三项中任一项不符合预期，应在开始实现前调整范围。

## 7. 官方依据

- MyBatis-Plus 安装与 Spring Boot 3 starter：
  https://baomidou.com/en/getting-started/install/
- Mapper 扫描配置：
  https://baomidou.com/en/getting-started/config/
- 配置参考与多模块 XML 路径：
  https://baomidou.com/en/reference/
- 分页插件、`jsqlparser` 独立依赖与插件顺序：
  https://baomidou.com/en/plugins/pagination/
