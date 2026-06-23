package io.github.lmliam.kotventure.test.snapshot

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import java.nio.file.Path

class SnapshotFilesTest :
    StringSpec(
        {
            "derives the source snapshot directory from a standard build-resources path" {
                sourceSnapshotDir(
                    Path.of("/repo", "module", "build", "resources", "test", "snapshots", "a.snapshot.json"),
                ) shouldBe Path.of("/repo", "module", "src", "test", "resources", "snapshots")
            }

            "handles a build-resources path sitting at the filesystem root" {
                sourceSnapshotDir(
                    Path.of("/build", "resources", "test", "snapshots", "x.snapshot.json"),
                ) shouldBe Path.of("/src", "test", "resources", "snapshots")
            }

            "returns null when the path is not under build/resources/test/snapshots" {
                sourceSnapshotDir(Path.of("/somewhere", "else", "a.snapshot.json")) shouldBe null
                sourceSnapshotDir(Path.of("/a", "b", "c", "d", "e.snapshot.json")) shouldBe null
            }

            "places a brand-new snapshot under the module test resources" {
                withSnapshotProperties {
                    val path = resolveSnapshotWritePath("never-recorded-fixture")

                    path.isAbsolute shouldBe true
                    path.toString() shouldEndWith
                        Path
                            .of("src", "test", "resources", "snapshots", "never-recorded-fixture.snapshot.json")
                            .toString()
                }
            }

            "writes directly under the override directory without a snapshots subfolder" {
                val dir = Path.of("/tmp", "custom-snaps")

                withSnapshotProperties(dir = dir.toString()) {
                    resolveSnapshotWritePath("x") shouldBe dir.resolve("x.snapshot.json")
                }
            }

            "rejects snapshot names that escape the snapshot directory" {
                shouldThrow<IllegalArgumentException> {
                    resolveSnapshotWritePath("../outside")
                }
                shouldThrow<IllegalArgumentException> {
                    resolveSnapshotWritePath("nested/../../outside")
                }
                shouldThrow<IllegalArgumentException> {
                    resolveSnapshotWritePath("/absolute")
                }
            }
        },
    )
