import type { ActionContext, JobItem, Octokit, RepositoryData, WorkflowData, WorkflowRunData } from './shared/action-context.js';
import { createValidators } from './shared/validation.js';
import { fetchWorkflowRunContext, validateEventRun } from './shared/run-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';
import {
  CI_WORKFLOW_NAME,
  CI_WORKFLOW_PATH,
  JUNIT_BUILD_SHARDS,
  JUNIT_REPORT_NAMES,
  MAX_JUNIT_ANNOTATIONS,
} from './junit-contract.js';
import type { JunitReportKind, JunitBuildShard } from './junit-contract.js';
import {
  aggregateJunitArtifactReports,
  JunitPublicationRejectedError,
  downloadJunitReports,
  selectJunitArtifact,
} from './junit-publisher-storage.js';
import type { JunitAggregate } from './junit-parser.js';
import { junitSummary } from './junit-parser.js';
import type { JunitAnnotation } from './junit-parser.js';
import { resolvePullRequestSource, QodanaSourceRejectedError } from './qodana-source.js';
import type { PullRequestSource } from './qodana-source.js';
import type { QodanaSourceKind } from './qodana-contract.js';
import {
  buildCheckExternalId,
  completeWorkflowCheck,
  ensureWorkflowCheck,
  updateWorkflowCheck,
} from './workflow-run-check.js';
import type {
  WorkflowCheckAnnotation,
  WorkflowCheckReference,
  WorkflowRunCheckContext,
} from './workflow-run-check.js';

const BUILD_JOB_NAMES = new Map<JunitBuildShard, string>([
  ['core', 'Build (core)'],
  ['text', 'Build (text)'],
  ['runtime', 'Build (runtime)'],
]);
const VANILLA_JOB_NAME = 'Vanilla conformance';
const OBSERVATION_INTERVAL_MS = 10_000;
const IN_PROGRESS_DEADLINE_MS = 45 * 60 * 1000;
const COMPLETED_RETRY_COUNT = 12;
const COMPLETED_RETRY_INTERVAL_MS = 5_000;

const {
  requireBoundedInteger,
  requireEqual,
  requireObject,
  requireSha,
  requireString,
} = createValidators((message: string): never => {
  throw new JunitPublicationRejectedError(message);
});

interface JunitSourceContext {
  repository: RepositoryData;
  run: WorkflowRunData;
  workflow: WorkflowData;
  source: PullRequestSource | null;
  stale: boolean;
}

interface ReportCheck {
  kind: JunitReportKind;
  name: string;
  externalId: string;
  reference: WorkflowCheckReference;
}

interface ReportObservation {
  ready: boolean;
  result: string | null;
  jobs: JobItem[];
}

function sourceEvent(eventRun: WorkflowRunEventRecord, run: WorkflowRunData, workflow: WorkflowData, repository: RepositoryData): void {
  validateEventRun((message) => {
    throw new JunitPublicationRejectedError(message);
  }, { eventRun, run });
  requireEqual(eventRun.event, 'pull_request', 'workflow run event');
  requireEqual(run.event, 'pull_request', 'workflow run event');
  requireEqual(run.repository?.full_name, repository.full_name, 'workflow run repository');
  requireEqual(run.repository?.id, repository.id, 'workflow run repository id');
  requireObject(run.head_repository, 'workflow head repository');
  requireSha(run.head_sha, 'workflow run head SHA');
  requireString(run.head_branch, 'workflow run head branch');
  requireEqual(workflow.id, run.workflow_id, 'workflow identity');
  requireEqual(workflow.name, CI_WORKFLOW_NAME, 'workflow name');
  requireEqual(workflow.path, CI_WORKFLOW_PATH, 'workflow path');
  if (eventRun.status != null && eventRun.status !== 'in_progress' && eventRun.status !== 'completed') {
    throw new JunitPublicationRejectedError('workflow run event status is invalid');
  }
  if (eventRun.status === 'completed' && run.status !== 'completed') {
    throw new JunitPublicationRejectedError('workflow run has not completed');
  }
}

async function resolveSource({
  github,
  context,
  eventRun,
}: {
  github: Octokit;
  context: ActionContext['context'];
  eventRun: WorkflowRunEventRecord;
}): Promise<JunitSourceContext> {
  const { repository, run, workflow } = await fetchWorkflowRunContext((message) => {
    throw new JunitPublicationRejectedError(message);
  }, {
    github,
    owner: context.repo.owner,
    repo: context.repo.repo,
    eventRun,
  });
  sourceEvent(eventRun, run, workflow, repository);
  try {
    const source = await resolvePullRequestSource({
      github,
      owner: context.repo.owner,
      repo: context.repo.repo,
      headSha: run.head_sha,
      waitForReleaseProvenance: false,
    });
    return { repository, run, workflow, source, stale: false };
  } catch (error) {
    if (error instanceof QodanaSourceRejectedError && error.stale) {
      return { repository, run, workflow, source: null, stale: true };
    }
    throw error;
  }
}

function checkContext(context: ActionContext['context'], run: WorkflowRunData): WorkflowRunCheckContext {
  return {
    serverUrl: context.serverUrl,
    repo: context.repo,
    runId: requireBoundedInteger(run.id, 'workflow run id'),
    runAttempt: requireBoundedInteger(run.run_attempt, 'workflow run attempt'),
  };
}

function reportHeadSha(source: JunitSourceContext): string {
  return requireSha(source.source?.headSha ?? source.run.head_sha, 'JUnit report head SHA');
}

function junitPublicationApplies(sourceKind: QodanaSourceKind): boolean {
  return sourceKind === 'code';
}

async function registerChecks({
  github,
  context,
  run,
  headSha,
}: {
  github: Octokit;
  context: ActionContext['context'];
  run: WorkflowRunData;
  headSha: string;
}): Promise<Record<JunitReportKind, ReportCheck>> {
  const checkContextValue = checkContext(context, run);
  const definitions: Array<{ kind: JunitReportKind; name: string }> = [
    { kind: 'build', name: JUNIT_REPORT_NAMES.build },
    { kind: 'vanilla', name: JUNIT_REPORT_NAMES.vanilla },
  ];
  const checks = await Promise.all(definitions.map(async ({ kind, name }) => {
    const externalId = buildCheckExternalId({
      kind: `junit-${kind}`,
      workflowId: run.workflow_id,
      runId: run.id,
      runAttempt: requireBoundedInteger(run.run_attempt, 'workflow run attempt'),
      headSha,
    });
    const reference = await ensureWorkflowCheck({
      github,
      context: checkContextValue,
      name,
      headSha,
      externalId,
      summary: `Trusted publication accepted the CI run and is waiting for ${name}.`,
      status: 'queued',
    });
    return { kind, name, externalId, reference };
  }));
  const build = checks.find((check) => check.kind === 'build');
  const vanilla = checks.find((check) => check.kind === 'vanilla');
  if (build == null || vanilla == null) throw new JunitPublicationRejectedError('JUnit checks were not registered');
  return { build, vanilla };
}

async function listJobs(github: Octokit, owner: string, repo: string, runId: number): Promise<JobItem[]> {
  const response = await github.rest.actions.listJobsForWorkflowRun({
    owner,
    repo,
    run_id: runId,
    per_page: 100,
  });
  if (response.data.total_count > response.data.jobs.length) {
    throw new JunitPublicationRejectedError('CI job list exceeds the validation bound');
  }
  return response.data.jobs;
}

function exactJob(jobs: JobItem[], name: string): JobItem | null {
  const matches = jobs.filter((job) => job.name === name);
  if (matches.length > 1) throw new JunitPublicationRejectedError(`duplicate CI job ${name}`);
  return matches[0] ?? null;
}

function jobResult(job: JobItem | null): string | null {
  if (job == null || job.status !== 'completed') return null;
  const conclusion = job.conclusion;
  if (conclusion === 'success' || conclusion === 'failure' || conclusion === 'cancelled'
    || conclusion === 'skipped' || conclusion === 'timed_out') return conclusion;
  return 'failure';
}

function buildResult(jobs: JobItem[], run: WorkflowRunData): string | null {
  const results = jobs.map(jobResult);
  if (results.some((result) => result == null)) return null;
  if (run.status === 'completed' && run.conclusion === 'timed_out') return 'timed_out';
  if (run.status === 'completed' && run.conclusion === 'cancelled') return 'cancelled';
  if (results.includes('timed_out')) return 'timed_out';
  if (results.includes('cancelled')) return 'cancelled';
  if (results.includes('failure')) return 'failure';
  if (results.every((result) => result === 'skipped')) return 'skipped';
  return results.every((result) => result === 'success') ? 'success' : 'failure';
}

function observationForBuild(jobs: JobItem[], run: WorkflowRunData): ReportObservation {
  const buildJobs = JUNIT_BUILD_SHARDS.map((shard) => exactJob(jobs, BUILD_JOB_NAMES.get(shard) ?? ''));
  if (buildJobs.some((job) => job == null)) {
    if (run.status !== 'completed') return { ready: false, result: null, jobs: [] };
    return { ready: true, result: run.conclusion === 'skipped' ? 'skipped' : 'failure', jobs: [] };
  }
  const resolvedJobs = buildJobs.filter((job): job is JobItem => job != null);
  const result = buildResult(resolvedJobs, run);
  return {
    ready: result != null,
    result,
    jobs: resolvedJobs,
  };
}

function observationForVanilla(jobs: JobItem[], run: WorkflowRunData): ReportObservation {
  const job = exactJob(jobs, VANILLA_JOB_NAME);
  if (job == null) {
    if (run.status !== 'completed') return { ready: false, result: null, jobs: [] };
    return { ready: true, result: run.conclusion === 'skipped' ? 'skipped' : 'failure', jobs: [] };
  }
  const result = jobResult(job);
  return {
    ready: result != null,
    result,
    jobs: [job],
  };
}

function annotationsFor(annotations: JunitAnnotation[]): { values: WorkflowCheckAnnotation[]; omitted: number } {
  const values = annotations.slice(0, MAX_JUNIT_ANNOTATIONS).map((annotation) => ({
    path: annotation.path,
    start_line: annotation.line,
    end_line: annotation.line,
    annotation_level: annotation.level,
    title: annotation.title,
    message: annotation.message,
  }));
  return { values, omitted: Math.max(0, annotations.length - values.length) };
}

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}

async function loadReports({
  github,
  context,
  source,
  kind,
  shards,
  tolerateMissing,
}: {
  github: Octokit;
  context: ActionContext['context'];
  source: JunitSourceContext;
  kind: JunitReportKind;
  shards: readonly JunitBuildShard[] | readonly ['vanilla'];
  tolerateMissing: boolean;
}): Promise<JunitAggregate> {
  const token = requireString(process.env.GITHUB_TOKEN, 'GITHUB_TOKEN');
  const response = await github.rest.actions.listWorkflowRunArtifacts({
    owner: context.repo.owner,
    repo: context.repo.repo,
    run_id: source.run.id,
    per_page: 100,
  });
  if (response.data.total_count > response.data.artifacts.length) {
    throw new JunitPublicationRejectedError('JUnit artifact list exceeds the validation bound');
  }
  const artifacts = response.data.artifacts;
  const reportSets = await Promise.all(shards.map(async (shard) => {
    const selection = selectJunitArtifact({
      artifacts,
      run: source.run,
      repository: source.repository,
      kind,
      shard,
      headSha: source.source?.headSha ?? source.run.head_sha,
      allowMissing: tolerateMissing,
    });
    if (selection == null) return [];
    return downloadJunitReports({
      owner: context.repo.owner,
      repo: context.repo.repo,
      artifact: selection.artifact,
      kind,
      token,
    });
  }));
  return aggregateJunitArtifactReports(reportSets);
}

async function publishReport({
  github,
  context,
  source,
  check,
  observation,
}: {
  github: Octokit;
  context: ActionContext['context'];
  source: JunitSourceContext;
  check: ReportCheck;
  observation: ReportObservation;
}): Promise<string | null> {
  if (!observation.ready || observation.result == null) return null;
  const checkContextValue = checkContext(context, source.run);
  const headSha = reportHeadSha(source);
  const result = observation.result;
  if (result === 'skipped' || result === 'cancelled' || result === 'timed_out') {
    await completeWorkflowCheck({
      github,
      context: checkContextValue,
      checkId: check.reference.id,
      name: check.name,
      headSha,
      externalId: check.externalId,
      conclusion: result,
      summary: `${check.name} was ${result} by the trusted source workflow.`,
    });
    return null;
  }

  await updateWorkflowCheck({
    github,
    context: checkContextValue,
    checkId: check.reference.id,
    name: check.name,
    headSha,
    externalId: check.externalId,
    status: 'in_progress',
    summary: `Trusted code is validating ${check.name}.`,
  });

  let aggregate: JunitAggregate | null = null;
  let publicationError: string | null = null;
  try {
    aggregate = await loadReports({
      github,
      context,
      source,
      kind: check.kind,
      shards: check.kind === 'build' ? JUNIT_BUILD_SHARDS : ['vanilla'],
      tolerateMissing: result !== 'success',
    });
  } catch (error) {
    publicationError = errorMessage(error);
  }
  const report = aggregate ?? {
    files: 0,
    cases: 0,
    passed: 0,
    failed: 0,
    errors: 0,
    skipped: 0,
    annotations: [],
  };
  const annotationData = annotationsFor(report.annotations);
  const conclusion = publicationError == null && result === 'success'
    && report.failed === 0 && report.errors === 0
    ? 'success'
    : 'failure';
  const details = publicationError == null ? '' : `\n\nPublication error: ${publicationError}`;
  await completeWorkflowCheck({
    github,
    context: checkContextValue,
    checkId: check.reference.id,
    name: check.name,
    headSha,
    externalId: check.externalId,
    conclusion,
    summary: junitSummary(check.name, report, annotationData.omitted) + details,
    annotations: annotationData.values,
  });
  return publicationError;
}

async function completeSkipped({
  github,
  context,
  source,
  checks,
}: {
  github: Octokit;
  context: ActionContext['context'];
  source: JunitSourceContext;
  checks: Record<JunitReportKind, ReportCheck>;
}): Promise<void> {
  const checkContextValue = checkContext(context, source.run);
  const headSha = reportHeadSha(source);
  await Promise.all(Object.values(checks).map((check) => completeWorkflowCheck({
    github,
    context: checkContextValue,
    checkId: check.reference.id,
    name: check.name,
    headSha,
    externalId: check.externalId,
    conclusion: 'skipped',
    summary: 'The trusted path classification proved that this report is not applicable.',
  })));
}

async function observeAndPublish({
  github,
  context,
  source,
  checks,
  inProgress,
}: {
  github: Octokit;
  context: ActionContext['context'];
  source: JunitSourceContext;
  checks: Record<JunitReportKind, ReportCheck>;
  inProgress: boolean;
}): Promise<string[]> {
  const errors: string[] = [];
  const deadline = Date.now() + (inProgress ? IN_PROGRESS_DEADLINE_MS : COMPLETED_RETRY_COUNT * COMPLETED_RETRY_INTERVAL_MS);
  let buildPublished = false;
  let vanillaPublished = false;
  let attempts = 0;
  while ((!buildPublished || !vanillaPublished) && Date.now() <= deadline) {
    const jobs = await listJobs(github, context.repo.owner, context.repo.repo, source.run.id);
    const build = observationForBuild(jobs, source.run);
    const vanilla = observationForVanilla(jobs, source.run);
    if (!buildPublished && build.ready) {
      const error = await publishReport({ github, context, source, check: checks.build, observation: build });
      if (error != null) errors.push(`Build: ${error}`);
      buildPublished = true;
    }
    if (!vanillaPublished && vanilla.ready) {
      const error = await publishReport({ github, context, source, check: checks.vanilla, observation: vanilla });
      if (error != null) errors.push(`Vanilla: ${error}`);
      vanillaPublished = true;
    }
    if (buildPublished && vanillaPublished) break;
    attempts += 1;
    if (!inProgress && attempts >= COMPLETED_RETRY_COUNT) break;
    await new Promise((resolve) => setTimeout(resolve, inProgress ? OBSERVATION_INTERVAL_MS : COMPLETED_RETRY_INTERVAL_MS));
  }
  if (!buildPublished) {
    const error = await publishReport({
      github,
      context,
      source,
      check: checks.build,
      observation: { ready: true, result: inProgress ? 'timed_out' : 'failure', jobs: [] },
    });
    if (error != null) errors.push(`Build: ${error}`);
  }
  if (!vanillaPublished) {
    const error = await publishReport({
      github,
      context,
      source,
      check: checks.vanilla,
      observation: { ready: true, result: inProgress ? 'timed_out' : 'failure', jobs: [] },
    });
    if (error != null) errors.push(`Vanilla: ${error}`);
  }
  return errors;
}

async function publishJunit({
  github,
  context,
}: {
  github: Octokit;
  context: ActionContext['context'];
}): Promise<string[]> {
  const eventRun = requireObject<WorkflowRunEventRecord>(context.payload?.workflow_run, 'workflow_run event');
  const source = await resolveSource({ github, context, eventRun });
  const checks = await registerChecks({
    github,
    context,
    run: source.run,
    headSha: reportHeadSha(source),
  });
  if (source.stale) {
    const checkContextValue = checkContext(context, source.run);
    const headSha = reportHeadSha(source);
    await Promise.all(Object.values(checks).map((check) => completeWorkflowCheck({
      github,
      context: checkContextValue,
      checkId: check.reference.id,
      name: check.name,
      headSha,
      externalId: check.externalId,
      conclusion: 'cancelled',
      summary: 'The pull request head is no longer current or open.',
    })));
    return [];
  }
  if (source.source == null) throw new JunitPublicationRejectedError('JUnit source is missing');
  if (!junitPublicationApplies(source.source.sourceKind)) {
    await completeSkipped({ github, context, source, checks });
    return [];
  }
  const inProgress = eventRun.status === 'in_progress';
  return observeAndPublish({ github, context, source, checks, inProgress });
}

async function writeJunitPublicationOutputs({
  github,
  context,
  core,
}: ActionContext): Promise<void> {
  try {
    const errors = await publishJunit({ github, context });
    if (errors.length > 0) core.setFailed(`JUnit publication failed: ${errors.join('; ')}`);
  } catch (error) {
    core.setFailed(`JUnit publication rejected: ${errorMessage(error)}`);
  }
}

export {
  buildResult,
  junitPublicationApplies,
  observationForBuild,
  observationForVanilla,
  publishJunit,
  sourceEvent,
  writeJunitPublicationOutputs,
};
