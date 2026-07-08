package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.dsl.once
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
) : ComponentScope {
    private var color: TextColor? by once()
    private var shadow: ShadowColor? by once()
    private var font: Key? by once()
    private var insertion: String? by once()
    private var style: Style? by once()
    private var click: ClickEvent<*>? by once()
    private var hover: HoverEventSource<*>? by once()
    private var bold: State? by once()
    private var italic: State? by once()
    private var underlined: State? by once()
    private var strikethrough: State? by once()
    private var obfuscated: State? by once()

    override fun color(color: TextColor?) {
        this.color = color
        builder.color(color)
    }

    override fun shadow(color: ShadowColor?) {
        this.shadow = color
        builder.style { styleBuilder -> styleBuilder.shadowColor(color) }
    }

    override fun font(font: Key?) {
        this.font = font
        builder.style { styleBuilder -> styleBuilder.font(font) }
    }

    override fun insertion(insertion: String?) {
        this.insertion = insertion
        builder.style { styleBuilder -> styleBuilder.insertion(insertion) }
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

    override fun click(event: ClickEvent<*>?) {
        this.click = event
        builder.clickEvent(event)
    }

    override fun hover(source: HoverEventSource<*>?) {
        this.hover = source
        builder.hoverEvent(source)
    }

    override fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    ) {
        decoration(decoration, State.byBoolean(flag))
    }

    override fun decoration(
        decoration: TextDecoration,
        state: State,
    ) {
        when (decoration) {
            TextDecoration.BOLD -> bold = state
            TextDecoration.ITALIC -> italic = state
            TextDecoration.UNDERLINED -> underlined = state
            TextDecoration.STRIKETHROUGH -> strikethrough = state
            TextDecoration.OBFUSCATED -> obfuscated = state
        }
        builder.decoration(decoration, state)
    }

    override fun <T : ComponentLike> append(component: T) {
        builder.append(component.asComponent())
    }

    override fun newline() {
        builder.append(Component.newline())
    }

    internal open fun build(): Component = builder.build()
}
