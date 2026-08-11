'use strict';

const { createValidators } = require('./validation.js');

function validateArtifactBinding(reject, { artifact, expected, maxBytes, label }) {
  const {
    requireEqual,
    requireInteger,
    requireObject,
  } = createValidators(reject);

  requireInteger(artifact.id, `${label} id`);
  if (artifact.expired !== false) {
    reject(`${label} is expired`);
  }
  requireInteger(artifact.size_in_bytes, `${label} size`, 1, maxBytes);

  const workflowRun = requireObject(artifact.workflow_run, `${label} workflow run`);
  requireEqual(workflowRun.id, expected.runId, `${label} workflow run id`);
  requireEqual(workflowRun.repository_id, expected.repositoryId, `${label} repository id`);
  requireEqual(workflowRun.head_repository_id, expected.headRepositoryId, `${label} head repository id`);
  requireEqual(workflowRun.head_branch, expected.headBranch, `${label} head branch`);
  requireEqual(workflowRun.head_sha, expected.headSha, `${label} head SHA`);
}

module.exports = { validateArtifactBinding };
