# Vanilla conformance

Kotventure validates DSL output against the Java Edition implementation. A dedicated test source set contains this work.
The selector suite is the first conformance suite. Add subsequent reference tests to the same source set, task, and workflow.

## Pinned baseline

| Setting | Value |
| --- | --- |
| Java Edition version | `26.2` |
| Official release manifest | `https://piston-meta.mojang.com/mc/game/version_manifest_v2.json` |
| Server bundle SHA-1 | `823e2250d24b3ddac457a60c92a6a941943fcd6a` |
| Repository-reviewed server bundle SHA-256 | `cdacdfb25898de5e4b4b0e5ddcc2722f77067e46605709c2d886c000ebb63ec5` |
| Required Java version | 25 |

[`gradle/vanilla-conformance.gradle`](../gradle/vanilla-conformance.gradle) contains the official download URL and both checksums.
The SHA-1 is Mojang's object identity. The SHA-256 is Kotventure's independently reviewed defence-in-depth pin.
The task downloads the bundle through bounded HTTPS, verifies both digests, and then extracts the unobfuscated server and libraries.
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
2. Update `targetMinecraftVersion` and `serverBundleSha1` in `gradle/vanilla-conformance.gradle`. The SHA-1 determines the download URL.
3. Download the bundle through the repository's bounded HTTPS path. Confirm that its SHA-1 matches Mojang's object checksum.
4. Calculate the SHA-256 locally from those same verified bytes with `shasum -a 256 <server-bundle>` and record the command and result in the pull request.
5. Update `serverBundleSha256` with the reviewed lower-case value. Review the version, source URL, SHA-1, and SHA-256 together.
6. Confirm that the required Java major version agrees with the repository toolchain.
7. Run `./gradlew :core:vanillaConformanceTest --rerun-tasks` again.
8. Update the test adapter only if Mojang changed the named parser API.
9. Compare the valid and invalid matrices with the release grammar changes. Then, run `./gradlew build`.
10. Confirm that the `core` POM and module metadata contain no Minecraft dependency. Then, merge the change.
