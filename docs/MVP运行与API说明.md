# MVP 运行与 API 说明

## 实现定位

本版本是技术设计的第一条可运行纵切片。生产目标仍是技术设计中的 React + TypeScript 前端；为尽快验证平台壳、认证和数据闭环，MVP 使用无第三方运行依赖的 HTML/CSS/ES Modules。前后端边界已经通过 REST API 固定，后续替换前端框架不需要改数据库或核心业务服务。

## API 清单

| 方法与路径 | 权限 | 说明 |
|---|---|---|
| `GET /api/v1/platform/auth/csrf` | 匿名 | 初始化 Session 并获取写请求 token |
| `POST /api/v1/platform/auth/login` | 匿名 + CSRF | 表单字段 `username`、`password` |
| `POST /api/v1/platform/auth/logout` | 已登录 + CSRF | 销毁服务端 Session |
| `GET /api/v1/platform/me` | 已登录 | 当前用户名、显示名、角色 |
| `GET /api/v1/clinical-pipeline/overview` | 已登录 | 总量和各状态统计，文案/色调由后端返回 |
| `GET /api/v1/clinical-pipeline/studies` | 已登录 | 研究项目列表 |
| `POST /api/v1/clinical-pipeline/studies` | 已登录 + CSRF | 创建研究项目 |
| `GET /api/v1/platform/settings/public` | 已登录 | 前端可展示的业务配置 |
| `GET /api/v1/platform/settings` | ADMIN | 全部业务配置 |
| `PUT /api/v1/platform/settings?key=...` | ADMIN + CSRF | 在线更新白名单内已有配置 |
| `GET /api/v1/platform/users` | ADMIN | 账号列表，不返回密码哈希 |
| `POST /api/v1/platform/users` | ADMIN + CSRF | 创建 ADMIN 或 USER 账号 |

登录接口使用 `application/x-www-form-urlencoded`，其余写接口使用 JSON。失败响应的基础形式为：

```json
{
  "code": "VALIDATION_FAILED",
  "message": "字段校验失败",
  "details": {"field": "code", "reason": "不能为空"},
  "timestamp": "2026-07-17T00:00:00Z"
}
```

## 数据库版本

`V1__baseline.sql` 创建：

- `plt_user`：自建账号及 Argon2 密码散列；用户名唯一。
- `biz_study`：研究项目主数据；项目编号唯一，状态和更新时间有索引。
- `plt_system_setting`：运行期业务配置及最后修改人/时间。
- `SPRING_SESSION*`：服务端 Session，支持未来双实例部署。

Flyway 在应用启动时迁移。生产数据库迁移账号与日常运行账号建议分离；MVP Compose 为简化首次运行暂使用同一个 schema 账号。

## 配置优先级

生产秘密不提供默认值。主要环境变量：

| 变量 | 是否必需 | 说明 |
|---|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | 是 | 独立 MySQL 连接 |
| `BOOTSTRAP_ADMIN_USERNAME` / `BOOTSTRAP_ADMIN_PASSWORD` | 仅首次 | 创建首个管理员，密码至少 12 位 |
| `SESSION_COOKIE_SECURE` | 生产必须为 true | 仅 HTTPS 发送 Session Cookie |
| `DB_POOL_MAX` / `DB_POOL_MIN` | 否 | 连接池大小 |
| `SESSION_TIMEOUT` | 否 | 默认 30 分钟 |
| `MANAGEMENT_PORT` | 否 | 默认 9090，仅内网监控使用 |

## 已知 MVP 限制

- 只有 ADMIN/USER 两类粗粒度角色，尚无项目级数据权限。
- 尚未实现改密、忘记密码、MFA、登录限流和失败锁定。
- 动态配置只保留最后修改人/时间，尚无不可覆盖的历史审计表。
- 研究项目暂不支持编辑、停用、分页和并发版本控制。
- 原型数据不迁移，月报、风险、里程碑等模块后续按技术设计逐片实现。

