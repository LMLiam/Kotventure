# Components: `net.kyori.adventure.text`

All component types are immutable `Component` subtypes. Their factories are on `Component`. Adventure also supplies
builders such as `Component.text()` with no argument. Kotventure usually calls direct factories and the `style` and
`append` functions on an immutable value.

## Text

- `Component.text("…")` → `TextComponent`. Overloads take `(content, TextColor)`,
  `(content, Style)`, `(content, TextColor, TextDecoration...)`.
- `Component.empty()`, `Component.newline()`, `Component.space()`.
- On any component: `.color(TextColor)`, `.decoration(TextDecoration, boolean|State)`,
  `.style(Style)`, `.append(ComponentLike)`, `.children(List<Component>)`,
  `.clickEvent(...)`, `.hoverEvent(...)`, `.insertion(String)`, `.font(Key)`.

## Translatable

- `Component.translatable(key)`, `(key, fallback)`, `(key, args...)` → `TranslatableComponent`.
- Arguments are `TranslationArgument` values: `TranslationArgument.component(...)`, `.numeric(...)`, and `.bool(...)`.
  Accessors are `.arguments()`, `.key()`, and `.fallback()`.

## Keybind / Score / Selector

- `Component.keybind("key.jump")` → `KeybindComponent` (`.keybind()` accessor).
- `Component.score(name, objective)` → `ScoreComponent` (`.name()`, `.objective()`).
- `Component.selector(pattern)` → `SelectorComponent` (`.pattern()`, optional
  `.separator(ComponentLike)`).

## NBT components

- `Component.blockNBT()`, `Component.entityNBT()`, `Component.storageNBT()` →
  `BlockNBTComponent` / `EntityNBTComponent` / `StorageNBTComponent`, all
  `NBTComponent<C, B>` with `.nbtPath()`, `.interpret()`, `.separator()`.
- `BlockNBTComponent.Pos` has `LocalPos` and `WorldPos` coordinate forms. Parse a string with
  `BlockNBTComponent.Pos.fromString("…")`.
- `EntityNBTComponent.selector()`. `StorageNBTComponent.storage()` is a `Key`.

## Binary NBT payloads

- Component data uses `BinaryTagHolder` from `net.kyori.adventure.nbt.api` in `adventure-api`. Construct it with
  `BinaryTagHolder.binaryTagHolder(snbtString)`.
- The full tag model is in the separate `adventure-nbt` artefact. The `minimessage` module depends
  on this artefact, but `core` does not. Use `CompoundBinaryTag.builder()` and typed tags such as
  `IntBinaryTag`, `StringBinaryTag`, `ListBinaryTag`, and the array tags. Use
  `TagStringIO.get().asString(tag)` and `.asCompound(snbt)` for an SNBT round trip.

## Object components (sprites, player heads)

- `` Component.`object`() `` uses backticks because `object` is a Kotlin keyword. It returns `ObjectComponent.Builder`
  with `.contents(ObjectContents)`. Get contents from
  `ObjectContents.sprite(...)` → `SpriteObjectContents` and
  `ObjectContents.playerHead()...` → `PlayerHeadObjectContents` (name / uuid / profile
  properties / texture skin sources).
- `ObjectComponent` has an optional fallback component for non-supporting viewers.

## Joining & iteration

- `Component.join(JoinConfiguration, ComponentLike...)`.
  `JoinConfiguration.separator(c)`, `.noSeparators()`, `.builder()` (prefix/suffix/last
  separator).
- `component.iterable(ComponentIteratorType.DEPTH_FIRST, flags)` /
  `.iterator(...)` with `ComponentIteratorFlag`. Kotventure exposes this as
  `Component.asSequence()` in `core/text`.
- `ComponentLike` is the general input type for a value that can become a component. Use it instead of `Component` in
  DSL signatures when possible. Call `.asComponent()` to get the component.
