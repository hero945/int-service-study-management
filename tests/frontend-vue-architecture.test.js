const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const root = path.resolve(__dirname, '..');
const read = (relativePath) =>
  fs.readFileSync(path.join(root, relativePath), 'utf8');

test('frontend uses a Vue entrypoint and keeps views behind the application root', () => {
  const indexHtml = read('frontend/index.html');
  const mainSource = read('frontend/src/main.ts');

  assert.match(indexHtml, /<div id="app"><\/div>/);
  assert.doesNotMatch(indexHtml, /id="(?:loginView|appView)"/);
  assert.match(mainSource, /createApp\(App\)/);
  assert.match(mainSource, /\.use\(router\)/);
});

test('frontend exposes a typed API boundary instead of calling fetch from views', () => {
  const apiClient = read('frontend/src/api/client.ts');
  const viewSources = [
    'frontend/src/views/LoginView.vue',
    'frontend/src/views/PipelineOverviewView.vue',
  ].map(read);

  assert.match(apiClient, /interface ApiClient/);
  assert.match(apiClient, /getCurrentUser/);
  assert.match(apiClient, /getPipelineOverview/);
  for (const source of viewSources) {
    assert.doesNotMatch(source, /\bfetch\s*\(/);
  }
});

test('login page uses an image-led Huadong composition with project-local assets', () => {
  const loginView = read('frontend/src/views/LoginView.vue');
  const panelRule = loginView.match(/\.login-form-panel\s*\{([^}]*)\}/)?.[1] ?? '';

  assert.match(loginView, /patient-centered-research-hero-transparent\.png/);
  assert.doesNotMatch(loginView, /patient-centered-research-hero\.jpg/);
  assert.match(loginView, /huadong-mark-blur-source\.png/);
  assert.match(loginView, /class="portal-brand-mark"/);
  assert.doesNotMatch(loginView, /class="login-panel-ambient"/);
  assert.doesNotMatch(loginView, /\.login-hero-figure::before/);
  assert.doesNotMatch(loginView, /border-bottom:\s*2px solid #70d3ff/);
  assert.match(loginView, /'has-error':\s*errorMessage/);
  assert.match(loginView, /aria-invalid/);
  assert.doesNotMatch(loginView, /huadong-medicine-logo\.png/);
  assert.match(loginView, /class="portal-header"/);
  assert.match(loginView, /class="portal-stage"/);
  assert.match(loginView, /class="portal-footer"/);
  assert.match(panelRule, /background:\s*transparent/);
  assert.match(panelRule, /border-radius:\s*0/);
  assert.match(panelRule, /box-shadow:\s*none/);
  assert.doesNotMatch(loginView, /核心价值观/);
  assert.doesNotMatch(loginView, /服务大众健康/);
  assert.doesNotMatch(loginView, /企业愿景/);
});

test('login visual styles stay isolated from the legacy global login selectors', () => {
  const loginView = read('frontend/src/views/LoginView.vue');

  assert.match(loginView, /class="portal-login-form"/);
  assert.doesNotMatch(loginView, /class="login-form"/);
  assert.doesNotMatch(loginView, /login-card-accent/);
  assert.match(loginView, /class="portal-password-field login-input-wrap"/);
  assert.match(loginView, /class="portal-password-toggle"/);
  assert.match(loginView, /\.portal-login-form input:-webkit-autofill/);
  assert.match(loginView, /\.portal-stage\s*\{[^}]*linear-gradient\(180deg,/s);
});

test('frontend navigation consumes permission codes instead of a coarse login role', () => {
  const types = read('frontend/src/api/types.ts');
  const router = read('frontend/src/router.ts');
  const shell = read('frontend/src/layout/AppShell.vue');
  const navigation = read('frontend/src/navigation.ts');
  const login = read('frontend/src/views/LoginView.vue');

  assert.match(types, /permissions:\s*PermissionCode\[\]/);
  assert.match(router, /requiredPermission/);
  assert.doesNotMatch(router, /user\.role\s*[!=]==?\s*['"]ADMIN['"]/);
  assert.doesNotMatch(shell, /user\.value\?\.role\s*[!=]==?\s*['"]ADMIN['"]/);
  assert.match(navigation, /visibleNavigationGroups/);
  assert.match(navigation, /permission:/);
  assert.doesNotMatch(navigation, /name:\s*'monthly'/);
  assert.match(shell, /NavIcon/);
  assert.doesNotMatch(shell, /icon:\s*'[◆◇⚠▦⚙⭐♑⌘]'/);
  assert.doesNotMatch(login, /from ['"]\.\.\/router['"]/);
});

test('cross-study monthly overview is removed without deleting study monthly reporting', () => {
  const router = read('frontend/src/router.ts');
  const apiClient = read('frontend/src/api/client.ts');
  const studyList = read('frontend/src/views/StudyListView.vue');

  assert.doesNotMatch(router, /MonthlyReportView/);
  assert.doesNotMatch(router, /path:\s*'monthly'/);
  assert.doesNotMatch(apiClient, /listMonthlyReports/);
  assert.equal(
    fs.existsSync(path.join(root, 'frontend/src/views/MonthlyReportView.vue')),
    false,
  );

  assert.match(router, /path:\s*'studies\/:studyId\/monthly-report'/);
  assert.match(apiClient, /getMonthlyReports/);
  assert.match(studyList, /\/studies\/\$\{studyId\}\/monthly-report/);
});

test('role permission management is a protected page after account management and uses the API boundary', () => {
  const router = read('frontend/src/router.ts');
  const navigation = read('frontend/src/navigation.ts');
  const apiClient = read('frontend/src/api/client.ts');
  const roleView = read('frontend/src/views/RolePermissionManagementView.vue');

  assert.match(router, /path:\s*'roles'/);
  assert.match(router, /requiredPermission:\s*'role\.page\.view'/);
  assert.ok(navigation.indexOf("path: '/accounts'") < navigation.indexOf("path: '/roles'"));
  assert.match(apiClient, /listRoles/);
  assert.match(apiClient, /listPermissions/);
  assert.match(apiClient, /createRole/);
  assert.match(apiClient, /updateRole/);
  assert.match(apiClient, /deleteRole/);
  assert.doesNotMatch(roleView, /\bfetch\s*\(/);
});

test('pipeline entity forms use the reduced fields and database-backed therapeutic area options', () => {
  const types = read('frontend/src/api/types.ts');
  const apiClient = read('frontend/src/api/client.ts');
  const configView = read('frontend/src/views/PipelineConfigView.vue');

  assert.match(apiClient, /listTherapeuticAreas/);
  assert.match(apiClient, /\/api\/v1\/clinical-pipeline\/therapeutic-areas/);
  assert.match(configView, /v-for="area in therapeuticAreas"/);
  assert.match(configView, />Program \*</);
  assert.match(configView, />Project 编号 \*</);
  assert.doesNotMatch(configView, />Program 名称 \*</);
  assert.doesNotMatch(configView, />Project 名称 \*</);
  assert.doesNotMatch(configView, />TA 编码 \*</);
  assert.doesNotMatch(configView, /Study 名称/);
  assert.doesNotMatch(types, /(?:studyName|programName|projectName|phaseStatusLabel)/);
  const columns = ['Source', 'Origin', 'Product', 'Program', 'MOA', 'Project', 'TA', 'Indication', 'Study No.', 'Phase Status'];
  const headerPositions = columns.map((column) =>
    configView.search(new RegExp(`<th[^>]*>${column.replace('.', '\\.')}</th>`)),
  );
  assert.ok(headerPositions.every((position) => position >= 0));
  for (let index = 1; index < columns.length; index += 1) {
    assert.ok(headerPositions[index - 1] < headerPositions[index]);
  }
  assert.match(configView, /class="[^"]*entity-program-table/);
  assert.match(configView, /class="project-drawer"/);
  assert.match(configView, /class="program-row"/);
  assert.match(configView, /class="project-sub-row"/);
  assert.match(configView, /toggleProgramExpand/);
  assert.match(configView, /expandedProgramIds/);
  assert.match(configView, /startProjectRowEdit/);
  assert.match(configView, /class="[^"]*entity-form-drawer/);
  assert.match(configView, /ref="studyProgramDetails"/);
  assert.match(configView, /ref="studyProjectDetails"/);
  assert.match(configView, /onDocumentPointerDown/);
  assert.match(configView, /placeholder="搜索 Study \/ TA \/ Program"/);
  assert.match(configView, /rows/);
  assert.match(configView, /ListPagination/);
  assert.match(read('frontend/src/components/ListPagination.vue'), /study-pagination/);
  assert.match(read('frontend/src/components/ListPagination.vue'), /DEFAULT_PAGE_SIZE = 10/);
  assert.match(configView, /保存 Program/);
  assert.match(configView, /保存 Project/);
  const programForm = configView.slice(
    configView.indexOf('v-if="entityDialog === \'program\'"'),
    configView.indexOf('<div v-else class="role-form-grid">'),
  );
  const programFields = ['Product *', 'Program *', 'MOA', 'Source *', 'Origin *'];
  for (let index = 1; index < programFields.length; index += 1) {
    assert.ok(programForm.indexOf(`>${programFields[index - 1]}`) < programForm.indexOf(`>${programFields[index]}`));
  }
  assert.match(configView, /新建 Program/);
  assert.match(configView, /新建 Project/);
});

test('audit log entry points are record or group scoped without module-wide buttons', () => {
  const accountView = read('frontend/src/views/AccountManagementView.vue');
  const roleView = read('frontend/src/views/RolePermissionManagementView.vue');
  const configView = read('frontend/src/views/PipelineConfigView.vue');
  const riskView = read('frontend/src/views/RiskManagementView.vue');
  const milestoneView = read('frontend/src/views/MilestoneView.vue');
  const monthlyView = read('frontend/src/views/MonthlyReportFillView.vue');

  for (const source of [
    accountView, roleView, configView, riskView, milestoneView, monthlyView,
  ]) {
    assert.doesNotMatch(source, />全部操作日志</);
  }

  for (const source of [accountView, roleView, configView, riskView]) {
    assert.match(source, />操作日志</);
    assert.match(source, />查看</);
  }

  assert.match(
    accountView,
    /<th class="account-actions-col">操作<\/th>\s*<th v-if="canAudit">操作日志<\/th>/,
  );
  assert.match(
    roleView,
    /<th>操作<\/th>\s*<th v-if="canAudit">操作日志<\/th>/,
  );
  assert.match(
    roleView,
    /<button class="text-button" type="button" @click="openRecordAuditLogs\([^>]+>查看<\/button>/,
  );
  assert.match(
    riskView,
    /<th v-if="canAudit">操作日志<\/th>\s*<\/tr>/,
  );
  assert.match(
    milestoneView,
    /<th v-if="canAudit" class="milestone-col-action">操作日志<\/th>\s*<\/tr>/,
  );
  assert.equal(
    (configView.match(
      /<th>操作<\/th>\s*<th v-if="canAudit">操作日志<\/th>/g,
    ) ?? []).length,
    3,
  );

  assert.match(milestoneView, /openGroupedAuditLogs\([^)]*'MILESTONE_STAGE'/s);
  assert.doesNotMatch(milestoneView, /node\.milestoneId\)/);
  assert.match(monthlyView, /openGroupedAuditLogs\([^)]*'MONTHLY_FUNCTION'/s);
  assert.doesNotMatch(monthlyView, /'MONTHLY_ENTRY', entry\.entryId/);
});

test('pipeline config keeps independent save state for each entity dialog', () => {
  const configView = read('frontend/src/views/PipelineConfigView.vue');

  assert.match(configView, /const programSaving = ref\(false\)/);
  assert.match(configView, /const projectSaving = ref\(false\)/);
  assert.match(configView, /const studySaving = ref\(false\)/);
  assert.doesNotMatch(configView, /const saving = ref\(false\)/);
  assert.match(configView, /:disabled="studySaving \|\| !studyForm\.projectId"/);
});

test('production packaging consumes the compiled frontend output', () => {
  const servicePom = read('study-management-service/pom.xml');
  const dockerfile = read('Dockerfile');

  assert.match(servicePom, /\.\.\/frontend\/dist/);
  assert.doesNotMatch(servicePom, /\.\.\/frontend\/src/);
  assert.match(dockerfile, /FROM node:[^\r\n]+ AS frontend-build/);
  assert.match(dockerfile, /npm (?:ci|run build)/);
});

test('mock development mode is configured in version-controlled Vite config', () => {
  const viteConfig = read('frontend/vite.config.ts');
  const mockSource = read('frontend/src/api/mock.ts');

  assert.match(viteConfig, /mode === 'mock'/);
  assert.match(viteConfig, /VITE_API_MODE/);
  assert.doesNotMatch(mockSource, /\?{3,}/);
  assert.match(mockSource, /displayName:\s*'陈研发'/);
  assert.match(mockSource, /indication:\s*'2型糖尿病'/);
  assert.match(mockSource, /code:\s*'HDM1005-T2DM-00'/);
  assert.match(mockSource, /\['milestone',\s*'milestone\.read',\s*'查看里程碑'/);
});

test('integrated local builds use root paths while Docker publishes the frontend under PLM', () => {
  const viteConfig = read('frontend/vite.config.ts');
  const router = read('frontend/src/router.ts');
  const dockerfile = read('Dockerfile');

  assert.match(viteConfig, /loadEnv/);
  assert.match(viteConfig, /base:\s*env\.VITE_BASE_PATH\s*\|\|\s*['"]\/['"]/);
  assert.match(router, /createWebHistory\(import\.meta\.env\.BASE_URL\)/);
  assert.match(dockerfile, /ARG VITE_BASE_PATH=\/PLM\//);
  assert.match(dockerfile, /ENV VITE_BASE_PATH=\$VITE_BASE_PATH/);
});

test('Vue type checking stays on the TypeScript compatibility line supported by vue-tsc', () => {
  const packageJson = JSON.parse(read('frontend/package.json'));

  assert.match(packageJson.devDependencies.typescript, /^6\./);
  assert.equal(packageJson.scripts.build, 'vue-tsc --noEmit && vite build');
});

test('Spring Boot serves Vite assets and forwards browser routes to the Vue entrypoint', () => {
  const security = read(
    'study-management-service/src/main/java/com/huadong/pipeline/security/SecurityConfig.java',
  );
  const spaController = read(
    'study-management-service/src/main/java/com/huadong/pipeline/web/SpaController.java',
  );

  assert.match(security, /"\/assets\/\*\*"/);
  assert.match(spaController, /"\/login"/);
  assert.match(spaController, /"\/pipeline"/);
  assert.match(spaController, /forward:\/index\.html/);
});

test('expired sessions redirect browser pages while API authentication errors stay centralized', () => {
  const apiClient = read('frontend/src/api/client.ts');
  const main = read('frontend/src/main.ts');
  const security = read(
    'study-management-service/src/main/java/com/huadong/pipeline/security/SecurityConfig.java',
  );

  assert.match(apiClient, /response\.status === 401/);
  assert.match(apiClient, /unauthorizedHandler/);
  assert.match(main, /setUnauthorizedHandler\(createSessionExpiredHandler/);
  assert.match(main, /router\.currentRoute\.value\.fullPath/);
  assert.match(security, /isBrowserPageRequest/);
  assert.match(security, /response\.sendRedirect\(loginRedirect\(request\)\)/);
  assert.match(security, /writeApiError\(response, mapper, 401, "UNAUTHENTICATED"/);
  assert.match(security, /body\.put\("details"/);
  assert.match(security, /body\.put\("timestamp"/);
});

test('pipeline overview keeps sticky id columns and stacked project/study drawers', () => {
  const mainCss = read('frontend/src/styles/main.css');
  const overview = read('frontend/src/views/PipelineOverviewView.vue');
  const studyList = read('frontend/src/views/StudyListView.vue');
  const projectDrawerPath = path.join(root, 'frontend/src/components/ProjectStudiesDrawer.vue');

  assert.match(mainCss, /\.study-row--clickable\s*\{[^}]*cursor:\s*pointer/s);
  assert.match(mainCss, /\.pipeline-table[^{]*th:nth-child\(1\)[\s\S]*?position:\s*sticky/s);
  assert.match(mainCss, /\.pipeline-table[^{]*td:nth-child\(1\)[\s\S]*?left:\s*0/s);
  assert.match(mainCss, /\.pipeline-table[^{]*td:nth-child\(2\)[\s\S]*?left:\s*130px/s);
  assert.match(mainCss, /\.pipeline-table[^{]*td:nth-child\(3\)[\s\S]*?left:\s*290px/s);
  assert.match(mainCss, /\.area-row-sticky\s*\{[^}]*position:\s*sticky[^}]*left:\s*0/s);
  assert.match(mainCss, /\.area-row-sticky\s*\{[^}]*min-width:\s*510px/s);
  assert.match(mainCss, /\.pipeline-table[^{]*th:nth-child\(3\)[\s\S]*?border-right:\s*1px/s);
  assert.match(mainCss, /\.status-chip\s*\{[^}]*width:\s*128px/s);
  assert.match(mainCss, /--green-bg:\s*#e5f4eb/);
  assert.match(mainCss, /\.status-chip--green[^}]*\{[^}]*background:\s*var\(--green-bg\)/s);
  assert.match(mainCss, /\.status-chip--blue[^}]*\{[^}]*border-color:\s*#b7cff5/s);
  assert.match(mainCss, /\.pipeline-id-cell\s*\{[^}]*cursor:\s*pointer/s);
  assert.match(mainCss, /\.cell-clickable\s*\{[^}]*cursor:\s*pointer/s);

  assert.ok(fs.existsSync(projectDrawerPath));
  assert.match(overview, /ProjectStudiesDrawer/);
  assert.match(overview, /StudyDetailDrawer/);
  assert.match(overview, /pipeline-id-cell/);
  assert.match(overview, /area-row-sticky/);
  assert.match(overview, /colspan="3"/);
  assert.match(overview, /pipeline-stage-wrap/);
  assert.match(overview, /cell-stage-caption/);
  assert.match(overview, /pipeline-hover-tip/);
  assert.match(overview, /showCellTip/);
  assert.doesNotMatch(overview, /legend-bar/);
  assert.doesNotMatch(overview, /quick-metrics/);
  assert.doesNotMatch(overview, /filter-bar/);
  assert.match(overview, /page-toolbar/);
  assert.match(overview, /filter-group/);
  assert.match(overview, /filter-field/);
  assert.match(overview, /filter-field__label/);
  assert.match(overview, /filter-count/);
  assert.match(mainCss, /\.filter-group\s*\{/s);
  assert.match(mainCss, /\.filter-field\s*\{/s);
  assert.match(mainCss, /\.filter-select\s*\{/s);
  assert.match(mainCss, /\.filter-count\s*\{/s);
  assert.match(studyList, /page-toolbar/);
  assert.match(studyList, /filter-group/);
  assert.match(studyList, /filter-field/);
  assert.match(overview, /openProjectDrawer/);
  assert.match(overview, /toStudy/);

  assert.match(studyList, /study-row--clickable/);
  assert.match(studyList, /StudyDetailDrawer/);
});
