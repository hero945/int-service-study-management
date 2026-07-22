# ADR-003：采用自建账号与服务端 Session

## 状态

Accepted（用户已确认采用自建账号）

## 日期

2026-07-17

## 实现状态（2026-07-22）

已实现邮箱密码登录、Argon2、数据库 Session、CSRF、账号停用以及账号角色或角色权限
变化后的关联 Session 失效。受保护 HTML 页面在 Session 过期后重定向登录页，API
保持 `401` JSON，由 Vue 客户端统一处理。改密/忘记密码、登录限流、失败锁定、MFA
和强制首次改密仍是后续安全加固项，不能因本 ADR 为 Accepted 而视为已经交付。

## 背景

当前 HTML 原型在浏览器保存演示账号和明文密码。正式系统没有可接入的统一身份前置条件，用户已确认一期采用自建账号。系统处理企业敏感的临床研发进度、风险和人员分工，因此认证不能延续浏览器本地账号，也不能只依赖前端角色判断。

## 决策

1. 使用Spring Security的邮箱/密码认证和服务端Session；邮箱转为小写后作为账号唯一标识。
2. 会话通过 `HttpOnly + Secure + SameSite` Cookie 传递；浏览器不在 `localStorage` 保存 JWT 或 Session token。
3. 使用 Spring Session JDBC 将 Session 存入 MySQL，以支持多实例、主动注销和并发会话控制。
4. 密码使用 Argon2id 单向哈希和独立盐；参数以 OWASP 最低基线为下限，并按生产服务器性能调优。
5. 所有非安全 HTTP 方法启用 CSRF；登录、退出、改密和重置同样受保护。
6. 登录实行账号+IP 限流、递增等待、临时锁定和统一失败文案。
7. 密码修改或重置后撤销其他 Session；重置 token 短时、一次性使用，数据库只保存哈希。
8. 首个管理员通过一次性初始化命令创建，强制首次登录改密；初始化成功后关闭入口。
9. 原型账号和密码不迁移，正式账号由平台管理员重新创建。

## 备选方案

### JWT 保存于浏览器 localStorage

优点：服务端可无状态扩展。

缺点：浏览器脚本可读取 token，撤销、改密后失效和并发会话管理更复杂；当前同域平台不需要该复杂度。

结论：不采用。

### 自行实现认证和密码算法

优点：定制自由。

缺点：安全风险高，容易遗漏 Session 固定、CSRF、密码升级、锁定和凭据擦除等成熟控制。

结论：不采用，使用 Spring Security 标准能力。

### 接入企业统一身份

优点：统一账号生命周期和单点登录。

缺点：当前需求已确认自建账号，且没有可接入平台壳或身份服务。

结论：一期不采用；未来如接入，需要新 ADR 和账号绑定方案。

## 后果

正面影响：

- 密码、Session、限流、CSRF 和账号状态由后端统一控制。
- 用户停用、改密和权限变更可以及时撤销会话。
- 前端平台壳只维护用户体验，不承担认证安全边界。

代价与约束：

- 团队必须负责账号创建、停用、重置、安全事件和密码策略运营。
- 多实例部署需要共享 Session 存储及清理任务。
- 未来接统一身份时，需要设计本地用户与外部身份的绑定和渐进切换。

## 验证

- API、日志、导出和备份不出现明文密码、密码哈希或 Session token。
- 登录成功轮换 Session ID；退出、停用、改密和重置能撤销 Session。
- CSRF、撞库限流、临时锁定、重置 token 重放和对象级越权测试通过。
- 生产初始化不包含原型账号，首个管理员首次登录必须改密。

## 参考

- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [Spring Security Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Spring Security Session Management](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)
