package io.github.lmliam.kotventure.test.compilation

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
internal fun assertCompiles(
    fileName: String,
    source: String,
) {
    val result =
        KotlinCompilation()
            .apply {
                inheritClassPath = true
                sources = listOf(SourceFile.kotlin(fileName, source))
            }.compile()

    result.exitCode shouldBe KotlinCompilation.ExitCode.OK
}

@OptIn(ExperimentalCompilerApi::class)
internal fun assertDoesNotCompile(
    fileName: String,
    source: String,
    vararg expectedMessages: String,
) {
    val result =
        KotlinCompilation()
            .apply {
                inheritClassPath = true
                sources = listOf(SourceFile.kotlin(fileName, source))
            }.compile()

    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    expectedMessages.forEach { expectedMessage ->
        result.messages shouldContain expectedMessage
    }
}
