package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslWriter
import io.github.lmliam.kotventure.minimessage.parser.parseMiniMessage
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverScope
import net.kyori.adventure.text.Component

/**
 * Parses MiniMessage markup into a component with Adventure's default parser.
 *
 * @sample io.github.lmliam.kotventure.minimessage.miniSample
 *
 * @param input the MiniMessage string to parse.
 */
public fun mini(input: String): Component = parseMiniMessage(input)

/**
 * Converts MiniMessage [input] into Kotventure component DSL source code: the text, structured (translatable, keybind,
 * score, selector), NBT, sprite- and player-head-object components MiniMessage produces, with their colours, shadow
 * colours, decorations, fonts, insertions, and click/hover events.
 *
 * `<gradient>` is expanded by the parser into one coloured child per character before conversion, so the output
 * reproduces those children rather than a `gradient` call — a lossy-but-faithful expansion: the rendering is exact, but
 * the `<gradient>` markup itself is not reconstructed.
 *
 * Selector patterns emit typed factories and arguments only when the resulting selector is byte-for-byte equivalent.
 * Unsupported, future, or normalizing syntax is preserved through `entitySelector("...")`.
 *
 * @throws IllegalArgumentException when [input] resolves to a shape with no DSL surface, such as a player head with no
 * single skin source, profile properties, or unsupported click or data-component payloads.
 */
public fun miniToDsl(input: String): String = MiniMessageToDslWriter.write(mini(input))

/**
 * Parses MiniMessage markup into a component, resolving custom placeholder tags configured in [init].
 *
 * @sample io.github.lmliam.kotventure.minimessage.miniWithPlaceholdersSample
 *
 * @param input the MiniMessage string to parse.
 * @param init registers the placeholder resolvers the markup may reference.
 */
public fun mini(
    input: String,
    init: MiniMessageResolverScope.() -> Unit,
): Component = parseMiniMessage(input, init)
