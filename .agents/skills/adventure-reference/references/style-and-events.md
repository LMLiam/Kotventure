# Style, colour, and events

## Style — `net.kyori.adventure.text.format`

- `Style.style(...)` overloads and `Style.empty()`. Create a builder with `Style.style { }` (Consumer) or
  `Style.style().build()`. Accessors mirror components: `.color()`, `.font()`,
  `.decoration(TextDecoration)`, `.clickEvent()`, `.hoverEvent()`, `.insertion()`.
- Merge styles with `style.merge(other, Style.Merge.Strategy...)`. Components inherit the parent style at render time.
  An unset value is not the same as `false`.

## Colour

- `TextColor.color(rgbInt)` / `(r, g, b)` / `(HSVLike)`. `TextColor.fromHexString("#a1b2c3")` returns null for invalid
  input. Use `.asHexString()` to get a hexadecimal string.
- Named colours include `NamedTextColor.AQUA`. Use `NamedTextColor.NAMES.value("aqua")` for lookup. It can return null.
  `NamedTextColor.nearestTo(color)`.
- Interpolation: `TextColor.lerp(t, a, b)`.
- Shadow: `ShadowColor.shadowColor(argbInt)` / `(TextColor, alpha)` / `(r, g, b, a)`. `ShadowColor` is separate from
  `TextColor` and contains an alpha value.

## Decorations

- `TextDecoration`: `BOLD`, `ITALIC`, `UNDERLINED`, `STRIKETHROUGH`, `OBFUSCATED`.
- Tri-state `TextDecoration.State` has `NOT_SET`, `FALSE`, and `TRUE`. `NOT_SET` inherits from the parent. `FALSE`
  disables the decoration. The DSL and matchers preserve this difference. Refer to the NOT_SET rules in
  `writing-component-tests`.
- `component.decoration(d)` returns the `State`; `.decoration(d, boolean)` and
  `.decoration(d, State)` set it.

## Click events — `net.kyori.adventure.text.event.ClickEvent`

In version 5.x, `ClickEvent<P>` and `ClickEvent.Action<P>` are generic over their payloads.

- String payloads: `openUrl`, `openFile`, `runCommand`, `suggestCommand`, `copyToClipboard`.
- Int payload: `changePage(page)`.
- Server-side callback: `ClickEvent.callback(ClickCallback<Audience>)`, optional
  `ClickCallback.Options` (`uses(n)`, `lifetime(Duration)`) via the options overload.
- Adventure also has custom and dialog payloads. The `core` `ClickActionScope` models the set that vanilla can express.
  Check `core/event` before you extend it.

## Hover events — `net.kyori.adventure.text.event.HoverEvent`

- `HoverEvent.showText(ComponentLike)`.
- `HoverEvent.showItem(Key, count)` and the data-components overload
  `showItem(Key, count, Map<Key, DataComponentValue>)`
  (`DataComponentValue.removed()` marks a removed component). The wrapper type is
  `HoverEvent.ShowItem`.
- `HoverEvent.showEntity(Key, UUID, Component?)`. Its wrapper type is `HoverEvent.ShowEntity`.
- `HoverEventSource<V>` is the "can become a hover event" input type (a `Component` is one).
