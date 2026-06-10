package io.github.lmliam.kotventure.core.key

import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

/**
 * Builds an Adventure [Key] from a [namespace] and [value].
 *
 * @throws InvalidKeyException when [namespace] or [value] is not valid for an Adventure key.
 */
public fun key(
    namespace: String,
    value: String,
): Key = Key.key(namespace, value)

/**
 * Builds an Adventure [Key] using this string as the namespace and [value] as the value.
 *
 * @throws InvalidKeyException when this namespace or [value] is not valid for an Adventure key.
 */
public infix fun String.namespace(value: String): Key = Key.key(this, value)

/**
 * Parses this string as an Adventure [Key].
 *
 * Bare values use Adventure's default `minecraft` namespace.
 *
 * @throws InvalidKeyException when this string is not valid for an Adventure key.
 */
public fun String.asKey(): Key = Key.key(this)
