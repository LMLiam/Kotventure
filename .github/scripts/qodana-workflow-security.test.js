'use strict';

const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const assert = require('node:assert/strict');

const repositoryRoot = path.resolve(__dirname, '..', '..');

function readRepositoryFile(relativePath) {
  return fs.readFileSync(path.join(repositoryRoot, relativePath), 'utf8');
}

test('the pull-request CI workflow does not run or publish Qodana results', () => {
  const workflow = readRepositoryFile('.github/workflows/ci.yml');

  assert.doesNotMatch(workflow, /^  qodana:/m);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.doesNotMatch(workflow, /Upload Qodana/);
});

test('the Qodana scan workflow is read-only and disables GitHub side effects', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana.yml');

  assert.match(workflow, /workflow_run:/);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.doesNotMatch(workflow, /(?:actions|checks|contents|pull-requests|security-events):\s*write/);
  assert.match(workflow, /upload-result:\s*false/);
  assert.match(workflow, /use-annotations:\s*false/);
  assert.match(workflow, /post-pr-comment:\s*false/);
  assert.match(workflow, /push-fixes:\s*none/);
  assert.match(workflow, /--project-dir source\s+--config qodana\.yaml/);
  assert.doesNotMatch(workflow, /--config source\/qodana\.yaml/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /git -C source rev-parse HEAD/);
  assert.match(workflow, /cp --remove-destination -- qodana\.yaml source\/qodana\.yaml/);
  assert.doesNotMatch(workflow, /normalize-qodana-sarif\.sh/);
});

test('only the trusted publication workflow can upload Qodana SARIF', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana-publish.yml');

  assert.match(workflow, /workflow_run:/);
  assert.match(workflow, /security-events:\s*write/);
  assert.match(workflow, /actions\/checkout@/);
  assert.match(workflow, /ref:\s*\$\{\{ github\.sha \}\}/);
  assert.match(workflow, /group: qodana-publication-\$\{\{ github\.event\.workflow_run\.id \}\}-\$\{\{ github\.event\.workflow_run\.run_attempt \}\}/);
  assert.match(workflow, /github\/codeql-action\/upload-sarif@/);
  assert.match(workflow, /CODEQL_ACTION_ANALYSIS_KEY: .github\/workflows\/ci\.yml:qodana/);
});

test('trusted Qodana runs are limited to trusted CI events', () => {
  const workflow = readRepositoryFile('.github/workflows/qodana-trusted.yml');

  assert.match(workflow, /workflow_run:/);
  assert.match(workflow, /event == 'push'/);
  assert.doesNotMatch(workflow, /merge_group/);
  assert.match(workflow, /security-events:\s*write/);
  assert.doesNotMatch(workflow, /QODANA_TOKEN/);
  assert.match(workflow, /resolveTrustedCiRun/);
  assert.match(workflow, /ref:\s*\$\{\{ steps\.source\.outputs\.head_sha \}\}/);
  assert.match(workflow, /--project-dir source\s+--config qodana\.yaml/);
  assert.doesNotMatch(workflow, /--config source\/qodana\.yaml/);
  assert.match(workflow, /ref:\s*refs\/heads\/\$\{\{ github\.event\.repository\.default_branch \}\}/);
  assert.match(workflow, /sha:\s*\$\{\{ steps\.source\.outputs\.head_sha \}\}/);
  assert.match(workflow, /checkout_path:\s*source/);
});
