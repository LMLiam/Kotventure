# Rename XScopeBuilder → XBuilder Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename all `internal` `XScopeBuilder` / `TextComponentBuilder` classes in `core` to `XBuilder` for consistency with `JoinBuilder` (introduced in #134).

**Architecture:** Pure internal rename — no public API surface changes. File names change in lockstep with class names. All callers (other builders, DSL entry-point functions) update their imports and usages.

**Tech Stack:** Kotlin, Kotest, Gradle with `./gradlew build` as verification.

---

## File Map

| Old file | New file | Old class | New class |
|---|---|---|---|
| `core/style/StyleScopeBuilder.kt` | `core/style/StyleBuilder.kt` | `StyleScopeBuilder` | `StyleBuilder` |
| `core/component/ComponentScopeBuilder.kt` | `core/component/ComponentBuilder.kt` | `ComponentScopeBuilder` | `ComponentBuilder` |
| `core/text/TextComponentBuilder.kt` | `core/text/TextBuilder.kt` | `TextComponentBuilder` | `TextBuilder` |
| `core/event/ClickActionScopeBuilder.kt` | `core/event/ClickBuilder.kt` | `ClickActionScopeBuilder` | `ClickBuilder` |
| `core/event/HoverContentScopeBuilder.kt` | `core/event/HoverBuilder.kt` | `HoverContentScopeBuilder` | `HoverBuilder` |

**Files updated (callers/importers — no class rename):**
- `core/style/Style.kt` — uses `StyleScopeBuilder`
- `core/component/ComponentBuilder.kt` (was `ComponentScopeBuilder.kt`) — imports `StyleScopeBuilder`, `TextComponentBuilder`
- `core/text/TextBuilder.kt` (was `TextComponentBuilder.kt`) — imports `ComponentScopeBuilder`
- `core/event/HoverBuilder.kt` (was `HoverContentScopeBuilder.kt`) — imports `TextComponentBuilder`
- `core/text/Component.kt` — uses `TextComponentBuilder`
- `core/text/Text.kt` — uses `TextComponentBuilder`
- `core/event/ClickEvent.kt` — uses `ClickActionScopeBuilder`
- `core/event/HoverEvent.kt` — uses `HoverContentScopeBuilder`
- `core/selector/SelectorComponentBuilder.kt` — imports `ComponentScopeBuilder`, `TextComponentBuilder`
- `core/nbt/NbtComponentBuilder.kt` — imports `ComponentScopeBuilder`, `TextComponentBuilder`
- `core/translatable/TranslatableComponentBuilder.kt` — imports `ComponentScopeBuilder`
- `core/score/ScoreComponentBuilder.kt` — imports `ComponentScopeBuilder`
- `core/keybind/KeybindComponentBuilder.kt` — imports `ComponentScopeBuilder`
- `core/objectcomponent/ObjectComponentBuilder.kt` — imports `ComponentScopeBuilder`, `TextComponentBuilder`

---

### Task 1: Rename StyleScopeBuilder → StyleBuilder

**Files:**
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/style/StyleBuilder.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/style/StyleScopeBuilder.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/style/Style.kt`

- [ ] **Step 1: Create StyleBuilder.kt** (same content as StyleScopeBuilder.kt, class renamed)

- [ ] **Step 2: Update Style.kt** — replace `StyleScopeBuilder` → `StyleBuilder`

- [ ] **Step 3: Delete StyleScopeBuilder.kt**

- [ ] **Step 4: Compile check** — `./gradlew :modules:core:compileKotlin`

---

### Task 2: Rename ComponentScopeBuilder → ComponentBuilder

**Files:**
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/component/ComponentBuilder.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/component/ComponentScopeBuilder.kt`
- Modify: all subclass files

- [ ] **Step 1: Create ComponentBuilder.kt** (same content, class renamed, imports updated)

- [ ] **Step 2: Update all subclass imports** (SelectorComponentBuilder, NbtComponentBuilder, TranslatableComponentBuilder, ScoreComponentBuilder, KeybindComponentBuilder, ObjectComponentBuilder)

- [ ] **Step 3: Delete ComponentScopeBuilder.kt**

- [ ] **Step 4: Compile check**

---

### Task 3: Rename TextComponentBuilder → TextBuilder

**Files:**
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/text/TextBuilder.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/text/TextComponentBuilder.kt`
- Modify: `Component.kt`, `Text.kt`, `ComponentBuilder.kt`, `HoverBuilder.kt`, `SelectorComponentBuilder.kt`, `NbtComponentBuilder.kt`, `ObjectComponentBuilder.kt`

- [ ] **Step 1: Create TextBuilder.kt** (same content, class renamed)

- [ ] **Step 2: Update all call sites**

- [ ] **Step 3: Delete TextComponentBuilder.kt**

---

### Task 4: Rename ClickActionScopeBuilder → ClickBuilder

**Files:**
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/event/ClickBuilder.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/event/ClickActionScopeBuilder.kt`
- Modify: `ClickEvent.kt`

- [ ] **Step 1: Create ClickBuilder.kt** (same content, class renamed)

- [ ] **Step 2: Update ClickEvent.kt** — `ClickActionScopeBuilder` → `ClickBuilder`

- [ ] **Step 3: Delete ClickActionScopeBuilder.kt**

---

### Task 5: Rename HoverContentScopeBuilder → HoverBuilder

**Files:**
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/event/HoverBuilder.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/event/HoverContentScopeBuilder.kt`
- Modify: `HoverEvent.kt`

- [ ] **Step 1: Create HoverBuilder.kt** (same content, class renamed, TextComponentBuilder → TextBuilder)

- [ ] **Step 2: Update HoverEvent.kt** — `HoverContentScopeBuilder` → `HoverBuilder`

- [ ] **Step 3: Delete HoverContentScopeBuilder.kt**

---

### Task 6: Full build + format

- [ ] **Step 1: Format** — `./gradlew ktlintFormat`

- [ ] **Step 2: Build** — `./gradlew build`

- [ ] **Step 3: Commit** — `refactor(core): rename XScopeBuilder classes to XBuilder`
