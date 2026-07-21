package io.github.lmliam.kotventure.core.key

import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

/**
 * Creates an Adventure [Key] from separate [namespace] and [value] strings.
 *
 * @throws InvalidKeyException when [namespace] or [value] is not valid for an Adventure key.
 */
public fun key(
    namespace: String,
    value: String,
): Key = Key.key(namespace, value)

/**
 * Creates an Adventure [Key] from a combined `namespace:value` string.
 *
 * Adventure uses the `minecraft` namespace when [key] does not contain a namespace.
 *
 * @throws InvalidKeyException when [key] is not valid for an Adventure key.
 */
public fun key(key: String): Key = Key.key(key)
