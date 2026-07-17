# 临床研发管理平台 MVP

这是从纯 HTML 原型拆出的最小可运行版本。它是一个模块化单体：浏览器端只处理平台导航、表单交互和数据渲染；Java 服务负责认证、权限、字段校验、状态文案、统计口径、动态配置和 MySQL 持久化。

## 已实现

- 自建账号、Argon2 密码散列、数据库 Session、CSRF 防护
- 通过环境变量安全引导首个管理员
- 平台壳，以及“临床研发管线”“平台管理”两个 Tab
- 研究项目创建、列表、状态统计；展示文案由后端返回
- 管理员创建账号、在线修改业务配置
- Flyway 建库、健康探针、Prometheus 指标
- Docker Compose、GitHub Actions CI/镜像发布

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

## 开发验证

```powershell
mvn test
npm.cmd --prefix frontend run check
node --test tests/backend-module-boundaries.test.js
node --test "管线总览 Coverpage.test.js"
```

当前前端源文件位于 `frontend/src/`。`npm.cmd --prefix frontend run build`
生成 `frontend/dist/`，可以在未来直接发布到 Nginx；当前 Maven 构建会把同一份
前端源文件收入 Spring Boot JAR，因此仍然只部署一个应用。

## 后端模块结构

后端是一个 Maven 多模块、单进程部署的模块化单体：

```text
study-management-api          对外 Java 接口和请求/响应契约
study-management-common       共享异常、枚举等稳定基础类型
study-management-domain       领域对象、业务端口和 Repository 接口
study-management-manager      业务用例编排和事务边界
study-management-repository   JDBC 与外部系统适配器，实现 Domain 端口
study-management-service      API 实现、HTTP、安全配置和唯一启动入口
study-management-test         跨模块集成测试
```

依赖方向为 `service → manager → domain ← repository`。Domain 不依赖
Repository 实现；最终部署
`study-management-service/target/study-management-service-*-exec.jar`。

生产运维详见 [云上安全运维方案](./docs/云上安全运维方案_v1.0.md)，接口与数据库总设计详见 [前后端拆分技术设计](./docs/前后端拆分技术设计_v1.0.md)，本机实测证据详见 [MVP 验证记录](./docs/验证记录_v1.0.md)。

## MVP 边界

原纯 HTML 文件仍保留作为需求与回归参考；新服务不迁移 localStorage 数据。月报、风险、里程碑和团队矩阵等功能尚未进入本次最小闭环。
