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

test('frontend navigation consumes permission codes instead of a coarse login role', () => {
  const types = read('frontend/src/api/types.ts');
  const router = read('frontend/src/router.ts');
  const shell = read('frontend/src/layout/AppShell.vue');

  assert.match(types, /permissions:\s*PermissionCode\[\]/);
  assert.match(router, /requiredPermission/);
  assert.doesNotMatch(router, /user\.role\s*[!=]==?\s*['"]ADMIN['"]/);
  assert.doesNotMatch(shell, /user\.value\?\.role\s*[!=]==?\s*['"]ADMIN['"]/);
});

test('role permission management is a protected page after account management and uses the API boundary', () => {
  const router = read('frontend/src/router.ts');
  const shell = read('frontend/src/layout/AppShell.vue');
  const apiClient = read('frontend/src/api/client.ts');
  const roleView = read('frontend/src/views/RolePermissionManagementView.vue');

  assert.match(router, /path:\s*'roles'/);
  assert.match(router, /requiredPermission:\s*'role\.page\.view'/);
  assert.ok(shell.indexOf("to: '/accounts'") < shell.indexOf("to: '/roles'"));
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
  for (let index = 1; index < columns.length; index += 1) {
    assert.ok(configView.indexOf(`<th>${columns[index - 1]}</th>`) < configView.indexOf(`<th>${columns[index]}</th>`));
  }
  assert.match(configView, /class="[^"]*entity-program-table/);
  assert.match(configView, /class="project-drawer"/);
  assert.match(configView, /class="[^"]*entity-form-drawer/);
  assert.match(configView, /ref="studyProgramDetails"/);
  assert.match(configView, /ref="studyProjectDetails"/);
  assert.match(configView, /onDocumentPointerDown/);
  assert.match(configView, /placeholder="搜索 Study \/ TA \/ Program"/);
  assert.match(configView, /pagedStudyRows/);
  assert.match(configView, /study-pagination/);
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

  assert.match(viteConfig, /mode === 'mock'/);
  assert.match(viteConfig, /VITE_API_MODE/);
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
  assert.match(security, /"code", "UNAUTHENTICATED"/);
});
