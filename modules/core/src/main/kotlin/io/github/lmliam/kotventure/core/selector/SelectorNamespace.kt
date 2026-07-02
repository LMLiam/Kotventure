package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

internal fun String.withDefaultNamespace(): String = if (":" in this) this else "minecraft:$this"

internal fun String.requireEntityTypeKey(): String {
    val namespaced = withDefaultNamespace()
    try {
        Key.key(namespaced)
    } catch (_: InvalidKeyException) {
        throw IllegalArgumentException("Entity type '$this' is not a valid namespaced key.")
    }
    return namespaced
}
