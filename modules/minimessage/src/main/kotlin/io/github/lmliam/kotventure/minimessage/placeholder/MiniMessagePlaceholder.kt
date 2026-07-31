package io.github.lmliam.kotventure.minimessage.placeholder

import net.kyori.adventure.text.ComponentLike
import kotlin.jvm.javaObjectType
import kotlin.reflect.KClass

/**
 * An immutable typed descriptor for one MiniMessage placeholder.
 *
 * The type parameter controls the values that [MiniMessageResolverScope.resolve] and
 * [bind][io.github.lmliam.kotventure.minimessage.template.bind] accept. Two descriptors are equal when they have the
 * same name and runtime value type. Template binding additionally requires the exact descriptor instance declared by
 * the template.
 *
 * @property name The MiniMessage tag name that this placeholder resolves.
 */
public class MiniMessagePlaceholder<T : Any> @PublishedApi internal constructor(
    public val name: String,
    internal val valueType: KClass<T>,
) {
    internal val acceptsComponents: Boolean =
        valueType.isComponentPlaceholderType

    init {
        name.requireValidMiniMessageTagName()

        require(valueType.isSupportedPlaceholderType()) {
            "Supported MiniMessage placeholder types are ComponentLike, String, Number, and Boolean; " +
                    "received ${valueType.displayName}."
        }
    }

    /** Returns `true` when [other] has the same [name] and runtime value type. */
    override fun equals(other: Any?): Boolean =
        (this === other) ||
                (
                        (other is MiniMessagePlaceholder<*>) &&
                                (name == other.name) &&
                                (valueType == other.valueType)
                        )

    /** Returns a hash code for [name] and the runtime value type. */
    override fun hashCode(): Int = 31 * name.hashCode() + valueType.hashCode()
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
    MiniMessagePlaceholder(
        name = name,
        valueType = T::class,
    )

private val KClass<*>.isComponentPlaceholderType: Boolean
    get() =
        ComponentLike::class.java.isAssignableFrom(javaObjectType)

private fun KClass<*>.isSupportedPlaceholderType(): Boolean {
    val javaType = javaObjectType

    return isComponentPlaceholderType ||
            javaType == String::class.java ||
            Number::class.java.isAssignableFrom(javaType) ||
            javaType == Boolean::class.javaObjectType
}

internal val KClass<*>.displayName: String
    get() = qualifiedName ?: simpleName ?: toString()
