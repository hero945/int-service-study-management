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
  fs.existsSync(path.join(root, 'frontend', 'package.json')),
  'missing independent frontend project',
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

console.log('PASS backend Maven modules and dependency direction');
