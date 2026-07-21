package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslWriter
import io.github.lmliam.kotventure.minimessage.parser.parseMiniMessage
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverScope
import net.kyori.adventure.text.Component

/**
 * Deserialises [input] with Adventure's default MiniMessage parser.
 *
 * The default parser is lenient and uses the standard Adventure tags. This function creates a component. It does not
 * send it to an audience. Use [validate] first when the input must have strict tag closure.
 *
 * @sample io.github.lmliam.kotventure.minimessage.miniSample
 *
 * @param input The MiniMessage markup to deserialise.
 * @throws net.kyori.adventure.text.minimessage.ParsingException when Adventure cannot deserialise [input].
 */
public fun mini(input: String): Component = parseMiniMessage(input)

/**
 * Converts MiniMessage [input] to Kotlin source that uses the Kotventure component DSL.
 *
 * The function first deserialises [input] with Adventure's lenient default parser. It then writes one deterministic
 * `component { }` expression. The output represents text, translatable, keybind, score, selector, NBT, sprite, and
 * player-head components. It also represents supported style, click-event, hover-event, and child data.
 *
 * Deserialisation expands a gradient to coloured component children. The output therefore represents those children
 * and does not reconstruct the source `gradient` call. The generated DSL has equivalent component data, but it is not
 * a lexical round trip of [input]. SNBT compound keys are sorted when the converter emits typed NBT blocks.
 *
 * Selector patterns use the strict core selector parser. The output uses typed selector factories and canonical
 * arguments.
 *
 * @throws IllegalArgumentException when a selector pattern is invalid or the component tree contains data that the DSL
 * cannot represent without loss. Examples include unsupported click payloads, unsupported data-component payloads,
 * player-head profile properties, and a player head without exactly one skin source.
 * @throws net.kyori.adventure.text.minimessage.ParsingException when Adventure cannot deserialise [input].
 */
public fun miniToDsl(input: String): String = MiniMessageToDslWriter.write(mini(input))

/**
 * Deserialises [input] with the custom placeholder resolvers from [init].
 *
 * The parser also supports the standard Adventure tags and remains lenient. Parsed string replacements are
 * MiniMessage markup. Unparsed strings and typed scalar values are literal text. Component values are inserted as
 * components.
 *
 * @sample io.github.lmliam.kotventure.minimessage.miniWithPlaceholdersSample
 *
 * @param input The MiniMessage markup to deserialise.
 * @param init Declares the placeholder resolvers that the markup can use.
 * @throws IllegalArgumentException when [init] contains an invalid or duplicate resolver name.
 * @throws net.kyori.adventure.text.minimessage.ParsingException when Adventure cannot deserialise [input] or a parsed
 * replacement.
 */
public fun mini(
    input: String,
    init: MiniMessageResolverScope.() -> Unit,
): Component = parseMiniMessage(input, init)
