# `kotventure-test-snapshot`

Snapshot assertions for Adventure `Component`s. This module is published separately from
[`kotventure-test`](../test/README.md) so whole-message golden tests do not force matcher-only consumers to pull
serializer and Gson runtime dependencies.

## Getting it

With the BOM imported (see the root README), add:

```kotlin
dependencies {
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test-snapshot")
}
```

Add `kotventure-test` alongside it when the test also needs structural matchers.

## APIs

This module exposes both exact and explicitly compacted snapshot operations:

- `matchSnapshot(name)` / `shouldMatchSnapshot(name)` compare the component as-is.
- `matchCompactedSnapshot(name)` / `shouldMatchCompactedSnapshot(name)` compact first, then compare.

```kotlin
import io.github.lmliam.kotventure.test.snapshot.shouldMatchSnapshot

component shouldMatchSnapshot "welcome-message"
```

The component is serialized to canonical JSON via the [`serializer`](../serializer) module and then pretty-printed so
committed snapshots produce reviewable line-by-line diffs. A mismatch, or a snapshot that has never been recorded,
fails the test with the offending JSON.

Snapshots live under your test resources at `snapshots/<name>.snapshot.json`:

```text
src/test/resources/snapshots/welcome-message.snapshot.json
```

## Recording and updating

Snapshots are never written silently. A missing or differing snapshot is only recorded when record mode is on, which is
opt-in via a system property or environment variable:

| Setting            | System property              | Environment variable | Effect                                                                                                                                         |
|--------------------|------------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Record mode        | `kotventure.snapshot.update` | `SNAPSHOT_UPDATE`    | When `true`/`1`/`yes` (case-insensitive), writes/updates the snapshot and passes instead of failing                                            |
| Snapshot directory | `kotventure.snapshot.dir`    | `SNAPSHOT_DIR`       | Reads and writes snapshots directly at `<dir>/<name>.snapshot.json` (no `snapshots/` subfolder), replacing the default test-resources location |

```bash
# Review the diff first; then record intentional changes:
SNAPSHOT_UPDATE=true ./gradlew :your-module:test
```

The matcher factory surface is intentionally pure: only the positive `shouldMatch…` assertions write in record mode,
so negated and composed matcher evaluations remain side-effect free.
