package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.minimessage.parser.parseMiniMessage
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverScope

/**
 * Parses [input] as MiniMessage and appends the resulting component to this scope.
 */
public fun ComponentScope.mini(input: String) {
    append(parseMiniMessage(input))
}

/**
 * Parses [input] as MiniMessage with placeholder resolvers configured by [init], then appends the result to this scope.
 */
public fun ComponentScope.mini(
    input: String,
    init: MiniMessageResolverScope.() -> Unit,
) {
    append(parseMiniMessage(input, init))
}
