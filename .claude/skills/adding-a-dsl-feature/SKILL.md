---
name: adding-a-dsl-feature
description: Use when implementing any new DSL feature or picking up a feature issue in Kotventure — the end-to-end, small-slice workflow from issue to merge.
---

# Adding a DSL feature

A repeatable, **small-slice** workflow. The goal is one focused, well-tested, idiomatic change — not a kitchen sink.

## 1. Scope from the issue

- Read the issue's **Scope** and **Acceptance criteria**. Implement exactly that — nothing speculative (YAGNI).
- Check **Dependencies** in the issue; if a dependency isn't merged, stop and reconsider.
- Confirm the target **module** and **feature package** from `docs/DESIGN.md` §4–5. `core` only depends on
  `adventure-api`.

## 2. Design the surface (tiny) — GATE: do this before writing any plan or code

- **Read "Designing the API shape" in skill `idiomatic-kotlin-dsl` first** and run your sketch through its resolution
  ladder and pressure-test. If you are about to introduce typed key classes, string overloads, registration side
  effects, or runtime `require(...)` checks for things a property could enforce at compile time — stop, you are on the
  wrong design. Sketch at least one alternative that moves validation to the compiler before committing to a shape.
- If you are posting an implementation plan to an issue, the plan must state which rung of the resolution ladder each
  named concept uses and why a higher rung wasn't possible.
- Sketch the smallest idiomatic DSL that satisfies the issue. Lambda-with-receiver builders under a `@DslMarker` scope
  are the default, but not the only tool — `object` declarations with delegated properties, extensions, and sealed
  types are equally in scope when they make the call site safer or simpler.
- Issue snippets and `docs/DESIGN.md` §5 are **illustrative, not contracts**. If the snippet doesn't type-check as
  written, design the API that delivers its intent — don't replicate its syntax with runtime machinery.
- The builder must produce a **real Adventure object**. Confirm the target type and methods via skill
  `kyori-adventure-reference` — don't guess.
- Public entry points get explicit visibility + return type + **KDoc** (`explicitApi()` is on).

## 3. Test first (when practical)

- Write Kotest specs asserting the produced component, using the project's **own matchers** (skill
  `writing-component-tests`).
- Cover the happy path, nesting, and the obvious edge case from the issue.

## 4. Implement

- One file = one responsibility. Keep the public DSL in the feature package; mark internals `internal`.
- Wrap Adventure; don't reinvent. No `ServiceLoader`/reflection.

## 5. Verify

```bash
./gradlew ktlintFormat
./gradlew build      # compiles, tests, lints
```

- `explicitApi()` must be satisfied (no missing visibility/types). KDoc on public API.

## 6. Wire up & document

- If you added a new module, enable it in `settings.gradle` and the `bom`.
- Update `docs/` examples and `CHANGELOG.md` if the change is user-facing.

## 7. Commit & PR

- Subjects: `verb(area): something` (e.g. `feat(core): add the style DSL`). Enforced in CI.
- Branch `feat/issue-<n>/short-desc`; PR uses the feature template; `Closes #<n>`.

## Definition of done

The issue's acceptance boxes are all tickable, CI is green, and the diff is small enough to review in one sitting.
