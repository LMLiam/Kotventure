package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Parses [input] with Adventure's default MiniMessage parser.
 */
public fun mini(input: String): Component = parseMiniMessage(input)

/**
 * Converts MiniMessage [input] into Kotventure component DSL source code.
 *
 * The current slices support plain text, recursive children, named and hex colours, the standard text decorations,
 * click events, hover events, and the structured components — translatable (with recursive arguments), keybind, score,
 * and selector. Unsupported component types or style attributes from later slices fail with an [IllegalArgumentException]
 * instead of producing lossy source.
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
