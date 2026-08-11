'use strict';

const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const assert = require('node:assert/strict');

const repositoryRoot = path.resolve(__dirname, '..', '..');

function readRepositoryFile(relativePath) {
  return fs.readFileSync(path.join(repositoryRoot, relativePath), 'utf8');
}

function readJob(workflow, name, nextName) {
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
  assert.match(workflow, /branches: \[master\]/);
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

test('only the trusted publication workflow can upload Qodana SARIF', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana-publish.yml');

  assert.match(workflow, /workflow_run:/);
  assert.match(workflow, /security-events:\s*write/);
  assert.doesNotMatch(workflow, /checks:\s*write/);
  assert.match(workflow, /actions\/checkout@/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /group: qodana-publication-\$\{\{ github\.event\.workflow_run\.id \}\}-\$\{\{ github\.event\.workflow_run\.run_attempt \}\}/);
  assert.match(workflow, /github\/codeql-action\/upload-sarif@f205ea1c3313d32999d8d6a48b4f6530d4437b38/);
  assert.match(workflow, /Verify the SARIF upload root is not a Git worktree/);
  assert.match(workflow, /git -C "\$UPLOAD_ROOT" rev-parse --is-inside-work-tree/);
  assert.match(workflow, /checkout_path:\s*\$\{\{ runner\.temp \}\}\/qodana-publication/);
  assert.match(workflow, /ref:\s*refs\/pull\/\$\{\{ steps\.publication\.outputs\.pull_number \}\}\/head/);
  assert.match(workflow, /sha:\s*\$\{\{ steps\.publication\.outputs\.head_sha \}\}/);
  assert.match(workflow, /CODEQL_ACTION_ANALYSIS_KEY: .github\/workflows\/ci\.yml:qodana/);
  assert.doesNotMatch(workflow, /Checkout pull-request code/);
  assert.doesNotMatch(workflow, /Complete the pull-request Qodana check/);
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
