package io.github.lmliam.kotventure.build

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class DownloadVerifiedFileDigestTest :
    StringSpec(
        {
            "accepts a file when both expected digests match" {
                withTemporaryFile { path ->
                    Files.writeString(path, "fixture bytes")

                    DownloadVerifiedFile.matchesExpectedDigests(
                        path,
                        "72f3d43d19a345ea7ceff688eb4cde9b323bf8e4",
                        "06ef5892a96037b4eaac8d9b3dd5ee8765933a1f199b36cea53d2a163e091ae0",
                    ) shouldBe true
                }
            }

            "rejects a file when its SHA-1 does not match" {
                withTemporaryFile { path ->
                    Files.writeString(path, "fixture bytes")

                    DownloadVerifiedFile.matchesExpectedDigests(
                        path,
                        "0000000000000000000000000000000000000000",
                        "06ef5892a96037b4eaac8d9b3dd5ee8765933a1f199b36cea53d2a163e091ae0",
                    ) shouldBe false
                }
            }

            "rejects a file when its SHA-256 does not match" {
                withTemporaryFile { path ->
                    Files.writeString(path, "fixture bytes")

                    DownloadVerifiedFile.matchesExpectedDigests(
                        path,
                        "72f3d43d19a345ea7ceff688eb4cde9b323bf8e4",
                        "0000000000000000000000000000000000000000000000000000000000000000",
                    ) shouldBe false
                }
            }
        },
    )

private inline fun withTemporaryFile(block: (java.nio.file.Path) -> Unit) {
    val directory = Files.createTempDirectory("kotventure-fixture-digests-")
    val path = directory.resolve("fixture.jar")
    try {
        block(path)
    } finally {
        directory.toFile().deleteRecursively()
    }
}
