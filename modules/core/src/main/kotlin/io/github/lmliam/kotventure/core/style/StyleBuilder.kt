package io.github.lmliam.kotventure.core.style

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
) : StyleScopeSupport() {
    override fun applyColor(color: TextColor?) {
        builder.color(color)
    }

    override fun applyShadow(color: ShadowColor?) {
        builder.shadowColor(color)
    }

    override fun applyFont(font: Key?) {
        builder.font(font)
    }

    override fun applyInsertion(insertion: String?) {
        builder.insertion(insertion)
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
}
