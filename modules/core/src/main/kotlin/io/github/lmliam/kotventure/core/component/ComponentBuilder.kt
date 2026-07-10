package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.style.OnceStyleBuilder
import io.github.lmliam.kotventure.core.style.StyleBuilder
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
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
) : OnceStyleBuilder(),
    ComponentScope {
    private var style: Style? by once()

    override fun applyColor(color: TextColor?) {
        builder.color(color)
    }

    override fun applyShadow(color: ShadowColor?) {
        builder.style { styleBuilder -> styleBuilder.shadowColor(color) }
    }

    override fun applyFont(font: Key?) {
        builder.style { styleBuilder -> styleBuilder.font(font) }
    }

    override fun applyInsertion(insertion: String?) {
        builder.style { styleBuilder -> styleBuilder.insertion(insertion) }
    }

    override fun applyClick(event: ClickEvent<*>?) {
        builder.clickEvent(event)
    }

    override fun applyHover(source: HoverEventSource<*>?) {
        builder.hoverEvent(source)
    }

    override fun applyDecoration(
        decoration: TextDecoration,
        state: State,
    ) {
        builder.decoration(decoration, state)
    }

    override fun style(style: Style) {
        this.style = style
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        builder.style { styleBuilder ->
            StyleBuilder(styleBuilder).init()
            style = styleBuilder.build()
        }
    }

    override fun <T : ComponentLike> append(component: T) {
        builder.append(component.asComponent())
    }

    override fun newline() {
        builder.append(Component.newline())
    }

    internal open fun build(): Component = builder.build()
}
