package io.github.lmliam.kotventure.core.keybind

import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

internal class KeybindComponentBuilder(
    keybind: String,
) : KeybindScope {
    private val builder: KeybindComponent.Builder = Component.keybind().keybind(keybind)

    override fun color(color: TextColor) {
        builder.color(color)
    }

    override fun style(style: Style) {
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        KeybindStyleScope(builder).init()
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

    override fun append(component: Component) {
        builder.append(component)
    }

    internal fun build(): Component = builder.build()
}
