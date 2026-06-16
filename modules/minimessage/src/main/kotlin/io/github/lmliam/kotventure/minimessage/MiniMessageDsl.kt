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
 * Every Adventure component type is covered: plain text with recursive children, the structured components
 * (translatable with recursive arguments, keybind, score, selector), the NBT components (block, entity, storage), and
 * object components with sprite contents. Styles emit named and hex colours, the standard text decorations, fonts,
 * insertion text, and click and hover events.
 *
 * A `<gradient>` is expanded by the MiniMessage parser into one coloured child per character before this converter ever
 * sees it, so the generated DSL faithfully reproduces those per-character children rather than re-deriving a `gradient`
 * call — verbose, but loss-free.
 *
 * Payloads that the component DSL cannot express — a shadow colour, a score's fixed value, a non-component translatable
 * argument, a legacy show-item NBT payload, or player-head object contents — fail with an [IllegalArgumentException]
 * rather than producing source that silently drops them.
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
