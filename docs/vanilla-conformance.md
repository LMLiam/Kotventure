# Vanilla conformance

Kotventure validates DSL output against the Java Edition implementation. A dedicated test source set contains this work.
The selector suite is the first conformance suite. Add subsequent reference tests to the same source set, task, and workflow.

## Pinned baseline

| Setting | Value |
| --- | --- |
| Java Edition version | `26.2` |
| Official release manifest | `https://piston-meta.mojang.com/mc/game/version_manifest_v2.json` |
| Server bundle SHA-1 | `823e2250d24b3ddac457a60c92a6a941943fcd6a` |
| Required Java version | 25 |

[`gradle/vanilla-conformance.gradle`](../gradle/vanilla-conformance.gradle) contains the checksum and official download URL.
The task downloads the bundle from Mojang and checks its manifest checksum. Then, it extracts the unobfuscated server and libraries.
The output directory is `modules/core/build/vanilla-conformance/26.2/`.

Minecraft classes do not occur in `main`, the normal `test` source set, a public signature, a published POM, or module metadata.

## Running the suites

```bash
./gradlew :core:vanillaConformanceTest
```

The selector suite starts `net.minecraft.commands.arguments.selector.EntitySelectorParser`. Then, it checks:

- All six selector heads and each argument that the typed DSL emits.
- Head-specific output, repeated filters, empty presence values, quotation, ranges, maps, SNBT, keys, and negation.
- Numeric boundary rendering.
- Representative invalid selectors for capabilities, ranges, maps, SNBT, quotation, and repeated positive type filters.

A failure includes the selector, vanilla parser offset, and grammar diagnostic. The task is not part of `check`.
The fixture download needs access to Mojang. It must not prevent the offline build.
CI runs the **Vanilla conformance** job for core PRs, pushes, the weekly schedule, and manual requests.
Refer to [`ci.yml`](../.github/workflows/ci.yml) and [CI.md](./CI.md).

## Updating the baseline

1. Find the new release in the official Mojang version manifest. Open its version JSON.
2. Update `targetMinecraftVersion` and `serverBundleSha1` in `gradle/vanilla-conformance.gradle`.
   The checksum determines the download URL.
3. Confirm that the required Java major version agrees with the repository toolchain.
4. Run `./gradlew :core:vanillaConformanceTest --rerun-tasks` again.
5. Update the test adapter only if Mojang changed the named parser API.
6. Compare the valid and invalid matrices with the release grammar changes. Then, run `./gradlew build`.
7. Confirm that the `core` POM and module metadata contain no Minecraft dependency. Then, merge the change.
