@file:OptIn(ExperimentalCompilerApi::class)

package io.github.lmliam.kotventure.test.compilation

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

internal fun assertCompiles(
    fileName: String,
    source: String,
) {
    val result = compile(fileName, source)

    withClue(result.messages) {
        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
}

internal fun assertDoesNotCompile(
    fileName: String,
    source: String,
    vararg expectedMessages: String,
) {
    val result = compile(fileName, source)

    withClue(result.messages) {
        result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
        expectedMessages.forEach { expectedMessage ->
            result.messages shouldContain expectedMessage
        }
    }
}

private fun compile(
    fileName: String,
    source: String,
): JvmCompilationResult =
    KotlinCompilation()
        .apply {
            inheritClassPath = true
            sources = listOf(SourceFile.kotlin(fileName, source))
        }.compile()
