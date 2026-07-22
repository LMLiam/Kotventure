# Kotventure Project Roadmap

> **Lifecycle Stages:** Pre‑Alpha → Alpha → Beta → Release
>
> This roadmap gives the purpose of each stage. The published version and changelog identify the current version.
> Stage transitions follow the milestone criteria below, not fixed version numbers: every pre-1.0 release is a
> `0.x` minor, and the Beta stage begins when the API freeze lands.

---

## Alpha (current)

**Focus:**

- Expand the core DSL without centralising all features in `ComponentScope`.
- Keep public API surface small while compatibility is still flexible.
- Improve MiniMessage templates, validation, serialisers, and component-test tools.
- Keep build, documentation, and release metadata consistent with published modules.

**Plan:** Refer to the [phased roadmap in `docs/DESIGN.md`](./DESIGN.md#11-phased-roadmap) for the Alpha phases.

---

## Beta (after the API freeze)

**Focus:**

- Complete all planned 1.0 features.
- Freeze the API. Guarantee backward compatibility before version 2.0.
- Add integration tests with real examples and sample projects.
- Prepare documentation for production use. Include a detailed architecture guide.
- Measure and optimise performance.

**Milestone Criteria:**

- Ready for production testing.
- No known breaking changes remain for 1.0.

---

## Release (`1.0.0`)

**Focus:**

- Provide a stable, documented API that is ready for production.
- Use semantic versioning.
- Publish signed artefacts.

---

## 🛣️ Long-Term Ideas (After 1.0)

- Add a theme and plugin system for styles.
- Add developer tools, such as DSL lint rules and IDE inspections.
- Improve performance for large component trees.
- Add a cookbook documentation site.

---

## 📆 Timeline

| Stage     | Versions                    | Key Deliverable                      |
|-----------|-----------------------------|--------------------------------------|
| Pre‑Alpha | `0.0.x`                     | Core DSL spike + snapshot publish    |
| Alpha     | `0.x` until the API freeze  | Public API expansion + feedback loop |
| Beta      | `0.x` after the API freeze  | API freeze, stability, doc polish    |
| Release   | `1.0.0`                     | Production‑ready stable release      |
