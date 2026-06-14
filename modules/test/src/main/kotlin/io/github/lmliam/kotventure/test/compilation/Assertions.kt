package io.github.lmliam.kotventure.test.compilation

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * Asserts that [source] fails to compile, optionally verifying that each of the [expectedMessages] appears in the
 * compiler output.
 */
@OptIn(ExperimentalCompilerApi::class)
public fun assertDoesNotCompile(
    fileName: String,
    source: String,
    vararg expectedMessages: String,
) {
    val compilation =
        KotlinCompilation().apply {
            inheritClassPath = true
            sources = listOf(SourceFile.kotlin(fileName, source))
        }

    val result = compilation.compile()

    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    expectedMessages.forEach { expectedMessage ->
        result.messages shouldContain expectedMessage
    }
}
