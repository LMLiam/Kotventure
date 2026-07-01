package io.github.lmliam.kotventure.core.selector

internal fun String.withDefaultNamespace(): String = if (":" in this) this else "minecraft:$this"
