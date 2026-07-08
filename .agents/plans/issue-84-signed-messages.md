# Issue #84 — signed message send + deletion helpers (`feat(audience)`)

## Context

Adventure 5.x replaced the legacy `Identity`/`Identified` message overloads with the signed/system
chat model: `Audience.sendMessage(SignedMessage, ChatType.Bound)`, `sendMessage(Component, ChatType.Bound)`,
and `deleteMessage(SignedMessage | SignedMessage.Signature)`. Issue #84 asks the audience DSL
(started in #33, merged as PR #229) to cover this modern chat surface. Both blockers are closed:
#80 (Adventure 5.1.1 baseline; repo is now on `adventure-api` 5.2.0) and #33.

Verified against `adventure-api-5.2.0-sources.jar`:

- `ChatType` — constants (`CHAT`, `SAY_COMMAND`, `MSG_COMMAND_INCOMING/OUTGOING`,
  `TEAM_MSG_COMMAND_INCOMING/OUTGOING`, `EMOTE_COMMAND`), `bind(name)` / `bind(name, target)` →
  `ChatType.Bound` (`type()`, `name(): Component`, `target(): Component?`).
- `SignedMessage` — only two factories exist: `SignedMessage.system(String, ComponentLike?)` and
  `SignedMessage.signature(byte[])`. Real player-signed messages can only be *acquired* on-platform
  (Paper chat event / Brigadier argument) — and no Paper module is scaffolded yet, so acquisition is
  **docs-prose only**, no Paper types anywhere.
- `Audience.deleteMessage(SignedMessage)` does `requireNonNull(signedMessage.signature())` → a bare
  NPE when the message is unsigned. `SignedMessage.canDelete()` exists as the proper precondition.
- `isSystem()` surfaces as a synthetic Kotlin property already; accessor wrappers from the issue's
  scope list add nothing → **dropped** (note this in the PR body against the scope bullet).

## Decided API surface (design forks settled with Liam)

All in `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/audience/`, package
`io.github.lmliam.kotventure.core.audience`. No `ChatType.CHAT.bind(...)` at call sites — a unified
`chat { }` DSL instead:

```kotlin
// 1. Unsigned component sent as player-styled (non-system) chat
audience.chat {
    name { text("Steve") { color(aqua) } }   // required → ISE if missing
    target { text("Alex") }                  // optional
    type(ChatType.MSG_COMMAND_OUTGOING)      // optional, defaults to ChatType.CHAT
    content { text("hi there") }             // required → ISE if missing
}

// 2. Already-signed message (scope has NO content — compile-enforced difference)
audience.chat(signed) {
    name { text("Steve") }
}

// 3. Deletion
audience.delete(signed)      // guard: check(signed.canDelete()) { ... } → ISE, else delegate
audience.delete(signature)   // pure delegate to deleteMessage(Signature)

// 4. System SignedMessage factory (string + optional block knob)
val msg = systemMessage("plain fallback") { text("Fancy") { color(gold) } }
val bare = systemMessage("just raw")      // unsignedContent = null
```

Strictness rules (repo convention, `reject malformed input`):
- every singleton slot (`name`, `target`, `type`, `content`) set twice → ISE naming the slot —
  use the existing `once()` / `OnceAssign` delegate from `core/dsl/OnceAssign.kt` (message format:
  `'name' is already set.`).
- missing required slot (`name`; `content` in the unsigned form) → ISE naming the slot at build time.

## Files

New (all under `modules/core/src/`):

| File | Contents |
|---|---|
| `main/.../core/audience/ChatScope.kt` | `@KotventureDslMarker` scopes: `BoundChatScope` (`name {}`, `target {}`, `type(ChatType)`) and `ChatScope : BoundChatScope` adding `content {}` |
| `main/.../core/audience/ChatBuilder.kt` | `internal` impls building `ChatType.Bound` (+ content component); required-slot checks live here |
| `main/.../core/audience/Chat.kt` | `public fun Audience.chat(init: ChatScope.() -> Unit)` and `public fun Audience.chat(signed: SignedMessage, init: BoundChatScope.() -> Unit)` |
| `main/.../core/audience/Delete.kt` | `public fun Audience.delete(signed: SignedMessage)` (canDelete guard) and `delete(signature: SignedMessage.Signature)` |
| `main/.../core/audience/SystemMessage.kt` | `public fun systemMessage(message: String, init: (ComponentScope.() -> Unit)? = null): SignedMessage` |
| `samples/.../core/audience/ChatSamples.kt` etc. | `internal fun ...Sample()` per entry point, `@sample`-linked (follow `score/ScoreSamples.kt` pattern) |
| `test/.../core/audience/ChatDslTest.kt`, `DeleteDslTest.kt`, `SystemMessageDslTest.kt` | Kotest `StringSpec`s (see Tests) |

Existing things to reuse (do not reinvent):
- `KotventureDslMarker` (`core/dsl/KotventureDslMarker.kt`), `once()`/`OnceAssign` (`core/dsl/OnceAssign.kt`)
- `component(init)` / `ComponentScope` (`core/component/`) for `name`/`target`/`content`/unsigned-content blocks
- Existing `Audience.message { }` in `core/audience/Message.kt` stays untouched.

File-split judgment call: if any of these files ends up trivially small, merging (e.g. scopes+builder
into one file, or `Delete.kt` into `Chat.kt`) is fine — SRP by feature, not one-declaration-per-file
dogma. Splitting shown above is the default.

## Implementation steps (TDD, one vertical slice)

1. **Branch** `feat/issue-84/signed-messages` off up-to-date `master` (#33/PR #229 is merged).
2. **`systemMessage` factory** — test first (`SystemMessageDslTest`): raw-only call has
   `unsignedContent() == null`, `isSystem` true, `message()` round-trips; block call builds the
   unsigned component (assert with `modules/test` component matchers). Then the one-liner impl.
3. **`chat { }` scopes/builder** — tests first (`ChatDslTest`), then implement:
   - unsigned form records `sendMessage(Component, ChatType.Bound)`: assert content component,
     `bound.type().key()`, `bound.name()`, `bound.target()` (null when omitted, set when given)
   - default type is `ChatType.CHAT` when `type(...)` omitted
   - signed form records `sendMessage(SignedMessage, ChatType.Bound)` with the same bound assertions
   - ISE on: missing `name`, missing `content` (unsigned form), duplicate `name`/`target`/`type`/`content`
     — **no error-message assertions** (repo test philosophy); assert `shouldThrow<IllegalStateException>` only
   - forwarding audience (`Audience.audience(a, b)`) receives the send once per member
4. **`delete` helpers** — tests first (`DeleteDslTest`), then implement:
   - `delete(signed)` forwards a deletable message (build via `SignedMessage.system` won't work — it
     has no signature; use a tiny private test `SignedMessage` impl or verify Adventure's default
     routing: `deleteMessage(SignedMessage)` delegates to `deleteMessage(Signature)`, so record at
     the signature overload)
   - `delete(signed)` on an unsigned/system message → ISE (our guard), not NPE
   - `delete(signature)` forwards the exact signature (`SignedMessage.signature(byteArrayOf(...))`)
5. **Samples + KDoc** — `internal fun ...Sample()` functions in `src/samples`, `@sample` FQN links
   from each public declaration's KDoc (pattern: `core/score/Score.kt` ↔ `ScoreSamples.kt`). KDoc on
   the signed `chat` overload and `delete` gets a short **prose-only** note on acquiring real signed
   messages on Paper (chat event `signedMessage()`, Brigadier signed-message argument) — explicitly
   platform-specific, no Paper types/imports. `@throws IllegalStateException` documented on every
   throwing entry point.
6. **Test fakes**: extend the local private `RecordingAudience` pattern from `MessageDslTest.kt`
   (per-test-file fake; #66 shared recording audience doesn't exist yet — do not build it here).
   Kotest note: `chat {}` extension may be shadowed by Kotest's `String.invoke` inside `StringSpec`
   lambdas — if so, use private test helper functions per the established convention
   (`dont-design-around-tests` memory).
7. **Format + verify** (below), then commit `feat(core): add signed message send and deletion helpers`
   (subject must be `verb(area):` lowercase), PR titled the same with `Closes #84`, feature PR template.
8. **Project metadata**: attach PR to `Kotventure Roadmap` project, mirror issue #84's fields
   (Status/Priority/Area/Kind/Effort/Risk/Contributor fit); issue currently shows Status **Blocked** —
   both blockers are closed, so set the issue to In progress when starting. Verify with
   `gh project item-list`.

## Verification

```bash
./gradlew ktlintFormat            # then hand-check 8-space continuation indent (ktlint won't fix it)
./gradlew build                   # compile + Kotest + ktlint/spotless + koverVerify (≥85% gate)
./gradlew koverHtmlReport         # spot-check new files' coverage
```

- All new public declarations: explicit visibility + return types + KDoc (`explicitApi()` fails the build otherwise).
- End-to-end check: the Kotest suites drive every public entry point through `RecordingAudience`
  fakes and assert on real `net.kyori` objects (`ChatType.Bound`, `SignedMessage`) — that *is* the
  runtime surface for a library; no separate app run needed.
- Docs: no user-facing docs site pages exist for the audience DSL yet beyond KDoc/samples; KDoc +
  samples satisfy the "docs updated" criterion. Never touch `CHANGELOG.md`.

## Acceptance criteria mapping

- Signed send with explicit bound chat type → `Audience.chat(signed) { ... }` ✓
- Deletion by message and by signature → `Audience.delete(...)` ×2 ✓
- Tests through a recording audience → local fakes (per #66-pending convention) ✓
- Docs/samples → `@sample`-linked sample files + Paper-acquisition prose in KDoc ✓
- Scope bullet "Kotlin-friendly system/unsigned wrappers" → `systemMessage(...)` factory; accessor
  wrappers deliberately dropped (`isSystem` is already idiomatic) — call this out in the PR body.
