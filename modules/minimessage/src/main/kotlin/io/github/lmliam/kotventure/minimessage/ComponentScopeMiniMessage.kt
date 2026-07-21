package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.minimessage.parser.parseMiniMessage
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverScope

/**
 * Deserialises [input] with Adventure's default MiniMessage parser and appends the result as the next child.
 *
 * The default parser is lenient. Use [validate] first when the input must have strict tag closure.
 *
 * @throws net.kyori.adventure.text.minimessage.ParsingException when Adventure cannot deserialise [input].
 */
public fun ComponentScope.mini(input: String) {
    append(parseMiniMessage(input))
}

/**
 * Deserialises [input] with the placeholder resolvers from [init] and appends the result as the next child.
 *
 * Resolver names must be valid MiniMessage tag names and must be unique in [init]. Parsed string replacements are
 * MiniMessage markup. Unparsed strings and typed scalar values are literal text.
 *
 * @throws IllegalArgumentException when [init] contains an invalid or duplicate resolver name.
 * @throws net.kyori.adventure.text.minimessage.ParsingException when Adventure cannot deserialise [input] or a parsed
 * replacement.
 */
public fun ComponentScope.mini(
    input: String,
    init: MiniMessageResolverScope.() -> Unit,
) {
    append(parseMiniMessage(input, init))
}
