package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import net.kyori.adventure.key.InvalidKeyException

internal fun String.requireEntityTypeKey(): String =
    try {
        key(this).asString()
    } catch (exception: InvalidKeyException) {
        throw IllegalArgumentException(
            "Entity type '$this' is not a valid namespaced key.",
            exception,
        )
    }
