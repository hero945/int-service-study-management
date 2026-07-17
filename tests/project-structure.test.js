const assert = require('assert');
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

const requiredFiles = [
  'index.html',
  'src/index.template.html',
  'src/components/coverpage.template.html',
  'src/components/coverpage.component.js',
  'src/styles/main.css',
  'src/vendor/react.production.min.js',
  'src/vendor/react-dom.production.min.js',
  'src/vendor/dc-runtime.js',
  'scripts/build.js',
];

for (const file of requiredFiles) {
  assert(fs.existsSync(path.join(root, file)), `missing standard project file: ${file}`);
}

const index = fs.readFileSync(path.join(root, 'index.html'), 'utf8');
assert(index.includes('src/styles/main.css'), 'index.html must load the external stylesheet');
assert(index.includes('src/vendor/react.production.min.js'), 'React runtime must be externalized');
assert(index.includes('src/vendor/react-dom.production.min.js'), 'ReactDOM runtime must be externalized');
assert(index.includes('src/vendor/dc-runtime.js'), 'DC runtime must be externalized');
assert(index.includes('<x-dc>'), 'built page must retain the DC component root');
assert(/<script[^>]*data-dc-script[^>]*>[\s\S]*class Component extends DCLogic/.test(index), 'built page must contain the compiled component logic');
assert(!index.includes('@license React'), 'third-party React source must not remain embedded in index.html');

const component = fs.readFileSync(path.join(root, 'src/components/coverpage.component.js'), 'utf8');
assert(component.includes('class Component extends DCLogic'), 'component source must remain independently editable');

console.log('PASS standard project structure and generated entrypoint');
