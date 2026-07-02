package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import net.kyori.adventure.key.InvalidKeyException

internal fun String.withDefaultNamespace(): String = if (":" in this) this else "minecraft:$this"

internal fun String.requireEntityTypeKey(): String {
    val namespaced = withDefaultNamespace()
    try {
        key(namespaced)
    } catch (exception: InvalidKeyException) {
        throw IllegalArgumentException("Entity type '$this' is not a valid namespaced key.", exception)
    }
    return namespaced
}
