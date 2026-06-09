package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

internal class TranslatableComponentBuilder(
    key: String,
) : TranslatableScope {
    private val builder: TranslatableComponent.Builder = Component.translatable().key(key)
    private val arguments = mutableListOf<TranslationArgument>()

    override fun fallback(fallback: String) {
        builder.fallback(fallback)
    }

    override fun arg(value: ComponentLike) {
        arguments += TranslationArgument.component(value)
    }

    override fun arg(value: Boolean) {
        arguments += TranslationArgument.bool(value)
    }

    override fun arg(value: Number) {
        arguments += TranslationArgument.numeric(value)
    }

    override fun args(vararg values: ComponentLike) {
        arguments += values.map(TranslationArgument::component)
    }

    override fun color(color: TextColor) {
        builder.color(color)
    }

    override fun style(style: Style) {
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        TranslatableStyleScope(builder).init()
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

    internal fun build(): Component = builder.arguments(arguments).build()
}
