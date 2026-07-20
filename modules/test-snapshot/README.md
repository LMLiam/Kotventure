# `kotventure-test-snapshot`

This module provides snapshot assertions for Adventure `Component` values. It is separate from
[`kotventure-test`](../test/README.md). Thus, matcher-only users do not need serializer and Gson runtime dependencies.

## Getting it

After you import the BOM, add this dependency. Refer to the root README.

```kotlin
dependencies {
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test-snapshot")
}
```

Add `kotventure-test` if the test also needs structural matchers.

## APIs

This module provides exact and compacted snapshot operations:

- `matchSnapshot(name)` and `shouldMatchSnapshot(name)` compare the component without changes.
- `matchCompactedSnapshot(name)` and `shouldMatchCompactedSnapshot(name)` compact the component before comparison.

```kotlin
import io.github.lmliam.kotventure.test.snapshot.shouldMatchSnapshot

component shouldMatchSnapshot "welcome-message"
```

The [`serializer`](../serializer) module converts the component to canonical JSON. The snapshot formatter adds indentation.
Thus, committed snapshots give clear line-by-line differences. A mismatch or a missing snapshot fails with the applicable JSON.

Snapshots live under your test resources at `snapshots/<name>.snapshot.json`:

```text
src/test/resources/snapshots/welcome-message.snapshot.json
```

## Recording and updating

The matcher does not write snapshots without record mode. Enable record mode with a system property or environment variable.
Record mode writes a missing or different snapshot:

| Setting            | System property              | Environment variable | Effect                                                                                                                                         |
|--------------------|------------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Record mode        | `kotventure.snapshot.update` | `SNAPSHOT_UPDATE`    | When `true`/`1`/`yes` (case-insensitive), writes/updates the snapshot and passes instead of failing                                            |
| Snapshot directory | `kotventure.snapshot.dir`    | `SNAPSHOT_DIR`       | Reads and writes snapshots directly at `<dir>/<name>.snapshot.json` (no `snapshots/` subfolder), replacing the default test-resources location |

```bash
# Review the diff first; then record intentional changes:
SNAPSHOT_UPDATE=true ./gradlew :your-module:test
```

Matcher factories have no side effects. In record mode, only positive `shouldMatch…` assertions write snapshots.
Negated and composed matcher evaluations do not write snapshots.
