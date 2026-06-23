package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.style.StyleBuilder
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State
import net.kyori.adventure.text.ComponentBuilder as AdventureComponentBuilder

internal open class ComponentBuilder<C : Component, B : AdventureComponentBuilder<C, B>>(
    protected val builder: B,
) : ComponentScope {
    override fun color(color: TextColor?) {
        builder.color(color)
    }

    override fun shadow(color: ShadowColor?) {
        builder.style { styleBuilder -> styleBuilder.shadowColor(color) }
    }

    override fun font(font: Key?) {
        builder.style { styleBuilder -> styleBuilder.font(font) }
    }

    override fun insertion(insertion: String?) {
        builder.style { styleBuilder -> styleBuilder.insertion(insertion) }
    }

    override fun style(style: Style) {
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        builder.style { styleBuilder -> StyleBuilder(styleBuilder).init() }
    }

    override fun click(event: ClickEvent<*>?) {
        builder.clickEvent(event)
    }

    override fun hover(source: HoverEventSource<*>?) {
        builder.hoverEvent(source)
    }

    override fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    ) {
        decoration(decoration, flag.toDecorationState())
    }

    override fun decoration(
        decoration: TextDecoration,
        state: State,
    ) {
        builder.decoration(decoration, state)
    }

    override fun append(component: Component) {
        builder.append(component)
    }

    override fun newline() {
        builder.append(Component.newline())
    }

    internal open fun build(): Component = builder.build()
}

private fun Boolean?.toDecorationState(): State =
    when (this) {
        true -> State.TRUE
        false -> State.FALSE
        null -> State.NOT_SET
    }
