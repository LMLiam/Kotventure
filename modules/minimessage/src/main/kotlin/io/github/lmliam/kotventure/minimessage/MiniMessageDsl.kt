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
 * Converts MiniMessage [input] into Kotventure component DSL source code. It converts text, structured components, NBT,
 * sprites, and player-head objects. Structured components include translatable, keybind, score, and selector
 * components. The output keeps colours, shadow colours, decorations, fonts, insertions, and click and hover events.
 *
 * Before conversion, the parser expands `<gradient>` into one coloured child for each character. Therefore, the output
 * contains those children and not a `gradient` call. The rendered result is accurate, but the output does not reproduce
 * the `<gradient>` markup.
 *
 * The strict `core` parser processes selector patterns. The output uses typed selector factories and canonicalised
 * arguments. An invalid pattern causes an exception that gives the error offset.
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
