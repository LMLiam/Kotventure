import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';
import test from 'node:test';

const repositoryRoot = path.resolve(__dirname, '..', '..');

function readRepositoryFile(relativePath: string): string {
  return fs.readFileSync(path.join(repositoryRoot, relativePath), 'utf8');
}

function readJob(workflow: string, name: string, nextName?: string): string {
  const start = workflow.indexOf(`  ${name}:\n`);
  assert.notEqual(start, -1, `job ${name} is missing`);
  const end = nextName == null ? workflow.length : workflow.indexOf(`  ${nextName}:\n`, start);
  assert.notEqual(end, -1, `job ${nextName} is missing`);
  assert.ok(end > start, `job ${name} must precede job ${nextName}`);
  const section = workflow.slice(start, end);
  assert.ok(section.trim().length > 0, `job ${name} is empty`);
  return section;
}

test('the pull-request CI workflow does not run or publish Qodana results', () => {
  const workflow = readRepositoryFile('.github/workflows/ci.yml');

  assert.doesNotMatch(workflow, /^  qodana:/m);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.doesNotMatch(workflow, /Upload Qodana/);
});

test('the Qodana scan workflow is read-only and disables GitHub side effects', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana.yml');
  const register = readJob(workflow, 'register', 'analyse');
  const analyse = readJob(workflow, 'analyse');

  assert.match(workflow, /pull_request_target:/);
  assert.match(workflow, /types: \[opened, synchronize, reopened\]/);
  assert.match(workflow, /group: qodana-\$\{\{ github\.event\.pull_request\.number \}\}/);
  assert.doesNotMatch(workflow, /workflow_run:/);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.doesNotMatch(register, /(?:actions|checks|contents|pull-requests|security-events):\s*write/);
  assert.doesNotMatch(analyse, /(?:actions|checks|contents|pull-requests|security-events):\s*write/);
  assert.doesNotMatch(workflow, /report-registration-failure/);
  assert.match(register, /resolvePullRequestEventSource/);
  assert.doesNotMatch(register, /createQodanaCheck/);
  assert.match(workflow, /upload-result:\s*false/);
  assert.match(workflow, /use-annotations:\s*false/);
  assert.match(workflow, /post-pr-comment:\s*false/);
  assert.match(workflow, /push-fixes:\s*none/);
  assert.match(workflow, /--project-dir source\s+--config qodana\.yaml/);
  assert.doesNotMatch(workflow, /--config source\/qodana\.yaml/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /git -C source rev-parse HEAD/);
  assert.match(analyse, /repository:\s*\$\{\{ needs\.register\.outputs\.head_repository \}\}/);
  assert.match(analyse, /ref:\s*\$\{\{ needs\.register\.outputs\.head_sha \}\}/);
  assert.match(workflow, /cp --remove-destination -- qodana\.yaml source\/qodana\.yaml/);
  assert.doesNotMatch(workflow, /normalize-qodana-sarif\.sh/);
});

test('trusted Qodana runs are limited to trusted default-branch refs', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana-trusted.yml');
  const analyse = readJob(workflow, 'analyse');

  assert.match(workflow, /push:/);
  assert.match(workflow, /branches: \[master\]/);
  assert.match(workflow, /schedule:/);
  assert.match(workflow, /workflow_dispatch:/);
  assert.doesNotMatch(workflow, /workflow_run:/);
  assert.doesNotMatch(workflow, /merge_group/);
  assert.doesNotMatch(workflow, /pull_request_target:/);
  assert.match(analyse, /security-events:\s*write/);
  assert.doesNotMatch(analyse, /checks:\s*write/);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.doesNotMatch(workflow, /resolveTrustedCiRun/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /--project-dir source\s+--config qodana\.yaml/);
  assert.doesNotMatch(workflow, /--config source\/qodana\.yaml/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.ref \}\}/);
  assert.match(workflow, /sha:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /checkout_path:\s*source/);
  assert.match(workflow, /cache-read-only: true/);
  assert.match(workflow, /use-caches: true/);
  assert.doesNotMatch(workflow, /Report trusted Qodana check/);
});

test('trusted metrics publication reports its result against the pull-request head', () => {
  const workflow = readRepositoryFile('.github/workflows/pr-metrics-publish.yml');

  assert.match(workflow, /workflow_run:/);
  assert.match(workflow, /checks:\s*write/);
  assert.match(workflow, /Register the metrics publication check/);
  assert.match(workflow, /HEAD_SHA: \$\{\{ steps\.source\.outputs\.head_sha \}\}/);
  assert.match(workflow, /Complete the metrics publication check/);
});
