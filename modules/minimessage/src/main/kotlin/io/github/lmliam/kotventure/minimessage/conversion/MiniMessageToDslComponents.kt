package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.StorageNBTComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent

/** Emits [component], or only its children when it is an empty, unstyled text root. */
internal fun KotlinSourceBuilder.appendRoot(component: Component) {
    if (component.isEmptyTextRoot()) {
        component.children().forEach(::appendComponent)
    } else {
        appendComponent(component)
    }
}

/** Emits the applicable Kotventure DSL representation of [component]. */
internal fun KotlinSourceBuilder.appendComponent(component: Component) {
    when (component) {
        is TextComponent -> appendText(component)
        is TranslatableComponent -> appendTranslatable(component)
        is KeybindComponent -> appendKeybind(component)
        is ScoreComponent -> appendScore(component)
        is SelectorComponent -> appendSelector(component)
        is BlockNBTComponent -> appendBlockNbt(component)
        is EntityNBTComponent -> appendEntityNbt(component)
        is StorageNBTComponent -> appendStorageNbt(component)
        is ObjectComponent -> appendObject(component)

        else ->
            conversionError(
                "miniToDsl cannot represent component type ${component::class.qualifiedName}.",
            )
    }
}

private fun Component.isEmptyTextRoot(): Boolean =
    this is TextComponent && content().isEmpty() && !hasDslOutput(style())
