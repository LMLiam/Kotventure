# Dokka @sample KDoc Examples Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all fenced ` ```kotlin ` KDoc examples with compiled `@sample` references so examples are type-checked and can't drift.

**Architecture:** Add Dokka 2.x to the build, create a `samples` source set per module that compiles against `main` but is excluded from publication. Each sample is a standalone function in a mirrored package, referenced from KDoc via `@sample fqn`.

**Tech Stack:** Dokka 2.0.0, Gradle Groovy DSL (existing), Kotlin 2.4.0, JDK 25.

## Global Constraints

- Build uses Gradle Groovy DSL (not Kotlin DSL). All build modifications use Groovy.
- `explicitApi()` is on for library modules — sample source sets must opt out.
- Samples must compile against the module's own `main` output + its dependencies. They are not published.
- Dokka plugin version: `2.0.0` (latest stable compatible with Kotlin 2.4.0).
- Existing tests/coverage must keep passing — samples are not instrumented by Kover.

---

### Task 1: Add Dokka plugin and configure sample source sets

**Files:**
- Modify: `gradle/libs.versions.toml` (add Dokka version + plugin)
- Modify: `build.gradle` (apply Dokka, configure `samples` source set for library modules)

**Interfaces:**
- Produces: `samples` source set available in each library module, compiling against `main` output and all `api`/`implementation` dependencies.

- [ ] **Step 1: Add Dokka to version catalog**

In `gradle/libs.versions.toml`, add:

```toml
# Under [versions]
dokka = "2.0.0"

# Under [plugins]
dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }
```

- [ ] **Step 2: Apply Dokka plugin in root build.gradle**

In `build.gradle`, add to the `plugins` block:

```groovy
alias(libs.plugins.dokka) apply false
```

In the `subprojects` block, inside the `if (libraryModule)` guard, add:

```groovy
apply plugin: 'org.jetbrains.dokka'
```

- [ ] **Step 3: Create shared samples source-set configuration**

Create `gradle/samples.gradle`:

```groovy
sourceSets {
    samples {
        kotlin {
            srcDir 'src/samples/kotlin'
        }
    }
}

// Samples compile against main but are not published or tested by Kover.
dependencies {
    samplesImplementation sourceSets.main.output
    samplesImplementation configurations.api
    samplesImplementation configurations.implementation
}

// Opt out of explicitApi for samples — they are internal documentation, not public API.
tasks.named('compileSamplesKotlin') {
    compilerOptions {
        freeCompilerArgs.add('-Xexplicit-api=disable')
    }
}

// Wire samples source root to Dokka so @sample resolves.
dokka {
    dokkaSourceSets.configureEach {
        samples.from(file('src/samples/kotlin'))
    }
}

// Exclude samples from Kover instrumentation.
kover {
    currentProject {
        sources {
            excludedSourceSets.add('samples')
        }
    }
}
```

- [ ] **Step 4: Apply samples.gradle in root build**

In `build.gradle`, inside the `if (libraryModule)` guard (after the spotless apply), add:

```groovy
apply from: rootProject.file('gradle/samples.gradle')
```

- [ ] **Step 5: Verify the build still passes**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (no samples exist yet, so no compilation errors)

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml build.gradle gradle/samples.gradle
git commit -m "docs(build): add Dokka plugin and samples source set infrastructure"
```

---

### Task 2: Write core module sample functions — color, style, text, component

**Files:**
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/color/ColorSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/color/GradientSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/style/StyleSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/text/TextSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/text/JoinSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/text/ComponentSequenceSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/component/ComponentScopeSamples.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/color/Color.kt` (replace fenced block with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/color/Gradient.kt` (replace fenced blocks with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/style/Style.kt` (replace fenced block with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/style/Styled.kt` (replace fenced block with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/style/StyleScope.kt` (replace fenced block with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/text/Text.kt` (replace fenced block with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/text/Join.kt` (replace fenced blocks with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/text/ComponentSequence.kt` (replace fenced block with @sample)
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/component/ComponentScope.kt` (replace fenced block with @sample)

**Interfaces:**
- Consumes: samples source set from Task 1
- Produces: compiled sample functions referenced by @sample in KDoc

- [ ] **Step 1: Create ColorSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.color

internal fun hexColorSample() {
    val gold = hex("#FFAA00")
}
```

- [ ] **Step 2: Create GradientSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.color

internal fun gradientSample() {
    val fire = gradient(hex("#FF0000"), hex("#FFAA00"), hex("#FFFF00"))
}

internal fun gradientTextSample() {
    val title = gradientText("Kotventure", hex("#FF0000"), hex("#0000FF"))
}
```

- [ ] **Step 3: Create StyleSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun styleSample() {
    val heading = style {
        color(gold)
        bold()
    }
    val title = text("Welcome") { style(heading) }
}

internal fun styledSample() {
    val heading = style {
        color(gold)
        bold()
    }
    val highlighted = io.github.lmliam.kotventure.core.component.component { text("important") } styled heading
}

internal fun styleScopeSample() {
    style {
        color(gold)
        bold()
        italic(false)
    }
}
```

- [ ] **Step 4: Create TextSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.gold

internal fun textSample() {
    val greeting = text("Hello") {
        color(gold)
        bold()
    }
}
```

- [ ] **Step 5: Create JoinSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.text

internal fun joinArraySample() {
    val list = arrayOf(text("a"), text("b"), text("c")).join { separator(text(", ")) }
}

internal fun joinIterableSample() {
    val list = listOf(text("a"), text("b")).join { separator(text(", ")) }
}
```

- [ ] **Step 6: Create ComponentSequenceSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.TextComponent

internal fun componentSequenceSample() {
    val root = text("Hello")
    val mentionsAlex = root.asSequence().any { it is TextComponent && "Alex" in it.content() }
}
```

- [ ] **Step 7: Create ComponentScopeSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.text.text

internal fun componentScopeSample() {
    component {
        text("Hello ") { color(aqua) }
        keybind("key.jump")
    }
}
```

- [ ] **Step 8: Replace fenced blocks in Color.kt**

Replace the ` ```kotlin ` block with:
```
 * @sample io.github.lmliam.kotventure.core.color.hexColorSample
```

- [ ] **Step 9: Replace fenced blocks in Gradient.kt**

Replace the two ` ```kotlin ` blocks with:
```
 * @sample io.github.lmliam.kotventure.core.color.gradientSample
```
and:
```
 * @sample io.github.lmliam.kotventure.core.color.gradientTextSample
```

- [ ] **Step 10: Replace fenced blocks in Style.kt**

Replace the ` ```kotlin ` block with:
```
 * @sample io.github.lmliam.kotventure.core.style.styleSample
```

- [ ] **Step 11: Replace fenced block in Styled.kt**

Replace with:
```
 * @sample io.github.lmliam.kotventure.core.style.styledSample
```

- [ ] **Step 12: Replace fenced block in StyleScope.kt**

Replace with:
```
 * @sample io.github.lmliam.kotventure.core.style.styleScopeSample
```

- [ ] **Step 13: Replace fenced block in Text.kt**

Replace with:
```
 * @sample io.github.lmliam.kotventure.core.text.textSample
```

- [ ] **Step 14: Replace fenced blocks in Join.kt**

Replace with:
```
 * @sample io.github.lmliam.kotventure.core.text.joinArraySample
```
and:
```
 * @sample io.github.lmliam.kotventure.core.text.joinIterableSample
```

- [ ] **Step 15: Replace fenced block in ComponentSequence.kt**

Replace with:
```
 * @sample io.github.lmliam.kotventure.core.text.componentSequenceSample
```

- [ ] **Step 16: Replace fenced block in ComponentScope.kt**

Replace with:
```
 * @sample io.github.lmliam.kotventure.core.component.componentScopeSample
```

- [ ] **Step 17: Verify compilation**

Run: `./gradlew :core:compileSamplesKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 18: Verify full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 19: Commit**

```bash
git add modules/core/src/samples/ modules/core/src/main/
git commit -m "docs(core): convert color, style, text, and component KDoc examples to @sample"
```

---

### Task 3: Write core module sample functions — events, keybind, score, translatable, theme

**Files:**
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/event/EventSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/keybind/KeybindSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/score/ScoreSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/translatable/TranslatableSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/theme/ThemeSamples.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/event/ClickEvent.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/event/ClickActionScope.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/event/HoverEvent.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/keybind/Keybind.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/score/Score.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/translatable/Translatable.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/theme/Theme.kt`

**Interfaces:**
- Consumes: samples source set from Task 1
- Produces: compiled sample functions referenced by @sample in KDoc

- [ ] **Step 1: Create EventSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.event

internal fun clickSample() {
    val link = click { openUrl("https://example.com") }
}

internal fun clickActionScopeSample() {
    click { openUrl("https://example.com") }
    click { run("/spawn") }
}

internal fun hoverSample() {
    val tooltip = hover { text("Click to teleport") }
}
```

- [ ] **Step 2: Create KeybindSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.keybind

import io.github.lmliam.kotventure.core.color.aqua

internal fun keybindSample() {
    val jump = keybind("key.jump") { color(aqua) }
}
```

- [ ] **Step 3: Create ScoreSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.score

internal fun scoreSample() {
    val kills = score(name = "@s", objective = "kills")
}
```

- [ ] **Step 4: Create TranslatableSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.translatable

internal fun translatableSample() {
    val died = translatable("death.attack.player") {
        arg { content("Alex") }
        fallback("Alex was slain")
    }
}
```

- [ ] **Step 5: Create ThemeSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.color.hex
import net.kyori.adventure.text.format.Style

internal fun themeSample() {
    object : Theme("brand") {
        val primary = hex("#5865F2")

        val header: Style by style {
            color(primary)
            bold()
        }
    }
}
```

- [ ] **Step 6: Replace fenced blocks in ClickEvent.kt, ClickActionScope.kt, HoverEvent.kt**

ClickEvent.kt:
```
 * @sample io.github.lmliam.kotventure.core.event.clickSample
```

ClickActionScope.kt:
```
 * @sample io.github.lmliam.kotventure.core.event.clickActionScopeSample
```

HoverEvent.kt:
```
 * @sample io.github.lmliam.kotventure.core.event.hoverSample
```

- [ ] **Step 7: Replace fenced blocks in Keybind.kt, Score.kt, Translatable.kt, Theme.kt**

Keybind.kt:
```
 * @sample io.github.lmliam.kotventure.core.keybind.keybindSample
```

Score.kt:
```
 * @sample io.github.lmliam.kotventure.core.score.scoreSample
```

Translatable.kt:
```
 * @sample io.github.lmliam.kotventure.core.translatable.translatableSample
```

Theme.kt:
```
 * @sample io.github.lmliam.kotventure.core.theme.themeSample
```

- [ ] **Step 8: Verify compilation**

Run: `./gradlew :core:compileSamplesKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add modules/core/src/samples/ modules/core/src/main/
git commit -m "docs(core): convert event, keybind, score, translatable, and theme KDoc examples to @sample"
```

---

### Task 4: Write core module sample functions — NBT, selector, object component

**Files:**
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/nbt/NbtSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/selector/SelectorSamples.kt`
- Create: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/objectcomponent/DisplaySamples.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/nbt/NbtPath.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/nbt/NbtPathFactory.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/nbt/NbtSelection.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/nbt/NbtPredicateScope.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/nbt/BlockNbt.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/nbt/EntityNbt.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/nbt/StorageNbt.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorFactory.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorScope.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/Selector.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/objectcomponent/Display.kt`

**Interfaces:**
- Consumes: samples source set from Task 1
- Produces: compiled sample functions referenced by @sample in KDoc

- [ ] **Step 1: Create NbtSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.selector.self

internal fun nbtPathSample() {
    nbtPath("Items")[0]["tag"]["display"]["Name"]
    nbtPath("Inventory")[all]["id"]
    nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
}

internal fun nbtPathVerbatimSample() {
    nbtPath("Items[{id:\"minecraft:diamond\"}].Count")
}

internal fun nbtPathKeySample() {
    nbtPath("tag")["display"]["Name"]
}

internal fun nbtPathIndexSample() {
    nbtPath("Items")[0]["id"]
}

internal fun nbtPathSelectionSample() {
    nbtPath("Inventory")[all]["id"]
    nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
}

internal fun nbtPathFactorySample() {
    // Structured
    nbtPath("Items")[0]["id"]

    // Pre-formed string, still chainable
    nbtPath("Items[0]")["tag"]
}

internal fun allSample() {
    nbtPath("Passengers")[all]["CustomName"]
}

internal fun matchingSample() {
    nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
}

internal fun nbtPredicateScopeSample() {
    matching {
        "id" eq "minecraft:diamond"
        "Count" eq 1.toByte()
        "tag" eq { "Unbreakable" eq 1.toByte() }
    }
}

internal fun blockNbtSample() {
    val sign = blockNbt(blockPos(0, 64, 0), nbtPath("front_text")["messages"][0])
}

internal fun entityNbtSample() {
    val health = entityNbt(self(), nbtPath("Health"))
}

internal fun storageNbtSample() {
    val score = storageNbt(key("myplugin", "scores"), nbtPath("top.player"))
}
```

- [ ] **Step 2: Create SelectorSamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.text.text

internal fun selectorSample() {
    val nearby = selector(entities { distance(atMost(10.0)) }) { separator { content(", ") } }
}

internal fun nearestPlayerSample() {
    nearestPlayer { distance(atMost(10.0)) }
}

internal fun allPlayersSample() {
    allPlayers { tag("admin") }
}

internal fun entitiesSample() {
    entities {
        type("armor_stand")
        distance(atMost(10.0))
        sort(nearest)
        limit(1)
        tag("display")
    }
}

internal fun entitySelectorScopeSample() {
    entities {
        type("armor_stand")
        distance(atMost(10.0))
        sort(nearest)
        limit(1)
        tag("display")
    }
}
```

- [ ] **Step 3: Create DisplaySamples.kt**

```kotlin
package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import java.util.UUID

internal fun displaySample() {
    val uuid = UUID.randomUUID()
    val head = display(head(uuid)) { fallback(component { text("?") }) }
}
```

- [ ] **Step 4: Replace fenced blocks in NBT files**

NbtPath.kt class KDoc:
```
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathSample
```

NbtPath.kt verbatim escape:
```
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathVerbatimSample
```

NbtPath.kt `get(key)`:
```
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathKeySample
```

NbtPath.kt `get(index)`:
```
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathIndexSample
```

NbtPath.kt `get(selection)`:
```
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathSelectionSample
```

NbtPathFactory.kt:
```
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathFactorySample
```

NbtSelection.kt `all`:
```
 * @sample io.github.lmliam.kotventure.core.nbt.allSample
```

NbtSelection.kt `matching`:
```
 * @sample io.github.lmliam.kotventure.core.nbt.matchingSample
```

NbtPredicateScope.kt:
```
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPredicateScopeSample
```

BlockNbt.kt:
```
 * @sample io.github.lmliam.kotventure.core.nbt.blockNbtSample
```

EntityNbt.kt:
```
 * @sample io.github.lmliam.kotventure.core.nbt.entityNbtSample
```

StorageNbt.kt:
```
 * @sample io.github.lmliam.kotventure.core.nbt.storageNbtSample
```

- [ ] **Step 5: Replace fenced blocks in selector files**

Selector.kt:
```
 * @sample io.github.lmliam.kotventure.core.selector.selectorSample
```

EntitySelectorFactory.kt `nearestPlayer`:
```
 * @sample io.github.lmliam.kotventure.core.selector.nearestPlayerSample
```

EntitySelectorFactory.kt `allPlayers`:
```
 * @sample io.github.lmliam.kotventure.core.selector.allPlayersSample
```

EntitySelectorFactory.kt `entities`:
```
 * @sample io.github.lmliam.kotventure.core.selector.entitiesSample
```

EntitySelectorScope.kt:
```
 * @sample io.github.lmliam.kotventure.core.selector.entitySelectorScopeSample
```

- [ ] **Step 6: Replace fenced block in Display.kt**

```
 * @sample io.github.lmliam.kotventure.core.objectcomponent.displaySample
```

- [ ] **Step 7: Verify compilation**

Run: `./gradlew :core:compileSamplesKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Verify full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 9: Commit**

```bash
git add modules/core/src/samples/ modules/core/src/main/
git commit -m "docs(core): convert nbt, selector, and object-component KDoc examples to @sample"
```

---

### Task 5: Write minimessage, serializer, and test-snapshot module samples

**Files:**
- Create: `modules/minimessage/src/samples/kotlin/io/github/lmliam/kotventure/minimessage/MiniMessageSamples.kt`
- Create: `modules/minimessage/src/samples/kotlin/io/github/lmliam/kotventure/minimessage/template/MiniTemplateSamples.kt`
- Create: `modules/serializer/src/samples/kotlin/io/github/lmliam/kotventure/serializer/SerializerSamples.kt`
- Create: `modules/test-snapshot/src/samples/kotlin/io/github/lmliam/kotventure/test/snapshot/SnapshotSamples.kt`
- Modify: `modules/minimessage/src/main/kotlin/io/github/lmliam/kotventure/minimessage/MiniMessageDsl.kt`
- Modify: `modules/minimessage/src/main/kotlin/io/github/lmliam/kotventure/minimessage/template/MiniTemplate.kt`
- Modify: `modules/serializer/src/main/kotlin/io/github/lmliam/kotventure/serializer/MiniMessageSerializer.kt`
- Modify: `modules/test-snapshot/src/main/kotlin/io/github/lmliam/kotventure/test/snapshot/SnapshotAssertions.kt`

**Interfaces:**
- Consumes: samples source set from Task 1, module-specific dependencies
- Produces: compiled sample functions for non-core modules

- [ ] **Step 1: Create MiniMessageSamples.kt**

```kotlin
package io.github.lmliam.kotventure.minimessage

internal fun miniSample() {
    val greeting = mini("<gold>Welcome <bold>back</bold>!")
}

internal fun miniWithPlaceholdersSample() {
    val playerName = "Alex"
    val line = mini("<greeting> <player>!") {
        parsed("greeting", "<gold>Welcome")
        unparsed("player", playerName)
    }
}
```

- [ ] **Step 2: Create MiniTemplateSamples.kt**

```kotlin
package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component

internal fun miniTemplateSample() {
    object : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
        val player = placeholder<Component>("player")
        val count = placeholder<Int>("count")
    }
}

internal fun miniTemplateRenderSample() {
    val template = object : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
        val player = placeholder<Component>("player")
        val count = placeholder<Int>("count")
    }

    val forAlex = template {
        player bind component { text("Alex") }
        count bind 3
    }
}
```

- [ ] **Step 3: Create SerializerSamples.kt**

```kotlin
package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun toMiniMessageSample() {
    val component = text("Welcome") { color(gold) }
    val markup = component.toMiniMessage() // e.g. "<gold>Welcome"
}
```

- [ ] **Step 4: Create SnapshotSamples.kt**

```kotlin
package io.github.lmliam.kotventure.test.snapshot

import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component

internal fun shouldMatchSnapshotSample() {
    val component: Component = text("Welcome")
    component shouldMatchSnapshot "welcome-message"
}
```

- [ ] **Step 5: Replace fenced blocks in MiniMessageDsl.kt**

`mini(input)`:
```
 * @sample io.github.lmliam.kotventure.minimessage.miniSample
```

`mini(input, init)`:
```
 * @sample io.github.lmliam.kotventure.minimessage.miniWithPlaceholdersSample
```

- [ ] **Step 6: Replace fenced block in MiniTemplate.kt**

Class-level KDoc:
```
 * @sample io.github.lmliam.kotventure.minimessage.template.miniTemplateRenderSample
```

- [ ] **Step 7: Replace fenced block in MiniMessageSerializer.kt**

```
 * @sample io.github.lmliam.kotventure.serializer.toMiniMessageSample
```

- [ ] **Step 8: Replace fenced block in SnapshotAssertions.kt**

```
 * @sample io.github.lmliam.kotventure.test.snapshot.shouldMatchSnapshotSample
```

- [ ] **Step 9: Verify compilation of all modules**

Run: `./gradlew compileSamplesKotlin`
Expected: BUILD SUCCESSFUL for all modules

- [ ] **Step 10: Verify full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL, all tests pass, coverage thresholds met

- [ ] **Step 11: Commit**

```bash
git add modules/minimessage/src/ modules/serializer/src/ modules/test-snapshot/src/
git commit -m "docs(minimessage,serializer,test-snapshot): convert KDoc examples to @sample"
```

---

### Task 6: Final verification and cleanup

**Files:**
- (No new files — verification only)

- [ ] **Step 1: Verify no fenced Kotlin blocks remain in main source KDoc**

Run: `grep -r '```kotlin' modules/*/src/main/ --include="*.kt"`
Expected: No output (all fenced examples have been converted)

- [ ] **Step 2: Run full build one final time**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run Dokka generation to confirm @sample resolution**

Run: `./gradlew dokkaGenerate`
Expected: BUILD SUCCESSFUL (no unresolved @sample warnings)

- [ ] **Step 4: Verify the generated HTML contains rendered samples**

Spot-check: look for sample content in a generated doc page to confirm it renders correctly.
