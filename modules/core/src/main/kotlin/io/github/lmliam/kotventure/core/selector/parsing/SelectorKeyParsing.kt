package io.github.lmliam.kotventure.core.selector.parsing

import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

internal fun SelectorReader.readSelectorKey(): Key {
    val start = offset
    return parseSelectorKey(readValueToken(), start)
}

internal fun SelectorReader.parseSelectorKey(
    value: String,
    valueOffset: Int,
): Key {
    if (value.isEmpty()) failAt(valueOffset, "Expected a namespaced key")
    return try {
        Key.key(value)
    } catch (_: InvalidKeyException) {
        failAt(valueOffset, "Invalid namespaced key '$value'")
    }
}
