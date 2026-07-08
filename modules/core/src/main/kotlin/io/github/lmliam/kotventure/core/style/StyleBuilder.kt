package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

internal class StyleBuilder(
    private val builder: Style.Builder,
) : StyleScope {
    private var color: TextColor? by once()
    private var shadow: ShadowColor? by once()
    private var font: Key? by once()
    private var insertion: String? by once()
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
        builder.shadowColor(color)
    }

    override fun font(font: Key?) {
        this.font = font
        builder.font(font)
    }

    override fun insertion(insertion: String?) {
        this.insertion = insertion
        builder.insertion(insertion)
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
}
