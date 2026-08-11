'use strict';

const { createValidators } = require('./validation.js');

const RELEASE_PROVENANCE_WORKFLOW_ID = 'release-provenance.yml';
const RELEASE_PROVENANCE_WORKFLOW_PATH = '.github/workflows/release-provenance.yml';
const TRUSTED_PROVENANCE_JOB_NAME = 'Trusted release provenance';

const { requireObject } = createValidators((message) => {
  throw new Error(message);
});

async function findReleaseProvenanceRun({
  github,
  owner,
  repo,
  repository,
  headSha,
  headRef,
  pullNumber,
}) {
  const runs = await github.paginate(github.rest.actions.listWorkflowRuns, {
    owner,
    repo,
    workflow_id: RELEASE_PROVENANCE_WORKFLOW_ID,
    event: 'pull_request_target',
    per_page: 100,
  });
  const repositoryName = typeof repository === 'string' ? repository : repository?.full_name;

  return runs
    .filter((run) => {
      const associatedPullRequests = Array.isArray(run?.pull_requests) ? run.pull_requests : [];
      return run?.event === 'pull_request_target'
        && run.repository?.full_name === repositoryName
        && run.head_sha === headSha
        && run.head_branch === headRef
        && (associatedPullRequests.length === 0
          || associatedPullRequests.some((item) => item.number === pullNumber));
    })
    .sort((left, right) => new Date(right.created_at) - new Date(left.created_at))[0];
}

async function readTrustedReleaseProvenance({ github, owner, repo, repository, pullRequest }) {
  let workflow;
  try {
    const response = await github.rest.actions.getWorkflow({
      owner,
      repo,
      workflow_id: RELEASE_PROVENANCE_WORKFLOW_ID,
    });
    workflow = requireObject(response.data, 'release provenance workflow');
  } catch {
    return 'missing';
  }
  if (workflow.path !== RELEASE_PROVENANCE_WORKFLOW_PATH) return 'missing';

  let candidate;
  try {
    candidate = await findReleaseProvenanceRun({
      github,
      owner,
      repo,
      repository,
      headSha: pullRequest.head.sha,
      headRef: pullRequest.head.ref,
      pullNumber: pullRequest.number,
    });
  } catch {
    return 'missing';
  }

  if (!candidate) return 'missing';
  if (candidate.status !== 'completed') return 'pending';
  if (candidate.conclusion !== 'success') return 'failed';

  let jobs;
  try {
    jobs = await github.paginate(github.rest.actions.listJobsForWorkflowRun, {
      owner,
      repo,
      run_id: candidate.id,
      per_page: 100,
    });
  } catch {
    return 'missing';
  }
  const trustedJob = jobs.find((job) => job?.name === TRUSTED_PROVENANCE_JOB_NAME);
  if (trustedJob?.status === 'completed' && trustedJob.conclusion === 'success') return 'success';
  return trustedJob?.status === 'completed' ? 'failed' : 'pending';
}

function wait(milliseconds) {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

async function hasTrustedReleaseProvenance(options) {
  const attempts = options.waitForReleaseProvenance === false ? 1 : 6;
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    const status = await readTrustedReleaseProvenance(options);
    if (status === 'success' || status === 'failed') return status === 'success';
    if (attempt + 1 < attempts) await wait(10_000);
  }
  return false;
}

module.exports = {
  findReleaseProvenanceRun,
  hasTrustedReleaseProvenance,
  readTrustedReleaseProvenance,
};
