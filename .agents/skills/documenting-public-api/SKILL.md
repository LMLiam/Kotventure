---
name: documenting-public-api
description: >-
  Use this skill to write or review KDoc, public declarations, samples, module READMEs, or docs. Also use it for Dokka
  warnings and documentation links.
---

# Documenting public API

Each library module uses `explicitApi()`. Compilation fails if a public declaration has no explicit visibility or
return type. Dokka uses `failOnWarning` and `reportUndocumented`. Thus, an undocumented public declaration or an
unresolved KDoc link fails `dokkaGenerate` in CI. This rule includes deliberate `toString`, `equals`, and `hashCode`
overrides, and companion objects. Give them a one-line contract such as `/** Compares values in the node list. */`.
Write documentation as part of the change.

## What KDoc must say

State the **contract**, not the signature. Explain the result, invariants, and each failure mode.

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

- Add `@throws` for each immediate failure. A duplicate singleton causes `IllegalStateException` as part of the DSL
  contract. Refer to `idiomatic-kotlin-dsl`.
- State when a function has **no side effects**. If it has side effects, identify them. For example, state that it
  "builds **and shows**" a boss bar.
- Do not restate the signature. Document behaviour that the types do not express.

## Linking symbols — priority order

1. Use `[Ref]` for a short bracket reference. **Import the symbol** so that it resolves.
2. Use `[text][fully.qualified.name]` when a short reference does not resolve or is unclear.
3. Use `` `code` `` only for text that is not a symbol.

Do not write a bare FQN link such as `[io.github.lmliam.…Foo]` when an import and `[Foo]` work.

## Samples — `@sample`

Each module has a `src/samples/kotlin` source set. The `check` task compiles it, Kover excludes it, and Dokka renders
it.

- Write an `internal fun <feature>Sample()` in the matching package under `src/samples/kotlin`. Refer to it as
  `@sample io.github.lmliam.kotventure.<module>.<pkg>.<name>Sample`.
- Samples are compiled code and detect API changes. Use them when a signature does not make the call form clear. These
  cases include DSL blocks, `bind`, and context parameters.

## Comments in code

- Comments explain a constraint that the code cannot show. Do not describe the next line, the source of the code, or a
  pull-request decision.
- Do not add narrative comments to Gradle or CI files. Job names, task `description` values, and `docs/CI.md` state the
  intent. SHA pin comments such as `# vX.Y.Z` are permitted in Actions workflows.
- Do not add internal audit identifiers to repository files. Do not describe deferred work in `docs/`.

## Beyond KDoc

- A **module README** explains the artefact, its dependency, and its primary API. Use `modules/test/README.md` as the
  model.
- Update **`docs/`** in the same pull request for a change that affects users. The examples in section 5 of
  `docs/DESIGN.md` are illustrative. Keep them plausible, but do not use them as API tests.
- Release-please generates **`CHANGELOG.md`**. Do not edit it. Write commit subjects that are suitable changelog entries.
- Verify the documentation build. `./gradlew build` compiles samples, and CI runs Dokka. Run `dokkaGenerate` locally
  after a large change to documentation markup.
