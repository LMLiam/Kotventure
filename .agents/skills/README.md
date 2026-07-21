# Project skills

These project-specific playbooks help AI coding agents work in Kotventure. They supplement
[`../../AGENTS.md`](../../AGENTS.md). AGENTS.md always applies. Load a skill **when it applies to the task**.

This directory is the **single source of truth**. Each entry in
[`.claude/skills/`](../../.claude/skills) is a symlink to this directory. Do not copy files between the two trees.

## Index

| Skill | Use when |
|---|---|
| [`adding-a-dsl-feature`](adding-a-dsl-feature/SKILL.md) | Start an issue-to-merge task |
| [`idiomatic-kotlin-dsl`](idiomatic-kotlin-dsl/SKILL.md) | **Required before API design or planning** and for DSL reviews |
| [`kotventure-reference`](kotventure-reference/SKILL.md) | Write test, sample, or usage code with an existing entry point |
| [`adventure-reference`](adventure-reference/SKILL.md) | Call a `net.kyori` API that you have not verified |
| [`minimessage-reference`](minimessage-reference/SKILL.md) | MiniMessage parsing, typed templates, validation, mini→DSL conversion |
| [`writing-component-tests`](writing-component-tests/SKILL.md) | Write or review matchers, snapshots, selector tests, and compile-fail tests |
| [`documenting-public-api`](documenting-public-api/SKILL.md) | KDoc, `@sample`, linking rules, module READMEs |
| [`fixing-ci-failures`](fixing-ci-failures/SKILL.md) | Investigate a failed or unusual CI check |
| [`reviewing-contributions`](reviewing-contributions/SKILL.md) | Review or land another person's pull request |
| [`pickup-issue`](pickup-issue/SKILL.md) | **User-invoked:** `/pickup-issue <n>` starts an issue end-to-end. |
| [`land-pr`](land-pr/SKILL.md) | **User-invoked:** `/land-pr <n>` starts a maintainer pass on a contributor PR. |

## Format

Each skill directory contains a `SKILL.md` file. This file has YAML frontmatter with `name` and `description`, followed
by the body. A skill can also contain a `references/` directory. Load reference material only when necessary. Follow
the [Agent Skills](https://agentskills.io) conventions:

- Write `description` in the third person and state when to use the skill. Do not summarise the workflow.
- Keep SKILL.md concise. Put detailed material in `references/*.md`, one link level deep.
- Link to repository documents such as `docs/CI.md` and `modules/test/README.md`. Do not duplicate them. Verify each API
  statement against the current codebase.

**Agents:** before writing code, check whether a skill applies and follow it. If a skill
conflicts with an explicit maintainer instruction, the maintainer wins.
