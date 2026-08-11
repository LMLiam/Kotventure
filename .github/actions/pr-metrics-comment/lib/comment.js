'use strict';

const MARKER = '<!-- pr-metrics -->';

async function upsertComment({ github, context, body, pullNumber = context.issue.number }) {
  if (!Number.isSafeInteger(pullNumber) || pullNumber < 1) throw new Error('pullNumber must be a positive integer');
  const fullBody = `${MARKER}\n${body}`.trimEnd() + '\n';
  const comments = await github.paginate(github.rest.issues.listComments, {
    owner: context.repo.owner,
    repo: context.repo.repo,
    issue_number: pullNumber,
    per_page: 100,
  });

  const existing = comments.find(
    (c) => c.user?.login === 'github-actions[bot]' && c.body.startsWith(MARKER),
  );
  if (existing) {
    await github.rest.issues.updateComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      comment_id: existing.id,
      body: fullBody,
    });
  } else {
    await github.rest.issues.createComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: pullNumber,
      body: fullBody,
    });
  }
}

module.exports = { upsertComment };
