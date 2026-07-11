---
name: documenting-public-api
description: Use when writing or reviewing KDoc, adding a public declaration (explicitApi is on), choosing how to link a symbol in docs, adding an @sample, updating module READMEs or docs/, or when Dokka fails on a warning.
---

# Documenting public API

`explicitApi()` is on for every library module (missing visibility/return types fail
compilation) and Dokka runs with `failOnWarning` + `reportUndocumented`: an undocumented
public declaration or an unresolvable KDoc link fails `dokkaGenerate` in CI. This includes
deliberate `toString`/`equals`/`hashCode` overrides and companion objects — give them a
one-line contract (`/** Value equality over the node list. */`). Docs are part of the
change, written alongside the code — not after.

## What KDoc must say

State the **contract**, not the signature: what the caller gets, the invariants, and every
failure mode.

```kotlin
/**
 * Builds a [Sound] configured by [init].
 *
 * Construction only — no side effects. Use this when a sound value is shared, stored, or
 * passed to a later playback API.
 *
 * @throws IllegalStateException when any slot is set twice.
 * @sample io.github.lmliam.kotventure.core.sound.soundSample
 */
```

- `@throws` on every fail-fast path — duplicate-singleton `IllegalStateException`s are part
  of the DSL contract (see `idiomatic-kotlin-dsl`), so they are documented, always.
- Say when a function has **no side effects** (or exactly which it has — "builds **and
  shows**") — that distinction is this DSL's biggest reader question.
- Don't restate the signature ("Sets the color" above `fun color(...)` adds nothing);
  document behaviour the types can't express.

## Linking symbols — priority order

1. `[Ref]` — short bracket reference; **import the symbol** so it resolves.
2. `[text][fully.qualified.name]` — when a short ref can't resolve or reads badly.
3. `` `code` `` — last resort, for non-symbols.

Never write a bare FQN link `[io.github.lmliam.…Foo]` when an import + `[Foo]` works.

## Samples — `@sample`

Each module has a `src/samples/kotlin` source set (compiled by `check`, excluded from
coverage, rendered by Dokka):

- Write an `internal fun <feature>Sample()` in the mirrored package under
  `src/samples/kotlin`, reference it as
  `@sample io.github.lmliam.kotventure.<module>.<pkg>.<name>Sample`.
- Samples are real compiled code — they break when the API changes, which is the point.
  Use them for any entry point whose call shape isn't obvious from the signature (DSL
  blocks, `bind`, context parameters).

## Comments in code

- Comments explain **why** — a constraint the code can't show. Never what the next line does,
  where code came from, or why a change is correct (that's PR-review talk).
- **No narrating comments in Gradle/CI glue** — job names, task `description`s, and
  `docs/CI.md` carry intent. SHA pin comments (`# vX.Y.Z`) on Actions are fine.
- No internal audit IDs in repo files; don't document deferred/future work in `docs/`.

## Beyond KDoc

- **Module README** (`modules/<name>/README.md`): what the artifact is, how to depend on it,
  the surface at a glance — follow `modules/test/README.md` as the exemplar.
- **`docs/`** updated in the same PR for user-facing changes; `docs/DESIGN.md` §5 snippets
  are illustrative — keep them plausible, don't treat them as API tests.
- **`CHANGELOG.md` is generated** (release-please) — never hand-edit; your commit subjects
  are the changelog entries, write them to be read.
- Verify docs build: `./gradlew build` covers sample compilation; Dokka runs in CI
  (`dokkaGenerate` locally when touching doc markup broadly).
