package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor

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

    override fun text(init: TextScope.() -> Unit) {
        builder.append(TextComponentBuilder().apply(init).build())
    }

    internal fun build(): Component = builder.build()
}
