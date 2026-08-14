import assert from 'node:assert/strict';
import * as path from 'node:path';
import test from 'node:test';
import {
  validateWorkflowDirectory,
  validateWorkflowSource,
} from './workflow-trust-policy.js';

function messages(source: string): string[] {
  return validateWorkflowSource(source, 'fixture.yml').map((violation) => violation.message);
}

test('treats fork and same-repository pull requests as read-only', () => {
  const source = `
    name: Fixture
    on:
      pull_request:
    permissions:
      contents: read
    jobs:
      build:
        permissions:
          contents: read
        steps:
          - uses: actions/checkout@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
          - run: ./gradlew test
  `;
  assert.deepEqual(messages(source), []);
});

test('rejects write-capable direct pull-request execution', () => {
  const source = `
    on: [pull_request]
    jobs:
      build:
        permissions:
          checks: write
        steps:
          - run: ./gradlew test
  `;
  assert.deepEqual(messages(source), ['pull_request execution has write permission']);
});

test('rejects direct security-events writes and local actions', () => {
  const source = `
    on: [pull_request]
    jobs:
      scan:
        permissions:
          security-events: write
        steps:
          - uses: ./.github/actions/scan
  `;
  assert.deepEqual(messages(source), [
    'pull_request execution has write permission',
    'write-capable trusted job invokes a local action',
  ]);
});

test('allows a trusted pull-request target checkout and rejects a PR ref', () => {
  const safe = `
    on: [pull_request_target]
    jobs:
      publish:
        permissions:
          checks: write
        steps:
          - uses: actions/checkout@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            with:
              ref: \${{ github.event.repository.default_branch }}
          - uses: actions/github-script@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
  `;
  assert.deepEqual(messages(safe), []);

  const unsafe = safe.replace('github.event.repository.default_branch', 'github.event.pull_request.head.sha');
  assert.deepEqual(messages(unsafe), ['write-capable trusted job checks out an untrusted ref']);
});

test('requires a recognised validator in a workflow_run publisher', () => {
  const unsafe = `
    on:
      workflow_run:
        workflows: [CI]
    jobs:
      publish:
        permissions:
          checks: write
        steps:
          - uses: actions/checkout@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            with:
              ref: \${{ github.sha }}
          - run: node source-artifact.js
  `;
  assert.deepEqual(messages(unsafe), ['workflow_run publisher has no recognised validation entry point']);

  const safe = unsafe.replace('node source-artifact.js', 'node junit-publisher.js');
  assert.deepEqual(messages(safe), []);
});

test('rejects untrusted event interpolation in a write-capable trusted job', () => {
  const source = `
    on: [workflow_run]
    jobs:
      publish:
        permissions:
          checks: write
        steps:
          - uses: actions/checkout@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            with:
              ref: \${{ github.sha }}
          - uses: actions/github-script@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            with:
              script: node junit-publisher.js
          - run: echo \${{ github.event.workflow_run.head_branch }}
  `;
  assert.deepEqual(messages(source), ['run command interpolates untrusted event text']);
});

test('the repository workflows satisfy the trust policy', () => {
  const workflowDirectory = path.resolve(
    process.cwd(),
    path.basename(process.cwd()) === '.github' ? 'workflows' : '.github/workflows',
  );
  assert.deepEqual(validateWorkflowDirectory(workflowDirectory), []);
});
