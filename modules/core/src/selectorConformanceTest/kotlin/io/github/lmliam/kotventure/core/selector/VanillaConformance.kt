package io.github.lmliam.kotventure.core.selector

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.SharedConstants
import net.minecraft.commands.arguments.selector.EntitySelectorParser
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions
import net.minecraft.server.Bootstrap

/** Asserts that the vanilla parser accepts this selector's rendered source. */
internal fun EntitySelector.shouldBeAcceptedByVanilla() {
    val source = asString()
    vanillaRejection(source)?.let { failure ->
        throw AssertionError(
            "Minecraft Java Edition $minecraftVersion rejected selector " +
                "`$source` at offset ${failure.offset}: ${failure.message}",
            failure.cause,
        )
    }
}

/** Asserts that the vanilla parser rejects this selector source. */
internal fun String.shouldBeRejectedByVanilla() {
    if (vanillaRejection(this) == null) {
        throw AssertionError(
            "Minecraft Java Edition $minecraftVersion accepted intentionally invalid selector `$this`",
        )
    }
}

private val minecraftVersion: String =
    checkNotNull(System.getProperty("kotventure.conformance.minecraftVersion")) {
        "Run the suite through the selectorConformanceTest Gradle task"
    }

private val vanillaParserBootstrap: Unit by lazy {
    SharedConstants.tryDetectVersion()
    Bootstrap.bootStrap()
    EntitySelectorOptions.bootStrap()
}

private fun vanillaRejection(source: String): VanillaRejection? {
    vanillaParserBootstrap
    val reader = StringReader(source)
    try {
        EntitySelectorParser(reader, true).parse()
    } catch (failure: CommandSyntaxException) {
        return VanillaRejection(
            offset = failure.cursor,
            message = failure.rawMessage.string,
            cause = failure,
        )
    }
    if (reader.canRead()) {
        return VanillaRejection(
            offset = reader.cursor,
            message = "Unexpected trailing selector input",
        )
    }
    return null
}

private data class VanillaRejection(
    val offset: Int,
    val message: String,
    val cause: Throwable? = null,
)
