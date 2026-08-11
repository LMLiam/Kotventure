import {
  RELEASE_ONLY_FILES,
  isDocumentationPath,
} from './shared/path-classification.js';
import { findReleaseProvenanceRun } from './shared/release-provenance.js';
import type { ActionContext, JobItem, Octokit, PullRequestFile, WorkflowRunListItem } from './shared/action-context.js';

export { RELEASE_ONLY_FILES, isDocumentationPath };

function setAlways(core: ActionContext['core'], documentationOnly = false): void {
  core.setOutput('run', 'true');
  core.setOutput('release_only', 'false');
  core.setOutput('release_candidate', 'false');
  core.setOutput('documentation_only', documentationOnly ? 'true' : 'false');
}

function setForceFull(core: ActionContext['core']): void {
  core.setOutput('run', 'true');
  core.setOutput('release_only', 'false');
  core.setOutput('release_candidate', 'true');
  core.setOutput('documentation_only', 'false');
}

async function decideGate({
  github,
  context,
  core,
}: {
  github: Octokit;
  context: ActionContext['context'];
  core: ActionContext['core'];
}): Promise<void> {
  if (context.eventName === 'push') {
    setAlways(core);
    return;
  }

  if (context.eventName !== 'pull_request') {
    core.info(`Event ${context.eventName}: always run heavy CI.`);
    setAlways(core);
    return;
  }

  const pullRequest = context.payload.pull_request;
  if (!pullRequest) {
    core.warning('Pull request payload is missing; run full CI.');
    setAlways(core);
    return;
  }

  const repository = `${context.repo.owner}/${context.repo.repo}`;
  let files: PullRequestFile[];
  try {
    files = await github.paginate(github.rest.pulls.listFiles, {
      owner: context.repo.owner,
      repo: context.repo.repo,
      pull_number: pullRequest.number,
      per_page: 100,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    core.warning(`Could not read pull-request files; run full CI: ${message}`);
    setForceFull(core);
    return;
  }

  const changedFileCount = pullRequest.changed_files;
  if (!Number.isInteger(changedFileCount) || changedFileCount <= 0 || files.length !== changedFileCount) {
    core.warning(
      `Could not verify the complete pull-request file list (returned ${files.length}; expected ${changedFileCount ?? 'unknown'}); run full CI.`,
    );
    setForceFull(core);
    return;
  }

  const changedPaths = files
    .flatMap((file) => [file.filename, file.previous_filename])
    .filter((name): name is string => Boolean(name));
  const unexpected = [...new Set(changedPaths
    .filter((name) => !RELEASE_ONLY_FILES.has(name)))].sort();
  const documentationOnly = changedPaths.length > 0
    && changedPaths.every(isDocumentationPath);
  core.setOutput('documentation_only', documentationOnly ? 'true' : 'false');
  const pureRelease = unexpected.length === 0;
  const releaseCandidate = pullRequest.head?.ref?.startsWith('release-please--') || pureRelease;

  if (!releaseCandidate) {
    setAlways(core, documentationOnly);
    return;
  }

  const trustedMetadata =
    context.payload.repository?.full_name === repository
    && pullRequest.base.repo?.full_name === repository
    && pullRequest.base.ref === 'master'
    && pullRequest.head.repo?.full_name === repository
    && pullRequest.head.ref === 'release-please--branches--master'
    && pullRequest.user.login === 'release-please-kotventure[bot]'
    && pullRequest.user.type === 'Bot'
    && context.payload.sender?.login === 'release-please-kotventure[bot]'
    && context.payload.sender?.type === 'Bot';

  if (!trustedMetadata || !pureRelease) {
    if (unexpected.length > 0) {
      core.info(`Release candidate has non-release changes; run full CI: ${unexpected.join(', ')}`);
    } else {
      core.info('Release candidate lacks trusted App provenance; run full CI.');
    }
    setForceFull(core);
    return;
  }

  let provenanceRun: WorkflowRunListItem | undefined;
  try {
    for (let attempt = 0; attempt < 6; attempt += 1) {
      provenanceRun = await findReleaseProvenanceRun({
        github,
        owner: context.repo.owner,
        repo: context.repo.repo,
        repository,
        headSha: pullRequest.head.sha,
        headRef: pullRequest.head.ref,
        pullNumber: pullRequest.number,
      });
      if (provenanceRun?.status === 'completed' || attempt === 5) break;
      await new Promise((resolve) => setTimeout(resolve, 10_000));
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    core.warning(`Could not read trusted Release Please provenance; run full CI: ${message}`);
    setForceFull(core);
    return;
  }

  if (!provenanceRun || provenanceRun.status !== 'completed') {
    core.info('Trusted Release Please provenance is missing or incomplete; run full CI.');
    setForceFull(core);
    return;
  }

  let jobs: JobItem[];
  try {
    jobs = await github.paginate(github.rest.actions.listJobsForWorkflowRun, {
      owner: context.repo.owner,
      repo: context.repo.repo,
      run_id: provenanceRun.id,
      per_page: 100,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    core.warning(`Could not read the provenance job result; run full CI: ${message}`);
    setForceFull(core);
    return;
  }
  const trustedJob = jobs.find((job) => job.name === 'Trusted release provenance');

  if (trustedJob?.status === 'completed' && trustedJob.conclusion === 'success') {
    core.info('Trusted Release Please provenance verified; skip heavy CI.');
    core.setOutput('run', 'false');
    core.setOutput('release_only', 'true');
    core.setOutput('release_candidate', 'true');
    return;
  }

  core.info('Trusted Release Please provenance did not succeed; run full CI.');
  setForceFull(core);
}

export {
  decideGate,
};
