'use strict';

const { resolveCiRun, QodanaSourceRejectedError } = require('./qodana-source.js');
const {
  QodanaPublicationRejectedError,
  selectQodanaRunArtifact,
  validateQodanaArtifactSource,
  validateQodanaWorkflowSource,
} = require('./qodana-publisher-validation.js');

function requireObject(value, label) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    throw new QodanaPublicationRejectedError(`${label} is missing`);
  }
  return value;
}

async function resolvePublication({ github, context }) {
  const eventRun = requireObject(context.payload?.workflow_run, 'workflow_run event');
  const owner = context.repo.owner;
  const repo = context.repo.repo;
  const repositoryResponse = await github.rest.repos.get({ owner, repo });
  const repository = requireObject(repositoryResponse.data, 'repository');
  if (repository.full_name !== `${owner}/${repo}`) {
    throw new QodanaPublicationRejectedError('repository identity is invalid');
  }
  const runResponse = await github.rest.actions.getWorkflowRun({
    owner,
    repo,
    run_id: eventRun.id,
  });
  const qodanaRun = requireObject(runResponse.data, 'Qodana workflow run');
  const workflowResponse = await github.rest.actions.getWorkflow({
    owner,
    repo,
    workflow_id: qodanaRun.workflow_id,
  });
  const workflow = requireObject(workflowResponse.data, 'Qodana workflow');
  validateQodanaWorkflowSource({ eventRun, run: qodanaRun, workflow, repository });

  const artifacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts, {
    owner,
    repo,
    run_id: qodanaRun.id,
    per_page: 100,
  });
  if (!Array.isArray(artifacts)) {
    throw new QodanaPublicationRejectedError('Qodana workflow artifacts are missing');
  }
  const selection = selectQodanaRunArtifact({ artifacts, qodanaRun, repository });
  const { descriptor } = selection;

  const source = await resolveCiRun({
    github,
    owner,
    repo,
    runId: descriptor.ciRunId,
    expectedRunAttempt: descriptor.ciRunAttempt,
    expectedHeadSha: descriptor.headSha,
    expectedBaseSha: descriptor.baseSha,
    waitForReleaseProvenance: false,
  });
  const delayedReleaseProvenance = source.sourceKind === 'release'
    && source.pathClassification === 'release-candidate'
    && descriptor.sourceKind === 'code';
  if (source.sourceKind !== descriptor.sourceKind && !delayedReleaseProvenance) {
    throw new QodanaPublicationRejectedError('Qodana source classification changed');
  }
  const artifactSource = delayedReleaseProvenance
    ? { ...source, sourceKind: descriptor.sourceKind }
    : source;

  validateQodanaArtifactSource({ descriptor, source: artifactSource });
  const { artifact } = selection;
  return {
    artifactId: artifact.id,
    headSha: source.headSha,
    pullNumber: source.pullRequest,
    sourceKind: source.sourceKind,
  };
}

async function writePublicationOutputs({ github, context, core }) {
  try {
    const publication = await resolvePublication({ github, context });
    core.setOutput('publish', 'true');
    core.setOutput('artifact_id', String(publication.artifactId));
    core.setOutput('head_sha', publication.headSha);
    core.setOutput('pull_number', String(publication.pullNumber));
    core.setOutput('source_kind', publication.sourceKind);
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

module.exports = {
  QodanaPublicationRejectedError,
  resolvePublication,
  writePublicationOutputs,
};
