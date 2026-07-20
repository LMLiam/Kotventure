package io.github.lmliam.kotventure.paper.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

internal fun Component.nonItalicByDefault(): Component =
    decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
