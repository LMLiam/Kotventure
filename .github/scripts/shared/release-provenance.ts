import type { JobItem, Octokit, PullRequestData, WorkflowData, WorkflowRunListItem } from './action-context.js';

const RELEASE_PROVENANCE_WORKFLOW_ID = 'release-provenance.yml';
const RELEASE_PROVENANCE_WORKFLOW_PATH = '.github/workflows/release-provenance.yml';
const TRUSTED_PROVENANCE_JOB_NAME = 'Trusted release provenance';

export interface FindReleaseProvenanceRunOptions {
  github: Octokit;
  owner: string;
  repo: string;
  repository: string | { readonly full_name?: string } | null;
  headSha: string;
  headRef: string;
  pullNumber: number;
}

export async function findReleaseProvenanceRun(
  options: FindReleaseProvenanceRunOptions,
): Promise<WorkflowRunListItem | undefined> {
  const { github, owner, repo, repository, headSha, headRef, pullNumber } = options;
  const runs = await github.paginate(github.rest.actions.listWorkflowRuns, {
    owner,
    repo,
    workflow_id: RELEASE_PROVENANCE_WORKFLOW_ID,
    event: 'pull_request_target',
    per_page: 100,
  });
  const repositoryName = typeof repository === 'string' ? repository : repository?.full_name;

  if (!repositoryName) return undefined;

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
    .sort((left, right) => new Date(right.created_at).getTime() - new Date(left.created_at).getTime())[0];
}

export type ReleaseProvenanceStatus = 'success' | 'pending' | 'failed' | 'missing';

export interface ReadTrustedReleaseProvenanceOptions {
  github: Octokit;
  owner: string;
  repo: string;
  repository: string | { readonly full_name?: string } | null;
  pullRequest: PullRequestData;
}

export async function readTrustedReleaseProvenance(
  options: ReadTrustedReleaseProvenanceOptions,
): Promise<ReleaseProvenanceStatus> {
  const { github, owner, repo, repository, pullRequest } = options;
  let workflow: WorkflowData;
  try {
    const response = await github.rest.actions.getWorkflow({
      owner,
      repo,
      workflow_id: RELEASE_PROVENANCE_WORKFLOW_ID,
    });
    workflow = response.data;
  } catch {
    return 'missing';
  }
  if (workflow.path !== RELEASE_PROVENANCE_WORKFLOW_PATH) return 'missing';

  let candidate: WorkflowRunListItem | undefined;
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

  let jobs: JobItem[];
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

export interface HasTrustedReleaseProvenanceOptions extends ReadTrustedReleaseProvenanceOptions {
  waitForReleaseProvenance?: boolean;
}

function wait(milliseconds: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

export async function hasTrustedReleaseProvenance(options: HasTrustedReleaseProvenanceOptions): Promise<boolean> {
  const attempts = options.waitForReleaseProvenance === false ? 1 : 6;
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    const status = await readTrustedReleaseProvenance(options);
    if (status === 'success' || status === 'failed') return status === 'success';
    if (attempt + 1 < attempts) await wait(10_000);
  }
  return false;
}
