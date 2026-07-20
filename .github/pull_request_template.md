# Pull Request Template

<!--
PR title MUST follow: verb(area): something  (e.g. feat(core): add the style DSL)
Enforced by the Conventional Titles workflow. See .github/CONTRIBUTING.md.

Prefer a type-specific template? Append ?template=feature.md (or bugfix.md / docs.md / chore.md)
to the PR URL, or pick one from .github/PULL_REQUEST_TEMPLATE/.
-->

## Summary

<!-- Describe the change and its purpose. Use short, clear sentences. -->

## Related issues

Closes #<issue-number>

## Type of change

<!-- Tick all that apply. -->

- [ ] feat: new capability
- [ ] fix: bug fix
- [ ] refactor: internal change with no behaviour change
- [ ] docs
- [ ] test
- [ ] chore / build / ci

## Testing

<!-- List the checks that you ran. Identify the Kotest specifications and the result of `./gradlew build`. -->

## Checklist

- [ ] Title and commit subjects follow `verb(area): something`
- [ ] Linked the related issue
- [ ] Added or updated tests. Used the `test` matchers where applicable.
- [ ] `./gradlew ktlintCheck spotlessCheck` passes.
- [ ] Public API has KDoc and satisfies `explicitApi()`.
- [ ] Updated `docs/` for user-facing changes.
