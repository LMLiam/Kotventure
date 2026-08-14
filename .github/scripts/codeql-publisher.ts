import type { ActionContext, Octokit } from './shared/action-context.js';
import { createValidators } from './shared/validation.js';
import { fetchWorkflowRunContext } from './shared/run-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';
import {
  CODEQL_CATEGORIES,
  CODEQL_WORKFLOW_NAME,
} from './codeql-contract.js';
import type { CodeqlCategory } from './codeql-contract.js';
import type { QodanaSourceKind } from './qodana-contract.js';
import {
  CodeqlPublicationRejectedError,
  selectCodeqlArtifact,
  validateCodeqlWorkflowSource,
} from './codeql-validation.js';
import { downloadCodeqlSarif } from './codeql-publisher-storage.js';
import { resolvePullRequestSource, QodanaSourceRejectedError } from './qodana-source.js';
import {
  buildCheckExternalId,
  completeWorkflowCheck,
  ensureWorkflowCheck,
  updateWorkflowCheck,
  workflowResultConclusion,
} from './workflow-run-check.js';
import type { WorkflowCheckReference, WorkflowRunCheckContext } from './workflow-run-check.js';

const CHECK_NAMES: Record<CodeqlCategory, string> = {
  actions: 'CodeQL publication (actions)',
  'java-kotlin': 'CodeQL publication (java-kotlin)',
};

const {
  requireBoundedInteger,
  requireObject,
  requireSha,
} = createValidators((message: string): never => {
  throw new CodeqlPublicationRejectedError(message);
});

interface CodeqlSourceContext {
  repository: Awaited<ReturnType<Octokit['rest']['repos']['get']>>['data'];
  run: Awaited<ReturnType<Octokit['rest']['actions']['getWorkflowRun']>>['data'];
  sourceKind: QodanaSourceKind | null;
  sourceHeadSha: string;
  pullNumber: number | null;
  stale: boolean;
}

interface CodeqlCheck {
  category: CodeqlCategory;
  name: string;
  externalId: string;
  reference: WorkflowCheckReference;
}

interface CodeqlOutputs {
  checks: Record<CodeqlCategory, CodeqlCheck>;
  source: CodeqlSourceContext;
}

function checkContext(context: ActionContext['context'], run: CodeqlSourceContext['run']): WorkflowRunCheckContext {
  return {
    serverUrl: context.serverUrl,
    repo: context.repo,
    runId: requireBoundedInteger(run.id, 'workflow run id'),
    runAttempt: requireBoundedInteger(run.run_attempt, 'workflow run attempt'),
  };
}

function codeqlAnalysisApplies(sourceKind: QodanaSourceKind | null): boolean {
  return sourceKind == null || sourceKind === 'code';
}

async function resolveSource({
  github,
  context,
  eventRun,
}: {
  github: Octokit;
  context: ActionContext['context'];
  eventRun: WorkflowRunEventRecord;
}): Promise<CodeqlSourceContext> {
  const { repository, run, workflow } = await fetchWorkflowRunContext((message) => {
    throw new CodeqlPublicationRejectedError(message);
  }, {
    github,
    owner: context.repo.owner,
    repo: context.repo.repo,
    eventRun,
  });
  const trustedRun = validateCodeqlWorkflowSource({ eventRun, run, workflow, repository });
  if (trustedRun.event === 'merge_group') {
    return {
      repository,
      run,
      sourceKind: 'code',
      sourceHeadSha: trustedRun.headSha,
      pullNumber: null,
      stale: false,
    };
  }
  try {
    const source = await resolvePullRequestSource({
      github,
      owner: context.repo.owner,
      repo: context.repo.repo,
      headSha: trustedRun.headSha,
      waitForReleaseProvenance: false,
    });
    return {
      repository,
      run,
      sourceKind: source.sourceKind,
      sourceHeadSha: source.headSha,
      pullNumber: source.pullRequest,
      stale: false,
    };
  } catch (error) {
    if (error instanceof QodanaSourceRejectedError && error.stale) {
      return {
        repository,
        run,
        sourceKind: null,
        sourceHeadSha: trustedRun.headSha,
        pullNumber: null,
        stale: true,
      };
    }
    throw error;
  }
}

async function registerChecks({
  github,
  context,
  source,
}: {
  github: Octokit;
  context: ActionContext['context'];
  source: CodeqlSourceContext;
}): Promise<Record<CodeqlCategory, CodeqlCheck>> {
  const checkContextValue = checkContext(context, source.run);
  const checks = await Promise.all(CODEQL_CATEGORIES.map(async (category) => {
    const externalId = buildCheckExternalId({
      kind: `codeql-${category}`,
      workflowId: source.run.workflow_id,
      runId: source.run.id,
      runAttempt: requireBoundedInteger(source.run.run_attempt, 'workflow run attempt'),
      headSha: source.sourceHeadSha,
    });
    const reference = await ensureWorkflowCheck({
      github,
      context: checkContextValue,
      name: CHECK_NAMES[category],
      headSha: source.sourceHeadSha,
      externalId,
      summary: `Trusted publication accepted the ${CODEQL_WORKFLOW_NAME} run and is waiting for ${category}.`,
      status: 'queued',
    });
    return { category, name: CHECK_NAMES[category], externalId, reference };
  }));
  const actions = checks.find((check) => check.category === 'actions');
  const javaKotlin = checks.find((check) => check.category === 'java-kotlin');
  if (actions == null || javaKotlin == null) throw new CodeqlPublicationRejectedError('CodeQL checks were not registered');
  return { actions, 'java-kotlin': javaKotlin };
}

async function listArtifacts(github: Octokit, context: ActionContext['context'], runId: number) {
  const response = await github.rest.actions.listWorkflowRunArtifacts({
    owner: context.repo.owner,
    repo: context.repo.repo,
    run_id: runId,
    per_page: 100,
  });
  if (response.data.total_count > response.data.artifacts.length) {
    throw new CodeqlPublicationRejectedError('CodeQL artefact list exceeds the validation bound');
  }
  return response.data.artifacts;
}

function outputPath(category: CodeqlCategory): string {
  const root = process.env.RUNNER_TEMP;
  if (typeof root !== 'string' || root.length < 1) throw new CodeqlPublicationRejectedError('runner temporary directory is missing');
  return `${root}/codeql-publication/${category}`;
}

function setOutput(core: ActionContext['core'], name: string, value: string | number | boolean): void {
  core.setOutput(name, value);
}

function outputKey(category: CodeqlCategory): string {
  return category === 'java-kotlin' ? 'java_kotlin' : category;
}

async function prepareCodeql({
  github,
  context,
  core,
}: ActionContext): Promise<CodeqlOutputs | null> {
  const eventRun = requireObject<WorkflowRunEventRecord>(context.payload?.workflow_run, 'workflow_run event');
  const source = await resolveSource({ github, context, eventRun });
  const checks = await registerChecks({ github, context, source });
  for (const category of CODEQL_CATEGORIES) {
    setOutput(core, `${outputKey(category)}_check_id`, checks[category].reference.id);
    setOutput(core, `${outputKey(category)}_external_id`, checks[category].externalId);
  }
  setOutput(core, 'head_sha', source.sourceHeadSha);
  setOutput(core, 'run_id', source.run.id);
  setOutput(core, 'run_attempt', requireBoundedInteger(source.run.run_attempt, 'workflow run attempt'));
  setOutput(core, 'pull_number', source.pullNumber ?? '');
  setOutput(core, 'upload_ref', source.pullNumber == null
    ? `refs/heads/${source.run.head_branch}`
    : `refs/pull/${source.pullNumber}/head`);
  setOutput(core, 'stale', source.stale);
  if (source.stale) {
    await completeAll({ github, context, source, checks, conclusion: 'cancelled', summary: 'The pull request head is no longer current or open.' });
    setOutput(core, 'publish', false);
    return { checks, source };
  }
  if (!codeqlAnalysisApplies(source.sourceKind)) {
    await completeAll({ github, context, source, checks, conclusion: 'skipped', summary: 'CodeQL analysis is not applicable to this pull request.' });
    setOutput(core, 'publish', false);
    return { checks, source };
  }
  if (eventRun.status === 'in_progress') {
    await updateAll({ github, context, source, checks, summary: 'Trusted code is waiting for CodeQL analysis.' });
    setOutput(core, 'publish', false);
    return { checks, source };
  }
  if (source.run.conclusion !== 'success') {
    const conclusion = source.run.conclusion == null ? 'failure' : workflowResultConclusion(source.run.conclusion);
    await completeAll({ github, context, source, checks, conclusion, summary: `The CodeQL source workflow ended with ${conclusion}.` });
    setOutput(core, 'publish', false);
    return { checks, source };
  }

  const artifacts = await listArtifacts(github, context, source.run.id);
  for (const category of CODEQL_CATEGORIES) {
    try {
      const selection = selectCodeqlArtifact({
        artifacts,
        run: source.run,
        repository: source.repository,
        category,
        headSha: source.sourceHeadSha,
      });
      const filePath = await downloadCodeqlSarif({
        owner: context.repo.owner,
        repo: context.repo.repo,
        artifact: selection.artifact,
        category,
        token: process.env.GITHUB_TOKEN ?? '',
        outputDirectory: outputPath(category),
      });
      setOutput(core, `${outputKey(category)}_path`, filePath);
    } catch (error) {
      setOutput(core, `${outputKey(category)}_error`, error instanceof Error ? error.message : String(error));
    }
  }
  await updateAll({ github, context, source, checks, summary: 'Trusted code is publishing validated CodeQL SARIF.' });
  setOutput(core, 'publish', true);
  return { checks, source };
}

async function updateAll({
  github,
  context,
  source,
  checks,
  summary,
}: {
  github: Octokit;
  context: ActionContext['context'];
  source: CodeqlSourceContext;
  checks: Record<CodeqlCategory, CodeqlCheck>;
  summary: string;
}): Promise<void> {
  const checkContextValue = checkContext(context, source.run);
  await Promise.all(CODEQL_CATEGORIES.map((category) => updateWorkflowCheck({
    github,
    context: checkContextValue,
    checkId: checks[category].reference.id,
    name: checks[category].name,
    headSha: source.sourceHeadSha,
    externalId: checks[category].externalId,
    status: 'in_progress',
    summary,
  })));
}

async function completeAll({
  github,
  context,
  source,
  checks,
  conclusion,
  summary,
}: {
  github: Octokit;
  context: ActionContext['context'];
  source: CodeqlSourceContext;
  checks: Record<CodeqlCategory, CodeqlCheck>;
  conclusion: string;
  summary: string;
}): Promise<void> {
  const checkContextValue = checkContext(context, source.run);
  await Promise.all(CODEQL_CATEGORIES.map((category) => completeWorkflowCheck({
    github,
    context: checkContextValue,
    checkId: checks[category].reference.id,
    name: checks[category].name,
    headSha: source.sourceHeadSha,
    externalId: checks[category].externalId,
    conclusion,
    summary,
  })));
}

async function completeCodeql({
  github,
  context,
  core,
  actionsOutcome,
  javaKotlinOutcome,
}: ActionContext & {
  actionsOutcome: string;
  javaKotlinOutcome: string;
}): Promise<void> {
  const eventRun = requireObject<WorkflowRunEventRecord>(context.payload?.workflow_run, 'workflow_run event');
  const source = await resolveSource({ github, context, eventRun });
  const checks = await registerChecks({ github, context, source });
  if (source.stale) {
    await completeAll({ github, context, source, checks, conclusion: 'cancelled', summary: 'The pull request head is no longer current or open.' });
    return;
  }
  if (!codeqlAnalysisApplies(source.sourceKind)) {
    await completeAll({ github, context, source, checks, conclusion: 'skipped', summary: 'CodeQL analysis is not applicable to this pull request.' });
    return;
  }
  if (eventRun.status !== 'completed' || source.run.conclusion !== 'success') return;
  const checkContextValue = checkContext(context, source.run);
  const outcomes: Record<CodeqlCategory, string> = {
    actions: actionsOutcome,
    'java-kotlin': javaKotlinOutcome,
  };
  await Promise.all(CODEQL_CATEGORIES.map((category) => {
    const outcome = outcomes[category];
    const error = outcome === 'success' ? '' : ` Upload outcome: ${outcome || 'missing'}.`;
    return completeWorkflowCheck({
      github,
      context: checkContextValue,
      checkId: checks[category].reference.id,
      name: checks[category].name,
      headSha: source.sourceHeadSha,
      externalId: checks[category].externalId,
      conclusion: outcome === 'success' ? 'success' : 'failure',
      summary: `Trusted CodeQL SARIF publication completed.${error}`,
    });
  }));
}

async function prepareCodeqlPublicationOutputs(action: ActionContext): Promise<void> {
  try {
    await prepareCodeql(action);
  } catch (error) {
    action.core.setFailed(`CodeQL publication rejected: ${error instanceof Error ? error.message : String(error)}`);
  }
}

async function completeCodeqlPublicationOutputs(action: ActionContext & {
  actionsOutcome: string;
  javaKotlinOutcome: string;
}): Promise<void> {
  try {
    await completeCodeql(action);
  } catch (error) {
    action.core.setFailed(`CodeQL publication completion rejected: ${error instanceof Error ? error.message : String(error)}`);
  }
}

export {
  codeqlAnalysisApplies,
  completeCodeqlPublicationOutputs,
  prepareCodeql,
  prepareCodeqlPublicationOutputs,
  validateCodeqlWorkflowSource,
};
