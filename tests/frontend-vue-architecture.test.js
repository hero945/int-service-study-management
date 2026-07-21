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
