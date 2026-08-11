import { resolvePullRequestSource, QodanaSourceRejectedError } from './qodana-source.js';
import type { PullRequestSource } from './qodana-source.js';
import type { QodanaSourceKind } from './qodana-contract.js';
import {
  QodanaPublicationRejectedError,
  selectQodanaRunArtifact,
  validateQodanaArtifactSource,
  validateQodanaWorkflowSource,
} from './qodana-publisher-validation.js';
import type { ArtifactSelection } from './qodana-publisher-validation.js';
import type { ActionContext, Octokit } from './shared/action-context.js';
import { createValidators } from './shared/validation.js';
import { fetchWorkflowRunContext } from './shared/run-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';

function rejectPublication(message: string): never {
  throw new QodanaPublicationRejectedError(message);
}

const { requireObject } = createValidators(rejectPublication);

export interface QodanaPublication {
  artifactId: number | null;
  headSha: string | null;
  pullNumber: number | null;
  publish: boolean;
  rejection: Error | null;
  sourceKind: QodanaSourceKind | null;
}

async function resolvePublication({
  github,
  context,
}: {
  github: Octokit;
  context: ActionContext['context'];
}): Promise<QodanaPublication> {
  const eventRun = requireObject<WorkflowRunEventRecord>(context.payload?.workflow_run, 'workflow_run event');
  const owner = context.repo.owner;
  const repo = context.repo.repo;
  const { repository, run: qodanaRun, workflow } = await fetchWorkflowRunContext(rejectPublication, {
    github,
    owner,
    repo,
    eventRun,
  });
  const trustedRun = validateQodanaWorkflowSource({
    eventRun,
    run: qodanaRun,
    workflow,
    repository,
  });
  if (trustedRun.conclusion !== 'success') {
    return {
      artifactId: null,
      headSha: null,
      pullNumber: null,
      publish: false,
      rejection: null,
      sourceKind: null,
    };
  }

  const artifacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts, {
    owner,
    repo,
    run_id: qodanaRun.id,
    per_page: 100,
  });
  let selection: ArtifactSelection;
  try {
    selection = selectQodanaRunArtifact({ artifacts, qodanaRun, repository });
  } catch (error) {
    if (error instanceof QodanaPublicationRejectedError) {
      return {
        artifactId: null,
        headSha: null,
        pullNumber: null,
        publish: false,
        rejection: error,
        sourceKind: null,
      };
    }
    throw error;
  }
  const { artifact, descriptor } = selection;

  let source: PullRequestSource;
  try {
    source = await resolvePullRequestSource({
      github,
      owner,
      repo,
      headSha: descriptor.headSha,
      expectedBaseSha: descriptor.baseSha,
      waitForReleaseProvenance: false,
    });
  } catch (error) {
    if (error instanceof QodanaSourceRejectedError) {
      return {
        artifactId: null,
        headSha: descriptor.headSha,
        pullNumber: null,
        publish: false,
        rejection: error.stale ? null : error,
        sourceKind: descriptor.sourceKind,
      };
    }
    throw error;
  }
  const delayedReleaseProvenance = source.sourceKind === 'release'
    && source.pathClassification === 'release-candidate'
    && descriptor.sourceKind === 'code';
  if (source.sourceKind !== descriptor.sourceKind && !delayedReleaseProvenance) {
    const error = new QodanaPublicationRejectedError('Qodana source classification changed');
    return {
      artifactId: null,
      headSha: descriptor.headSha,
      pullNumber: source.pullRequest,
      publish: false,
      rejection: error,
      sourceKind: descriptor.sourceKind,
    };
  }
  const artifactSource = delayedReleaseProvenance
    ? { ...source, sourceKind: descriptor.sourceKind }
    : source;
  validateQodanaArtifactSource({ descriptor, source: artifactSource });
  return {
    artifactId: artifact.id,
    headSha: source.headSha,
    pullNumber: source.pullRequest,
    publish: true,
    rejection: null,
    sourceKind: source.sourceKind,
  };
}

async function writePublicationOutputs({
  github,
  context,
  core,
}: {
  github: Octokit;
  context: ActionContext['context'];
  core: ActionContext['core'];
}): Promise<QodanaPublication | null> {
  try {
    const publication = await resolvePublication({ github, context });
    core.setOutput('publish', String(publication.publish));
    if (publication.artifactId != null) core.setOutput('artifact_id', String(publication.artifactId));
    if (publication.headSha != null) core.setOutput('head_sha', publication.headSha);
    if (publication.pullNumber != null) core.setOutput('pull_number', String(publication.pullNumber));
    if (publication.sourceKind != null) core.setOutput('source_kind', publication.sourceKind);
    if (publication.rejection) core.setFailed(`Qodana publication rejected: ${publication.rejection.message}`);
    return publication;
  } catch (error) {
    if (error instanceof QodanaSourceRejectedError && error.stale) {
      core.warning(`Qodana publication skipped because the pull request changed: ${error.message}`);
      core.setOutput('publish', 'false');
      return null;
    }
    if (error instanceof QodanaPublicationRejectedError) {
      core.setFailed(`Qodana publication rejected: ${error.message}`);
      return null;
    }
    throw error;
  }
}

export {
  QodanaPublicationRejectedError,
  resolvePublication,
  writePublicationOutputs,
};
