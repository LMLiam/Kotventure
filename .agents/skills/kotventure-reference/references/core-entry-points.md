# `core` entry points by feature package

Package prefix: `io.github.lmliam.kotventure.core`. Optional knobs always live in the
trailing block; required values are parameters. Everything below is public API with KDoc —
open the file for exact signatures.

## Contents

- component / text / style / color
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

## style & color

- `style { }` → `Style`; `component styled someStyle` (infix on `ComponentLike`).
- `StyleScope` slots: `color(c)`, `shadow(color[, alpha])`, `font(key)`, `insertion(s)`,
  `bold()` / `italic()` / `underlined()` / `strikethrough()` / `obfuscated()` (each also
  taking `Boolean?` or `TextDecoration.State`), `decorate(d)`, plus click/hover (below).
  Each slot throws `IllegalStateException` if set twice in one block.
- Colors: `hex("#a1b2c3")`, `rgb(r, g, b)`, `hsv(h, s, v)`, `interpolate(t, a, b)`,
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
- Boss bars: `Audience.bossBar { }` (builds **and shows**, returns the bar),
  `Audience.show(bar)` / `hide(bar)` — also for `TimedBossBar`.
- Books: `Audience.book { }` (builds and opens), `Audience.open(book)`.
- Tab list: `Audience.tabList { header { }; footer { } }`.

## book & bossbar

- `book { title { }; author { }; page { }; pages(...) }` → `Book`.
- `bossBar { name { }; progress(0.5f); color(...); overlay(...); darkenScreen(); playBossMusic(); createWorldFog() }`
  → `BossBar` (`BossBarScope` extends the appearance scope).
- Timed: `core.bossbar.timed` — self-advancing bars driven by a `Ticker` supplied as a
  context parameter: `context(ticker) { audience.bossBar(over = 10.seconds) { } }` →
  `TimedBossBar` (runtime/shutdown types in the package).

## event

- `click { runCommand("/spawn") }` → `ClickEvent<*>`; scope covers `openUrl`, `openFile`,
  `runCommand`, `suggestCommand`, `changePage`, `copyToClipboard`, `callback(fn)` /
  `callback(options, fn)`.
- `hover { text { } / item(...) / entity(...) }` → `HoverEvent<*>`; item data components via
  `ItemDataComponentScope` (`removed()` marks removals).
- Both also available directly inside any `StyleScope` block.

## key, keybind, score, sound

- `key("namespace:value")` / `key(namespace, value)`.
- `keybind("key.jump") { }`; also `ComponentScope.keybind(...)`.
- `score(name, objective) { }`; also as scope extension.
- `sound(key) { source(...); volume(...); pitch(...); seed(...) }` → `Sound`.

## selector (typed vanilla entity selectors)

- Factories: `self { }`, `nearestPlayer { }`, `allPlayers { }`, `randomPlayer { }`,
  `entities { }`, `nearestEntity { }` → `EntitySelector` (renders canonically via
  `.asString()`; `selector(...)`/`ComponentScope.selector(...)` for selector components).
- String bridge: `parseSelector("@e[type=zombie]")` — strict, offset-reporting
  (`EntitySelectorParseException`).
- Ranges: `atMost(x)`, `atLeast(x)`, `exactly(x)` (`SelectorRange` double / `SelectorIntRange`
  int), plus Kotlin range overloads.
- Scopes model what vanilla accepts per head (`@n` keeps `sort`/`limit`); repeatables
  accumulate, singletons throw on duplicates.

## nbt & objectcomponent

- `nbt { }` → `BinaryTagHolder` (compound scope: `"key" eq value` for every primitive,
  nested compounds via `"key" eq { }`, `list()`); `nbt("{snbt}")` string bridge;
  `nbtPath("key")`/`nbtPath(index)` typed paths; `matching { }` → `NbtSelection`.
- NBT components: `blockNbt(path, pos) { }`, `entityNbt(path, selector) { }`,
  `storageNbt(path, key) { }`; positions via `blockPos(x, y, z)`,
  `relativeBlockPos(...)`, `blockPos("~1 ~2 ~3")`.
- Object components: `display(contents) { }` with `sprite(...)` / `head(...)` contents;
  `Component.renderObjectFallbacks()` for non-supporting viewers.

## translatable, theme, time, uuid

- `translatable("key") { fallback("…"); arg(...); args(...) }` (component/boolean/number
  argument overloads); also as scope extension.
- `theme`: subclass `Theme` and declare `val header by style { }` — the property name is the
  dynamic lookup key (resolution-ladder rung 2); `ThemeRegistry` (explicit register/lookup),
  `ThemeProvider`.
- `time`: `Ticker` / `TickerTask` — scheduling seam used by timed boss bars (platform modules
  will supply real implementations).
- `uuid("string")` → `java.util.UUID`.
