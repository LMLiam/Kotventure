package io.github.lmliam.kotventure.build

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class VerifiedFileReplacementTest :
    StringSpec(
        {
            "uses an atomic replacement when the file system supports it" {
                withTemporaryDirectory { directory ->
                    val temporary = directory.resolve("download.part")
                    val destination = directory.resolve("fixture.jar")
                    Files.writeString(temporary, "new fixture")
                    Files.writeString(destination, "old fixture")
                    val calls = mutableListOf<List<CopyOption>>()
                    val moveOperation = recordingMove(calls)

                    VerifiedFileReplacement.replace(temporary, destination, moveOperation)

                    calls shouldBe listOf(
                        listOf(StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING),
                    )
                    Files.readString(destination) shouldBe "new fixture"
                }
            }

            "falls back only when atomic replacement is unsupported" {
                withTemporaryDirectory { directory ->
                    val temporary = directory.resolve("download.part")
                    val destination = directory.resolve("fixture.jar")
                    Files.writeString(temporary, "new fixture")
                    Files.writeString(destination, "old fixture")
                    val calls = mutableListOf<List<CopyOption>>()
                    val moveOperation = object : FileMoveOperation {
                        override fun move(source: Path, target: Path, vararg options: CopyOption): Path {
                            calls += options.toList()
                            if (options.contains(StandardCopyOption.ATOMIC_MOVE)) {
                                throw AtomicMoveNotSupportedException(source.toString(), target.toString(), "test")
                            }
                            return Files.move(source, target, *options)
                        }
                    }

                    VerifiedFileReplacement.replace(temporary, destination, moveOperation)

                    calls shouldBe listOf(
                        listOf(StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING),
                        listOf(StandardCopyOption.REPLACE_EXISTING),
                    )
                    Files.readString(destination) shouldBe "new fixture"
                }
            }

            "does not treat an unsupported operation as an atomic move limitation" {
                withTemporaryDirectory { directory ->
                    val temporary = directory.resolve("download.part")
                    val destination = directory.resolve("fixture.jar")
                    Files.writeString(temporary, "new fixture")
                    Files.writeString(destination, "old fixture")
                    val moveOperation = object : FileMoveOperation {
                        override fun move(source: Path, target: Path, vararg options: CopyOption): Path {
                            throw UnsupportedOperationException("test")
                        }
                    }

                    shouldThrow<UnsupportedOperationException> {
                        VerifiedFileReplacement.replace(temporary, destination, moveOperation)
                    }

                    Files.readString(destination) shouldBe "old fixture"
                }
            }

            "does not treat an I/O failure as an atomic move limitation" {
                withTemporaryDirectory { directory ->
                    val temporary = directory.resolve("download.part")
                    val destination = directory.resolve("fixture.jar")
                    Files.writeString(temporary, "new fixture")
                    Files.writeString(destination, "old fixture")
                    val moveOperation = object : FileMoveOperation {
                        override fun move(source: Path, target: Path, vararg options: CopyOption): Path {
                            throw IOException("test")
                        }
                    }

                    shouldThrow<IOException> {
                        VerifiedFileReplacement.replace(temporary, destination, moveOperation)
                    }

                    Files.readString(destination) shouldBe "old fixture"
                }
            }
        },
    )

private fun recordingMove(calls: MutableList<List<CopyOption>>): FileMoveOperation =
    object : FileMoveOperation {
        override fun move(source: Path, target: Path, vararg options: CopyOption): Path {
            calls += options.toList()
            return Files.move(source, target, *options)
        }
    }

private inline fun withTemporaryDirectory(block: (Path) -> Unit) {
    val directory = Files.createTempDirectory("kotventure-fixture-replacement-")
    try {
        block(directory)
    } finally {
        directory.toFile().deleteRecursively()
    }
}
