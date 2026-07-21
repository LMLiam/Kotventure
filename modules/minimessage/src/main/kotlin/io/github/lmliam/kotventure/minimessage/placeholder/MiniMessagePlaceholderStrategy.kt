package io.github.lmliam.kotventure.minimessage.placeholder

import net.kyori.adventure.text.ComponentLike
import kotlin.jvm.javaObjectType

@PublishedApi
internal enum class MiniMessagePlaceholderStrategy {
    COMPONENT,
    LITERAL,
}

@PublishedApi
internal inline fun <reified T : Any> miniMessagePlaceholderStrategy(): MiniMessagePlaceholderStrategy {
    val valueType = T::class.javaObjectType

    return when {
        ComponentLike::class.javaObjectType.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.COMPONENT
        String::class.javaObjectType.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.LITERAL
        Number::class.javaObjectType.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.LITERAL
        Boolean::class.javaObjectType.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.LITERAL
        else ->
            throw IllegalArgumentException(
                "Supported MiniMessage placeholder types are ComponentLike, String, Number, and Boolean; " +
                        "received ${T::class.qualifiedName}.",
            )
    }
}
