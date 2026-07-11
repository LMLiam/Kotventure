---
name: pickup-issue
description: Start work on a Kotventure issue from a number — fetches the issue, sets up the branch, and drives the full issue-to-merge workflow. Invoked by the user as /pickup-issue <issue-number>.
argument-hint: <issue-number>
disable-model-invocation: true
---

# Pick up issue $ARGUMENTS

Drive the issue end-to-end. The workflow is `adding-a-dsl-feature` — this command just wires
the issue in; every gate in that skill (design-first, test-first, verify, project metadata)
applies.

1. **Read the issue completely**: `gh issue view $ARGUMENTS --comments`. Extract Scope,
   Acceptance criteria, Dependencies, milestone, and any maintainer decisions in the
   comments. Comments override the issue body when they conflict.
2. **Stop-and-ask conditions** — raise to the maintainer before any code: a listed dependency
   is unmerged; the scope is ambiguous; or the sketched API fails the `idiomatic-kotlin-dsl`
   pressure-test and a better shape should be proposed first.
3. **Branch** from up-to-date `origin/master`: `type/issue-$ARGUMENTS/short-desc`
   (type matching the issue's kind: `feat`/`fix`/`refactor`/…).
4. **Execute the `adding-a-dsl-feature` checklist** from step 2 (design gate) onward,
   copying its checklist into progress tracking.
5. **Finish** with the PR linked via `Closes #$ARGUMENTS`, project fields mirrored from the
   issue, and CI green — per that skill's definition of done.
