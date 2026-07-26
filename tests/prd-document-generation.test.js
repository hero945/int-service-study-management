const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const test = require('node:test');

const root = path.resolve(__dirname, '..');
const sourcePath = path.join(root, 'docs', '临床研发管线管理系统_PRD_v1.0.md');
const outputPath = path.join(root, 'docs', '临床研发管线管理系统_PRD_v1.0.html');
const generatorPath = path.join(root, 'docs', 'generate-prd-html.js');

test('generated PRD uses source metadata and preserves the historical prototype link', () => {
  execFileSync(process.execPath, [generatorPath], { cwd: root, stdio: 'pipe' });

  const markdown = fs.readFileSync(sourcePath, 'utf8');
  const html = fs.readFileSync(outputPath, 'utf8');
  const version = markdown.match(/^> 文档版本：(.+?)\s*$/m)?.[1];

  assert.ok(version, 'PRD source must declare a document version');
  assert.ok(
    html.includes(`<span>版本 ${version}</span>`),
    `generated PRD must display source version ${version}`,
  );
  assert.ok(
    html.includes('href="../管线总览%20Coverpage.dc%282%29.html"'),
    'historical prototype link must keep its complete filename',
  );
  assert.match(
    html,
    /<div class="source-meta">[\s\S]*当前实现基线：Git/,
    'document metadata must render as a dedicated block',
  );
});
