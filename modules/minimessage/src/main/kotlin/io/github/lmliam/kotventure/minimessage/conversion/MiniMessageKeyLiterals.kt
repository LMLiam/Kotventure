package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.key.Key

/** Returns [key] as a `key("namespace", "value")` call. */
internal fun keyLiteral(key: Key): String = "key(${quoted(key.namespace())}, ${quoted(key.value())})"
