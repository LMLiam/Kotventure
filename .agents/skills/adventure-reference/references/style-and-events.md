# Style, color, and events

## Style — `net.kyori.adventure.text.format`

- `Style.style(...)` overloads; `Style.empty()`; builder via `Style.style { }` (Consumer) or
  `Style.style().build()`. Accessors mirror components: `.color()`, `.font()`,
  `.decoration(TextDecoration)`, `.clickEvent()`, `.hoverEvent()`, `.insertion()`.
- Merge semantics: `style.merge(other, Style.Merge.Strategy...)`; components inherit parent
  style at render time — unset ≠ false.

## Color

- `TextColor.color(rgbInt)` / `(r, g, b)` / `(HSVLike)`; `TextColor.fromHexString("#a1b2c3")`
  (nullable on bad input); `.asHexString()`.
- `NamedTextColor.AQUA` etc.; lookup `NamedTextColor.NAMES.value("aqua")` (nullable);
  `NamedTextColor.nearestTo(color)`.
- Interpolation: `TextColor.lerp(t, a, b)`.
- Shadow: `ShadowColor.shadowColor(argbInt)` / `(TextColor, alpha)` / `(r, g, b, a)` —
  a separate type from `TextColor`, carries alpha.

## Decorations

- `TextDecoration`: `BOLD`, `ITALIC`, `UNDERLINED`, `STRIKETHROUGH`, `OBFUSCATED`.
- Tri-state `TextDecoration.State`: `NOT_SET` / `FALSE` / `TRUE` — the crucial distinction:
  `NOT_SET` inherits from the parent, `FALSE` actively disables. DSL and matchers preserve
  this (see the decoration matchers' NOT_SET semantics in `writing-component-tests`).
- `component.decoration(d)` returns the `State`; `.decoration(d, boolean)` and
  `.decoration(d, State)` set it.

## Click events — `net.kyori.adventure.text.event.ClickEvent`

Since 5.x `ClickEvent<P>` is generic over its payload; `ClickEvent.Action<P>` likewise.

- String payloads: `openUrl`, `openFile`, `runCommand`, `suggestCommand`, `copyToClipboard`.
- Int payload: `changePage(page)`.
- Server-side callback: `ClickEvent.callback(ClickCallback<Audience>)`, optional
  `ClickCallback.Options` (`uses(n)`, `lifetime(Duration)`) via the options overload.
- Custom/dialog payloads exist; `core`'s `ClickActionScope` models the vanilla-expressible
  set — check `core/event` before extending.

## Hover events — `net.kyori.adventure.text.event.HoverEvent`

- `HoverEvent.showText(ComponentLike)`.
- `HoverEvent.showItem(Key, count)` and the data-components overload
  `showItem(Key, count, Map<Key, DataComponentValue>)`
  (`DataComponentValue.removed()` marks a vanished component); wrapper type
  `HoverEvent.ShowItem`.
- `HoverEvent.showEntity(Key, UUID, Component?)`; wrapper type `HoverEvent.ShowEntity`.
- `HoverEventSource<V>` is the "can become a hover event" input type (a `Component` is one).
