# Project skills

Reusable, project-specific playbooks for AI coding agents working in Kotventure.
They complement [`../../AGENTS.md`](../../AGENTS.md): AGENTS.md is the always-on contract; these skills are loaded **when relevant to the task at hand**.

Each skill is a directory with a `SKILL.md` (YAML frontmatter `name` + `description`, then the body).

| Skill | Use when |
|-------|----------|
| [`adding-a-dsl-feature`](adding-a-dsl-feature/SKILL.md) | Implementing any new DSL feature / picking up a feature issue |
| [`idiomatic-kotlin-dsl`](idiomatic-kotlin-dsl/SKILL.md) | Designing or reviewing DSL/builder code |
| [`kyori-adventure-reference`](kyori-adventure-reference/SKILL.md) | You need an Adventure API and must not guess its shape |
| [`writing-component-tests`](writing-component-tests/SKILL.md) | Writing tests for components / audiences |

**Agents:** before writing code, check whether one of these applies and follow it. If a skill conflicts with an explicit maintainer instruction, the maintainer wins.
