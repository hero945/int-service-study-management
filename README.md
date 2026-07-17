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
node --check src/main/resources/static/app.js
node --test "管线总览 Coverpage.test.js"
```

生产运维详见 [云上安全运维方案](./docs/云上安全运维方案_v1.0.md)，接口与数据库总设计详见 [前后端拆分技术设计](./docs/前后端拆分技术设计_v1.0.md)，本机实测证据详见 [MVP 验证记录](./docs/验证记录_v1.0.md)。

## MVP 边界

原纯 HTML 文件仍保留作为需求与回归参考；新服务不迁移 localStorage 数据。月报、风险、里程碑和团队矩阵等功能尚未进入本次最小闭环。
