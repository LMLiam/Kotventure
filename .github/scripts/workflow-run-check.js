'use strict';

const { createValidators } = require('./shared/validation.js');

const KIND_PATTERN = /^[a-z0-9-]{1,48}$/;
const EXTERNAL_ID_PREFIX = 'workflow-run-check';
const CHECK_APP_SLUG = 'github-actions';
const WORKFLOW_RESULTS = new Set([
  'success',
  'failure',
  'cancelled',
  'skipped',
  'timed_out',
]);

const {
  requireBoundedInteger,
  requireEqual,
  requireObject,
  requireSha,
  requireText,
} = createValidators((message) => {
  throw new Error(message);
});

function workflowRunUrl(context) {
  const serverUrl = requireText(context?.serverUrl, 'GitHub server URL', 512);
  const repository = `${requireText(context?.repo?.owner, 'repository owner', 100)}/${requireText(context?.repo?.repo, 'repository name', 100)}`;
  const runId = requireBoundedInteger(Number(context?.runId), 'workflow run id');
  const runAttempt = requireBoundedInteger(Number(context?.runAttempt), 'workflow run attempt');
  return `${serverUrl}/${repository}/actions/runs/${runId}/attempts/${runAttempt}`;
}

function buildCheckExternalId({ kind, runId, runAttempt, headSha }) {
  if (typeof kind !== 'string' || !KIND_PATTERN.test(kind)) throw new Error('check kind is invalid');
  return `${EXTERNAL_ID_PREFIX}:${kind}:${requireBoundedInteger(Number(runId), 'workflow run id')}:${requireBoundedInteger(Number(runAttempt), 'workflow run attempt')}:${requireSha(headSha, 'check head SHA')}`;
}

function validateCreatedCheck(check, expected) {
  requireObject(check, 'created check');
  requireBoundedInteger(check.id, 'created check id');
  requireEqual(check.name, expected.name, 'created check name');
  requireEqual(check.head_sha, expected.headSha, 'created check head SHA');
  requireEqual(check.external_id, expected.externalId, 'created check external id');
  requireEqual(check.app?.slug, CHECK_APP_SLUG, 'created check application');
}

async function createWorkflowCheck({
  github,
  context,
  name,
  headSha,
  externalId,
  summary,
}) {
  const owner = requireText(context?.repo?.owner, 'repository owner', 100);
  const repo = requireText(context?.repo?.repo, 'repository name', 100);
  const trustedName = requireText(name, 'check name', 100);
  const trustedHeadSha = requireSha(headSha, 'check head SHA');
  const trustedExternalId = requireText(externalId, 'check external id', 256);
  const trustedSummary = requireText(summary, 'check summary', 65_535);
  const response = await github.rest.checks.create({
    owner,
    repo,
    name: trustedName,
    head_sha: trustedHeadSha,
    status: 'in_progress',
    external_id: trustedExternalId,
    details_url: workflowRunUrl(context),
    started_at: new Date().toISOString(),
    output: {
      title: trustedName,
      summary: trustedSummary,
    },
  });
  const check = response?.data;
  validateCreatedCheck(check, {
    name: trustedName,
    headSha: trustedHeadSha,
    externalId: trustedExternalId,
  });
  return {
    id: check.id,
    externalId: trustedExternalId,
    headSha: trustedHeadSha,
    name: trustedName,
  };
}

function workflowResultConclusion(result) {
  if (typeof result !== 'string' || !WORKFLOW_RESULTS.has(result)) throw new Error('workflow result is invalid');
  return result;
}

async function completeWorkflowCheck({
  github,
  context,
  checkId,
  name,
  headSha,
  externalId,
  conclusion,
  summary,
}) {
  const owner = requireText(context?.repo?.owner, 'repository owner', 100);
  const repo = requireText(context?.repo?.repo, 'repository name', 100);
  const trustedCheckId = requireBoundedInteger(checkId, 'check id');
  const trustedName = requireText(name, 'check name', 100);
  const trustedHeadSha = requireSha(headSha, 'check head SHA');
  const trustedExternalId = requireText(externalId, 'check external id', 256);
  const trustedConclusion = workflowResultConclusion(conclusion);
  const trustedSummary = requireText(summary, 'check summary', 65_535);
  const response = await github.rest.checks.get({
    owner,
    repo,
    check_run_id: trustedCheckId,
  });
  const check = requireObject(response?.data, 'registered check');
  requireEqual(check.id, trustedCheckId, 'check id');
  requireEqual(check.name, trustedName, 'check name');
  requireEqual(check.head_sha, trustedHeadSha, 'check head SHA');
  requireEqual(check.external_id, trustedExternalId, 'check external id');
  requireEqual(check.app?.slug, CHECK_APP_SLUG, 'check application');

  await github.rest.checks.update({
    owner,
    repo,
    check_run_id: trustedCheckId,
    status: 'completed',
    conclusion: trustedConclusion,
    completed_at: new Date().toISOString(),
    details_url: workflowRunUrl(context),
    output: {
      title: trustedName,
      summary: trustedSummary,
    },
  });
}

module.exports = {
  buildCheckExternalId,
  completeWorkflowCheck,
  createWorkflowCheck,
  workflowResultConclusion,
};
