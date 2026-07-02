# Entity selector domain model redesign

## Context

PR #215 adds strict parsing for Java Edition entity selectors. Its parser and DSL builder already produce the same
ordered `EntitySelectorArgument` representation, but the public API discards that structure when the DSL returns the
string-only `EntitySelector` value class. Callers must stringify and parse a DSL-built selector to inspect it.

The redesign makes the shared model public and explicit. All selector source entering Kotventure is validated; there
is no parallel unchecked representation or raw-only factory.

## Public model

`EntitySelector` becomes the immutable structured domain class. It owns an `EntitySelectorHead`, an ordered defensive
snapshot of `EntitySelectorArgument` values, and `hasExplicitArgumentList` so `@e[]` remains distinguishable from
`@e`.

```kotlin
public class EntitySelector(
    public val head: EntitySelectorHead,
    arguments: Collection<EntitySelectorArgument>,
    public val hasExplicitArgumentList: Boolean = arguments.isNotEmpty(),
) {
    public val arguments: List<EntitySelectorArgument>
    public fun asString(): String
}
```

Every typed selector factory and `parseEntitySelector` returns `EntitySelector` directly. The selector and NBT
component DSLs continue accepting `EntitySelector`. The former string-wrapping `entitySelector(raw)` factory is
removed; dynamic source must pass through `parseEntitySelector(source)`.

```text
typed DSL ─┐
           ├─> EntitySelector ─> canonical rendering
parser ────┘
```

## Validity contract

`EntitySelector` represents a grammar-valid selector supported by Kotventure. It does not promise every server-side
semantic rule that vanilla may apply after parsing.

Argument constructors enforce local invariants that do not depend on surrounding selector state, including positive
limits, finite coordinates, non-empty named tag/team conditions, and validated compound SNBT. The structured selector
constructor enforces head capabilities such as player heads rejecting `type` and `@s` rejecting `limit` and `sort`.

Duplicate single-use arguments and other vanilla-conformance rules remain outside this redesign. Issue #205 owns those
rules after they are measured against the Java Edition oracle.

## Presence and SNBT values

Tag and team filters no longer encode presence conditions as empty strings. Their public argument values use an
explicit sealed condition:

```kotlin
public sealed interface SelectorStringCondition {
    public data class Named(public val value: String) : SelectorStringCondition
    public data class Presence(public val value: SelectorPresence) : SelectorStringCondition
}
```

`Named` rejects empty values. `Presence` expresses the two vanilla empty-value forms without coupling meaning to an
empty string plus a separate Boolean. Tag and team share this condition abstraction because their grammars are
identical.

NBT arguments use `SnbtCompoundSource`, a validated immutable value with a private constructor and a strict public
`SnbtCompoundSource.parse(source)` factory. It guarantees compound SNBT syntax without introducing a second NBT object
model.

## Rendering and source fidelity

The structured model promises semantic round-tripping with canonical output, not byte-for-byte concrete-syntax
preservation. Argument order, repetition, empty argument lists, negation, and SNBT payloads remain represented.
Numeric lexemes, redundant exact ranges, escape spelling, namespace spelling, and quote delimiters may canonicalize.

`Name` therefore stores the decoded value and negation only; it does not expose an original quote delimiter. The
renderer quotes and escapes a name only when required.

MiniMessage-to-DSL conversion validates selector patterns immediately and emits `parseEntitySelector(source)` for
dynamic Adventure selector and entity-NBT components. Valid non-canonical source may render canonically after
conversion. Unknown or unsupported syntax fails conversion with `EntitySelectorParseException`; it is not carried
through unchecked.

## Compatibility and migration

This is a deliberate pre-release API correction on the feature branch:

- The string-only `EntitySelector` value class becomes the structured model.
- `ParsedEntitySelector` and `ParsedEntitySelector.asEntitySelector()` are removed.
- Typed selector factories continue returning `EntitySelector`, now without discarding structure.
- `parseEntitySelector` returns `EntitySelector`.
- `entitySelector(raw)` is removed; `parseEntitySelector(raw)` is the sole dynamic-source bridge.
- Existing component entry points continue accepting `EntitySelector`.

Documentation and samples show structured selectors being passed directly to component DSL entry points.

## Testing

Changes follow red-green-refactor in focused steps:

1. Pin direct structured `EntitySelector` return types for the DSL and parser.
2. Pin strict dynamic-source validation and removal of the unchecked factory.
3. Pin constructor invariants and head compatibility.
4. Pin explicit tag/team presence conditions.
5. Pin validated SNBT construction.
6. Update parser round trips for canonical rendering and remove quote-delimiter assertions.
7. Update MiniMessage conversion to validate selector patterns and emit the strict parser call.
8. Run selector-focused tests, formatting checks, and the full Gradle build.

Tests assert model structure and real component integration rather than proving sharing through stringify-and-reparse.

## Out of scope

- A lossless concrete syntax tree with raw lexemes or source spans.
- A general NBT value model.
- Runtime selector execution or server-state resolution.
- New selector arguments beyond issues #193–#201.
- Duplicate and cross-argument vanilla conformance rules owned by issue #205.
