---
name: land-pr
description: >-
  Run the maintainer review of a numbered Kotventure pull request. Review its API, improve the branch, answer review
  threads, and make it ready to land. The user invokes this skill with /land-pr <pr-number>.
argument-hint: <pr-number>
disable-model-invocation: true
---

# Land PR $ARGUMENTS

Use the `reviewing-contributions` workflow to make the submitted pull request ready to land.

1. **Load the context:** Run `gh pr view $ARGUMENTS --comments`, `gh pr diff $ARGUMENTS`,
   `gh pr checks $ARGUMENTS`, plus unresolved review threads
   (`gh api repos/{owner}/{repo}/pulls/$ARGUMENTS/comments`). Also read the linked issue. Compare the pull request with
   its acceptance criteria.
2. **Run the `reviewing-contributions` process:** Decide whether to improve or rebuild the API. Then, make changes on
   then improvements on the PR branch, ending green locally
   (`./gradlew ktlintFormat build`).
3. **Answer each thread:** State what changed or why it did not change. Do not resolve a thread silently.
4. **Complete the work:** Make sure that the pull-request body is accurate. Set the project fields and verify them with
   `gh project item-list`, CI green after the final push. Report what was changed on the
   contributor's work and what remains for the maintainer to decide.

Do not merge unless the maintainer gives an explicit instruction.
