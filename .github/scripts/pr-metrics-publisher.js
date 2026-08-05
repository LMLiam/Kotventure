'use strict';

const fs = require('fs');
const path = require('path');
const {
  deserializeMetricsResult,
} = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const { upsertComment } = require('../actions/pr-metrics-comment/lib/comment.js');
const { buildReport } = require('../actions/pr-metrics-comment/lib/report.js');
const { readMetricsArtifact } = require('./pr-metrics-publisher-storage.js');
const {
  PublicationRejectedError,
  selectMetricsArtifact,
  validateResultProvenance,
  validateWorkflowSource,
} = require('./pr-metrics-publisher-validation.js');

async function resolveSource({ github, context }) {
  const eventRun = context.payload.workflow_run;
  if (!eventRun || !Number.isSafeInteger(eventRun.id) || eventRun.id < 1) {
    throw new PublicationRejectedError('workflow run event is invalid');
  }
  const { owner, repo } = context.repo;
  const [{ data: repository }, { data: run }] = await Promise.all([
    github.rest.repos.get({ owner, repo }),
    github.rest.actions.getWorkflowRun({ owner, repo, run_id: eventRun?.id }),
  ]);
  const { data: workflow } = await github.rest.actions.getWorkflow({
    owner,
    repo,
    workflow_id: run.workflow_id,
  });
  let pullNumber;
  if (Array.isArray(run.pull_requests) && run.pull_requests.length === 1) {
    pullNumber = run.pull_requests[0].number;
  } else if (Array.isArray(run.pull_requests) && run.pull_requests.length === 0) {
    const associatedPullRequests = await github.paginate(
      github.rest.repos.listPullRequestsAssociatedWithCommit,
      {
        owner,
        repo,
        commit_sha: run.head_sha,
        per_page: 100,
      },
    );
    if (associatedPullRequests.length !== 1) {
      throw new PublicationRejectedError('workflow run must identify exactly one pull request');
    }
    pullNumber = associatedPullRequests[0].number;
  }
  if (!Number.isSafeInteger(pullNumber) || pullNumber < 1) {
    throw new PublicationRejectedError('workflow run has no pull request');
  }
  const { data: pullRequest } = await github.rest.pulls.get({
    owner,
    repo,
    pull_number: pullNumber,
  });
  const source = validateWorkflowSource({
    eventRun,
    run,
    workflow,
    repository,
    pullRequest,
    defaultBranch: repository.default_branch,
    pullNumber,
  });
  const artifacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts, {
    owner,
    repo,
    run_id: source.runId,
    per_page: 100,
  });
  const trustedSource = { ...source, repositoryId: repository.id };
  const metricsArtifact = selectMetricsArtifact({ artifacts, run, source: trustedSource });
  return {
    ...trustedSource,
    repositoryId: repository.id,
    artifactId: metricsArtifact.id,
    artifactName: metricsArtifact.name,
    artifacts,
  };
}

function setSourceOutputs(core, source) {
  core.setOutput('publish', 'true');
  core.setOutput('artifact_name', source.artifactName);
  core.setOutput('run_id', String(source.runId));
  core.setOutput('run_attempt', String(source.runAttempt));
  core.setOutput('pull_number', String(source.pullRequest));
  core.setOutput('artifact_id', String(source.artifactId));
}

async function validateSource({ github, context, core }) {
  try {
    const source = await resolveSource({ github, context });
    setSourceOutputs(core, source);
  } catch (error) {
    if (error instanceof PublicationRejectedError) {
      core.warning(`Metrics publication skipped: ${error.message}`);
      core.setOutput('publish', 'false');
      return;
    }
    throw error;
  }
}

function readGateThreshold(gateFile) {
  if (!gateFile || !fs.existsSync(gateFile)) {
    return null;
  }
  const match = fs.readFileSync(gateFile, 'utf8').match(/coverageLineThreshold\s*=\s*(\d+)/);
  return match ? Number.parseInt(match[1], 10) : null;
}

function safeRefLabel(ref) {
  return ref.replace(/[^A-Za-z0-9._\/-]/g, '_');
}

function artifactLinks({ context, source }) {
  const runUrl = `${context.serverUrl}/${source.repository}/actions/runs/${source.runId}`;
  const links = { run: runUrl };
  for (const [name, key] of [['dokka-preview', 'dokka'], ['gradle-test-results', 'tests']]) {
    const artifact = source.artifacts.find((candidate) => candidate.name === name);
    if (artifact && Number.isSafeInteger(artifact.id)) {
      links[key] = `${runUrl}/artifacts/${artifact.id}`;
    }
  }
  return links;
}

async function publishMetrics({ github, context, core, artifactDirectory }) {
  let source;
  try {
    source = await resolveSource({ github, context });
  } catch (error) {
    if (error instanceof PublicationRejectedError) {
      core.warning(`Metrics publication skipped: ${error.message}`);
      return;
    }
    throw error;
  }
  const result = readMetricsArtifact(artifactDirectory);
  validateResultProvenance(result, source);
  const metrics = deserializeMetricsResult(result);
  const workspace = process.env.GITHUB_WORKSPACE || process.cwd();
  const { body, warnings } = buildReport({
    ...metrics,
    gateThreshold: readGateThreshold(path.join(workspace, 'gradle/coverage.gradle')),
    growthThreshold: 10,
    baseLabel: `${safeRefLabel(source.baseRef)}@${source.baseSha.slice(0, 7)}`,
    headSha: source.headSha.slice(0, 7),
    links: artifactLinks({ context, source }),
  });
  for (const warning of warnings) {
    core.warning(warning);
  }
  await upsertComment({
    github,
    context,
    body,
    pullNumber: source.pullRequest,
  });
  core.info(`Published CI metrics for PR #${source.pullRequest}`);
}

module.exports = {
  publishMetrics,
  resolveSource,
  validateSource,
};
