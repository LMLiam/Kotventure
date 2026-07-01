package io.github.lmliam.kotventure.core.selector

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.SharedConstants
import net.minecraft.commands.arguments.selector.EntitySelectorParser
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions
import net.minecraft.server.Bootstrap

internal object VanillaSelectorGrammar {
    private const val MINECRAFT_VERSION = "26.2"

    private val initialized: Unit by lazy {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        EntitySelectorOptions.bootStrap()
    }

    fun shouldAccept(selectors: List<EntitySelector>) {
        selectors.forEach { selector ->
            val source = selector.asString()
            rejection(source)?.let { failure ->
                throw AssertionError(
                    "Minecraft Java Edition $MINECRAFT_VERSION rejected selector " +
                        "`$source` at offset ${failure.offset}: ${failure.message}",
                    failure.cause,
                )
            }
        }
    }

    fun shouldReject(vararg selectors: String) {
        selectors.forEach { source ->
            if (rejection(source) == null) {
                throw AssertionError(
                    "Minecraft Java Edition $MINECRAFT_VERSION accepted intentionally invalid " +
                        "selector `$source`",
                )
            }
        }
    }

    private fun rejection(source: String): VanillaSelectorRejection? {
        initialized
        val reader = StringReader(source)
        try {
            EntitySelectorParser(reader, true).parse()
        } catch (failure: CommandSyntaxException) {
            return VanillaSelectorRejection(
                offset = failure.cursor,
                message = failure.rawMessage.string,
                cause = failure,
            )
        }
        if (reader.canRead()) {
            return VanillaSelectorRejection(
                offset = reader.cursor,
                message = "Unexpected trailing selector input",
            )
        }
        return null
    }
}

private data class VanillaSelectorRejection(
    val offset: Int,
    val message: String,
    val cause: Throwable? = null,
)
