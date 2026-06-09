package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.style.StyleScope
import io.github.lmliam.kotventure.core.text.TextComponentBuilder
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

internal class SelectorComponentBuilder(
    pattern: String,
) : SelectorScope {
    private val builder: SelectorComponent.Builder = Component.selector().pattern(pattern)

    override fun separator(separator: ComponentLike) {
        builder.separator(separator)
    }

    override fun separator(init: TextScope.() -> Unit) {
        builder.separator(TextComponentBuilder().apply(init).build())
    }

    override fun color(color: TextColor) {
        builder.color(color)
    }

    override fun style(style: Style) {
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        SelectorStyleScope(builder).init()
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
