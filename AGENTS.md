# AGENTS.md

Operating guide for AI coding agents (Codex, Claude, etc.) working in this repository.
Humans should read [`CONTRIBUTING.md`](.github/CONTRIBUTING.md); this file restates the same rules in an agent-friendly,
enforce-able form.

> **Priority:** an explicit maintainer instruction always wins over this file. This file wins over your defaults.

---

## 1. What this project is

A **batteries-included, multi-platform Kotlin DSL for [Adventure](https://github.com/PaperMC/adventure)** (Paper /
Velocity / Fabric). It wraps Adventure with an idiomatic, type-safe DSL and adds tooling rivals lack: typed + validated
MiniMessage, a component-testing toolkit, ANSI preview, and codegen.

**Sources of truth — read before non-trivial work:**

- [`docs/DESIGN.md`](docs/DESIGN.md) — architecture, module map, canonical DSL surface, roadmap. **This governs design
  decisions.**
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — phase sequencing.
- **Epic [#5](https://github.com/LMLiam/Kotventure/issues/5)** + its milestoned sub-issues — the work, sliced.

## 2. Golden rules

1. **One issue → one small vertical slice → one small PR.** If a change is growing large or doing two things, stop and
   split it.
2. **Wrap, don't reinvent.** Build on `net.kyori.*` types; never re-implement what Adventure already does.
3. **Respect the architecture** (§4). Don't add cross-module dependencies that aren't in the design.
4. **Tests are part of the change**, not a follow-up.
5. **YAGNI.** Implement what the issue asks for — no speculative abstractions.

## 3. Build, test, format

JDK **25** (provisioned via the Gradle toolchain). Always use the wrapper.

```bash
./gradlew build          # compile + test + lint
./gradlew test           # Kotest suites
./gradlew ktlintFormat    # auto-fix Kotlin style   (or: ./gradlew spotlessApply)
./gradlew ktlintCheck spotlessCheck   # verify style
```

Run formatting and the relevant tests **before** committing. CI must be green.

Shared dependency and plugin coordinates live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). Add shared
versions there first, then consume them through catalog aliases or the existing Gradle helper scripts that delegate to
the catalog.

## 4. Architecture you must respect

See `docs/DESIGN.md` §4 for the full map. Non-negotiables:

- **Modules are added lazily, per phase** (issue #7 owns the restructure). Each lives under `modules/<name>` and is
  re-enabled in `settings.gradle` when it lands.
- **`core` depends only on `adventure-api`.** Do not pull MiniMessage, coroutines, or platform code into `core`.
- **Hybrid extensibility:** a small **explicit registry** (`AdventureDsl`) holds pluggable pieces (MiniMessage tags,
  theme providers, animation drivers, platform adapters). **Do NOT reintroduce `ServiceLoader`/SPI/reflection** — it was
  removed deliberately (#6).
- **Public API is explicit:** `explicitApi()` is on for library modules. Every public/`protected` declaration needs an
  explicit visibility modifier, an explicit return type, and **KDoc**.

## 5. Code quality & structure — the part that matters

### Single Responsibility (SRP)

- One file / class / object = **one reason to change**. If a file does two things, split it.
- Prefer **many small, focused files** over a few large ones. A file you can't hold in your head is too big.
- A function does one thing at one level of abstraction. Extract when it grows, nests deeply, or needs a comment to
  explain a block.

### Folder / package structure

- **Package by feature, not by layer.** Use `…core.text`, `…core.style`, `…core.color`, `…core.event`,
  `…minimessage.template` — **not** `builders/`, `models/`, `utils/`, `helpers/`.
- Public DSL entry points live in the feature package; keep implementation detail `internal`.
- No `util`/`misc`/`common` dumping grounds. If something needs a home, it belongs to a feature.

### API design judgment — decide the shape before you plan

Before sketching any public surface (and **before posting an implementation plan to an issue**), read
"Designing the API shape" in the skill `idiomatic-kotlin-dsl` and apply it. The short form:

- **Compile-time beats runtime.** If a named thing can be a property, make it a property. Delegated properties
  (`by` + `PropertyDelegateProvider`) give you a compile-checked property *and* its name for runtime/registry interop —
  reach for them before inventing key types or string lookups. Every runtime `require(...)` for a
  missing/typo'd/duplicate
  name is a compile error you failed to design for.
- **One way per use case.** Never ship a typed API and string overloads of it in parallel. Pick one; expose a single
  explicit string bridge only where interop (config, MiniMessage, cross-plugin) demands it.
- **No side effects on construction.** Defining a value never registers it; registration in `AdventureDsl` is a
  separate explicit call.
- **Smallest surface that passes the acceptance criteria.** Count your public declarations and make each pay rent.
  Lookup sugar and convenience overloads can be added later; they can't be removed.
- **Snippets in issues / `docs/DESIGN.md` are illustrative, not contracts.** If a sketch can't type-check as written,
  design the API that delivers its *intent* — don't replicate its syntax with runtime machinery.

### Idiomatic Kotlin — do

- Build DSLs with **lambda-with-receiver builders** + `@DslMarker` to prevent scope leakage.
- `val` over `var`; immutable data; `data class` for value types; `sealed`/`enum` for closed hierarchies.
- **Extension functions** for the ergonomic surface; **expression bodies** for one-liners.
- Null-safety: no `!!`. Prefer `?:`, `?.`, `requireNotNull(x) { "msg" }`, `checkNotNull`.
- Use scope functions (`apply`/`also`/`let`/`run`/`with`) where they read better — not gratuitously.
- Clear, full names. No abbreviations or Hungarian notation.

### Idiomatic Kotlin — don't

- No Java-isms: no manual getters/setters where properties fit, no hand-rolled builder classes where a DSL fits, no
  `java.util.*` where the Kotlin stdlib fits.
- **No wildcard imports** (enforced by ktlint).
- No god objects, no static `Utils` classes.
- Don't expose mutable state across module/public boundaries.
- Don't over-engineer: no interfaces with a single implementation "just in case", no premature generics.

### Wrapping Adventure

- Every builder must produce a **valid Adventure object** — verify by constructing and asserting on the real `net.kyori`
  type.
- See the skill `adventure-reference` before using an API you're unsure of — **do not guess API shapes.**

## 6. Testing

- **Kotest** for everything. Every behavioural change ships with tests; write the test first when practical.
- **Dogfood the project's own matchers** (the `test` module). See the skill `writing-component-tests`.
- Use **snapshot tests** for message regressions where appropriate.

## 7. Commits, PRs, branches (enforced in CI)

- **Titles and every commit subject** follow `verb(area): something` (all lowercase verb + area). Enforced by
  `.github/workflows/conventional-titles.yml`. Recommended verbs/areas are listed in `CONTRIBUTING.md`.
- **Branch:** `type/issue-<n>/short-desc` (e.g. `feat/issue-19/style-dsl`).
- **Link issues:** `Closes #<n>` in the PR; pick the matching PR template.
- **Project metadata:** if the issue is attached to a GitHub Project (for example `Kotventure Roadmap`), attach the PR
  to the same project and set/verify the visible planning fields: `Status`, `Priority`, `Area`, `Kind`, `Effort`,
  `Risk`, and `Contributor fit`. Mirror the issue's values unless the maintainer explicitly says otherwise, and verify
  with `gh project item-list` before reporting completion.

## 8. Definition of done (per issue)

- [ ] Public DSL surface + implementation in the **correct module and feature package**
- [ ] Tests (matchers / snapshots) pass
- [ ] `ktlintCheck` + `spotlessCheck` clean; `explicitApi()` satisfied with KDoc on public API
- [ ] `docs/` and `CHANGELOG.md` updated if the change is user-facing
- [ ] Title + commit subjects conform; issue linked

## 9. Project skills

Reusable playbooks — available via the `Skill` tool (Claude Code: `.claude/skills/`) or the `skill` tool (Copilot/Codex:
`.agents/skills/`):

- **`adding-a-dsl-feature`** — the end-to-end workflow for a new DSL feature.
- **`idiomatic-kotlin-dsl`** — **required reading before designing or planning any API surface**: the resolution
  ladder (property → delegate → typed key → string), the design pressure-test, and DSL idioms with do/don't examples.
- **`adventure-reference`** — a map of the Adventure API so you don't hallucinate types.
- **`writing-component-tests`** — Kotest + the component matchers + snapshots.

## 10. Don't touch without a reason tied to your issue

- Build wiring / module layout is restructured per phase (#7). Keep build changes minimal and scoped.
- Don't change the public API of a frozen module without updating the binary-compatibility baseline (post-#55).
