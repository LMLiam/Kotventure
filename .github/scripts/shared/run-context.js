'use strict';

const { createValidators } = require('./validation.js');

function validateEventRun(reject, { eventRun, run }) {
  const { requireEqual, requireObject } = createValidators(reject);
  requireObject(eventRun, 'workflow_run event');
  requireEqual(eventRun.id, run.id, 'workflow run id');
  requireEqual(eventRun.run_attempt, run.run_attempt, 'workflow run attempt');
  requireEqual(eventRun.head_sha, run.head_sha, 'workflow run head SHA');
  if (eventRun.workflow_id != null) requireEqual(eventRun.workflow_id, run.workflow_id, 'workflow run workflow id');
}

async function fetchWorkflowRunContext(reject, { github, owner, repo, eventRun }) {
  const { requireEqual, requireInteger, requireObject } = createValidators(reject);
  requireObject(eventRun, 'workflow_run event');
  requireInteger(eventRun.id, 'workflow run id');

  const [{ data: repository }, { data: run }] = await Promise.all([
    github.rest.repos.get({ owner, repo }),
    github.rest.actions.getWorkflowRun({ owner, repo, run_id: eventRun.id }),
  ]);

  requireObject(repository, 'repository');
  requireEqual(repository.full_name, `${owner}/${repo}`, 'repository identity');
  requireObject(run, 'workflow run');
  validateEventRun(reject, { eventRun, run });

  const { data: workflow } = await github.rest.actions.getWorkflow({
    owner,
    repo,
    workflow_id: run.workflow_id,
  });
  requireObject(workflow, 'workflow');

  return { repository, run, workflow };
}

module.exports = { fetchWorkflowRunContext, validateEventRun };
