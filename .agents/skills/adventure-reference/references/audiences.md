# Audiences — `net.kyori.adventure.audience.Audience`

An `Audience` is a message receiver, such as a player, console, group, or proxy source. Use
`Audience.audience(a, b, …)` and `Audience.empty()` for composition. Kotventure supplies `audienceOf(...)` and
`emptyAudience()` in `core/audience`.

## Messages & chat

- `sendMessage(Component)` — plain system-style message.
- Bound chat: `sendMessage(Component, ChatType.Bound)` / `sendMessage(SignedMessage, ChatType.Bound)`.
  - `ChatType` constants: `CHAT`, `SAY_COMMAND`, `MSG_COMMAND_INCOMING/OUTGOING`,
    `TEAM_MSG_COMMAND_INCOMING/OUTGOING`, `EMOTE_COMMAND` (`net.kyori.adventure.chat`).
  - `chatType.bind(nameComponent, targetComponent?)` → `ChatType.Bound`.
- `SignedMessage.system(content, Component?)`; signatures via
  `SignedMessage.signature(ByteArray)`.
- Deletion: `deleteMessage(SignedMessage)` / `deleteMessage(SignedMessage.Signature)`.
- `sendActionBar(Component)`.
- `Identity` (`net.kyori.adventure.identity`) identifies message senders where required.

## Titles — `net.kyori.adventure.title`

- `Title.title(main, subtitle)` / `(main, subtitle, Title.Times)`.
- `Title.Times.times(fadeIn, stay, fadeOut)` — three `java.time.Duration` values. Tick helpers are in
  `net.kyori.adventure.util.Ticks` (`Ticks.duration(n)`).
- `showTitle(Title)`, `clearTitle()`, `resetTitle()`; partial updates via
  `sendTitlePart(TitlePart.TITLE|SUBTITLE|TIMES, value)`.

## Sounds — `net.kyori.adventure.sound`

- `Sound.sound(Key, Sound.Source, volume, pitch)`. The `Sound.sound()` builder adds `.seed(long)`.
- Play: `playSound(Sound)` (at the listener), `playSound(Sound, Emitter)`
  (`Sound.Emitter.self()` or an entity), `playSound(Sound, x, y, z)`.
- Stop: `stopSound(SoundStop)` with `SoundStop.all()`, `.named(Key)`, `.source(Source)`, or
  `.namedOnSource(Key, Source)`. The convenience form is `stopSound(Sound)`.

## Boss bars — `net.kyori.adventure.bossbar`

- `BossBar.bossBar(name, progress, Color, Overlay)`. Progress is in the range `0f..1f`
  (`BossBar.MIN_PROGRESS`/`MAX_PROGRESS`).
- Enums: `BossBar.Color` (PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE),
  `BossBar.Overlay` (PROGRESS, NOTCHED_6/10/12/20), `BossBar.Flag`
  (DARKEN_SCREEN, PLAY_BOSS_MUSIC, CREATE_WORLD_FOG).
- A `BossBar` is a live, mutable, shared object. Change it with `.name(c)`, `.progress(f)`, or `.addFlag(s)`. Use
  `showBossBar(bar)` and `hideBossBar(bar)` for each audience.

## Books & tab list

- `Book.book(title, author, pages...)` in `net.kyori.adventure.inventory`.
  `openBook(Book)`.
- `sendPlayerListHeaderAndFooter(header, footer)` does not permit a partial update. Send `Component.empty()` for the
  side that you do not set.

## Pointers

- `Audience` extends `Pointered`. The `get(Pointer)` and `getOrDefault` functions supply identity, locale, and display
  name data. Platform modules use these functions.
