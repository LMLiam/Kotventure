package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Parses [input] with Adventure's default MiniMessage parser.
 */
public fun mini(input: String): Component = parseMiniMessage(input)

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
