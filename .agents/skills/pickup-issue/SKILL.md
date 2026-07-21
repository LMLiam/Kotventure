---
name: pickup-issue
description: >-
  Start work on a numbered Kotventure issue. Get the issue, create the branch, and complete the issue-to-merge workflow.
  The user invokes this skill with /pickup-issue <issue-number>.
argument-hint: <issue-number>
disable-model-invocation: true
---

# Pick up issue $ARGUMENTS

Complete the issue-to-merge workflow in `adding-a-dsl-feature`. Apply all its gates for design, tests, verification,
and project metadata.

1. **Read the issue completely**: `gh issue view $ARGUMENTS --comments`. Extract Scope,
   Acceptance criteria, Dependencies, milestone, and any maintainer decisions in the
   comments. Comments override the issue body when they conflict.
2. **Conditions that require maintainer input:** Stop before you write code if a dependency is not merged or the scope
   is unclear. Also stop if the proposed API fails the `idiomatic-kotlin-dsl` design test. Propose a better API form
   when applicable.
3. **Branch:** Create `type/issue-$ARGUMENTS/short-desc` from the current `origin/master`. Match the type to the issue
   kind, such as `feat`, `fix`, or `refactor`.
4. **Execute the `adding-a-dsl-feature` checklist** from step 2 (design gate) onward,
   copying its checklist into progress tracking.
5. **Finish:** Link the pull request with `Closes #$ARGUMENTS`, copy the project fields from the issue, and make CI
   green. Meet the definition of done in that skill.
