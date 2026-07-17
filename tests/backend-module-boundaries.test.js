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

const servicePom = read('study-management-service/pom.xml');
assert(
  servicePom.includes('<artifactId>spring-boot-maven-plugin</artifactId>'),
  'service must be the single deployable Spring Boot module',
);
assert(
  servicePom.includes('<directory>../frontend/src</directory>'),
  'service must package the independent frontend source',
);

assert(
  fs.existsSync(path.join(root, 'frontend', 'package.json')),
  'missing independent frontend project',
);
for (const asset of ['index.html', 'app.js', 'app.css', 'overrides.css']) {
  assert(
    fs.existsSync(path.join(root, 'frontend', 'src', asset)),
    `missing frontend source asset: ${asset}`,
  );
}

console.log('PASS backend Maven modules and dependency direction');
