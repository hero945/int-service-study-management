# Agent Harness：临床研发管线管理系统

本文件是智能体进入仓库后的操作入口，不是完整知识库。目标是让每次改动都能被定位、约束、验证和复现。遇到细节时，按下面的索引读取最相关的文档，不要一次性加载整个仓库。

## 1. 任务完成标准

一次任务只有同时满足以下条件才算完成：

1. 用户要求的行为已经实现，而不只是生成了代码或文档。
2. 改动遵守模块依赖、安全边界和“源文件/生成物”规则。
3. 根据改动范围运行了对应检查，并如实报告结果。
4. 没有覆盖用户已有改动，没有顺手修改无关文件。
5. 交付说明包含：改了什么、如何验证、仍有哪些限制。

不要把“命令未报错”直接等同于“需求完成”。页面改动要检查页面，接口改动要检查请求与响应，数据改动要检查迁移和权限。

## 2. 先定位，再行动

开始任务时按以下顺序工作：

1. 阅读用户要求、`git status` 和相关文件，确认当前真实状态。
2. 只加载与任务有关的上下文；代码行为优先于过期文档。
3. 涉及多个文件或存在设计取舍时，先写一个短计划。
4. 实现最小的端到端改动，随后立即运行最接近改动点的检查。
5. 检查差异和失败输出；修复原因，不要无依据地反复重试。
6. 最后运行改动范围对应的回归检查。

如果信息不足但可以从仓库中查明，先查明。只有缺少会显著改变结果的业务决策时才询问用户。

## 3. 仓库地图与信息源

### 后端：模块化单体

- `study-management-api/`：稳定的对外 Java 接口和请求/响应模型。
- `study-management-common/`：异常、枚举和少量稳定基础类型；禁止变成通用杂物箱。
- `study-management-domain/`：领域模型和 Repository 端口；保持纯净，不依赖数据库实现。
- `study-management-manager/`：用例编排、业务流程和事务边界。
- `study-management-repository/`：数据库及外部系统适配器，实现 Domain 中的端口。
- `study-management-service/`：API 实现、Web、安全、配置和唯一的 Spring Boot 启动入口。
- `study-management-test/`：跨模块 Spring Boot 集成测试。

必须保持的主要依赖方向：

```text
service -> api
service -> manager -> domain <- repository
各模块 -> common（仅在确有共享需要时）
test -> 需要验证的模块
```

禁止 Domain 依赖 Repository 实现。Manager 通过 Domain 定义的端口访问数据；Repository 负责实现端口。跨层调用不要通过新增“临时依赖”绕开边界。

### 前端与历史原型

- `frontend/index.html`：Vite HTML 入口，只保留挂载节点和静态元数据，不在这里堆业务页面。
- `frontend/src/main.ts`、`frontend/src/App.vue`：Vue 应用启动入口和根组件。
- `frontend/src/router.ts`、`frontend/src/session.ts`：页面路由、登录恢复和前端导航守卫。
- `frontend/src/views/`：登录、管线、Study、月报、风险、团队、配置、导出和账号页面。
- `frontend/src/layout/`、`frontend/src/components/`、`frontend/src/styles/`：共享布局、组件和原型视觉样式。
- `frontend/src/api/`：前后端交互的唯一客户端边界；业务页面不得直接调用 `fetch`。
- `frontend/src/domain/`：可独立测试的前端展示规则，不承载服务端权限或持久化规则。
- `frontend/public/`：由 Vite 原样复制的静态资源。
- `frontend/dist/`：构建产物，不手工编辑。
- `管线总览 Coverpage.dc.html`、`管线总览 Coverpage.test.js`：历史离线原型及其测试。
- `support.js`：生成的 DC 运行时，不手工编辑。

前端使用 Vue、TypeScript、Vue Router 和 Vite。`npm run dev:mock` 使用前端演示数据；
`npm run dev` 将 `/api` 代理到本地 Spring Boot。mock 只用于页面开发和视觉回归，
不得被描述为真实后端、生产权限或数据库。

前后端当前以一个 Spring Boot 服务交付：首次安装 npm 依赖后，Maven 在
`generate-resources` 阶段执行 Vite 构建并生成 `frontend/dist/`，随后把该目录复制到
后端 classpath；直接调用 `spring-boot:run` 时仍需显式先执行 `generate-resources`。
Dockerfile 会按 Node 构建前端、Maven 构建后端的顺序完成这两步。除非任务明确要求
改变部署方式，不要新增第二个生产运行时或第二套前端源代码。

### 产品、架构与运行文档（本地 `docs/`，不入库）

`docs/` 整目录被 `.gitignore` 忽略，**不上传远程仓库**。本机维护；每次增删改
`docs/` 下文件时，必须同步更新 [`docs/README.md`](docs/README.md) 索引。

- `README.md`：项目入口和当前运行方式。
- `docs/README.md`：本地文档唯一入口索引。
- `docs/product/临床研发管线管理系统_PRD_v1.0.md`：产品需求源文件。
- `docs/architecture/前后端拆分技术设计_v1.0.md`：部署形态和未来拆分边界。
- `docs/devops/云上安全运维方案_v1.0.md`：生产安全和运维约束。
- `docs/devops/MVP运行与API说明.md`：本地运行和 API 使用说明。
- `docs/devops/验证记录_v1.0.md`：已执行验证及证据。
- `docs/decisions/ADR-001-采用模块化单体与页面读模型.md`：总体架构决策。
- `docs/decisions/ADR-003-采用自建账号与服务端Session.md`：认证决策。
- `docs/decisions/ADR-004-建设项目独立MySQL数据库.md`：数据库决策。

按任务选择上下文：

- 改认证、账号或权限：先读 ADR-003、安全配置和相关集成测试。
- 改数据库、Repository 或迁移：先读 ADR-004、`db/migration/` 和 Domain 端口。
- 改模块边界：先读 ADR-001、父 `pom.xml` 和边界测试。
- 改页面：先读 `frontend/README.md`、`frontend/src/router.ts`、对应 `views/` 和
  `frontend/src/api/`；样式调整还要对照历史原型。
- 改产品规则：先读 PRD；若代码与 PRD 冲突，明确指出，不静默猜测。
- 改部署或运维：先读前后端拆分设计、云上安全运维方案、`Dockerfile` 和 `compose.yaml`。

`tests/project-structure.test.js` 属于已放弃的框架重构试验，除非恢复该重构，否则不作为发布门禁。

## 4. 分层护栏

- API：只表达稳定契约，不放数据库实体、控制器逻辑或具体实现。
- Common：只放真正跨模块且稳定的基础类型；业务规则归属 Domain。
- Domain：定义领域对象、规则和数据访问端口；不得依赖 Spring Web、JDBC 或 Repository 实现。
- Manager：组织用例、权限后的业务流程和事务；不要直接编写 SQL。
- Repository：实现 Domain 端口，隔离数据库和外部系统；SQL 必须参数化。
- Service：实现 API，处理 HTTP、安全和启动配置；不要复制 Manager 的业务逻辑。
- Test：模块内测试跟随所属模块；跨模块和启动级验证放入 `study-management-test`。

新增能力时优先沿既有边界扩展。若边界不适用，先记录设计理由，再调整结构；不要用循环依赖解决问题。

## 5. 安全与数据规则

- 不提交密钥、口令、令牌、生产连接串或真实患者数据。
- 不把 `localStorage` 演示数据描述成生产数据库。
- 服务端必须在查询和聚合前执行页面访问权、操作/CRUD 权限和数据范围检查。
- 客户端校验只是体验优化，不能代替服务端授权。
- Vue 路由守卫只负责导航体验；页面、API 和数据范围权限仍必须由 Spring Security
  及后端用例校验。`/accounts` 必须同时保持前端管理员守卫和服务端管理员校验。
- 认证继续使用服务端 Session 与 CSRF，不在 `localStorage`、`sessionStorage`
  或前端状态中保存认证令牌。
- 登录接口不得返回密码或密码散列；日志不得记录凭据和敏感健康信息。
- 数据库查询使用参数化语句；外部输入必须在系统边界完成校验。
- 涉及医药或临床术语时，首次出现要附一句通俗解释。

## 6. 编辑与生成物规则

- 保留工作区中已有的用户改动；先看差异，再编辑重叠文件。
- 只修改任务需要的文件，不进行无关重构、格式化或依赖升级。
- 前端编辑 `frontend/index.html`、`frontend/public/`、`frontend/src/` 和必要的
  Vite/TypeScript 配置，通过构建生成 `frontend/dist/`。
- 新后端调用先扩展 `frontend/src/api/types.ts` 与 `frontend/src/api/client.ts`，
  再由页面消费；mock 实现放在 `frontend/src/api/mock.ts`，不得散落到视图组件。
- PRD 先改 `docs/product/` 下 Markdown，再运行 `node docs/tools/generate-prd-html.js`；
  不要直接修 `docs/generated/` 中的 HTML。
- 图表先改 `docs/tools/diagram-source.html`，再更新 `docs/assets/diagrams/` PNG。
- 改动 `docs/` 后必须更新 `docs/README.md`；不要把 `docs/` 提交或推送到远程。
- 不手工编辑 `support.js`。
- 新依赖必须有明确用途，并优先复用仓库现有能力。
- 与“华东”有关的文档先写入当前工作区；未经用户明确确认，不得复制、覆盖或移动到 `C:\Users\admin\Desktop\obsidian-workspace`。

## 7. 验证矩阵

在 Windows PowerShell 中使用：

```powershell
# 前端语法、资源和构建检查
npm.cmd --prefix frontend run check

# Vue 结构、API 边界和 Spring Boot 静态资源衔接
node --test tests\frontend-vue-architecture.test.js

# 后端全部模块及集成测试
mvn test

# Maven 模块依赖边界
node --test tests\backend-module-boundaries.test.js

# PRD HTML 生成及链接、图片、脚本校验（需本机 docs/）
node docs\tools\generate-prd-html.js

# 历史原型业务逻辑
node ".\管线总览 Coverpage.test.js"
```

按改动范围选择最小充分验证：

- `frontend/index.html`、`frontend/public/**`、`frontend/src/**` 或前端配置：
  运行前端检查和 Vue 架构测试；视觉改动还要在 Chrome 中检查登录前后跳转、
  交互、控制台、键盘访问、图片加载和 1440px 无裁切。
- 后端 Java、POM 或配置：运行 `mvn test`；改模块依赖时再运行边界测试。
- API、认证、权限、数据库迁移：除单元测试外，必须运行相关集成测试。
- PRD 或图表：运行 PRD 生成器并检查生成页面。
- 历史原型：运行对应 Node 测试，并直接打开 HTML 检查。
- Docker、Compose 或生产配置：至少完成配置解析和构建检查；确认生产镜像使用
  非 mock 前端，并且 JAR 中包含最新 `frontend/dist/`。没有实际启动就不要声称部署成功。

若受环境限制无法运行某项验证，说明具体命令、阻塞原因和未验证风险。不要伪造通过结果。

## 8. 让失败改进 Harness

遇到问题时先判断缺少的是代码、测试、文档、工具还是约束。若同类错误可能再次发生，并且与当前任务直接相关，应补充可执行测试、边界检查或对应文档，而不只修复单个症状。不要为了“完善体系”扩大当前任务范围；不在范围内的缺口记录在交付说明中。

## 9. 提交与交付

- 未经用户要求，不自动暂存、提交、推送或创建 PR。
- 需要提交时使用 Conventional Commits，例如 `fix: correct study aggregation`、`docs: update deployment guide`。
- PR 说明应包含范围、关键设计、验证命令和结果；视觉改动附前后截图。
- 最终回复先给结果，再列验证情况和剩余限制。只报告实际执行过的操作。
