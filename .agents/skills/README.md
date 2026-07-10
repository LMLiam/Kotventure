# Project skills

Reusable, project-specific playbooks for AI coding agents working in Kotventure. They
complement [`../../AGENTS.md`](../../AGENTS.md): AGENTS.md is the always-on contract; skills
load **when relevant to the task at hand**.

This directory is the **single source of truth**. Each entry in
[`.claude/skills/`](../../.claude/skills) is a symlink back here — never copy files between
the two trees.

## Index

| Skill | Use when |
|---|---|
| [`adding-a-dsl-feature`](adding-a-dsl-feature/SKILL.md) | Starting any issue-to-merge slice — the end-to-end workflow |
| [`idiomatic-kotlin-dsl`](idiomatic-kotlin-dsl/SKILL.md) | **Required before designing/planning any API surface**; reviewing DSL shape |
| [`kotventure-reference`](kotventure-reference/SKILL.md) | Writing any arrange/act/sample code — find the existing entry point, dogfood it |
| [`adventure-reference`](adventure-reference/SKILL.md) | Calling a `net.kyori` API whose shape you haven't verified |
| [`minimessage-reference`](minimessage-reference/SKILL.md) | MiniMessage parsing, typed templates, validation, mini→DSL conversion |
| [`writing-component-tests`](writing-component-tests/SKILL.md) | Writing/reviewing tests — matchers, snapshots, selector and compile-fail patterns |
| [`documenting-public-api`](documenting-public-api/SKILL.md) | KDoc, `@sample`, linking rules, module READMEs |
| [`fixing-ci-failures`](fixing-ci-failures/SKILL.md) | A CI check is red or looks odd — fixes and known noise |
| [`reviewing-contributions`](reviewing-contributions/SKILL.md) | Reviewing/landing someone else's PR |

## Format

Each skill is a directory with a `SKILL.md` (YAML frontmatter `name` + `description`, then
the body), optionally with a `references/` directory for material loaded on demand. Follows
the [Agent Skills](https://agentskills.io) conventions:

- `description` is third person, states *when to use*, and never summarises the workflow.
- SKILL.md stays concise; heavy reference goes in `references/*.md`, one link level deep.
- Skills point at repo docs (`docs/CI.md`, `modules/test/README.md`) instead of duplicating
  them; every API claim must be verifiable against the current codebase.

**Agents:** before writing code, check whether a skill applies and follow it. If a skill
conflicts with an explicit maintainer instruction, the maintainer wins.
