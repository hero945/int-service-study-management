# 角色权限管理常见问题 QA

本文记录角色权限管理开发与联调中已经实际遇到的两个问题，便于后续开发、测试和运维排查。

## Q1：数据库已经给管理员角色增加了权限，为什么账号仍然收到 403？

### 现象

- 账号处于启用状态，并已关联启用的 `ADMIN` 角色。
- 数据库中 `ADMIN` 已关联 `role.page.view`。
- 访问角色权限管理页面或接口时仍返回 `403 ACCESS_DENIED`。

### 原理

`@PreAuthorize("hasAuthority('role.page.view')")` 检查的不是数据库实时数据，也不会因为角色名是 `ADMIN` 就自动放行。

登录时，系统按以下链路加载权限：

```text
hd_plt_user
  → hd_plt_user_role
  → hd_plt_role
  → hd_plt_role_permission
  → hd_plt_permission.permission_code
  → Spring Security Authentication.authorities
  → 数据库 Session
```

后续请求中的 `hasAuthority` 会对当前 Session 中的 Authority 做精确字符串匹配。只有 Session 中确实存在 `role.page.view` 才会通过。

### 已确认根因

问题账号的 Session 创建于 `16:03:26`，而新增角色管理权限的 Flyway V3 迁移完成于 `16:04:31`。旧 Session 保存的是迁移前的权限快照，因此其中没有 `role.page.view`。

通过角色管理接口修改权限时，`RoleSessionInvalidator` 会清除受影响用户的 Session；但 Flyway 迁移或直接执行 SQL 不经过该接口，所以不会触发这段清理逻辑。

### 处理方式

1. 退出登录并重新登录，让系统重新查询角色和权限。
2. 登录后检查 `GET /api/v1/platform/me` 返回的 `permissions` 是否包含 `role.page.view`。
3. 若仍无权限，检查账号、用户角色、角色权限和权限字典各层的状态及逻辑删除标记。

推荐使用以下只读 SQL 排查：

```sql
SELECT DISTINCT
    u.email,
    u.status_code AS user_status,
    u.sys_deleted AS user_deleted,
    r.role_name,
    r.status_code AS role_status,
    r.sys_deleted AS role_deleted,
    ur.sys_deleted AS user_role_deleted,
    p.permission_code,
    p.status_code AS permission_status,
    p.sys_deleted AS permission_deleted,
    rp.sys_deleted AS role_permission_deleted
FROM hd_plt_user u
LEFT JOIN hd_plt_user_role ur ON ur.user_id = u.id
LEFT JOIN hd_plt_role r ON r.id = ur.role_id
LEFT JOIN hd_plt_role_permission rp ON rp.role_id = r.id
LEFT JOIN hd_plt_permission p ON p.id = rp.permission_id
WHERE LOWER(u.email) = LOWER(?);
```

> 使用参数化查询替换 `?`，不要把邮箱直接拼接进 SQL。

### 后续设计注意点

- 页面或 API 修改角色权限：应立即使关联用户的 Session 失效。
- 数据库迁移或运维 SQL 修改权限：需要单独安排 Session 失效策略。
- 更通用的方案是维护权限版本号或安全戳，使旧 Session 能被自动识别为失效。

## Q2：操作成功提示 `.role-notice` 为什么一直不消失？刷新后仍可能看到旧行为？

### 现象

新增、编辑或删除角色后，页面显示“角色已新增”等成功提示，但提示长期停留。

### 第一层根因：页面状态没有清理

原实现只给 `notice` 赋值，没有定时将其清空，因此：

```vue
<p v-if="notice" class="role-notice" role="status">{{ notice }}</p>
```

会一直存在。

当前实现统一通过 `showNotice()` 显示提示：

- 提示显示 4 秒后自动消失。
- 连续操作会取消旧定时器并重新计时。
- 组件卸载时会清理定时器，避免残留回调。

对应回归测试验证：提示在 `3999ms` 时仍显示，到 `4000ms` 时消失。

### 第二层原因：浏览器仍运行旧的 SPA 实例

Vue 是单页应用（SPA）。通过侧边栏切换页面只发生前端路由跳转，不会重新下载 JavaScript。若页面在修复前已经打开，即使服务器已经部署新文件，当前标签页仍可能继续执行内存中的旧代码。

Vite 热更新还可能保留已有组件状态。修复前已经出现的旧提示不会自动获得新版本的定时器。

### 处理方式

1. 使用 `Ctrl + F5` 强制重新加载页面，或关闭标签页后重新打开。
2. 再执行一次新增、编辑或删除操作，观察新提示是否在 4 秒后消失。
3. 确认当前访问的是预期地址和端口，避免连接到另一个旧的 Vite 或 Spring Boot 进程。

### 如何区分浏览器缓存与服务端旧构建

比较下面三处 `index-*.js` 文件名是否一致：

```text
浏览器当前服务返回的 index.html
frontend/dist/index.html
study-management-service/target/classes/static/index.html
```

本次排查中三处均为 `assets/index-C6LqlsAH.js`，并且服务响应头包含：

```text
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

因此当时不是 Spring Boot 返回了旧构建，而是已经打开的标签页仍在运行旧的前端实例。

## 快速判断表

| 问题 | 首要检查 | 常见处理 |
| --- | --- | --- |
| 数据库有权限但接口返回 403 | 当前 Session 是否早于权限变更 | 退出并重新登录 |
| 通过页面编辑权限后用户仍在线 | 是否经过角色更新接口、是否找到关联账号 | 检查 Session 失效逻辑 |
| 成功提示一直不消失 | 是否运行包含 `showNotice()` 的新前端 | `Ctrl + F5` 后重新操作 |
| 强制刷新后仍是旧行为 | 服务返回的 JS 哈希是否与 `frontend/dist` 一致 | 重新构建并重启服务 |
