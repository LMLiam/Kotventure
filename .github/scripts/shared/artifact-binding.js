'use strict';

const { createValidators } = require('./validation.js');

function createArtifactBinding(reject) {
  const {
    requireEqual,
    requireInteger,
    requireObject,
  } = createValidators(reject);

  function validateArtifactBinding({ artifact, expected, maxBytes, label }) {
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

  return { validateArtifactBinding };
}

module.exports = { createArtifactBinding };
