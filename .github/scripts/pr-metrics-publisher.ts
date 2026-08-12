import * as fs from 'node:fs';
import * as path from 'node:path';
import {
  deserializeMetricsResult,
} from '../actions/pr-metrics-comment/lib/metrics-result.js';
import { upsertComment } from '../actions/pr-metrics-comment/lib/comment.js';
import { buildReport } from '../actions/pr-metrics-comment/lib/report.js';
import type { ActionContext, Octokit, WorkflowRunArtifact, WorkflowRunData } from './shared/action-context.js';
import { readMetricsArtifact } from './pr-metrics-publisher-storage.js';
import {
  PublicationRejectedError,
  selectMetricsArtifact,
  validateResultProvenance,
  validateWorkflowSource,
} from './pr-metrics-publisher-validation.js';
import type { WorkflowSource } from './pr-metrics-publisher-validation.js';
import { fetchWorkflowRunContext } from './shared/run-context.js';

const COVERAGE_GATE_FILE = 'gradle/coverage.gradle';
const JAR_GROWTH_WARNING_THRESHOLD = 10;

const REPORT_ARTIFACT_LINKS = new Map([
  ['dokka-preview', 'dokka'],
  ['gradle-test-results', 'tests'],
]);

function rejectPublication(message: string): never {
  throw new PublicationRejectedError(message);
}

function requirePositiveInteger(value: number, message: string): number {
  if (!Number.isSafeInteger(value) || value < 1) rejectPublication(message);

  return value;
}

export interface ResolvedMetricsSource extends WorkflowSource {
  artifactId: number;
  artifactName: string;
  artifacts: WorkflowRunArtifact[];
}

async function resolvePullRequestNumber({
  github,
  owner,
  repo,
  run,
}: {
  github: Octokit;
  owner: string;
  repo: string;
  run: WorkflowRunData;
}): Promise<number> {
  if (!Array.isArray(run.pull_requests)) rejectPublication('workflow run pull requests are invalid');

  if (run.pull_requests.length > 1) rejectPublication('workflow run must identify exactly one pull request');

  if (run.pull_requests.length === 1) {
    const pullRequest = run.pull_requests[0];
    if (pullRequest == null) rejectPublication('workflow run has an invalid pull request');

    return requirePositiveInteger(
      pullRequest.number,
      'workflow run has an invalid pull request',
    );
  }

  const associatedPullRequests = await github.paginate(
    github.rest.repos.listPullRequestsAssociatedWithCommit,
    {
      owner,
      repo,
      commit_sha: run.head_sha,
      per_page: 100,
    },
  );

  if (associatedPullRequests.length !== 1) rejectPublication('workflow run must identify exactly one pull request');

  const pullRequest = associatedPullRequests[0];
  if (pullRequest == null) rejectPublication('workflow run has an invalid pull request');

  return requirePositiveInteger(
    pullRequest.number,
    'workflow run has an invalid pull request',
  );
}

async function resolveSource({
  github,
  context,
}: {
  github: Octokit;
  context: ActionContext['context'];
}): Promise<ResolvedMetricsSource> {
  const eventRun = context.payload?.workflow_run;

  if (!eventRun || !Number.isSafeInteger(eventRun.id) || eventRun.id < 1) {
    rejectPublication('workflow run event is invalid');
  }

  const { owner, repo } = context.repo;

  const { repository, run, workflow } = await fetchWorkflowRunContext(rejectPublication, {
    github,
    owner,
    repo,
    eventRun,
  });

  const pullNumber = await resolvePullRequestNumber({
    github,
    owner,
    repo,
    run,
  });

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
    pullNumber,
  });

  const artifacts = await github.paginate(
    github.rest.actions.listWorkflowRunArtifacts,
    {
      owner,
      repo,
      run_id: source.runId,
      per_page: 100,
    },
  );

  const artifact = selectMetricsArtifact({
    artifacts,
    source,
  });

  return {
    ...source,
    artifactId: artifact.id,
    artifactName: artifact.name,
    artifacts,
  };
}

async function resolvePublishableSource({
  github,
  context,
  core,
}: {
  github: Octokit;
  context: ActionContext['context'];
  core: ActionContext['core'];
}): Promise<ResolvedMetricsSource | null> {
  try {
    return await resolveSource({
      github,
      context,
    });
  } catch (error) {
    if (!(error instanceof PublicationRejectedError)) throw error;

    core.warning(`Metrics publication skipped: ${error.message}`);
    return null;
  }
}

function setSourceOutputs(core: ActionContext['core'], source: ResolvedMetricsSource): void {
  core.setOutput('publish', 'true');
  core.setOutput('artifact_name', source.artifactName);
  core.setOutput('run_id', String(source.runId));
  core.setOutput('run_attempt', String(source.runAttempt));
  core.setOutput('pull_number', String(source.pullRequest));
  core.setOutput('artifact_id', String(source.artifactId));
  core.setOutput('head_sha', source.headSha);
}

async function validateSource({
  github,
  context,
  core,
}: {
  github: Octokit;
  context: ActionContext['context'];
  core: ActionContext['core'];
}): Promise<void> {
  const source = await resolvePublishableSource({
    github,
    context,
    core,
  });

  if (!source) {
    core.setOutput('publish', 'false');
    return;
  }

  setSourceOutputs(core, source);
}

function readCoverageGateThreshold(filePath: string): number | null {
  if (!fs.existsSync(filePath)) return null;

  const contents = fs.readFileSync(filePath, 'utf8');
  const match = contents.match(/coverageLineThreshold\s*=\s*(\d+)/);
  const threshold = match?.[1];

  return threshold != null ? Number.parseInt(threshold, 10) : null;
}

function safeRefLabel(ref: string): string {
  return ref.replace(/[^A-Za-z0-9._/-]/g, '_');
}

function buildArtifactLinks({
  context,
  source,
}: {
  context: ActionContext['context'];
  source: ResolvedMetricsSource;
}): Record<string, string> {
  const runUrl = `${context.serverUrl}/${source.repository}/actions/runs/${source.runId}`;
  const links: Record<string, string> = {
    run: runUrl,
  };

  for (const artifact of source.artifacts) {
    const key = REPORT_ARTIFACT_LINKS.get(artifact.name);

    if (key && Number.isSafeInteger(artifact.id)) links[key] = `${runUrl}/artifacts/${artifact.id}`;
  }

  return links;
}

async function publishMetrics({
  github,
  context,
  core,
  artifactDirectory,
}: {
  github: Octokit;
  context: ActionContext['context'];
  core: ActionContext['core'];
  artifactDirectory: string;
}): Promise<boolean> {
  const source = await resolvePublishableSource({
    github,
    context,
    core,
  });

  if (!source) return false;

  const result = validateResultProvenance(
    readMetricsArtifact(artifactDirectory),
    source,
  );

  const metrics = deserializeMetricsResult(result);
  const workspace = process.env.GITHUB_WORKSPACE || process.cwd();

  const { body, warnings } = buildReport({
    ...metrics,
    gateThreshold: readCoverageGateThreshold(
      path.join(workspace, COVERAGE_GATE_FILE),
    ),
    growthThreshold: JAR_GROWTH_WARNING_THRESHOLD,
    baseLabel: `${safeRefLabel(source.baseRef)}@${source.baseSha.slice(0, 7)}`,
    headSha: source.headSha.slice(0, 7),
    links: buildArtifactLinks({
      context,
      source,
    }),
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
  return true;
}

export {
  publishMetrics,
  resolveSource,
  validateSource,
};
