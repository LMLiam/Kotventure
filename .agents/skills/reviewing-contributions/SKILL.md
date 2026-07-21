---
name: reviewing-contributions
description: >-
  Use this skill to review, improve, or land another person's Kotventure pull request. It covers design decisions,
  branch changes, review threads, and project fields.
---

# Reviewing contributions

Use this maintainer process to make a submitted pull request ready to land. Apply the same quality requirements as for
original work.

## 1. Judge the shape first, then the code

Read the diff and apply the design skills before you write line comments:

- `idiomatic-kotlin-dsl` supplies the resolution ladder, pressure-test, and house rules. If a pull request uses the wrong level,
  discuss the API design first. Examples include typed keys with string overloads and runtime checks for compile-time
  facts.
- Hard structural rules: Use one top-level class, interface, or object per file. Feature-grouped top-level functions
  and values can share a file. Use feature packages, `internal` implementations, `explicitApi()`, KDoc, and tests.
  Refer to `writing-component-tests`.

If the primary design is correct, improve it in place. If the API form is incorrect, rebuild it on the contributor's
branch. Preserve the intent of the commits, credit the contributor, and explain the rebuild in the pull request.

## 2. Present design alternatives

When you find a better API form, present it to the maintainer and author as an alternative. Include concrete before and
after call-site examples and a recommendation. You can review issue text and prior plans. The maintainer makes the final
API decision.

## 3. Improve

- Work on the pull-request branch. You can force-push this feature branch. Before version 1.0, do not keep deprecated
  forwarders or two APIs. Delete replaced forms.
- Every improvement pass ends green locally: `./gradlew ktlintFormat build` (see
  `fixing-ci-failures` for anything red).
- Use the conventional `verb(area): …` format for commit subjects. They become the squash title and changelog entries.

## 4. Communicate

- **Pull-request body:** Keep it accurate after your changes. Describe the current change, include `Closes #<n>`, and
  complete the applicable template sections.
- **Review threads:** Answer each open thread with what changed or why it did not change. Do not resolve another
  person's thread without a reply.
- State if you rebuilt the change. A weaker or skipped test blocks completion.

## 5. Project metadata & merge gates

Before you report completion:

- Attach the PR to the issue's GitHub Project, such as Kotventure Roadmap. Mirror the
  issue's fields: `Status`, `Priority`, `Area`, `Kind`, `Effort`, `Risk`, `Contributor fit`.
  Verify with `gh project item-list`.
- Required checks are the Build, Test, and Lint aggregate, both title validations, and dependency review. Also require
  one code-owner approval and no unresolved conversation. Use only a squash merge.
- Monitor CI after the final push. If `master` becomes red after the merge, investigate it immediately.

## Prohibited review forms

- ❌ Do not reject correct code only because you prefer a different style. Review the API form first, and then apply
  the skill requirements.
- ❌ Do not add many small comments when the primary design is incorrect. Discuss the API form first.
- ❌ Do not rewrite silently. Explain what you changed in the contributor's work and why.
- ❌ Do not merge with unset project fields or unanswered threads.
