# Kotventure — Project Roadmap

> **Lifecycle Stages:** Pre‑Alpha → Alpha → Beta → Release  
> **Status:** Currently in **Pre‑Alpha** (`0.0.x`)

---

## 📍 Current Stage — Pre‑Alpha (`0.0.x`)
**Focus:**
- Prove core DSL concept for Kotlin → Adventure component creation.
- Ship *minimum viable* feature slice:
    - `text` component builder with `color` + `decoration`
    - Basic nesting
- Verify build + CI flow (per‑module JARs and aggregate JAR).
- Publish snapshots for internal and early‑access testers.

**Plan:** See [Pre‑Alpha Plan](./PRE-ALPHA-PLAN.md) for detail

---

## 🔜 Next Stage — Alpha (`0.1.x` → `0.8.x`)
**Focus:**
- Expand component coverage: `translate`, `keybind`, `score`, etc.
- API surface “mostly there” but open to iteration.
- Public pre‑release artifacts with “alpha” tag.
- Introduce null‑safety and compile‑time validations across the DSL.
- Begin collecting structured feedback via GitHub Issues

**Milestone Criteria:**
- Core API stable enough for hobbyist / experimental use.
- README and module docs updated with examples and onboarding instructions.

---

## 📦 Beta (`0.9.x`)
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

## 🚀 Release (`1.0.0`)
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
| Pre‑Alpha | 0.0.x (Now)  | Core DSL spike + snapshot publish    |
| Alpha     | 0.1.x–0.8.x  | Public API expansion + feedback loop |
| Beta      | 0.9.x        | API freeze, stability, doc polish    |
| Release   | 1.0.0        | Production‑ready stable release      |

