---
name: account-management-page-features
overview: 根据目标设计图补齐账号管理页面功能：统计卡片、搜索过滤、增强列表、新增/编辑/分配操作，以及对应后端 API 与权限。
design:
  architecture:
    framework: vue
  styleKeywords:
    - Apple HIG
    - 明亮干净
    - 圆角卡片
    - 柔和阴影
    - 蓝色标签
    - 绿色状态
    - 企业级表格
  fontSystem:
    fontFamily: PingFang SC
    heading:
      size: 24px
      weight: 600
    subheading:
      size: 14px
      weight: 400
    body:
      size: 14px
      weight: 400
  colorSystem:
    primary:
      - "#2563EB"
      - "#3B82F6"
      - "#1D4ED8"
    background:
      - "#F8FAFC"
      - "#FFFFFF"
      - "#F1F5F9"
    text:
      - "#1E293B"
      - "#475569"
      - "#94A3B8"
    functional:
      - "#22C55E"
      - "#DCFCE7"
      - "#F59E0B"
      - "#EF4444"
todos:
  - id: expand-backend-user-api
    content: 扩展后端用户列表与统计 API，返回范围模式、权限摘要、可见 Study 数量
    status: completed
  - id: implement-user-write-apis
    content: 实现 PATCH 用户、PUT 角色分配、DELETE 停用账号接口及对应权限点
    status: completed
    dependencies:
      - expand-backend-user-api
  - id: extend-frontend-api
    content: 扩展前端 API 类型、Client 与 Mock，接入新用户接口
    status: completed
    dependencies:
      - expand-backend-user-api
  - id: add-toolbar-and-stats
    content: 在账号管理页新增统计卡片、搜索框、角色筛选与新增按钮
    status: completed
    dependencies:
      - extend-frontend-api
  - id: rebuild-user-table
    content: 重构用户表格，展示角色描述、范围模式、可见范围、权限摘要与行内操作
    status: completed
    dependencies:
      - extend-frontend-api
  - id: implement-user-modals
    content: 实现创建/编辑账号弹窗与分配角色/Study 下拉操作
    status: completed
    dependencies:
      - implement-user-write-apis
      - rebuild-user-table
---

## 产品概述
升级现有「账号管理」页面，使其从只读列表升级为支持统计、搜索、筛选、角色分配和状态管理的完整账号与权限管理界面。

## 核心功能
- **顶部统计卡片**：展示账号总数、系统管理员数、业务成员数、只读用户数
- **搜索与筛选**：按姓名/登录邮箱关键词搜索、按角色下拉筛选
- **新增账号**：将当前静态按钮接入可填写的创建表单
- **增强账号表格**：列扩展为姓名、登录邮箱、角色（显示角色描述）、范围模式、可见范围、权限摘要、状态、操作
- **行内操作**：编辑账号信息、分配角色/Study
- **状态管理**：启用/停用账号

## 技术栈
- 前端：Vue 3 + TypeScript + Vite（沿用现有栈）
- 后端：Spring Boot 模块化单体（service → manager → domain ← repository）
- 数据库：MyBatis-Plus + H2/MySQL（沿用现有 ORM 与迁移脚本）

## 实现策略
### 后端
1. 扩展 `UserResponse`：在现有 `id/username/displayName/roles/enabled` 基础上，增加 `dataScope`、`permissions`、`assignedStudyCount`、`roleDescriptions`
2. 新增或扩展查询接口：
   - `GET /api/v1/platform/users` 增加关键词/角色过滤与分页
   - `GET /api/v1/platform/users/statistics` 返回四类账号统计
3. 实现设计文档已规划的写接口：
   - `PATCH /api/v1/platform/users/{id}`：修改显示名、状态（邮箱不可修改）
   - `PUT /api/v1/platform/users/{id}/roles`：分配角色
   - `DELETE /api/v1/platform/users/{id}`：软删除停用
4. 在 Repository 层通过 `hd_plt_team_assignment` 统计每个用户的可见 Study 数量

### 前端
1. 扩展 `PlatformUser` 类型与 `ApiClient` 方法
2. 在 `AccountManagementView.vue` 中新增统计卡片、搜索/筛选工具栏
3. 使用现有 `PageState`、`status-chip`、`data-table` 样式体系扩展表格列
4. 新增创建/编辑弹窗组件，复用现有表单与 CSRF 请求模式
5. mock 模式同步扩展，保证视觉回归与离线开发

## 关键执行要点
- 服务端权限：新增 `account.update`、`account.delete`、`account.assignRole` 等权限点，并用 `@PreAuthorize` 保护对应端点
- 数据安全：密码哈希绝不返回前端；写操作必须携带 CSRF token
- 软删除：`DELETE` 仅更新 `sys_deleted=1`，禁止物理删除
- 性能：用户列表一次性最多 500 条；统计与 Study 数量通过聚合查询批量返回，避免 N+1
- 向后兼容：保留现有 `PlatformUser` 字段，新增字段可选，避免破坏其他消费方

## 设计风格
采用与现有临床研发平台一致的 Apple HIG 风格：明亮、干净、圆角、柔和阴影，重点信息使用蓝色标签与绿色状态点。页面保持 1440px 桌面优先布局，左侧导航固定，右侧内容区留白充足。

## 页面结构
1. **页面标题区**：左侧「账号管理」主标题 + 「用户、角色与权限」副标题；右侧保留全局搜索占位
2. **工具栏**：搜索框（姓名/登录邮箱）+ 角色下拉筛选 + 右侧蓝色「＋新增账号」主按钮
3. **统计卡片行**：4 张等宽白色卡片，分别显示账号总数、系统管理员、业务成员、只读用户，数字使用大字号加粗
4. **账号与权限表格**：
   - 表头：姓名、登录邮箱、角色、范围模式、可见范围、权限摘要、状态、操作
   - 角色列使用蓝色圆角标签显示角色描述（如「系统管理员」）
   - 权限摘要使用浅灰/蓝色小标签展示关键权限名称
   - 状态「启用」使用绿色背景标签
   - 操作列提供「编辑」文字链接 + 「分配...」下拉菜单入口
5. **弹窗**：创建/编辑账号使用居中白色弹窗，表单字段分组清晰，提交按钮主蓝色，取消按钮次灰色

## 交互
- 搜索框输入后 300ms 防抖触发列表刷新
- 角色下拉改变即时过滤
- 表格行 hover 时操作按钮高亮
- 状态切换、删除操作需二次确认

## Agent Extensions
### SubAgent
- **code-explorer**
  - Purpose: 在实现前进一步定位后端 UserManager、Repository、MyBatis Mapper 与权限种子数据的精确位置，验证新增字段与接口的可行性
  - Expected outcome: 输出需要修改/新增的后端文件清单、权限码建议、以及 `hd_plt_team_assignment` 聚合查询的推荐实现位置
