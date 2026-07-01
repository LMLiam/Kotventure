package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal data class SelectorPredicateFilter(
    val key: Key,
    val isNegated: Boolean,
) {
    val rendered: String
        get() = if (isNegated) "!${key.asString()}" else key.asString()
}
