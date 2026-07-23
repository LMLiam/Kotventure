# Issue hand-off prompt

Paste the block below into a new agent chat. Replace `<N>` with the issue number.

The block sets the working agreement for a roadmap issue: design interrogation first, implementation
second. Refer to [`AGENTS.md`](../AGENTS.md) for the repository rules that the block depends on.

---

```
Kotventure — design and implement issue #<N>.

Read AGENTS.md first, then the project skills it lists (idiomatic-kotlin-dsl and
adding-a-dsl-feature are mandatory before you propose any public API). Read the
issue, the epic #5 entry, and the code the change touches.

HOW I WANT YOU TO WORK

Design before code. Do not write a line of implementation until I have signed off
on the public API. I expect a lot of questions — this is the part I care about.

Interrogate the issue. The issue text, the roadmap, and any prior decision are
starting points, not constraints. If you see a better design, say so and show me
why. "The issue says X" is not a reason to do X.

Every design fork comes to me with real call sites. Not prose descriptions of
options — the actual Kotlin I would write. Put long code in the message body:
AskUserQuestion previews truncate at roughly ten lines and I cannot read them.
Keep the question options themselves to short labels.

Recommend, do not survey. Give me your pick and the reason. If two options are
genuinely close, say that too. Never hand me an exhaustive list with no opinion.

Tell me what the platform actually permits before I choose. Check Adventure and
Paper APIs and report the real constraints — what a client can render, what an
audience can expose, what a callback gives you. Several of my design instincts
have been wrong because I did not know a limit existed.

Surface consequences you discover later. If the formatter, the compiler, or a
test reveals that an approved design reads worse than the sketch, bring it back
to me rather than shipping it quietly.

Then use plan mode, write the plan file, and get my approval before implementing.

DEFINITION OF DONE

- Tests alongside the code, Kotest, dogfooding the `test` module matchers.
- One top-level class, interface, or object for each file. This is a hard rule.
- KDoc on every public declaration, with compiled @sample links.
- Full ASD-STE100 (simplified technical English) in all KDoc, README, and docs.
- ./gradlew build passes, which includes ktlint, spotless, and the 85% kover gate.
- Branch type/issue-<n>/short-desc, commits and PR title verb(area): something.
- PR body from the matching .github/PULL_REQUEST_TEMPLATE, linking the issue.
- Add the PR to project 6, copying the seven fields from the issue.
- Watch CI to green. Triage CodeRabbit: fix what is valid, reject what is not with
  a reason. The neutral Qodana for JVM formatting notices are known noise.
- File follow-up issues for anything you deliberately left out, after asking me.

Do not spawn subagents unless I ask.
```
