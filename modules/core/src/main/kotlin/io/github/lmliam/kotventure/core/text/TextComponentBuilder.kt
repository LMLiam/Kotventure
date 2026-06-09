package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.keybind.KeybindScope
import io.github.lmliam.kotventure.core.score.ScoreScope
import io.github.lmliam.kotventure.core.selector.SelectorScope
import io.github.lmliam.kotventure.core.style.StyleScope
import io.github.lmliam.kotventure.core.translatable.TranslatableScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import io.github.lmliam.kotventure.core.keybind.keybind as keybindComponent
import io.github.lmliam.kotventure.core.score.score as scoreComponent
import io.github.lmliam.kotventure.core.selector.selector as selectorComponent
import io.github.lmliam.kotventure.core.translatable.translatable as translatableComponent

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
        TextStyleScope(builder).init()
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

    override fun translatable(
        key: String,
        init: TranslatableScope.() -> Unit,
    ) {
        append(translatableComponent(key, init))
    }

    override fun keybind(
        keybind: String,
        init: KeybindScope.() -> Unit,
    ) {
        append(keybindComponent(keybind, init))
    }

    override fun score(
        name: String,
        objective: String,
        init: ScoreScope.() -> Unit,
    ) {
        append(scoreComponent(name, objective, init))
    }

    override fun selector(
        pattern: String,
        init: SelectorScope.() -> Unit,
    ) {
        append(selectorComponent(pattern, init))
    }

    override fun newline() {
        builder.append(Component.newline())
    }

    internal fun build(): Component = builder.build()
}
