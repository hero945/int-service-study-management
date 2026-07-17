import { cp, mkdir, readFile, rm, stat } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const frontendRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const sourceDirectory = resolve(frontendRoot, 'src');
const outputDirectory = resolve(frontendRoot, 'dist');
const requiredAssets = ['index.html', 'app.js', 'app.css', 'overrides.css'];

await rm(outputDirectory, { recursive: true, force: true });
await mkdir(outputDirectory, { recursive: true });
await cp(sourceDirectory, outputDirectory, { recursive: true });

for (const asset of requiredAssets) {
  const assetPath = resolve(outputDirectory, asset);
  const assetStats = await stat(assetPath);
  if (!assetStats.isFile() || assetStats.size === 0) {
    throw new Error(`Invalid frontend build asset: ${asset}`);
  }
}

const indexHtml = await readFile(resolve(outputDirectory, 'index.html'), 'utf8');
for (const reference of ['/app.js', '/app.css', '/overrides.css']) {
  if (!indexHtml.includes(reference)) {
    throw new Error(`Frontend entrypoint is missing asset reference: ${reference}`);
  }
}

console.log(`Built ${requiredAssets.length} frontend assets into ${outputDirectory}`);
