package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.ComponentLike
import kotlin.jvm.javaObjectType

@PublishedApi
internal enum class MiniMessagePlaceholderStrategy {
    COMPONENT,
    LITERAL,
}

@PublishedApi
internal inline fun <reified T : Any> miniMessagePlaceholderStrategy(): MiniMessagePlaceholderStrategy {
    // Box Kotlin primitive types so Int/Double/Boolean classify with their JVM wrapper families.
    val valueType = T::class.javaObjectType

    return when {
        ComponentLike::class.java.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.COMPONENT
        String::class.java.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.LITERAL
        Number::class.java.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.LITERAL
        Boolean::class.javaObjectType.isAssignableFrom(valueType) -> MiniMessagePlaceholderStrategy.LITERAL
        else ->
            throw IllegalArgumentException(
                "Supported MiniMessage placeholder types are ComponentLike, String, Number, and Boolean; " +
                    "received ${T::class.qualifiedName}.",
            )
    }
}
