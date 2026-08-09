'use strict';

const { resolveCiRun, QodanaSourceRejectedError } = require('./qodana-source.js');
const {
  QodanaPublicationRejectedError,
  selectQodanaCheckArtifact,
  selectQodanaRunArtifact,
  validateQodanaArtifactSource,
  validateQodanaCheckSource,
  validateQodanaWorkflowSource,
} = require('./qodana-publisher-validation.js');
const { buildCheckExternalId } = require('./workflow-run-check.js');

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
  const trustedRun = validateQodanaWorkflowSource({
    eventRun,
    run: qodanaRun,
    workflow,
    repository,
  });

  const artifacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts, {
    owner,
    repo,
    run_id: qodanaRun.id,
    per_page: 100,
  });
  if (!Array.isArray(artifacts)) {
    throw new QodanaPublicationRejectedError('Qodana workflow artifacts are missing');
  }
  const checkSelection = selectQodanaCheckArtifact({ artifacts, qodanaRun, repository });
  const { descriptor: checkDescriptor } = checkSelection;

  let source;
  try {
    source = await resolveCiRun({
      github,
      owner,
      repo,
      runId: checkDescriptor.ciRunId,
      expectedRunAttempt: checkDescriptor.ciRunAttempt,
      expectedHeadSha: checkDescriptor.headSha,
      expectedBaseSha: checkDescriptor.baseSha,
      waitForReleaseProvenance: false,
    });
  } catch (error) {
    if (error instanceof QodanaSourceRejectedError) {
      return {
        artifactId: null,
        checkConclusion: error.stale ? 'cancelled' : 'failure',
        checkExternalId: buildCheckExternalId({
          kind: 'qodana-pr',
          runId: qodanaRun.id,
          runAttempt: qodanaRun.run_attempt,
          headSha: checkDescriptor.headSha,
        }),
        checkRunId: checkDescriptor.checkRunId,
        headSha: checkDescriptor.headSha,
        pullNumber: null,
        publish: false,
        rejection: error.stale ? null : error,
        sourceKind: checkDescriptor.sourceKind,
      };
    }
    throw error;
  }
  const delayedCheckReleaseProvenance = source.sourceKind === 'release'
    && source.pathClassification === 'release-candidate'
    && checkDescriptor.sourceKind === 'code';
  const checkSource = delayedCheckReleaseProvenance
    ? { ...source, sourceKind: checkDescriptor.sourceKind }
    : source;
  validateQodanaCheckSource({ descriptor: checkDescriptor, source: checkSource });
  const common = {
    checkConclusion: trustedRun.conclusion === 'success' ? null : trustedRun.conclusion,
    checkExternalId: buildCheckExternalId({
      kind: 'qodana-pr',
      runId: qodanaRun.id,
      runAttempt: qodanaRun.run_attempt,
      headSha: source.headSha,
    }),
    checkRunId: checkDescriptor.checkRunId,
    headSha: source.headSha,
    pullNumber: source.pullRequest,
    sourceKind: source.sourceKind,
  };
  if (trustedRun.conclusion !== 'success') {
    return {
      ...common,
      artifactId: null,
      publish: false,
      rejection: null,
    };
  }

  let artifact;
  try {
    const selection = selectQodanaRunArtifact({ artifacts, qodanaRun, repository });
    const { descriptor } = selection;
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
    artifact = selection.artifact;
  } catch (error) {
    if (error instanceof QodanaPublicationRejectedError) {
      return {
        ...common,
        artifactId: null,
        checkConclusion: 'failure',
        publish: false,
        rejection: error,
      };
    }
    throw error;
  }
  return {
    ...common,
    artifactId: artifact.id,
    publish: true,
    rejection: null,
  };
}

async function writePublicationOutputs({ github, context, core }) {
  try {
    const publication = await resolvePublication({ github, context });
    core.setOutput('publish', String(publication.publish));
    if (publication.artifactId != null) {
      core.setOutput('artifact_id', String(publication.artifactId));
    }
    core.setOutput('head_sha', publication.headSha);
    if (publication.pullNumber != null) {
      core.setOutput('pull_number', String(publication.pullNumber));
    }
    core.setOutput('source_kind', publication.sourceKind);
    core.setOutput('check_run_id', String(publication.checkRunId));
    core.setOutput('check_external_id', publication.checkExternalId);
    core.setOutput('check_conclusion', publication.checkConclusion || '');
    if (publication.rejection) {
      core.setFailed(`Qodana publication rejected: ${publication.rejection.message}`);
    }
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
