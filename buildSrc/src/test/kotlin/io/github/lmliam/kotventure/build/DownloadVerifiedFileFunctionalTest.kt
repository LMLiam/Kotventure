package io.github.lmliam.kotventure.build

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class DownloadVerifiedFileFunctionalTest :
    StringSpec(
        {
            "revalidates an output instead of reporting it up to date" {
                withTemporaryDirectory { directory ->
                    val destination = directory.resolve("build/fixture.jar")
                    Files.createDirectories(destination.parent)
                    Files.writeString(destination, "fixture bytes")
                    copyBuildLogic(directory)
                    writeBuildFiles(directory, sha1(destination))

                    val first = runGradle(directory)
                    first.task(":download")?.outcome shouldBe TaskOutcome.SUCCESS

                    val second = runGradle(directory)
                    second.task(":download")?.outcome shouldBe TaskOutcome.SUCCESS
                    Files.readString(destination) shouldBe "fixture bytes"
                }
            }

            "preserves a destination that cannot be read" {
                withTemporaryDirectory { directory ->
                    val destination = directory.resolve("build/fixture.jar")
                    Files.createDirectories(destination)
                    copyBuildLogic(directory)
                    writeBuildFiles(directory, "0".repeat(40))

                    runGradleAndExpectFailure(directory)

                    Files.isDirectory(destination) shouldBe true
                }
            }
        },
    )

private fun runGradle(directory: Path) =
    GradleRunner.create()
        .withProjectDir(directory.toFile())
        .withArguments("download")
        .forwardOutput()
        .build()

private fun runGradleAndExpectFailure(directory: Path) =
    GradleRunner.create()
        .withProjectDir(directory.toFile())
        .withArguments("download")
        .forwardOutput()
        .buildAndFail()

private fun copyBuildLogic(directory: Path) {
    val sourceRoot = sequenceOf(
        Path.of("src/main/groovy"),
        Path.of("buildSrc/src/main/groovy"),
    ).first { Files.exists(it) }
    val buildLogicRoot = directory.resolve("buildSrc")
    Files.createDirectories(buildLogicRoot.resolve("src/main/groovy"))
    Files.walk(sourceRoot).use { paths ->
        paths.filter { path -> path.toString().endsWith(".groovy") }.forEach { source ->
        val relativePath = sourceRoot.relativize(source)
        val destination = buildLogicRoot.resolve("src/main/groovy").resolve(relativePath.toString())
        Files.createDirectories(destination.parent)
        Files.copy(source, destination)
        }
    }
    Files.writeString(
        buildLogicRoot.resolve("build.gradle"),
        """
        plugins {
            id 'groovy'
        }

        dependencies {
            implementation gradleApi()
        }
        """.trimIndent(),
    )
}

private fun writeBuildFiles(directory: Path, expectedSha1: String) {
    Files.writeString(directory.resolve("settings.gradle"), "rootProject.name = 'fixture-test'\n")
    Files.writeString(
        directory.resolve("build.gradle"),
        """
        import io.github.lmliam.kotventure.build.DownloadVerifiedFile

        tasks.register('download', DownloadVerifiedFile) {
            sourceUrl.set('https://fixture.test/fixture')
            expectedSha1.set('$expectedSha1')
            destination.set(layout.buildDirectory.file('fixture.jar'))
        }
        """.trimIndent(),
    )
}

private fun sha1(path: Path): String {
    val digest = MessageDigest.getInstance("SHA-1")
    Files.newInputStream(path).use { input ->
        val buffer = ByteArray(8 * 1024)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) {
                break
            }
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { byte -> (byte.toInt() and 0xff).toString(16).padStart(2, '0') }
}

private inline fun withTemporaryDirectory(block: (Path) -> Unit) {
    val directory = Files.createTempDirectory("kotventure-verified-file-functional-")
    try {
        block(directory)
    } finally {
        directory.toFile().deleteRecursively()
    }
}
