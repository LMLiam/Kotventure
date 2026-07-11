# Vanilla conformance

Kotventure validates DSL output against the real Java Edition implementation in a dedicated,
test-only source set. The selector suite is the first conformance suite; future oracles join the
same source set, task, and workflow.

## Pinned baseline

| Setting | Value |
| --- | --- |
| Java Edition version | `26.2` |
| Official release manifest | `https://piston-meta.mojang.com/mc/game/version_manifest_v2.json` |
| Server bundle SHA-1 | `823e2250d24b3ddac457a60c92a6a941943fcd6a` |
| Required Java version | 25 |

The checksum and matching official download URL are declared together in
[`gradle/vanilla-conformance.gradle`](../gradle/vanilla-conformance.gradle). The task downloads the
bundle directly from Mojang, verifies the manifest checksum before use, and extracts the unobfuscated
server plus its bundled libraries under `modules/core/build/vanilla-conformance/26.2/`.

No Minecraft class appears in `main`, the normal `test` source set, a public signature, a published
POM, or Gradle module metadata.

## Running the suites

```bash
./gradlew :core:vanillaConformanceTest
```

The selector suite bootstraps `net.minecraft.commands.arguments.selector.EntitySelectorParser`,
then checks:

- all six selector heads and every argument emitted by the typed DSL;
- head-specific receiver output, repeated filters, empty presence values, quoting, ranges, maps,
  SNBT, keys, and negation;
- numeric boundary rendering; and
- representative invalid selectors, including failures for head capabilities, ranges, maps, SNBT,
  quoting, and repeated positive type filters.

Failures include the selector, the vanilla parser offset, and its grammar diagnostic. The task is
not part of `check` — the fixture download needs network access to Mojang, which must never gate the
offline-safe build. CI runs it as the **Vanilla conformance** job in
[`ci.yml`](../.github/workflows/ci.yml) on core-path pull requests, pushes, a weekly schedule, and
manual dispatch (see [CI.md](./CI.md)).

## Updating the baseline

1. Find the new release in Mojang's official version manifest and open its version JSON.
2. Update `targetMinecraftVersion` and `serverBundleSha1` in
   `gradle/vanilla-conformance.gradle`. The download URL is derived from that checksum.
3. Confirm the version JSON's required Java major matches the repository toolchain.
4. Re-run `./gradlew :core:vanillaConformanceTest --rerun-tasks`; update the test adapter only if
   Mojang changed the named parser API.
5. Audit the valid and invalid matrices against release grammar changes, then run
   `./gradlew build`.
6. Verify the generated `core` POM and Gradle module metadata still contain no Minecraft dependency
   before merging.
