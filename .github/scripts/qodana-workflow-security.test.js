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
  const end = nextName == null ? workflow.length : workflow.indexOf(`  ${nextName}:\n`, start);
  assert.notEqual(start, -1, `job ${name} is missing`);
  assert.notEqual(end, -1, `job ${nextName} is missing`);
  return workflow.slice(start, end);
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
  const analyse = readJob(workflow, 'analyse', 'report-registration-failure');
  const registrationFailure = readJob(workflow, 'report-registration-failure');

  assert.match(workflow, /workflow_run:/);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.match(register, /checks:\s*write/);
  assert.doesNotMatch(analyse, /(?:actions|checks|contents|pull-requests|security-events):\s*write/);
  assert.match(register, /registerQodanaCheck/);
  assert.match(register, /Upload the Qodana check registration/);
  assert.match(registrationFailure, /needs\.register\.result != 'success'/);
  assert.match(registrationFailure, /Complete the failed Qodana registration check/);
  assert.match(workflow, /upload-result:\s*false/);
  assert.match(workflow, /use-annotations:\s*false/);
  assert.match(workflow, /post-pr-comment:\s*false/);
  assert.match(workflow, /push-fixes:\s*none/);
  assert.match(workflow, /--project-dir source\s+--config qodana\.yaml/);
  assert.doesNotMatch(workflow, /--config source\/qodana\.yaml/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /git -C source rev-parse HEAD/);
  assert.match(analyse, /ref:\s*\$\{\{ needs\.register\.outputs\.head_sha \}\}/);
  assert.match(workflow, /cp --remove-destination -- qodana\.yaml source\/qodana\.yaml/);
  assert.doesNotMatch(workflow, /normalize-qodana-sarif\.sh/);
});

test('only the trusted publication workflow can upload Qodana SARIF', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana-publish.yml');

  assert.match(workflow, /workflow_run:/);
  assert.match(workflow, /security-events:\s*write/);
  assert.match(workflow, /checks:\s*write/);
  assert.match(workflow, /actions\/checkout@/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /group: qodana-publication-\$\{\{ github\.event\.workflow_run\.id \}\}-\$\{\{ github\.event\.workflow_run\.run_attempt \}\}/);
  assert.match(workflow, /github\/codeql-action\/upload-sarif@/);
  assert.match(workflow, /CODEQL_ACTION_ANALYSIS_KEY: .github\/workflows\/ci\.yml:qodana/);
  assert.match(workflow, /Complete the pull-request Qodana check/);
});

test('trusted Qodana runs are limited to trusted CI events', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana-trusted.yml');
  const register = readJob(workflow, 'register', 'analyse');
  const analyse = readJob(workflow, 'analyse', 'report');
  const report = readJob(workflow, 'report');

  assert.match(workflow, /workflow_run:/);
  assert.match(workflow, /event == 'push'/);
  assert.doesNotMatch(workflow, /merge_group/);
  assert.match(workflow, /security-events:\s*write/);
  assert.match(register, /checks:\s*write/);
  assert.doesNotMatch(analyse, /checks:\s*write/);
  assert.match(report, /checks:\s*write/);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.match(workflow, /resolveTrustedCiRun/);
  assert.match(workflow, /ref:\s*\$\{\{ needs\.register\.outputs\.head_sha \}\}/);
  assert.match(workflow, /--project-dir source\s+--config qodana\.yaml/);
  assert.doesNotMatch(workflow, /--config source\/qodana\.yaml/);
  assert.match(workflow, /ref:\s*refs\/heads\/\$\{\{ github\.event\.repository\.default_branch \}\}/);
  assert.match(workflow, /sha:\s*\$\{\{ needs\.register\.outputs\.head_sha \}\}/);
  assert.match(workflow, /checkout_path:\s*source/);
  assert.match(report, /Complete the trusted Qodana check/);
});

test('trusted metrics publication reports its result against the pull-request head', () => {
  const workflow = readRepositoryFile('.github/workflows/pr-metrics-publish.yml');

  assert.match(workflow, /workflow_run:/);
  assert.match(workflow, /checks:\s*write/);
  assert.match(workflow, /Register the metrics publication check/);
  assert.match(workflow, /HEAD_SHA: \$\{\{ steps\.source\.outputs\.head_sha \}\}/);
  assert.match(workflow, /Complete the metrics publication check/);
});
