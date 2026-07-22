const assert = require('node:assert');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..');
const modules = [
  'study-management-api',
  'study-management-common',
  'study-management-domain',
  'study-management-manager',
  'study-management-repository',
  'study-management-service',
  'study-management-test',
];

const read = (relativePath) => fs.readFileSync(path.join(root, relativePath), 'utf8');

const readJavaSources = (moduleName) => {
  const sourceRoot = path.join(root, moduleName, 'src', 'main', 'java');
  const sources = [];

  const visit = (directory) => {
    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
      const entryPath = path.join(directory, entry.name);
      if (entry.isDirectory()) visit(entryPath);
      if (entry.isFile() && entry.name.endsWith('.java')) {
        sources.push(fs.readFileSync(entryPath, 'utf8'));
      }
    }
  };

  visit(sourceRoot);
  return sources.join('\n');
};

for (const moduleName of modules) {
  assert(
    fs.existsSync(path.join(root, moduleName, 'pom.xml')),
    `missing Maven module: ${moduleName}`,
  );
}

const parentPom = read('pom.xml');
for (const moduleName of modules) {
  assert(
    parentPom.includes(`<module>${moduleName}</module>`),
    `parent pom must aggregate ${moduleName}`,
  );
}

const domainPom = read('study-management-domain/pom.xml');
assert(
  !domainPom.includes('<artifactId>study-management-repository</artifactId>'),
  'domain must not depend on repository implementations',
);

const repositoryPom = read('study-management-repository/pom.xml');
assert(
  repositoryPom.includes('<artifactId>study-management-domain</artifactId>'),
  'repository must implement ports owned by domain',
);
assert(
  repositoryPom.includes('<artifactId>mybatis-plus-spring-boot3-starter</artifactId>'),
  'repository must use the MyBatis-Plus Spring Boot 3 starter',
);

for (const moduleName of [
  'study-management-api',
  'study-management-domain',
  'study-management-manager',
]) {
  const sources = readJavaSources(moduleName);
  assert(
    !sources.includes('com.baomidou') && !sources.includes('org.apache.ibatis'),
    `${moduleName} must not depend on MyBatis persistence types`,
  );
}

const repositorySources = readJavaSources('study-management-repository');
assert(
  !repositorySources.includes('JdbcClient'),
  'business repositories must not use JdbcClient after the MyBatis-Plus migration',
);
assert(
  repositorySources.includes('BaseMapper'),
  'repository must provide MyBatis-Plus mapper implementations',
);

const servicePom = read('study-management-service/pom.xml');
assert(
  servicePom.includes('<artifactId>spring-boot-maven-plugin</artifactId>'),
  'service must be the single deployable Spring Boot module',
);
assert(
  servicePom.includes('<directory>../frontend/dist</directory>'),
  'service must package the compiled frontend output',
);
assert(
  servicePom.includes('<artifactId>exec-maven-plugin</artifactId>') &&
    servicePom.includes('<phase>generate-resources</phase>') &&
    servicePom.includes('<argument>build</argument>'),
  'service generate-resources must rebuild the Vue frontend before copying it',
);
assert(
  servicePom.includes('<executable>${npm.executable}</executable>') &&
    servicePom.includes('<family>Windows</family>') &&
    servicePom.includes('<npm.executable>npm.cmd</npm.executable>'),
  'service frontend build must use npm.cmd on Windows and remain cross-platform',
);

assert(
  fs.existsSync(path.join(root, 'frontend', 'package.json')),
  'missing independent frontend project',
);
const dockerfile = read('Dockerfile');
assert(
  dockerfile.includes('mvn -B -ntp -Dfrontend.build.skip=true verify'),
  'Docker Maven stage must reuse the frontend-build output instead of requiring npm',
);
for (const asset of [
  path.join('frontend', 'index.html'),
  path.join('frontend', 'src', 'main.ts'),
  path.join('frontend', 'src', 'App.vue'),
  path.join('frontend', 'src', 'api', 'client.ts'),
]) {
  assert(
    fs.existsSync(path.join(root, asset)),
    `missing frontend source asset: ${asset}`,
  );
}

const mysqlNameRemoval = read(
  'study-management-service/src/main/resources/db/migration/mysql/V9__remove_pipeline_entity_names.sql',
);
const h2NameRemoval = read(
  'study-management-service/src/main/resources/db/migration/h2/V9__remove_pipeline_entity_names.sql',
);
for (const column of [
  'study_name',
  'program_name_snapshot',
  'project_name_snapshot',
  'project_name',
  'program_name',
]) {
  assert(
    mysqlNameRemoval.includes(`DROP COLUMN ${column}`),
    `MySQL V9 must remove ${column}`,
  );
  assert(
    h2NameRemoval.includes(`DROP COLUMN ${column}`),
    `H2 V9 must remove ${column}`,
  );
}

const pipelineJavaSources = [
  'study-management-api',
  'study-management-domain',
  'study-management-manager',
  'study-management-repository',
  'study-management-service',
].map(readJavaSources).join('\n');
assert(
  !/(studyName|programName|projectName|phaseStatusLabel|rename-impact|RenameImpact)/.test(pipelineJavaSources),
  'pipeline Java contracts must not expose removed name, phase label, or rename-impact fields',
);

for (const database of ['mysql', 'h2']) {
  const migrationDirectory = path.join(
    root, 'study-management-service', 'src', 'main', 'resources', 'db', 'migration', database,
  );
  const versions = fs.readdirSync(migrationDirectory)
    .map((name) => /^V([^_]+)__/.exec(name)?.[1])
    .filter(Boolean);
  assert.equal(
    new Set(versions).size,
    versions.length,
    `${database} Flyway migrations must not reuse a version`,
  );
}
assert(
  fs.existsSync(path.join(
    root,
    'study-management-service/src/main/resources/db/migration/mysql/V8__team_matrix.sql',
  )),
  'MySQL migration history must retain the already-applied V8 team matrix migration',
);

console.log('PASS backend Maven modules and dependency direction');
