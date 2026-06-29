package io.github.lmliam.kotventure.minimessage

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
internal fun compileGeneratedDsl(source: String): Component {
    val generated =
        """
        package io.github.lmliam.kotventure.minimessage.generated

        import io.github.lmliam.kotventure.core.color.*
        import io.github.lmliam.kotventure.core.component.component
        import io.github.lmliam.kotventure.core.event.click
        import io.github.lmliam.kotventure.core.event.hover
        import io.github.lmliam.kotventure.core.event.removed
        import io.github.lmliam.kotventure.core.key.key
        import io.github.lmliam.kotventure.core.keybind.keybind
        import io.github.lmliam.kotventure.core.nbt.blockNbt
        import io.github.lmliam.kotventure.core.nbt.blockPos
        import io.github.lmliam.kotventure.core.nbt.entityNbt
        import io.github.lmliam.kotventure.core.nbt.nbt
        import io.github.lmliam.kotventure.core.nbt.nbtPath
        import io.github.lmliam.kotventure.core.nbt.storageNbt
        import io.github.lmliam.kotventure.core.objectcomponent.display
        import io.github.lmliam.kotventure.core.objectcomponent.sprite
        import io.github.lmliam.kotventure.core.objectcomponent.head
        import io.github.lmliam.kotventure.core.score.score
        import io.github.lmliam.kotventure.core.selector.entitySelector
        import io.github.lmliam.kotventure.core.selector.selector
        import io.github.lmliam.kotventure.core.text.text
        import io.github.lmliam.kotventure.core.translatable.translatable
        import io.github.lmliam.kotventure.core.uuid.uuid
        import net.kyori.adventure.text.Component
        import net.kyori.adventure.text.format.NamedTextColor
        import net.kyori.adventure.text.format.TextColor
        import net.kyori.adventure.text.format.TextDecoration

        fun renderGenerated(): Component =
        ${source.prependIndent("    ")}
        """.trimIndent()

    val result =
        KotlinCompilation()
            .apply {
                inheritClassPath = true
                sources = listOf(SourceFile.kotlin("GeneratedDsl.kt", generated))
            }.compile()

    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

    val generatedClass =
        result.classLoader.loadClass("io.github.lmliam.kotventure.minimessage.generated.GeneratedDslKt")
    return generatedClass.getMethod("renderGenerated").invoke(null) as Component
}
