# Frontend

当前前端使用 Vue、TypeScript、Vue Router 和 Vite。页面视觉延续历史
`管线总览 Coverpage.dc.html` 的浅灰画布、白色侧栏、蓝色强调色和紧凑表格。

```powershell
npm ci
npm run dev:mock
npm run test
npm run check
npm run build
```

- `src/views/`：登录、管线、研究、月报、风险、团队、配置和账号页面。
- `src/api/client.ts`：前后端交互端口；页面组件不得直接调用 `fetch`。
- `npm run dev:mock`：使用演示数据查看全部页面，不依赖后端。
- `npm run dev`：将 `/api` 代理到 `http://localhost:8080`。
- `dist/` 是独立部署到 Nginx/CDN 时使用的构建产物，不提交 Git。
- 当前单体部署由 Maven 将 Vite 生成的 `dist/` 收入 Spring Boot 可执行 JAR。
