package io.github.lmliam.kotventure.core.selector

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.kotest.assertions.fail
import net.minecraft.SharedConstants
import net.minecraft.commands.arguments.selector.EntitySelectorParser
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions
import net.minecraft.server.Bootstrap

/**
 * Test helpers asserting vanilla Minecraft selector parse behaviour.
 *
 * These helpers are intentionally small and throw test failures via Kotest's [fail].
 */
internal fun EntitySelector.shouldBeAcceptedByVanilla() {
    val source = asString()
    vanillaRejection(source)?.let { failure ->
        val base =
            "Minecraft Java Edition $minecraftVersion rejected selector `$source` " +
                    "at offset ${failure.offset}: ${failure.message}"
        val message =
            failure.cause?.let { cause ->
                "$base (cause: ${cause::class.simpleName}: ${cause.message})"
            } ?: base
        fail(message)
    }
}

/** Asserts that the vanilla parser rejects this selector source. */
internal fun String.shouldBeRejectedByVanilla() =
    if (vanillaRejection(this) == null) {
        fail("Minecraft Java Edition $minecraftVersion accepted intentionally invalid selector `$this`")
    } else {
        Unit
    }

/** Minecraft version used in assertion messages; must be provided by the test runner. */
private val minecraftVersion: String =
    requireNotNull(
        System.getProperty("kotventure.conformance.minecraftVersion"),
    ) {
        "Run the suite through the vanillaConformanceTest Gradle task"
    }

/** Ensure Minecraft/Brigadier parser bootstrapping runs once on first use. */
private val vanillaParserBootstrap: Unit by lazy {
    SharedConstants.tryDetectVersion()
    Bootstrap.bootStrap()
    EntitySelectorOptions.bootStrap()
}

/**
 * Returns a [VanillaRejection] describing why the vanilla parser rejected [source],
 * or `null` when the parser accepts the source.
 */
private fun vanillaRejection(source: String): VanillaRejection? {
    vanillaParserBootstrap
    val reader = StringReader(source)

    val parseFailure =
        runCatching {
            EntitySelectorParser(reader, true).parse()
        }.exceptionOrNull() as? CommandSyntaxException

    return when {
        parseFailure != null ->
            VanillaRejection(
                offset = parseFailure.cursor,
                message = parseFailure.rawMessage.string,
                cause = parseFailure,
            )

        reader.canRead() ->
            VanillaRejection(
                offset = reader.cursor,
                message = "Unexpected trailing selector input",
            )

        else -> null
    }
}

private data class VanillaRejection(
    val offset: Int,
    val message: String,
    val cause: Throwable? = null,
)
