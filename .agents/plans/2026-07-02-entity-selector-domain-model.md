# Entity Selector Domain Model Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the string-only/parsed selector split with one validated, structured `EntitySelector` model and remove unchecked selector construction.

**Architecture:** Typed DSL factories and `parseEntitySelector` both construct the same immutable `EntitySelector`. Its arguments are valid locally, tag/team presence is explicit, SNBT source is validated, and selector-head compatibility is enforced at construction. Runtime strings enter only through `parseEntitySelector`; MiniMessage conversion validates before emitting parser-based Kotlin source.

**Tech Stack:** Kotlin 2.4, Adventure 5.1.1, Kotest, Gradle, ktlint, Spotless

---

## File structure

- Modify `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelector.kt`: own the immutable structured selector.
- Delete `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/ParsedEntitySelector.kt`: remove the duplicate public model.
- Modify `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorFactory.kt`: return structured selectors and remove unchecked construction.
- Modify `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorParser.kt`: return the common selector type.
- Create `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorStringCondition.kt`: model named and presence tag/team values explicitly.
- Create `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SnbtCompoundSource.kt`: own validated SNBT compound source.
- Modify `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorArgument.kt`: enforce local invariants and use the new value types.
- Modify `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorArgumentConversion.kt`: convert DSL state into valid argument values.
- Modify `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorArgumentRenderer.kt`: render canonical names, explicit conditions, and validated SNBT.
- Modify `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorFilterArgumentParsing.kt`: construct the new semantic values without retaining quote delimiters.
- Modify MiniMessage conversion files and their tests: validate dynamic selector source and emit `parseEntitySelector(...)`.
- Modify selector/NBT tests, samples, and `docs/DESIGN.md`: remove raw construction and document canonical strict parsing.

### Task 1: Consolidate selectors into one structured type

**Files:**
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorParserTest.kt`
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorTest.kt`
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/SelectorDslTest.kt`
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/nbt/EntityNbtDslTest.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelector.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/ParsedEntitySelector.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorFactory.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorParser.kt`

- [ ] **Step 1: Write failing model-sharing tests**

Replace the stringify/reparse sharing test with direct structural assertions:

```kotlin
"DSL factories return the shared structured selector model" {
    val built =
        entities {
            !type("zombie")
            tag("boss")
        }

    built.head shouldBe EntitySelectorHead.ENTITIES
    built.arguments shouldHaveSize 2
    built.arguments.first().shouldBeInstanceOf<EntitySelectorArgument.Type>()
    built.arguments.last().shouldBeInstanceOf<EntitySelectorArgument.Tag>()
}
```

Change the component integration assertion to pass the parsed selector directly:

```kotlin
"supplies parsed selectors directly to selector components" {
    val parsed = parseEntitySelector("@a[tag=admin]")

    selector(parsed)
        .shouldBeSelectorComponent()
        .shouldHaveSelectorPattern("@a[tag=admin]")
}
```

Change the defensive-snapshot constructor from `ParsedEntitySelector(...)` to `EntitySelector(...)`. Remove raw escape-hatch tests and replace remaining valid raw call sites with `parseEntitySelector(...)`.

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew :core:test --tests '*EntitySelectorParserTest'
```

Expected: compilation fails because DSL-built `EntitySelector` does not expose `head` or `arguments`, and parsed selectors cannot yet be supplied directly.

- [ ] **Step 3: Implement the common structured selector**

Replace the value class in `EntitySelector.kt` with the immutable model formerly split into `ParsedEntitySelector`:

```kotlin
public class EntitySelector(
    public val head: EntitySelectorHead,
    arguments: Collection<EntitySelectorArgument>,
    hasExplicitArgumentList: Boolean = arguments.isNotEmpty(),
) {
    public val arguments: List<EntitySelectorArgument> = arguments.immutableSnapshot()

    public val hasExplicitArgumentList: Boolean =
        hasExplicitArgumentList || this.arguments.isNotEmpty()

    public fun copy(
        head: EntitySelectorHead = this.head,
        arguments: Collection<EntitySelectorArgument> = this.arguments,
        hasExplicitArgumentList: Boolean = this.hasExplicitArgumentList,
    ): EntitySelector = EntitySelector(head, arguments, hasExplicitArgumentList)

    public fun asString(): String {
        val suffix =
            if (!hasExplicitArgumentList && arguments.isEmpty()) {
                ""
            } else {
                arguments.joinToString(",", "[", "]", transform = EntitySelectorArgument::render)
            }
        return "${head.token}$suffix"
    }

    public override fun equals(other: Any?): Boolean =
        other is EntitySelector &&
            head == other.head &&
            arguments == other.arguments &&
            hasExplicitArgumentList == other.hasExplicitArgumentList

    public override fun hashCode(): Int {
        var result = head.hashCode()
        result = 31 * result + arguments.hashCode()
        result = 31 * result + hasExplicitArgumentList.hashCode()
        return result
    }

    public override fun toString(): String = asString()
}
```

Retain explicit public KDoc and update it to describe construction through typed factories or `parseEntitySelector`.
Delete `ParsedEntitySelector.kt`.

In `EntitySelectorFactory.kt`, remove `entitySelector(raw)` and return:

```kotlin
return EntitySelector(head, builder.selectorArguments())
```

In `EntitySelectorParser.kt`, return `EntitySelector` for both empty and argument-bearing selectors and remove all `ParsedEntitySelector`/escape-hatch KDoc.

- [ ] **Step 4: Run core selector and NBT tests and verify GREEN**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew :core:test --tests '*EntitySelectorParserTest' --tests '*EntitySelectorTest' --tests '*SelectorDslTest' --tests '*EntityNbtDslTest'
```

Expected: PASS.

- [ ] **Step 5: Commit the consolidated model**

```bash
git add modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector \
  modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector \
  modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/nbt/EntityNbtDslTest.kt
git commit -m "refactor(core): unify entity selector model"
```

### Task 2: Make argument values valid by construction

**Files:**
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorStringCondition.kt`
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SnbtCompoundSource.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelector.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorArgument.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorArgumentConversion.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorArgumentRenderer.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorFilterArgumentParsing.kt`
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorParserTest.kt`

- [ ] **Step 1: Write failing constructor-invariant tests**

Add:

```kotlin
"rejects invalid public argument construction" {
    shouldThrow<IllegalArgumentException> {
        EntitySelectorArgument.Limit(0)
    }
    shouldThrow<IllegalArgumentException> {
        EntitySelectorArgument.Coordinate(SelectorCoordinate.X, Double.NaN)
    }
    shouldThrow<IllegalArgumentException> {
        SelectorStringCondition.Named("")
    }
    shouldThrow<EntitySelectorParseException> {
        SnbtCompoundSource.parse("definitely not SNBT")
    }
}

"rejects arguments incompatible with the selector head" {
    shouldThrow<IllegalArgumentException> {
        EntitySelector(
            EntitySelectorHead.ALL_PLAYERS,
            listOf(
                EntitySelectorArgument.Type(
                    key("minecraft", "zombie"),
                    isTag = false,
                    isNegated = false,
                ),
            ),
        )
    }
    shouldThrow<IllegalArgumentException> {
        EntitySelector(
            EntitySelectorHead.SELF,
            listOf(EntitySelectorArgument.Limit(1)),
        )
    }
}

"models tag and team presence explicitly" {
    val parsed = parseEntitySelector("@e[tag=,tag=!,team=red,team=!blue]")
    val tags = parsed.arguments.filterIsInstance<EntitySelectorArgument.Tag>()
    val teams = parsed.arguments.filterIsInstance<EntitySelectorArgument.Team>()

    tags.map(EntitySelectorArgument.Tag::condition) shouldBe
        listOf(
            SelectorStringCondition.Presence(SelectorPresence.NONE),
            SelectorStringCondition.Presence(SelectorPresence.ANY),
        )
    teams.map(EntitySelectorArgument.Team::condition) shouldBe
        listOf(
            SelectorStringCondition.Named("red"),
            SelectorStringCondition.Named("blue"),
        )
    teams.map(EntitySelectorArgument.Team::isNegated) shouldBe listOf(false, true)
}

"exposes validated SNBT source" {
    val nbt =
        parseEntitySelector("@e[nbt=!{Health:20.0f}]")
            .arguments
            .filterIsInstance<EntitySelectorArgument.Nbt>()
            .single()

    nbt.snbt.value shouldBe "{Health:20.0f}"
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew :core:test --tests '*EntitySelectorParserTest'
```

Expected: compilation fails because `SelectorStringCondition`, `SnbtCompoundSource`, and the semantic condition properties do not exist.

- [ ] **Step 3: Implement semantic value types**

Create `SelectorStringCondition.kt`:

```kotlin
public sealed interface SelectorStringCondition {
    public data class Named(
        public val value: String,
    ) : SelectorStringCondition {
        init {
            require(value.isNotEmpty()) { "Named selector conditions must not be empty." }
            require(value.all(Char::isAllowedInUnquotedSelectorToken)) {
                "Selector condition '$value' contains characters outside vanilla's unquoted-token syntax."
            }
        }
    }

    public data class Presence(
        public val value: SelectorPresence,
    ) : SelectorStringCondition
}
```

Create `SnbtCompoundSource.kt` with a private constructor, immutable `value`, structural equality, `toString`, and:

```kotlin
public companion object {
    public fun parse(source: String): SnbtCompoundSource {
        val reader = SelectorReader(source)
        reader.validateSnbtCompound()
        if (!reader.isAtEnd()) reader.fail("Unexpected trailing SNBT content")
        return SnbtCompoundSource(source)
    }

    internal fun validated(source: String): SnbtCompoundSource = SnbtCompoundSource(source)
}
```

Give every public declaration explicit KDoc.

- [ ] **Step 4: Enforce argument and selector invariants**

Add `init` checks:

```kotlin
public data class Coordinate(...) : EntitySelectorArgument {
    init {
        require(value.isFinite()) { "Selector coordinate must be finite, got: $value" }
    }
}

public data class Limit(...) : EntitySelectorArgument {
    init {
        require(value > 0) { "Selector limit must be positive, got: $value" }
    }
}
```

Remove `Name.quote`. Change tag/team arguments to:

```kotlin
public data class Tag(
    public val condition: SelectorStringCondition,
    override val isNegated: Boolean,
) : Negatable {
    init {
        require(condition is SelectorStringCondition.Named || !isNegated) {
            "Selector presence conditions cannot be prefix-negated."
        }
    }
}
```

Apply the same shape to `Team`. Change NBT to:

```kotlin
public data class Nbt(
    public val snbt: SnbtCompoundSource,
    override val isNegated: Boolean,
) : Negatable
```

In `EntitySelector.init`, reject `Type` when `head.acceptsTypeFilters` is false and reject `Limit`/`Sort` when
`head.acceptsResultControls` is false. Include the head token and argument name in each exception.

- [ ] **Step 5: Update builder conversion, parsing, and rendering**

Convert filter-group values with:

```kotlin
private fun stringConditionArgument(
    value: String,
    isNegated: Boolean,
    create: (SelectorStringCondition, Boolean) -> EntitySelectorArgument,
): EntitySelectorArgument =
    if (value.isEmpty()) {
        create(
            SelectorStringCondition.Presence(
                if (isNegated) SelectorPresence.ANY else SelectorPresence.NONE,
            ),
            false,
        )
    } else {
        create(SelectorStringCondition.Named(value), isNegated)
    }
```

Use it for `Tag` and `Team`. Wrap rendered builder SNBT with `SnbtCompoundSource.validated(...)`.

In parser functions, discard the quote delimiter after decoding `Name`; map empty tag/team tokens to `Presence` and
non-empty tokens to `Named`; wrap cursor-validated SNBT with `SnbtCompoundSource.validated(...)`.

Render names canonically with double quotes only when required. Render conditions with:

```kotlin
private fun SelectorStringCondition.render(): String =
    when (this) {
        is SelectorStringCondition.Named -> value
        is SelectorStringCondition.Presence -> value.value
    }
```

- [ ] **Step 6: Update canonical-round-trip expectations**

Change:

```kotlin
parseEntitySelector("@e[name='Boss Mob']").asString() shouldBe "@e[name=\"Boss Mob\"]"
```

Keep argument order, explicit empty lists, repeated filters, and validated SNBT coverage. Update direct `Tag`, `Team`,
`Name`, and `Nbt` constructor calls to their new signatures.

- [ ] **Step 7: Run focused tests and verify GREEN**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew :core:test --tests '*EntitySelectorParserTest' --tests '*EntitySelectorTest'
```

Expected: PASS.

- [ ] **Step 8: Commit valid-by-construction arguments**

```bash
git add modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector \
  modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector
git commit -m "refactor(core): validate selector model"
```

### Task 3: Make conversion use strict selector parsing

**Files:**
- Modify: `modules/minimessage/src/main/kotlin/io/github/lmliam/kotventure/minimessage/conversion/MiniMessageToDslStructuredComponents.kt`
- Modify: `modules/minimessage/src/main/kotlin/io/github/lmliam/kotventure/minimessage/conversion/MiniMessageToDslNbtComponents.kt`
- Modify: `modules/minimessage/src/test/kotlin/io/github/lmliam/kotventure/minimessage/MiniMessageToDslStructuredComponentTest.kt`
- Modify: `modules/minimessage/src/test/kotlin/io/github/lmliam/kotventure/minimessage/MiniMessageToDslTextRenderingTest.kt`

- [ ] **Step 1: Write failing strict-conversion tests**

Replace generated `entitySelector(...)` source with `parseEntitySelector(...)`. Use canonical namespaced type keys in
round-trip fixtures:

```kotlin
input = "<selector:'@e[type=minecraft:armor_stand,limit=1]'>"
```

Add:

```kotlin
test("rejects unsupported selector syntax during conversion") {
    val component = Component.selector("@future[unknown=value]")

    shouldThrow<EntitySelectorParseException> {
        MiniMessageToDslWriter.write(component)
    }
}

test("rejects unsupported entity NBT selector syntax during conversion") {
    val component = Component.entityNBT("Health", "@future[unknown=value]")

    shouldThrow<EntitySelectorParseException> {
        MiniMessageToDslWriter.write(component)
    }
}
```
- [ ] **Step 2: Run the MiniMessage test and verify RED**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew :minimessage:test --tests '*MiniMessageToDslStructuredComponentTest'
```

Expected: source assertions fail because conversion still emits the removed unchecked factory, and unsupported source
does not throw.

- [ ] **Step 3: Validate and emit parser calls**

Import `parseEntitySelector` and validate before emitting:

```kotlin
val pattern = component.pattern()
parseEntitySelector(pattern)
val source = escapeKotlinString(pattern)
```

Emit `selector(parseEntitySelector("$source"))`. Apply the same flow to `EntityNBTComponent.selector()` and emit
`entityNbt(parseEntitySelector("$source"), ...)`.

- [ ] **Step 4: Run MiniMessage tests and verify GREEN**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew :minimessage:test --tests '*MiniMessageToDslStructuredComponentTest' --tests '*MiniMessageToDslTextRenderingTest'
```

Expected: PASS.

- [ ] **Step 5: Commit strict conversion**

```bash
git add modules/minimessage/src/main/kotlin/io/github/lmliam/kotventure/minimessage/conversion \
  modules/minimessage/src/test/kotlin/io/github/lmliam/kotventure/minimessage
git commit -m "refactor(minimessage): validate selector source"
```

### Task 4: Update public documentation and samples

**Files:**
- Modify: `docs/DESIGN.md`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/CommonEntitySelectorScope.kt`
- Modify: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/selector/SelectorSamples.kt`
- Modify any remaining Kotlin files reported by the required source scan.

- [ ] **Step 1: Update canonical examples and contracts**

Change the parser example to:

```kotlin
val parsedSelector = parseEntitySelector("@e[type=minecraft:zombie,tag=!hidden]")
selector(parsedSelector)
```

Document one structured `EntitySelector`, canonical semantic rendering, and strict failure for unknown syntax. Remove
all raw escape-hatch guidance. In selector-scope KDoc, direct dynamic full-selector interop to
`parseEntitySelector(...)`.

- [ ] **Step 2: Scan for stale API and contract references**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk rg -n \
  'ParsedEntitySelector|asEntitySelector|entitySelector\\(|raw selector|escape.hatch|lossless' \
  modules docs --glob '*.kt' --glob '*.md'
```

Expected: no stale selector API or unchecked-source references. Any unrelated use of “lossless” must describe its own
feature accurately.

- [ ] **Step 3: Format and run focused checks**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew ktlintFormat
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew \
  :core:test :minimessage:test ktlintCheck spotlessCheck
```

Expected: PASS.

- [ ] **Step 4: Commit documentation**

```bash
git add docs/DESIGN.md \
  modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/CommonEntitySelectorScope.kt \
  modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/selector/SelectorSamples.kt
git commit -m "docs(core): document strict selectors"
```

### Task 5: Full verification

**Files:**
- Verify all changed files.

- [ ] **Step 1: Run the complete build**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk ./gradlew build
```

Expected: BUILD SUCCESSFUL, including tests, explicit API, lint, Spotless, and Kover.

- [ ] **Step 2: Verify the final diff**

Run:

```bash
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk git diff origin/master...HEAD --check
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk git status --short --branch
/Users/liam/Library/Application\ Support/Headroom/headroom/bin/rtk git log -6 --oneline
```

Expected: no whitespace errors, a clean worktree, and conventional commit subjects.
