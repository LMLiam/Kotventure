<!--
PR title MUST follow: verb(area): something  (e.g. feat(core): add the style DSL)
Enforced by the Conventional Titles workflow. See .github/CONTRIBUTING.md.

Prefer a type-specific template? Append ?template=feature.md (or bugfix.md / docs.md / chore.md)
to the PR URL, or pick one from .github/PULL_REQUEST_TEMPLATE/.
-->

## Summary
<!-- What does this change and why? Keep it concise but clear. -->

## Related issues
Closes #<issue-number>

## Type of change
<!-- Tick all that apply. -->
- [ ] feat — new capability
- [ ] fix — bug fix
- [ ] refactor — internal, no behaviour change
- [ ] docs
- [ ] test
- [ ] chore / build / ci

## Testing
<!-- What did you add/run? Note the Kotest specs and whether `./gradlew build` passes. -->

## Checklist
- [ ] Title and commit subjects follow `verb(area): something`
- [ ] Linked the related issue
- [ ] Tests added/updated (dogfood the `test` matchers where applicable)
- [ ] `./gradlew ktlintCheck spotlessCheck` clean; `explicitApi()` satisfied with KDoc on public API
- [ ] Updated `docs/` and `CHANGELOG.md` if user-facing
