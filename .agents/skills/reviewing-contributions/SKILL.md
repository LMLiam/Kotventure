---
name: reviewing-contributions
description: Use when reviewing, improving, or landing someone else's PR or PR stack in Kotventure (contributor, intern, or bot-authored) — deciding whether to rework or rebuild, raising design alternatives, updating PR bodies and review threads, and setting project board fields before merge.
---

# Reviewing contributions

The maintainer-side pass that takes an incoming PR from "submitted" to "landable". The bar is
the same as for original work: the reviewed PR should read as if a staff engineer wrote it.

## 1. Judge the shape first, then the code

Read the diff against the design skills before line-commenting:

- `idiomatic-kotlin-dsl` — resolution ladder, pressure-test, house rules. A PR built on the
  wrong rung (typed keys + string overloads, runtime checks for compile-time facts) gets a
  **shape** conversation, not fifty nitpicks.
- Hard structural rules: one top-level class/interface/object per file (feature-grouped
  top-level functions/vals may share), package-by-feature, `internal` implementation,
  `explicitApi()` + KDoc, tests included (`writing-component-tests`).

**Rebuild-or-improve decision:** if the core design is right, improve in place. If the shape
is wrong, rebuilding on the contributor's branch is usually cheaper and kinder than a
20-round review — keep their commits' intent, credit them, and say so in the PR.

## 2. Raise design forks as questions, not fiats

When a genuinely better shape exists, present it to the maintainer/author as a fork with
**concrete call-site previews** — the before/after as a consumer writes it — and a
recommendation. No design decision is locked (issue text and prior plans included), but the
final call on API shape is the maintainer's.

## 3. Improve

- Work on the PR branch; **force-push is fine** — feature-branch history is disposable and
  there are no backwards-compatibility obligations pre-1.0 (no deprecated forwarders, no dual
  APIs; delete superseded forms outright).
- Every improvement pass ends green locally: `./gradlew ktlintFormat build` (see
  `fixing-ci-failures` for anything red).
- Keep commit subjects conventional (`verb(area): …`) — they become the squashed title and
  the changelog.

## 4. Communicate

- **PR body:** keep it true after your changes — what the PR does now, `Closes #<n>`, the
  matching template's sections filled in.
- **Review threads:** answer every open thread — what changed, or why not (branch protection
  requires conversations resolved). Don't resolve someone else's thread without a reply.
- Report honestly: if you rebuilt, say you rebuilt; if a test was weakened or skipped, that's
  a blocker, not a footnote.

## 5. Project metadata & merge gates

Before calling it done:

- Attach the PR to the issue's GitHub Project (e.g. Kotventure Roadmap) and mirror the
  issue's fields: `Status`, `Priority`, `Area`, `Kind`, `Effort`, `Risk`, `Contributor fit`.
  Verify with `gh project item-list`.
- Required checks: Build/Test/Lint aggregate, both title validations, dependency review —
  plus one approving code-owner review and all conversations resolved. Squash-merge only.
- Watch CI after the final push; a merged-then-red master is your incident.

## Anti-patterns

- ❌ Approving code you'd have written differently *on principle* — review the shape, then
  let style points go if they meet the skills' bars.
- ❌ Nitpick cascades on a PR whose design is wrong — have the shape conversation first.
- ❌ Silent rewrites — always narrate what you changed on the contributor's work and why.
- ❌ Merging with unset project fields or unanswered threads.
