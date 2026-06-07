package io.github.lmliam.kotventure.core.architecture

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

class NoServiceLoaderTest :
    StringSpec({
        "production sources do not use ServiceLoader or SPI annotations" {
            val forbiddenTerms =
                listOf(
                    "ServiceLoader",
                    "ServiceContract",
                    "ServiceProvider",
                    "META-INF/services",
                )
            val productionFiles =
                Files.walk(Path("src/main")).use { files ->
                    files
                        .filter { it.isRegularFile() }
                        .filter { it.extension in setOf("kt", "java", "kts") }
                        .toList()
                }

            val offenders =
                productionFiles
                    .filter { file -> forbiddenTerms.any { term -> file.readText().contains(term) } }
                    .map { it.toString() }

            offenders.shouldBeEmpty()
        }
    })
