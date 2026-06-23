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
