import * as fs from 'node:fs';
import * as path from 'node:path';
import { sanitizeModule } from './names.js';
import { countClassEntries } from './zip.js';

export interface JarInfo {
  readonly size: number;
  readonly classes: number | null;
}

function parseModuleJar(filename: string): { module: string; version: string } | null {
  if (!filename.endsWith('.jar') || filename.includes('-sources') || filename.includes('-javadoc')) return null;
  const match = filename.match(/^kotventure-(.+)-(\d+\.\d+\.\d+(?:[-+][A-Za-z0-9.]+)?)\.jar$/);
  if (!match) return null;
  const module = match[1];
  const version = match[2];
  if (!module || !version) return null;
  return { module: sanitizeModule(module), version };
}

function versionKey(version: string): string {
  return version.split(/[.+-]/).map((part) => {
    const n = Number.parseInt(part, 10);
    return Number.isFinite(n) ? n.toString().padStart(8, '0') : part;
  }).join('.');
}

export async function collectJars(rootDir: string): Promise<Map<string, JarInfo>> {
  const sizes = new Map<string, JarInfo>();
  const bestVersion = new Map<string, string>();
  if (!fs.existsSync(rootDir)) return sizes;
  async function walk(dir: string): Promise<void> {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        await walk(full);
        continue;
      }
      if (!entry.isFile()) continue;
      const parsed = parseModuleJar(entry.name);
      if (!parsed) continue;
      const prev = bestVersion.get(parsed.module);
      if (!prev || versionKey(parsed.version) > versionKey(prev)) {
        const size = fs.statSync(full).size;
        const classes = await countClassEntries(fs.readFileSync(full));
        bestVersion.set(parsed.module, parsed.version);
        sizes.set(parsed.module, { size, classes });
      }
    }
  }
  await walk(rootDir);
  return sizes;
}
