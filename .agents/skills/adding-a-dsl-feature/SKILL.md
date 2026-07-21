---
name: adding-a-dsl-feature
description: >-
  Use this skill to implement a new DSL feature or a roadmap issue. Use it at the start of the work, before you read
  code or post a plan.
---

# Adding a DSL feature

Use this workflow for one small change from an issue to a pull request. Copy this checklist to the task record. Complete
the steps in the specified order:

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

- Implement only the issue's **Scope** and **Acceptance criteria**.
- If a listed **Dependency** is not merged, stop and tell the maintainer.
- Confirm the target **module** and **feature package** in sections 4 and 5 of `docs/DESIGN.md`. The `core` module
  depends only on `adventure-api`. Enable modules in `settings.gradle` only when they are necessary.

## 2. Design the surface: gate

Read the `idiomatic-kotlin-dsl` skill **before you make a plan or write code**. Apply its resolution ladder, design
test, and project rules to your proposal. Review the design if it contains typed key classes, string overloads,
registration side effects, or `var` properties in scopes. Also review it if runtime `require(...)` replaces a
compile-time property check.

- Issue snippets and section 5 of `docs/DESIGN.md` are **illustrative and not contracts**. Implement their intent. If
  you find a better design, show the maintainer concrete call-site examples.
- The builder must produce an Adventure object. Use the `adventure-reference` skill to confirm types. Do not guess.

## 3. Plan

For an issue with multiple slices or many design decisions, write the implementation plan in `.agents/plans/`. If you
post a plan to the issue, identify the resolution-ladder level for each named concept. Explain why a higher level is
not possible.

## 4. Test first

Write Kotest specifications that examine the Adventure object. Use the project's matchers and follow the
`writing-component-tests` skill. Test the usual path, nested content, and each edge case in the issue. Include contracts
that immediately throw `IllegalStateException`.

## 5. Implement

- Put only one top-level class, interface, or object in each file. Put the public DSL in the feature package. Make
  implementation details `internal`.
- Wrap Adventure. Do not reimplement it. Do not use `ServiceLoader`, reflection, or process-wide registries.
- Write KDoc for each public declaration. Follow the `documenting-public-api` skill. The `@sample` tag uses the
  `src/samples/kotlin` source set.

## 6. Verify locally

```bash
./gradlew ktlintFormat   # or: spotlessApply
./gradlew build          # compile + tests + lint + koverVerify (85% line gate)
```

If a check fails, use the `fixing-ci-failures` skill. Do not decrease the coverage threshold. Add tests.

## 7. Document

- Update `docs/` if the change affects users. **Do not edit `CHANGELOG.md`** because release-please generates it. Your
  conventional commit subjects become changelog entries.
- For a new module, enable it in `settings.gradle`, add it to the `bom`, and add a module README. Use an existing module
  README as a model.

## 8. Commit, PR, project metadata

- Use `verb(area): something` for commit subjects and the pull-request title. Use a lowercase verb and include a scope.
  CI enforces this format. `.github/CONTRIBUTING.md` lists the recommended verbs and areas.
- Branch: `type/issue-<n>/short-desc`, for example `feat/issue-19/style-dsl`.
- Select the applicable template from `.github/PULL_REQUEST_TEMPLATE/`. Link the issue with `Closes #<n>`.
- If the issue is in a GitHub Project, add the pull request to the same project. Copy `Status`, `Priority`, `Area`,
  `Kind`, `Effort`, `Risk`, and `Contributor fit` from the issue. Before you report completion, use
  `gh project item-list` to verify these values.

## Definition of done

The acceptance boxes are complete. CI is green. The diff is small enough for one review session. The change includes
its matchers and tests.
