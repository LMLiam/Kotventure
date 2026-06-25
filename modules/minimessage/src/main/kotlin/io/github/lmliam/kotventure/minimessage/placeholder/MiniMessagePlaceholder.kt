package io.github.lmliam.kotventure.minimessage.placeholder

import net.kyori.adventure.text.ComponentLike
import kotlin.jvm.javaObjectType

internal val MINI_MESSAGE_TAG_NAME_REGEX: Regex = Regex("[!?#]?[a-z0-9_-]+")

internal fun requireValidMiniMessageTagName(name: String) {
    require(MINI_MESSAGE_TAG_NAME_REGEX.matches(name)) {
        "MiniMessage placeholder names must match ${MINI_MESSAGE_TAG_NAME_REGEX.pattern}; received '$name'."
    }
}

/**
 * A typed MiniMessage placeholder descriptor.
 *
 * The type parameter controls which values can be bound with [MiniMessageResolverScope.resolve]. Two descriptors are
 * equal when they share both name and value type.
 */
public class MiniMessagePlaceholder<T : Any>
@PublishedApi
internal constructor(
    public val name: String,
    internal val type: Class<T>,
    internal val strategy: MiniMessagePlaceholderStrategy,
) {
    init {
        requireValidMiniMessageTagName(name)
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is MiniMessagePlaceholder<*> && name == other.name && type == other.type)

    override fun hashCode(): Int = 31 * name.hashCode() + type.hashCode()
}

/**
 * Creates a typed MiniMessage placeholder descriptor named [name].
 *
 * Supported value families are [ComponentLike], [String], [Number], and [Boolean]. String, number, and boolean
 * placeholders bind as literal text; use [MiniMessageResolverScope.parsed] for markup-bearing string substitutions.
 *
 * @throws IllegalArgumentException when [T] is outside the supported value families or [name] is not a valid
 *   MiniMessage tag name.
 */
public inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
    // Box Kotlin primitive types so Int/Double/Boolean compare equal across creation sites.
    MiniMessagePlaceholder(name, T::class.javaObjectType, miniMessagePlaceholderStrategy<T>())
