---
name: adding-a-dsl-feature
description: Use when implementing a new DSL feature, picking up a roadmap/feature issue, or starting any issue-to-merge slice in Kotventure. Use at the START of the work, before reading code or posting a plan.
---

# Adding a DSL feature

The end-to-end, **small-slice** workflow: one issue → one focused, well-tested, idiomatic
change → one reviewable PR. Copy this checklist into your progress tracking and work it in
order:

```text
- [ ] 1. Scope from the issue
- [ ] 2. Design the surface (GATE: idiomatic-kotlin-dsl first)
- [ ] 3. Plan (if the issue warrants one) → .agents/plans/
- [ ] 4. Test first
- [ ] 5. Implement
- [ ] 6. Verify locally
- [ ] 7. Document
- [ ] 8. Commit, PR, project metadata
```

## 1. Scope from the issue

- Implement exactly the issue's **Scope** + **Acceptance criteria** — nothing speculative.
- If a listed **Dependency** isn't merged, stop and raise it.
- Confirm the target **module** and **feature package** from `docs/DESIGN.md` §4–5
  (`core` depends only on `adventure-api`; modules enable lazily in `settings.gradle`).

## 2. Design the surface — GATE

Read the `idiomatic-kotlin-dsl` skill **before any plan or code** and run your sketch through
its resolution ladder, pressure-test, and house rules. Signals you're on the wrong shape:
typed key classes, string overloads, registration side effects, `var` properties in scopes, or
runtime `require(...)` for what a property could enforce at compile time.

- Issue snippets and `docs/DESIGN.md` §5 are **illustrative, not contracts** — deliver the
  intent, not the syntax, and raise better shapes with call-site examples.
- The builder must produce a **real Adventure object**; confirm types via the
  `adventure-reference` skill — don't guess.

## 3. Plan

For multi-slice or design-heavy issues, write the implementation plan to `.agents/plans/`.
If posting a plan to the issue, state which rung of the resolution ladder each named concept
uses and why a higher rung wasn't possible.

## 4. Test first

Write Kotest specs asserting the produced Adventure object using the project's own matchers —
follow the `writing-component-tests` skill (dogfooding rules, matcher semantics, snapshot and
compile-fail patterns). Cover the happy path, nesting, and the issue's named edge cases,
including fail-fast `IllegalStateException` contracts.

## 5. Implement

- One top-level class/interface/object per file; public DSL in the feature package,
  implementation detail `internal`.
- Wrap Adventure; don't reinvent. No `ServiceLoader`/reflection/process-global registries.
- KDoc every public declaration — see the `documenting-public-api` skill (`@sample` uses the
  `src/samples/kotlin` source set).

## 6. Verify locally

```bash
./gradlew ktlintFormat   # or: spotlessApply
./gradlew build          # compile + tests + lint + koverVerify (85% line gate)
```

If a check fails, the `fixing-ci-failures` skill maps each failure to its fix. Don't lower
the coverage threshold — add tests.

## 7. Document

- Update `docs/` if the change is user-facing. **Never touch `CHANGELOG.md`** — release-please
  owns it; your conventional commit subjects become the changelog.
- New module? Enable it in `settings.gradle` and add it to the `bom`, plus a module README
  following the existing ones.

## 8. Commit, PR, project metadata

- Subjects and PR title: `verb(area): something` — lowercase, scope required (enforced by CI;
  recommended verbs/areas in `.github/CONTRIBUTING.md`).
- Branch: `type/issue-<n>/short-desc` (e.g. `feat/issue-19/style-dsl`).
- PR: pick the matching template from `.github/PULL_REQUEST_TEMPLATE/`, link with
  `Closes #<n>`.
- If the issue sits on a GitHub Project (e.g. Kotventure Roadmap): attach the PR to the same
  project and mirror `Status`, `Priority`, `Area`, `Kind`, `Effort`, `Risk`,
  `Contributor fit` from the issue; verify with `gh project item-list` before reporting done.

## Definition of done

Acceptance boxes tickable · CI green · diff reviewable in one sitting · matchers/tests
shipped with the change, not promised after.
