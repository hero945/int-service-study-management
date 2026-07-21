# MVP 运行与 API 说明

## 实现定位

本文件记录早期MVP纵切片的运行接口和数据库基线。当前前端已经迁移为Vue +
TypeScript + Vite；本文API和 `V1__baseline.sql`、`V2__role_permission_authorization.sql`
说明仍用于识别旧MVP运行边界，
不代表完整PRD的25表目标结构已经接入Java。

## API 清单

| 方法与路径 | 权限 | 说明 |
|---|---|---|
| `GET /api/v1/platform/auth/csrf` | 匿名 | 初始化 Session 并获取写请求 token |
| `POST /api/v1/platform/auth/login` | 匿名 + CSRF | 表单字段 `username`、`password` |
| `POST /api/v1/platform/auth/logout` | 已登录 + CSRF | 销毁服务端 Session |
| `GET /api/v1/platform/me` | 已登录 | 当前用户名、显示名、角色列表、权限码和数据范围 |
| `GET /api/v1/clinical-pipeline/overview` | `pipeline.page.view` | 总量和各状态统计，文案/色调由后端返回 |
| `GET /api/v1/clinical-pipeline/studies` | `study.read` | 研究项目列表 |
| `POST /api/v1/clinical-pipeline/studies` | `config.create` + CSRF | 创建研究项目 |
| `GET /api/v1/platform/settings/public` | 已登录 | 前端可展示的业务配置 |
| `GET /api/v1/platform/settings` | `platform.setting.read` | 全部业务配置 |
| `PUT /api/v1/platform/settings?key=...` | `platform.setting.update` + CSRF | 在线更新白名单内已有配置 |
| `GET /api/v1/platform/users` | `account.page.view` | 账号列表和角色列表，不返回密码哈希 |
| `POST /api/v1/platform/users` | `account.create` + CSRF | 创建账号并分配一个或多个 `roleCodes` |

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

### 当前运行基线

Java运行时、Flyway和Repository已经统一使用 `hd_plt_*` 目标模型：

- [V1__hd_plt_full_schema.sql](database/V1__hd_plt_full_schema.sql)既是25表MySQL建表语句，
  也是打入Spring Boot JAR的Flyway V1基线。
- V2负责预置角色、权限、角色权限关系和平台显示名称；已有旧Session表会原地改名为
  `hd_plt_spring_session*`，不会复制认证令牌到前端。
- 账号以 `hd_plt_user.email` 登录，一个账号可关联多个角色；接口鉴权使用角色汇总后的
  权限码，不直接判断角色名称。
- Study写入 `hd_plt_study` 前必须验证Program、Project和治疗领域的层级关系，并把
  Program/Project/治疗领域/适应症信息保存为创建时快照。

生产数据库迁移账号与日常运行账号建议分离；MVP Compose 为简化首次运行暂使用同一个
schema账号。H2仅用于自动化测试，真实运行默认连接MySQL。

## 配置优先级

生产秘密不提供默认值。主要环境变量：

| 变量 | 是否必需 | 说明 |
|---|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | 是 | 独立 MySQL 连接 |
| `BOOTSTRAP_ADMIN_USERNAME` / `BOOTSTRAP_ADMIN_PASSWORD` | 仅首次 | 前者填写管理员邮箱，密码至少 12 位 |
| `SESSION_COOKIE_SECURE` | 生产必须为 true | 仅 HTTPS 发送 Session Cookie |
| `DB_POOL_MAX` / `DB_POOL_MIN` | 否 | 连接池大小 |
| `SESSION_TIMEOUT` | 否 | 默认 30 分钟 |
| `MANAGEMENT_PORT` | 否 | 默认 9090，仅内网监控使用 |

## 已知 MVP 限制

- 已支持一个用户多个角色及角色权限并集；尚未提供角色模板、角色分配的管理接口。
- 已返回并执行 `ALL/ASSIGNED_STUDY` 数据范围；`ASSIGNED_STUDY` 会在Study列表和
  状态聚合前按 `hd_plt_team_assignment` 过滤。当前预置角色仍均为 `ALL`。
- Study状态不再单独存库：由计划/实际开始和结束日期推导；“暂停”需后续定义独立业务规则。
- 尚未实现改密、忘记密码、MFA、登录限流和失败锁定。
- 动态配置只保留最后修改人/时间，尚无不可覆盖的历史审计表。
- 研究项目暂不支持编辑、停用、分页和并发版本控制。
- 原型数据不迁移，月报、风险、里程碑等模块后续按技术设计逐片实现。

