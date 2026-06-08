package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.style.StyleBuilder
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

internal class TextComponentBuilder : TextScope {
    private val builder: TextComponent.Builder = Component.text()

    override fun content(value: String) {
        builder.content(value)
    }

    override fun color(color: TextColor) {
        builder.color(color)
    }

    override fun style(style: Style) {
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        builder.style(StyleBuilder().apply(init).build())
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

    override fun text(
        value: String,
        init: TextScope.() -> Unit,
    ) {
        text {
            content(value)
            init()
        }
    }

    override fun text(init: TextScope.() -> Unit) {
        builder.append(TextComponentBuilder().apply(init).build())
    }

    override fun append(component: Component) {
        builder.append(component)
    }

    override fun newline() {
        builder.append(Component.newline())
    }

    internal fun build(): Component = builder.build()
}
