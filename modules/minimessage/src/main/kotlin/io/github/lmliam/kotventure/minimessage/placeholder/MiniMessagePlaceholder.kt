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
 * An immutable typed descriptor for one MiniMessage placeholder.
 *
 * The type parameter controls the values that [MiniMessageResolverScope.resolve] and
 * [bind][io.github.lmliam.kotventure.minimessage.template.bind] accept. Two descriptors are equal when they have the
 * same name and boxed JVM value type. Template binding also checks that the descriptor is the same instance that the
 * template declared.
 *
 * @property name The MiniMessage tag name that this placeholder resolves.
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

    /** Returns `true` when [other] has the same [name] and boxed JVM value type. */
    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is MiniMessagePlaceholder<*> && name == other.name && type == other.type)

    /** Returns a hash code for [name] and the boxed JVM value type. */
    override fun hashCode(): Int = 31 * name.hashCode() + type.hashCode()
}

/**
 * Creates a typed MiniMessage placeholder descriptor with [name].
 *
 * [T] must be [ComponentLike], [String], [Number], [Boolean], or a subtype of one of those types. Components retain
 * their structure. Strings, numbers, and booleans bind as literal text. Use [MiniMessageResolverScope.parsed] for a
 * string replacement that contains markup.
 *
 * @throws IllegalArgumentException when [T] is unsupported or [name] does not match `[!?#]?[a-z0-9_-]+`.
 */
public inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
    MiniMessagePlaceholder(name, T::class.javaObjectType, miniMessagePlaceholderStrategy<T>())
