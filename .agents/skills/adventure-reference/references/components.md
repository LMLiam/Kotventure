# Components — `net.kyori.adventure.text`

All component types are immutable `Component` subtypes; factories live on `Component`.
Builders (`Component.text()` no-arg → `TextComponent.Builder`) exist, but Kotventure's DSL
usually calls the direct factories and `style`/`append` on the immutable value.

## Text

- `Component.text("…")` → `TextComponent`; overloads take `(content, TextColor)`,
  `(content, Style)`, `(content, TextColor, TextDecoration...)`.
- `Component.empty()`, `Component.newline()`, `Component.space()`.
- On any component: `.color(TextColor)`, `.decoration(TextDecoration, boolean|State)`,
  `.style(Style)`, `.append(ComponentLike)`, `.children(List<Component>)`,
  `.clickEvent(...)`, `.hoverEvent(...)`, `.insertion(String)`, `.font(Key)`.

## Translatable

- `Component.translatable(key)`, `(key, fallback)`, `(key, args...)` → `TranslatableComponent`.
- Arguments are `TranslationArgument` (`TranslationArgument.component(...)`,
  `.numeric(...)`, `.bool(...)`); accessor `.arguments()`, key `.key()`, fallback `.fallback()`.

## Keybind / Score / Selector

- `Component.keybind("key.jump")` → `KeybindComponent` (`.keybind()` accessor).
- `Component.score(name, objective)` → `ScoreComponent` (`.name()`, `.objective()`).
- `Component.selector(pattern)` → `SelectorComponent` (`.pattern()`, optional
  `.separator(ComponentLike)`).

## NBT components

- `Component.blockNBT()`, `Component.entityNBT()`, `Component.storageNBT()` →
  `BlockNBTComponent` / `EntityNBTComponent` / `StorageNBTComponent`, all
  `NBTComponent<C, B>` with `.nbtPath()`, `.interpret()`, `.separator()`.
- `BlockNBTComponent.Pos` — `LocalPos` / `WorldPos` coordinate forms; parse with
  `BlockNBTComponent.Pos.fromString("…")`.
- `EntityNBTComponent.selector()`; `StorageNBTComponent.storage()` is a `Key`.

## Binary NBT payloads

- Component-attached data uses `BinaryTagHolder` (`net.kyori.adventure.nbt.api`, part of
  `adventure-api`): `BinaryTagHolder.binaryTagHolder(snbtString)`.
- The full tag model lives in the separate `adventure-nbt` artifact (a `minimessage`
  dependency, **not** available in `core`): `CompoundBinaryTag.builder()`, typed tags
  (`IntBinaryTag`, `StringBinaryTag`, `ListBinaryTag`, array tags…), SNBT round-trip via
  `TagStringIO.get().asString(tag)` / `.asCompound(snbt)`.

## Object components (sprites, player heads)

- `` Component.`object`() `` (backticks — `object` is a Kotlin keyword) → `ObjectComponent.Builder`
  with `.contents(ObjectContents)` — contents from
  `ObjectContents.sprite(...)` → `SpriteObjectContents` and
  `ObjectContents.playerHead()...` → `PlayerHeadObjectContents` (name / uuid / profile
  properties / texture skin sources).
- `ObjectComponent` has an optional fallback component for non-supporting viewers.

## Joining & iteration

- `Component.join(JoinConfiguration, ComponentLike...)`;
  `JoinConfiguration.separator(c)`, `.noSeparators()`, `.builder()` (prefix/suffix/last
  separator).
- `component.iterable(ComponentIteratorType.DEPTH_FIRST, flags)` /
  `.iterator(...)` with `ComponentIteratorFlag` — Kotventure exposes this as
  `Component.asSequence()` in `core/text`.
- `ComponentLike` is the universal "can become a component" input type — accept it in DSL
  signatures instead of `Component` where possible; call `.asComponent()`.
