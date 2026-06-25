package io.github.lmliam.kotventure.minimessage.parser

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverBuilder
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

internal fun parseMiniMessage(input: String): Component = MiniMessage.miniMessage().deserialize(input)

internal fun parseMiniMessage(
    input: String,
    init: MiniMessageResolverScope.() -> Unit,
): Component = MiniMessage.miniMessage().deserialize(input, MiniMessageResolverBuilder().apply(init).build())
