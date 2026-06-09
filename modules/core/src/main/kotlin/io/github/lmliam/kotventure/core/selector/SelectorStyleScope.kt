package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

internal class SelectorStyleScope(
    private val builder: SelectorComponent.Builder,
) : StyleScope {
    override fun color(color: TextColor) {
        builder.color(color)
    }

    override fun decorate(decoration: TextDecoration) {
        builder.decoration(decoration, true)
    }

    override fun bold() {
        decorate(TextDecoration.BOLD)
    }

    override fun italic() {
        decorate(TextDecoration.ITALIC)
    }

    override fun underlined() {
        decorate(TextDecoration.UNDERLINED)
    }

    override fun strikethrough() {
        decorate(TextDecoration.STRIKETHROUGH)
    }

    override fun obfuscated() {
        decorate(TextDecoration.OBFUSCATED)
    }
}
