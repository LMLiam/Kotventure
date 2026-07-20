# `core` entry points by feature package

The package prefix is `io.github.lmliam.kotventure.core`. Put optional values in the final block and required values in
parameters. The items below are public API with KDoc. Open the applicable file for the exact signature.

## Contents

- component / text / style / colour
- audience (messages, chat, titles, sounds, tab list…)
- book / bossbar (incl. timed)
- event (click / hover)
- key / keybind / score / selector / sound
- nbt / objectcomponent / translatable / theme / time / uuid

## component & text

- `component { }` → `Component`; `emptyComponent()`.
- `text("value") { …style/children… }` and `text { content("…"); … }`; both also as
  `ComponentScope.text(...)` for nesting. `ComponentScope` adds `append(componentLike)`,
  `newline()`, `style(Style)`, `style { }`.
- `Component.asSequence()` — depth-first traversal (`core.text`).

## style and colour

- `style { }` → `Style`; `component styled someStyle` (infix on `ComponentLike`).
- `StyleScope` slots: `color(c)`, `shadow(color[, alpha])`, `font(key)`, `insertion(s)`,
  `bold()` / `italic()` / `underlined()` / `strikethrough()` / `obfuscated()` (each also
  taking `Boolean?` or `TextDecoration.State`), `decorate(d)`, plus click/hover (below).
  Each slot throws `IllegalStateException` if set twice in one block.
- Colours: `hex("#a1b2c3")`, `rgb(r, g, b)`, `hsv(h, s, v)`, `interpolate(t, a, b)`,
  `namedColor("aqua")` (nullable) / `namedColorOrThrow(...)`.
- Gradients: `gradient(vararg stops)` / `gradient(iterable)` → `ColorGradient`;
  `gradientText("text", gradient)` → per-character coloured component.

## audience

- Composition: `audienceOf(vararg members)`, `emptyAudience()`.
- Send: `Audience.message { }`, `Audience.actionBar { }`,
  `Audience.chat { }` (bound chat: `type(...)`, sender name/target),
  `Audience.chat(signedMessage) { }`, `systemMessage(...)` → `SignedMessage`,
  `signature(bytes)`, `Audience.delete(signed | signature)`.
- Titles: `Audience.title { }` (`TitleScope` with `times { fadeIn/stay/fadeOut }`).
- Sounds: `Audience.sound { }`, `Audience.play(sound[, emitter | x,y,z])`,
  `Audience.stopSound { }`.
- Boss bars: `Audience.bossBar { }` builds and shows a bar, and then returns it. `Audience.show(bar)` and `hide(bar)`
  also accept `TimedBossBar`.
- Books: `Audience.book { }` (builds and opens), `Audience.open(book)`.
- Tab list: `Audience.tabList { header { }; footer { } }`.

## book & bossbar

- `book { title { }; author { }; page { }; pages(...) }` → `Book`.
- `bossBar { name { }; progress(0.5f); color(...); overlay(...); darkenScreen(); playBossMusic(); createWorldFog() }`
  → `BossBar` (`BossBarScope` extends the appearance scope).
- Timed: `core.bossbar.timed` contains automatic bars. A `Ticker` context parameter controls them. Use
  `context(ticker) { audience.bossBar(over = 10.seconds) { } }` to get `TimedBossBar`. The package also contains the
  runtime and shutdown types.

## event

- `click { runCommand("/spawn") }` → `ClickEvent<*>`; scope covers `openUrl`, `openFile`,
  `runCommand`, `suggestCommand`, `changePage`, `copyToClipboard`, `callback(fn)` /
  `callback(options, fn)`.
- `hover { text { } / item(...) / entity(...) }` → `HoverEvent<*>`; item data components via
  `ItemDataComponentScope` (`removed()` marks removals).
- Both are also available directly in a `StyleScope` block.

## key, keybind, score, sound

- `key("namespace:value")` / `key(namespace, value)`.
- `keybind("key.jump") { }`; also `ComponentScope.keybind(...)`.
- `score(name, objective) { }`; also as scope extension.
- `sound(key) { source(...); volume(...); pitch(...); seed(...) }` → `Sound`.

## selector (typed vanilla entity selectors)

- Factories: `self { }`, `nearestPlayer { }`, `allPlayers { }`, `randomPlayer { }`,
  `entities { }`, `nearestEntity { }` → `EntitySelector`. The `.asString()` function renders the canonical form. Use
  `selector(...)` and `ComponentScope.selector(...)` for selector components.
- String bridge: `parseSelector("@e[type=zombie]")` is strict. `EntitySelectorParseException` gives the error offset.
- Ranges: `atMost(x)`, `atLeast(x)`, `exactly(x)` (`SelectorRange` double / `SelectorIntRange`
  int), plus Kotlin range overloads.
- Scopes model what vanilla accepts for each head. For example, `@n` keeps `sort` and `limit`. Repeatable arguments
  accumulate. Duplicate singleton arguments cause an exception.

## nbt & objectcomponent

- `nbt { }` → `BinaryTagHolder`. In a compound scope, use `"key" eq value` for a primitive, `"key" eq { }` for a nested
  compound, and `list()` for a list. `nbt("{snbt}")` is the string bridge. `nbtPath("key")` and `nbtPath(index)` are
  typed paths. `matching { }` returns `NbtSelection`.
- NBT components: `blockNbt(path, pos) { }`, `entityNbt(path, selector) { }`,
  `storageNbt(path, key) { }`; positions via `blockPos(x, y, z)`,
  `relativeBlockPos(...)`, `blockPos("~1 ~2 ~3")`.
- Object components: `display(contents) { }` with `sprite(...)` or `head(...)` contents.
  `Component.renderObjectFallbacks()` for non-supporting viewers.

## translatable, theme, time, uuid

- `translatable("key") { fallback("…"); arg(...); args(...) }` has component, Boolean, and number argument overloads. It
  is also a scope extension.
- `theme`: Subclass `Theme` and declare `val header by style { }`. The property name is the dynamic lookup key at
  resolution-ladder level 2. `ThemeRegistry` supplies explicit registration and lookup. The package also supplies
  `ThemeProvider`.
- `time`: `Ticker` and `TickerTask` are the schedule interface for timed boss bars. Platform modules supply concrete
  implementations.
- `uuid("string")` → `java.util.UUID`.
