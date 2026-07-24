# MVP 运行与 API 说明

## 实现定位

本文记录当前可运行纵切片的接口和数据库基线。前端使用 Vue、TypeScript 和 Vite；
Java 运行时、Repository、Spring Session 与 Flyway 已统一接入 `hd_plt_*` 目标表。
登录权限、管线/Study/配置、风险、团队矩阵、里程碑、Study 月报填报与月报导出
均已贯通数据库、API 与 Vue 页面。跨 Study 月报完成率列表与改密/MFA 等仍属待补项。

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
| `GET /api/v1/platform/settings/public` | 匿名 | 登录页等场景可展示的公开业务配置 |
| `GET /api/v1/platform/settings` | `platform.setting.read` | 全部业务配置 |
| `PUT /api/v1/platform/settings?key=...` | `platform.setting.update` + CSRF | 在线更新白名单内已有配置 |
| `GET /api/v1/platform/users` | `account.page.view` | 账号列表和角色列表，不返回密码哈希 |
| `POST /api/v1/platform/users` | `account.create` + CSRF | 创建账号并分配一个或多个 `roleCodes` |
| `PATCH /api/v1/platform/users/{id}` | `account.update` + CSRF | 修改账号显示名、状态或密码 |
| `PUT /api/v1/platform/users/{id}/roles` | `account.assignRole` + CSRF | 替换账号角色并使受影响 Session 失效 |
| `DELETE /api/v1/platform/users/{id}` | `account.delete` + CSRF | 删除允许删除的账号 |
| `GET /api/v1/platform/roles`、`GET /api/v1/platform/roles/{roleId}` | `role.page.view` | 角色列表及详情 |
| `GET /api/v1/platform/permissions` | `role.page.view` | 权限字典 |
| `POST/PUT/DELETE /api/v1/platform/roles...` | `role.create/update/delete` + CSRF | 新增、编辑或删除角色；权限变化会使关联用户 Session 失效 |
| `GET /api/v1/clinical-pipeline/pipeline-config` | `config.page.view` | Study 扁平配置明细 |
| `GET /api/v1/clinical-pipeline/therapeutic-areas` | `config.page.view` | TA（治疗领域）下拉选项 |
| `GET/POST/PATCH/DELETE /api/v1/clinical-pipeline/programs...` | `config.page.view/create/update/delete` | Program 查询、新增、更新、影响预览和删除 |
| `GET/POST/PATCH/DELETE /api/v1/clinical-pipeline/projects...` | `config.page.view/create/update/delete` | Project 查询、新增、更新、影响预览和删除 |
| `PATCH/DELETE /api/v1/clinical-pipeline/studies/{id}` | `config.update/delete` | 更新或删除 Study 配置 |
| `GET /api/v1/risk-management/risks` | `risk.read` | 按关键字、功能线、状态和等级分页查询风险及聚合统计 |
| `GET /api/v1/risk-management/risks/{riskCode}` | `risk.read` | 风险详情、历次评估和多条控制措施 |
| `GET /api/v1/risk-management/form-options` | `risk.read` | 返回当前数据范围内的 Study、功能线和团队成员选项 |
| `POST /api/v1/risk-management/risks` | `risk.create` + CSRF | 新建风险、首次评估和零至多条控制措施 |
| `PATCH /api/v1/risk-management/risks/{riskCode}` | `risk.update` + CSRF | 更新风险、关闭或重开；状态变化必须填写原因 |
| `DELETE /api/v1/risk-management/risks/{riskCode}` | `risk.delete` + CSRF | 按版本号软删除风险 |
| `POST/PATCH/DELETE /api/v1/risk-management/risks/{riskCode}/actions...` | `risk.update` + CSRF | 新增、更新或软删除控制措施 |
| `GET /api/v1/team-matrix` | `team.page.view` | 团队矩阵查询（Study × 角色分配） |
| `GET /api/v1/studies/{studyId}/team` | `study.read` | Study 抽屉团队只读（不要求 team.page.view） |
| `PUT /api/v1/team-matrix/assignments` | `team.update` + CSRF | 批量替换团队分配 |
| `GET /api/v1/studies/{studyId}/milestones` | `milestone.read` | Study 里程碑分组与节点 |
| `PUT /api/v1/studies/{studyId}/milestones/{milestoneCode}` | `milestone.update` + CSRF | 更新单个里程碑节点 |
| `GET /api/v1/studies/{studyId}/stage-projection` | `milestone.read` | 阶段投影（供管线总览/里程碑页） |
| `GET /api/v1/studies/{studyId}/monthly-reports` | `monthly.read` | Study×月度填报页读模型 |
| `POST /api/v1/monthly-reports/{reportId}/entries` | `monthly.create` + CSRF | 新增功能线进展明细 |
| `PATCH/DELETE /api/v1/monthly-report-entries/{entryId}` | `monthly.update` + CSRF | 修改或删除进展明细 |
| `GET /api/v1/studies/{studyId}/monthly-reports/history` | `monthly.read` | 功能线历史月份 |
| `GET /api/v1/reports/monthly/preview` | `report.page.view` | 月报导出预览（起止日期 + ALL/TA/PROGRAM） |
| `GET /api/v1/reports/monthly/export` | `report.export` | 下载 html / csv / xlsx |

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
- V10 增加风险规则版本、评估历史、控制措施、并发版本号和风险权限。风险评分为
  `影响程度 × 发生可能性 × 可探测性`，V1 阈值为 1–12 低、13–36 中、37–125 高。

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

- 已支持一个用户多个角色、角色权限并集、账号角色分配和角色权限管理；暂未提供角色复制或合并工具。
- 已返回并执行 `ALL/ASSIGNED_STUDY` 数据范围；`ASSIGNED_STUDY` 会在Study列表和
  状态聚合前按 `hd_plt_team_assignment` 过滤。当前预置角色仍均为 `ALL`。
- Study状态不再单独存库：由计划/实际开始和结束日期推导；“暂停”需后续定义独立业务规则。
- 尚未实现改密、忘记密码、MFA、登录限流和失败锁定。
- 动态配置只保留最后修改人/时间，尚无不可覆盖的历史审计表。
- 管线配置已支持 Program/Project/Study 新增、更新和受约束删除；三类实体以业务编号作为展示标识，不再维护名称字段；尚未实现分页、并发版本控制和合并工具。
- 原型数据不迁移；风险、团队、里程碑、Study 月报与月报导出均已落库；跨 Study 月报完成率列表仍待补。

## Session 过期行为

- 浏览器直接访问受保护的 HTML 页面时，未登录请求由后端重定向到 `/login?redirect=原地址`。
- API 继续返回结构化 `401` JSON；Vue API 客户端收到后清理内存中的当前用户，并跳转登录页。
- `403` 表示已经登录但权限不足，不会被误判为 Session 过期。
- 系统不轮询 Session；用户长时间无请求时，会在下一次导航或 API 请求时发现过期。

