// Copies the production Angular build into the Spring Boot static folder,
// so `mvn package` produces one jar that serves both the UI and the API.
import { cpSync, existsSync, rmSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const source = resolve(root, 'dist/frontend/browser');
const target = resolve(root, '../backend/src/main/resources/static');

if (!existsSync(source)) {
  console.error(`No build found at ${source}. Run "npm run build" first.`);
  process.exit(1);
}

rmSync(target, { recursive: true, force: true });
cpSync(source, target, { recursive: true });
console.log(`Copied UI build to ${target}`);
