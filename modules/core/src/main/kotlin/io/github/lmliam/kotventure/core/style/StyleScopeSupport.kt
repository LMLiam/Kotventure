package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

/**
 * Shared once-assign tracking and [StyleScope] dispatch for builders that apply style to an Adventure
 * target (reusable [Style] or a component builder).
 *
 * Subclasses only implement the Adventure apply hooks; slot uniqueness lives here so decoration and
 * attribute once-assign cannot drift between [StyleBuilder] and component builders.
 */
internal abstract class StyleScopeSupport : StyleScope {
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

    final override fun color(color: TextColor?) {
        this.color = color
        applyColor(color)
    }

    final override fun shadow(color: ShadowColor?) {
        this.shadow = color
        applyShadow(color)
    }

    final override fun font(font: Key?) {
        this.font = font
        applyFont(font)
    }

    final override fun insertion(insertion: String?) {
        this.insertion = insertion
        applyInsertion(insertion)
    }

    final override fun click(event: ClickEvent<*>?) {
        this.click = event
        applyClick(event)
    }

    final override fun hover(source: HoverEventSource<*>?) {
        this.hover = source
        applyHover(source)
    }

    final override fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    ) {
        decoration(decoration, State.byBoolean(flag))
    }

    final override fun decoration(
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
        applyDecoration(decoration, state)
    }

    protected abstract fun applyColor(color: TextColor?)

    protected abstract fun applyShadow(color: ShadowColor?)

    protected abstract fun applyFont(font: Key?)

    protected abstract fun applyInsertion(insertion: String?)

    protected abstract fun applyClick(event: ClickEvent<*>?)

    protected abstract fun applyHover(source: HoverEventSource<*>?)

    protected abstract fun applyDecoration(
        decoration: TextDecoration,
        state: State,
    )
}
