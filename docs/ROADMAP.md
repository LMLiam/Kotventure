# Kotventure — Project Roadmap

> **Lifecycle Stages:** Pre‑Alpha → Alpha → Beta → Release
>
> This roadmap describes what each stage is *for*. It deliberately does not track which version is current —
> that lives in the published version and the changelog, not in prose that would rot.

---

## Alpha (`0.1.x`–`0.8.x`)

**Focus:**

- Expand the core DSL without centralizing every feature in `ComponentScope`.
- Keep public API surface small while compatibility is still flexible.
- Harden MiniMessage templates, validation, serializers, and component-testing tools.
- Keep build, docs, and release metadata aligned with the currently shipped modules.

**Plan:** See the [phased roadmap in `docs/DESIGN.md`](./DESIGN.md#11-phased-roadmap) for the alpha-phase breakdown.

---

## Beta (`0.9.x`)

**Focus:**

- Feature‑complete for planned 1.0.
- API freeze — backwards‑compatibility guaranteed until 2.0.
- Integration tests with real‑world scenarios / sample projects.
- Docs polished for production use (including Architecture deep‑dive).
- Performance profiling and optimisations.

**Milestone Criteria:**

- Ready for production testing.
- No known breaking changes pending for 1.0.

---

## Release (`1.0.0`)

**Focus:**

- Stable, documented, production‑ready API.
- Semantic versioning commitment.
- Signed artifacts deployed.

---

## 🛣️ Long‑Term Ideas (Post‑1.0)

- Rich theme/plugin system for styling.
- Developer tooling (e.g., DSL linting rules, IDE inspections).
- Performance tuning for large‑scale component trees.
- Cookbook‑style documentation site.

---

## 📆 High‑Level Timeline

| Stage     | Target Range | Key Deliverable                      |
|-----------|--------------|--------------------------------------|
| Pre‑Alpha | 0.0.x        | Core DSL spike + snapshot publish    |
| Alpha     | 0.1.x–0.8.x  | Public API expansion + feedback loop |
| Beta      | 0.9.x        | API freeze, stability, doc polish    |
| Release   | 1.0.0        | Production‑ready stable release      |
