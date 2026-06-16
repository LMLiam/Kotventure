package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Parses [input] with Adventure's default MiniMessage parser.
 */
public fun mini(input: String): Component = parseMiniMessage(input)

/**
 * Converts MiniMessage [input] into Kotventure component DSL source code: the text, structured (translatable, keybind,
 * score, selector), NBT, and sprite-object components MiniMessage produces, with their colours, decorations, fonts,
 * insertions, and click/hover events.
 *
 * `<gradient>` is expanded by the parser into one coloured child per character before conversion, so the output
 * reproduces those children rather than a `gradient` call — a lossy-but-faithful expansion: the rendering is exact, but
 * the `<gradient>` markup itself is not reconstructed.
 *
 * Shadow colours (`<shadow>`) and player-head object contents (`<head>`) have no DSL surface yet, so they fail with an
 * [IllegalArgumentException] rather than being dropped silently.
 *
 * @throws IllegalArgumentException when [input] contains a shadow colour or player-head contents.
 */
public fun miniToDsl(input: String): String = MiniMessageToDslWriter.write(mini(input))

/**
 * Parses [input] with Adventure's default MiniMessage parser after configuring placeholder resolvers with [init].
 */
public fun mini(
    input: String,
    init: MiniMessageResolverScope.() -> Unit,
): Component = parseMiniMessage(input, init)

internal fun parseMiniMessage(input: String): Component = MiniMessage.miniMessage().deserialize(input)

internal fun parseMiniMessage(
    input: String,
    init: MiniMessageResolverScope.() -> Unit,
): Component = MiniMessage.miniMessage().deserialize(input, MiniMessageResolverBuilder().apply(init).build())
