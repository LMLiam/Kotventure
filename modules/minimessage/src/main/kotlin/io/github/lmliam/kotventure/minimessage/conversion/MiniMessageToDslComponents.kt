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

/** Emits [component], unwrapping content-less, style-less text root into bare sequence children. */
internal fun KotlinSourceBuilder.appendRoot(component: Component) {
    if (component is TextComponent && component.content().isEmpty() && !hasDslOutput(component.style())) {
        component.children().forEach { appendComponent(it) }
        return
    }

    appendComponent(component)
}

internal fun KotlinSourceBuilder.appendComponent(component: Component) {
    val emitter =
        componentEmitters.firstOrNull { it.accepts(component) }
            ?: conversionError("miniToDsl cannot represent component type ${component::class.qualifiedName}.")

    emitter.emit(this, component)
}

private val componentEmitters: List<ComponentEmitter> =
    listOf(
        componentEmitter<TextComponent> { appendText(it) },
        componentEmitter<TranslatableComponent> { appendTranslatable(it) },
        componentEmitter<KeybindComponent> { appendKeybind(it) },
        componentEmitter<ScoreComponent> { appendScore(it) },
        componentEmitter<SelectorComponent> { appendSelector(it) },
        componentEmitter<BlockNBTComponent> { appendBlockNbt(it) },
        componentEmitter<EntityNBTComponent> { appendEntityNbt(it) },
        componentEmitter<StorageNBTComponent> { appendStorageNbt(it) },
        componentEmitter<ObjectComponent> { appendObject(it) },
    )

private class ComponentEmitter(
    private val accepts: (Component) -> Boolean,
    private val emitComponent: KotlinSourceBuilder.(Component) -> Unit,
) {
    fun accepts(component: Component): Boolean = accepts.invoke(component)

    fun emit(
        builder: KotlinSourceBuilder,
        component: Component,
    ) {
        emitComponent.invoke(builder, component)
    }
}

private inline fun <reified T : Component> componentEmitter(
    noinline emit: KotlinSourceBuilder.(T) -> Unit,
): ComponentEmitter =
    ComponentEmitter(
        accepts = { it is T },
        emitComponent = { component -> emit(component as T) },
    )
