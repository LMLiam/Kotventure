package io.github.lmliam.kotventure.test.snapshot

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import java.nio.file.Path

class SnapshotFilesTest :
    StringSpec(
        {
            afterTest {
                System.clearProperty(SnapshotConfig.DIR_PROPERTY)
            }

            "rewrites a build-resources path back onto the source tree" {
                rewriteBuildPathToSource("/repo/module/build/resources/test/snapshots/a.snapshot.json") shouldBe
                    "/repo/module/src/test/resources/snapshots/a.snapshot.json"
            }

            "leaves a path without the build-resources segment unchanged" {
                rewriteBuildPathToSource("/somewhere/else/a.snapshot.json") shouldBe "/somewhere/else/a.snapshot.json"
            }

            "places a brand-new snapshot under the module test resources" {
                val path = resolveSnapshotWritePath("never-recorded-fixture")

                path.isAbsolute shouldBe true
                path.toString() shouldEndWith
                    Path
                        .of("src", "test", "resources", "snapshots", "never-recorded-fixture.snapshot.json")
                        .toString()
            }

            "writes directly under the override directory without a snapshots subfolder" {
                System.setProperty(SnapshotConfig.DIR_PROPERTY, Path.of("/tmp", "custom-snaps").toString())

                resolveSnapshotWritePath("x").toString() shouldBe
                    Path.of("/tmp", "custom-snaps", "x.snapshot.json").toString()
            }
        },
    )
