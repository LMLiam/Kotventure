package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

internal object MiniMessageToDslSupport {
    val decorations: List<Pair<TextDecoration, String>> =
        listOf(
            TextDecoration.BOLD to "bold",
            TextDecoration.ITALIC to "italic",
            TextDecoration.UNDERLINED to "underlined",
            TextDecoration.STRIKETHROUGH to "strikethrough",
            TextDecoration.OBFUSCATED to "obfuscated",
        )

    /**
     * Validates that the whole [component] tree can be reproduced without data loss before any source is emitted, so a
     * payload the DSL cannot represent fails loudly rather than being silently dropped from the generated source.
     *
     * Every Adventure component type has a DSL form, so there is no type allow-list here; the walk instead rejects the
     * specific attributes and payloads ([requireSupported] for styles, [requireSupportedPayload] for type-specific
     * extras) that have no representation.
     */
    fun requireSupported(component: Component) {
        requireSupported(component.style())
        component.children().forEach(::requireSupported)
        requireSupportedPayload(component)
    }

    /**
     * Validates the payload a component carries beyond its style and children, recursing the nested components so an
     * unrepresentable style anywhere in the tree is rejected up front:
     *
     * - translatable arguments and selector / NBT separators and object fallbacks are recursed,
     * - a score's obsolete fixed [ScoreComponent.value] is rejected because the DSL cannot represent it and dropping it
     *   would be lossy.
     */
    private fun requireSupportedPayload(component: Component) {
        when (component) {
            is TranslatableComponent -> component.arguments().forEach(::requireSupportedArgument)
            is SelectorComponent -> component.separator()?.let(::requireSupported)
            is NBTComponent<*> -> component.separator()?.let(::requireSupported)
            is ObjectComponent -> component.fallback()?.let(::requireSupported)
            is ScoreComponent ->
                require(component.value() == null) {
                    "miniToDsl cannot represent score components with a fixed value."
                }

            else -> Unit
        }
    }

    private fun requireSupportedArgument(argument: TranslationArgument) {
        val value = argument.value()
        require(value is Component) {
            "miniToDsl cannot represent non-component translatable arguments (${value::class.qualifiedName})."
        }
        requireSupported(value)
    }

    fun requireSupported(style: Style) {
        require(style.shadowColor() == null) {
            "miniToDsl cannot represent shadow colours: the component DSL has no shadow-colour surface."
        }
        style.hoverEvent()?.let { hover -> requireSupportedHoverPayload(hover) }
    }

    /**
     * Validates the nested component a hover event renders, so unrepresentable styles or non-text payloads are rejected
     * up front instead of being silently dropped or triggering a cast failure while the DSL is written.
     */
    private fun requireSupportedHoverPayload(hover: HoverEvent<*>) {
        when (hover.action()) {
            HoverEvent.Action.SHOW_TEXT -> requireSupported(hover.value() as Component)
            HoverEvent.Action.SHOW_ENTITY -> {
                val entity = hover.value() as HoverEvent.ShowEntity
                entity.name()?.let { name -> requireSupported(name) }
            }

            else -> Unit
        }
    }

    fun hasDslOutput(style: Style): Boolean =
        style.color() != null ||
            style.font() != null ||
            style.insertion() != null ||
            style.clickEvent() != null ||
            style.hoverEvent() != null ||
            decorations.any { (decoration) -> style.decoration(decoration) != State.NOT_SET }
}
