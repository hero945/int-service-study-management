# Frontend

当前前端使用原生 HTML、CSS 和 ES Modules，不依赖外部 npm 包。

```powershell
npm run check
npm run build
```

- `src/` 是唯一可编辑源文件。
- `dist/` 是独立部署到 Nginx/CDN 时使用的构建产物，不提交 Git。
- 当前单体部署由 Maven 直接将 `src/` 收入 Spring Boot 可执行 JAR。
