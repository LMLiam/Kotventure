# KyoriAdventureDSL — Pre‑Alpha Plan

> **Version Range:** `0.0.x`  
> **Lifecycle Stage:** Pre‑Alpha  
> **Status:** In Development — unstable, API and behavior subject to change without notice.

---

## 🎯 Purpose of Pre‑Alpha
- **Prove the concept** of a Kotlin DSL for [Kyori Adventure](https://github.com/KyoriPowered/adventure) components.
- Establish **baseline syntax ergonomics** and builder patterns.
- Validate **feasibility** of type‑safe, chainable component creation.
- Lay groundwork for **multi‑module Gradle setup** and reproducible builds.

---

## 🧩 Scope for 0.0.x
- **Minimal vertical slice** of the DSL:
    - `text` component builder with `color` and `decoration` support
    - Nesting support for child components
- Basic null‑safety and compile‑time constraints.
- **Per‑module JAR outputs** + one aggregate JAR to verify build orchestration.
- Initial CI pipeline:
    - Linting + formatting
    - Unit test execution
    - Pre-alpha publishing to artifact repo

---

## 📦 Deliverables
- `kyoriadventuredsl-core-0.0.x-PRE-ALPHA.jar`
- `kyoriadventuredsl-0.0.x-PRE-ALPHA.jar`
- Minimal `README.md` with example:
  ```kotlin
  component {
      text("Hello Kyori") {
          color(NamedTextColor.AQUA)
          decorate(TextDecoration.BOLD)
      }
  }


---

## ✅ Success Crieria
- Can build and publish artifacts locally and via CI
- DSL products valid Kyori Adventure `Component` objects for implemented features
- No hard-coded, one-off logic -- architecture allows new component types to slot in without refactors
- Contributors can clone, build, and run tests without prior Gradle configuration knowledge

---

## ⚠️ Limitations & Warnings
- Expected frequent **breaking changes** until Alpha
- Limited component coverage -- not representative of final API surface 
- No performance optimisations -- focus is on correctness and developer experience