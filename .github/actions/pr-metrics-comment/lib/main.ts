import * as fs from 'node:fs';
import { collect } from './collect.js';
import type { ActionContext } from '../../../scripts/shared/action-context.js';

async function run({ github, context, core }: ActionContext): Promise<void> {
  const result = await collect({ env: process.env, context, github, core });
  if (result === null) return;
  const outputPath = process.env.OUTPUT_PATH;
  if (!outputPath) throw new Error('OUTPUT_PATH is required');
  fs.writeFileSync(outputPath, JSON.stringify(result), 'utf8');
  core.info(`Wrote PR metrics result to ${outputPath}`);
}

export = run;
