'use strict';

const fs = require('node:fs');
const path = require('node:path');
const {
  QODANA_CHECK_FILE_NAME,
  buildCheckArtifactName,
} = require('./qodana-contract.js');
const {
  buildCheckExternalId,
  createWorkflowCheck,
} = require('./workflow-run-check.js');

const QODANA_PR_CHECK_NAME = 'Qodana / pull request';

async function registerQodanaCheck({
  github,
  context,
  source,
  qodanaRunId,
  qodanaRunAttempt,
  outputDirectory,
}) {
  const externalId = buildCheckExternalId({
    kind: 'qodana-pr',
    runId: qodanaRunId,
    runAttempt: qodanaRunAttempt,
    headSha: source.headSha,
  });
  const check = await createWorkflowCheck({
    github,
    context,
    name: QODANA_PR_CHECK_NAME,
    headSha: source.headSha,
    externalId,
    summary: 'Qodana is analysing the validated pull-request source.',
  });
  const artifactName = buildCheckArtifactName({
    sourceKind: source.sourceKind,
    runId: source.runId,
    runAttempt: source.runAttempt,
    qodanaRunId,
    qodanaRunAttempt,
    checkRunId: check.id,
    headSha: source.headSha,
    baseSha: source.baseSha,
  });
  fs.mkdirSync(outputDirectory, { recursive: true });
  fs.writeFileSync(
    path.join(outputDirectory, QODANA_CHECK_FILE_NAME),
    `${JSON.stringify({
      version: 1,
      artifactName,
      check,
      pullRequest: source.pullRequest,
    })}\n`,
    { mode: 0o600 },
  );
  return { artifactName, check };
}

module.exports = {
  QODANA_PR_CHECK_NAME,
  registerQodanaCheck,
};
