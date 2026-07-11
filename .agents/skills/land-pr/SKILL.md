---
name: land-pr
description: Run the maintainer-side pass on a Kotventure pull request from a number — review the shape, improve or rebuild on the branch, work the review threads, and get it to a landable state. Invoked by the user as /land-pr <pr-number>.
argument-hint: <pr-number>
disable-model-invocation: true
---

# Land PR $ARGUMENTS

Take the PR from "submitted" to "landable". The workflow is `reviewing-contributions` —
this command wires the PR in.

1. **Load full context**: `gh pr view $ARGUMENTS --comments`, `gh pr diff $ARGUMENTS`,
   `gh pr checks $ARGUMENTS`, plus unresolved review threads
   (`gh api repos/{owner}/{repo}/pulls/$ARGUMENTS/comments`). Read the linked issue too —
   the PR is judged against its acceptance criteria.
2. **Run the `reviewing-contributions` pass**: shape verdict first (improve vs rebuild),
   then improvements on the PR branch, ending green locally
   (`./gradlew ktlintFormat build`).
3. **Work every thread**: reply with what changed or why not; never resolve silently.
4. **Close out**: PR body still accurate, project fields set and verified with
   `gh project item-list`, CI green after the final push. Report what was changed on the
   contributor's work and what remains for the maintainer to decide.

Do not merge — merging is the maintainer's click unless explicitly told otherwise.
