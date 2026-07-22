# ADR-006：采用 Vue 单页应用与扁平平台路由

## 状态

Accepted

## 日期

2026-07-22

## 背景

历史原型以单页 HTML 承载多个业务区域。当前系统已经使用 Vue、TypeScript、Vue Router 和统一 API 客户端，需要明确唯一前端源代码、路由结构和权限边界。

## 决策

- `frontend/src/` 是前端业务源代码，`frontend/dist/` 仅为 Vite 构建产物。
- 页面采用扁平业务路由并复用统一平台壳；账号管理与角色权限管理作为管理员页面。
- 所有 HTTP 调用集中在 `frontend/src/api/`，视图组件不得直接调用 `fetch`。
- Vue 路由守卫只改善导航体验，最终页面权限、操作权限和数据范围由 Spring Security 与后端用例校验。
- 生产交付仍为单个 Spring Boot 服务：先构建 Vue，再将 `frontend/dist/` 打包进后端。

## 影响

前端页面可以独立使用 Vite 开发和测试，同时保持单一生产运行时。新增页面必须注册路由、通过统一 API 边界访问后端，并同时落实服务端授权。
