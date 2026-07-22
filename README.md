# 临床研发管理平台 MVP

这是从纯 HTML 原型拆出的最小可运行版本。当前形态是“Vue 单页应用 +
Java 模块化单体”：浏览器端负责路由、页面交互和数据渲染；Java 服务负责认证、
权限、字段校验、统计口径、动态配置和 MySQL 持久化。生产环境仍然只交付一个
Spring Boot 应用。

## 已实现

- 自建账号、Argon2 密码散列、数据库 Session、CSRF 防护
- 通过环境变量安全引导首个管理员
- Vue 登录页、平台壳和登录后的浏览器路由
- 管线总览、Study 列表、月报、风险、团队矩阵、管线配置、导出、账号管理和角色权限管理页面
- 管线总览、Study 列表及 Program/Project/Study 管线配置读取真实后端接口
- 管理员维护账号、用户角色、角色权限和在线业务配置
- 25 表 `hd_plt_*` Flyway 基线、健康探针、Prometheus 指标
- Session 过期时，页面访问和 Vue API 请求统一返回登录页；API 本身仍保持 `401` JSON 契约
- Vite 生产构建、Spring Boot JAR 打包、Docker Compose 和 GitHub Actions

## 数据库结构状态

完整PRD目标数据库已经确认并在本机MySQL 8.0.46空库执行成功：

- 逻辑库：`study_management`
- 表数量：25张，统一使用 `hd_plt_` 前缀
- 设计规格：[完整PRD数据库设计规格](./docs/database/完整PRD数据库设计规格.md)
- Flyway/MySQL建表脚本：[V1__hd_plt_full_schema.sql](./docs/database/V1__hd_plt_full_schema.sql)

当前 Java 运行时、Repository、Spring Session 和 Flyway 已统一使用 `hd_plt_*` 模型。
正式环境应由 Flyway 从空库建立结构；不要把仅手工执行过建表 SQL、但缺少
`flyway_schema_history` 的数据库直接当作已迁移环境使用。

## 最快启动方式

前置条件：Docker Engine 24+ 与 Docker Compose v2。

```powershell
Copy-Item .env.example .env
# 编辑 .env，把所有 replace-with... 替换为不同的强随机密码
docker compose up -d --build
```

应用仅监听本机 `http://127.0.0.1:8080`。生产环境必须由 HTTPS 负载均衡器或反向代理转发，并把 `.env` 替换为云秘密管理服务。

开启本机监控：

```powershell
docker compose --profile monitoring up -d
```

- 平台：`http://127.0.0.1:8080`
- Prometheus：`http://127.0.0.1:9091`
- Grafana：`http://127.0.0.1:3000`

首个管理员创建后，应创建日常管理员账号，从部署环境移除 `BOOTSTRAP_ADMIN_PASSWORD`，再重启应用。数据库中已有同名账号时不会覆盖密码。

## 前端开发

前置条件：Node.js 24 和 npm。

首次安装依赖：

```powershell
npm.cmd --prefix frontend ci
```

不启动后端、使用演示数据查看全部页面：

```powershell
npm.cmd --prefix frontend run dev:mock
```

连接本地 Spring Boot；Vite 会把 `/api` 代理到 `http://localhost:8080`：

```powershell
npm.cmd --prefix frontend run dev
```

前端入口为 `frontend/index.html`，Vue 源代码位于 `frontend/src/`。所有后端请求
统一通过 `frontend/src/api/client.ts`；页面组件不直接调用 `fetch`。

## 本地构建与验证

直接使用 Maven 打包前，必须先生成最新前端产物：

```powershell
npm.cmd --prefix frontend ci
npm.cmd --prefix frontend run build
mvn test
```

`frontend/dist/` 会被复制进 Spring Boot JAR。Docker 构建已经包含前端 Node
构建阶段，不需要在宿主机预先生成 `dist/`。

完整验证命令：

```powershell
npm.cmd --prefix frontend run check
node --test tests/frontend-vue-architecture.test.js
mvn test
node --test tests/backend-module-boundaries.test.js
node --test "管线总览 Coverpage.test.js"
```

## 后端模块结构

后端是一个 Maven 多模块、单进程部署的模块化单体：

```text
study-management-api          对外 Java 接口和请求/响应契约
study-management-common       共享异常、枚举等稳定基础类型
study-management-domain       领域对象、业务端口和 Repository 接口
study-management-manager      业务用例编排和事务边界
study-management-repository   MyBatis-Plus 与外部系统适配器，实现 Domain 端口
study-management-service      API 实现、HTTP、安全配置和唯一启动入口
study-management-test         跨模块集成测试
```

依赖方向为 `service → manager → domain ← repository`。Domain 不依赖
Repository 实现；最终部署
`study-management-service/target/study-management-service-*-exec.jar`。

生产运维详见 [云上安全运维方案](./docs/云上安全运维方案_v1.0.md)，接口与架构设计详见 [前后端拆分技术设计](./docs/前后端拆分技术设计_v1.0.md)，目标数据库以[完整PRD数据库设计规格](./docs/database/完整PRD数据库设计规格.md)为准，本机实测证据详见 [MVP 验证记录](./docs/验证记录_v1.0.md)。

## MVP 边界

原纯 HTML 文件和 `support.js` 仍保留作为需求、样式与业务回归参考，不作为当前
Vue 应用的运行依赖；新服务也不迁移原型中的 `localStorage` 数据。

以下页面已经完成前端拆分，并可在 `dev:mock` 中查看；对应真实后端能力仍是预留接口：

- 风险：`GET /api/v1/risk-management/risks`
- 月报：`GET /api/v1/monthly-reports`
- 团队矩阵：`GET /api/v1/team-assignments`
- 管线配置：`GET /api/v1/clinical-pipeline/pipeline-config`、`/programs`、`/projects`、`/therapeutic-areas`，以及对应 CRUD 接口；Program、Project、Study 均以业务编号作为唯一展示标识
- 团队矩阵：`GET /api/v1/team-matrix`、`PUT /api/v1/team-matrix/assignments`
- 管线配置：`GET /api/v1/clinical-pipeline/pipeline-config`、`/programs`、`/projects`、`/therapeutic-areas`，以及对应 CRUD/重命名影响预览接口

除已落地的管线配置接口外，其余预留接口在真实后端模式下会显示加载失败状态。前端路由守卫
只改善导航体验，最终页面权限、操作权限和数据范围必须由服务端执行。
