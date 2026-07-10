# Audiences — `net.kyori.adventure.audience.Audience`

An `Audience` is any message receiver (player, console, group, proxy source). Composition:
`Audience.audience(a, b, …)`, `Audience.empty()` — Kotventure wraps these as `audienceOf(...)`
and `emptyAudience()` in `core/audience`.

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
- `Title.Times.times(fadeIn, stay, fadeOut)` — three `java.time.Duration`s; tick helpers in
  `net.kyori.adventure.util.Ticks` (`Ticks.duration(n)`).
- `showTitle(Title)`, `clearTitle()`, `resetTitle()`; partial updates via
  `sendTitlePart(TitlePart.TITLE|SUBTITLE|TIMES, value)`.

## Sounds — `net.kyori.adventure.sound`

- `Sound.sound(Key, Sound.Source, volume, pitch)`; builder `Sound.sound()` adds `.seed(long)`.
- Play: `playSound(Sound)` (at the listener), `playSound(Sound, Emitter)`
  (`Sound.Emitter.self()` or an entity), `playSound(Sound, x, y, z)`.
- Stop: `stopSound(SoundStop)` — `SoundStop.all()`, `.named(Key)`, `.source(Source)`,
  `.namedOnSource(Key, Source)`; convenience `stopSound(Sound)`.

## Boss bars — `net.kyori.adventure.bossbar`

- `BossBar.bossBar(name, progress, Color, Overlay)`; progress is `0f..1f`
  (`BossBar.MIN_PROGRESS`/`MAX_PROGRESS`).
- Enums: `BossBar.Color` (PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE),
  `BossBar.Overlay` (PROGRESS, NOTCHED_6/10/12/20), `BossBar.Flag`
  (DARKEN_SCREEN, PLAY_BOSS_MUSIC, CREATE_WORLD_FOG).
- A `BossBar` is a live, mutable, shared object: mutate with `.name(c)`, `.progress(f)`,
  `.addFlag(s)`; show/hide per audience with `showBossBar(bar)` / `hideBossBar(bar)`.

## Books & tab list

- `Book.book(title, author, pages...)` (`net.kyori.adventure.inventory`);
  `openBook(Book)`.
- `sendPlayerListHeaderAndFooter(header, footer)` — no partial update; send
  `Component.empty()` for the side you don't set.

## Pointers

- `Audience` extends `Pointered`: `get(Pointer)`/`getOrDefault` expose identity, locale,
  display name, etc. — relevant once platform modules land.
