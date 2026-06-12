package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.ComponentLike
import kotlin.jvm.javaObjectType

/**
 * A typed MiniMessage placeholder descriptor.
 *
 * The type parameter controls which values can be bound with [MiniMessageResolverScope.resolve].
 */
public class MiniMessagePlaceholder<T : Any>
    @PublishedApi
    internal constructor(
        public val name: String,
        internal val strategy: MiniMessagePlaceholderStrategy,
    )

/**
 * Creates a typed MiniMessage placeholder descriptor named [name].
 *
 * Supported value families are [ComponentLike], [String], [Number], and [Boolean]. String, number, and boolean
 * placeholders bind as literal text; use [MiniMessageResolverScope.parsed] for markup-bearing string substitutions.
 */
public inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
    MiniMessagePlaceholder(name, miniMessagePlaceholderStrategy<T>())

@PublishedApi
internal enum class MiniMessagePlaceholderStrategy {
    COMPONENT,
    LITERAL,
}

@PublishedApi
internal inline fun <reified T : Any> miniMessagePlaceholderStrategy(): MiniMessagePlaceholderStrategy =
    when {
        ComponentLike::class.java.isAssignableFrom(T::class.javaObjectType) -> MiniMessagePlaceholderStrategy.COMPONENT
        String::class.java.isAssignableFrom(T::class.javaObjectType) -> MiniMessagePlaceholderStrategy.LITERAL
        Number::class.java.isAssignableFrom(T::class.javaObjectType) -> MiniMessagePlaceholderStrategy.LITERAL
        Boolean::class.javaObjectType.isAssignableFrom(
            T::class.javaObjectType,
        ) -> MiniMessagePlaceholderStrategy.LITERAL
        else ->
            error(
                "Supported MiniMessage placeholder types are ComponentLike, String, Number, and Boolean; " +
                    "received ${T::class.qualifiedName}.",
            )
    }
