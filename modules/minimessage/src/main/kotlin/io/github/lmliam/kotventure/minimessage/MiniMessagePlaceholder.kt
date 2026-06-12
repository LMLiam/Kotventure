package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.ComponentLike

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
    ) {
        override fun equals(other: Any?): Boolean =
            this === other ||
                other is MiniMessagePlaceholder<*> &&
                name == other.name &&
                strategy == other.strategy

        override fun hashCode(): Int = 31 * name.hashCode() + strategy.hashCode()
    }

/**
 * Creates a typed MiniMessage placeholder descriptor named [name].
 *
 * Supported value families are [ComponentLike], [String], [Number], and [Boolean]. String, number, and boolean
 * placeholders bind as literal text; use [MiniMessageResolverScope.parsed] for markup-bearing string substitutions.
 *
 * @throws IllegalArgumentException when [T] is outside the supported value families.
 */
public inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
    MiniMessagePlaceholder(name, miniMessagePlaceholderStrategy<T>())
