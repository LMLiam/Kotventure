package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.StorageNBTComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument

/** Emits [component], unwrapping a content-less, style-less text root into a bare sequence of its children. */
internal fun KotlinSourceBuilder.appendRoot(component: Component) {
    if (component is TextComponent &&
        component.content().isEmpty() &&
        !hasDslOutput(component.style())
    ) {
        component.children().forEach { appendComponent(it) }
        return
    }

    appendComponent(component)
}

/**
 * Dispatches to the emitter for [component]'s concrete type. Every emission is a self-contained call expression, so it
 * reads the same whether it appends a child inside a scope or stands alone as a translatable argument or separator.
 */
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
        else -> conversionError("miniToDsl cannot represent component type ${component::class.qualifiedName}.")
    }
}

private fun KotlinSourceBuilder.appendText(component: TextComponent) {
    val text = component.content()
    val hasBlockBody = hasDslOutput(component.style()) || component.children().isNotEmpty()

    if (!hasBlockBody) {
        line("text(\"${escapeKotlinString(text)}\")")
        return
    }

    val header = if (text.isEmpty()) "text" else "text(\"${escapeKotlinString(text)}\")"
    block(header) {
        appendStyle(component.style())
        component.children().forEach { appendComponent(it) }
    }
}

private fun KotlinSourceBuilder.appendTranslatable(component: TranslatableComponent) {
    val fallback = component.fallback()
    val arguments = component.arguments()
    appendStructured(
        header = "translatable(\"${escapeKotlinString(component.key())}\")",
        component = component,
        hasExtraBody = fallback != null || arguments.isNotEmpty(),
    ) {
        fallback?.let { line("fallback(\"${escapeKotlinString(it)}\")") }
        arguments.forEach { appendArgument(it) }
    }
}

private fun KotlinSourceBuilder.appendKeybind(component: KeybindComponent) {
    appendStructured("keybind(\"${escapeKotlinString(component.keybind())}\")", component) {}
}

private fun KotlinSourceBuilder.appendScore(component: ScoreComponent) {
    val name = escapeKotlinString(component.name())
    val objective = escapeKotlinString(component.objective())
    appendStructured("score(\"$name\", \"$objective\")", component) {}
}

private fun KotlinSourceBuilder.appendSelector(component: SelectorComponent) {
    val separator = component.separator()
    appendStructured(
        header = "selector(\"${escapeKotlinString(component.pattern())}\")",
        component = component,
        hasExtraBody = separator != null,
    ) {
        separator?.let { appendComponentArgument("separator", it) }
    }
}

private fun KotlinSourceBuilder.appendBlockNbt(component: BlockNBTComponent) {
    appendNbt("blockNbt", "blockPos(\"${escapeKotlinString(component.pos().asString())}\")", component)
}

private fun KotlinSourceBuilder.appendEntityNbt(component: EntityNBTComponent) {
    appendNbt("entityNbt", "\"${escapeKotlinString(component.selector())}\"", component)
}

private fun KotlinSourceBuilder.appendStorageNbt(component: StorageNBTComponent) {
    appendNbt("storageNbt", keyLiteral(component.storage()), component)
}

/**
 * Emits an NBT component as `$function($source, "nbtPath")` — where [source] is the per-type first argument (a
 * `blockPos(...)`, a selector string, or a storage `key(...)`) — followed by the `interpret` flag and separator that
 * every NBT component can carry.
 */
private fun KotlinSourceBuilder.appendNbt(
    function: String,
    source: String,
    component: NBTComponent<*>,
) {
    val interpret = component.interpret()
    val separator = component.separator()
    appendStructured(
        header = "$function($source, \"${escapeKotlinString(component.nbtPath())}\")",
        component = component,
        hasExtraBody = interpret || separator != null,
    ) {
        if (interpret) line("interpret(true)")
        separator?.let { appendComponentArgument("separator", it) }
    }
}

private fun KotlinSourceBuilder.appendObject(component: ObjectComponent) {
    val fallback = component.fallback()
    appendStructured(
        header = "display(${objectContentsLiteral(component.contents())})",
        component = component,
        hasExtraBody = fallback != null,
    ) {
        fallback?.let { appendComponentArgument("fallback", it) }
    }
}

/**
 * Emits [header] as a bare call when [component] carries no style, children, or [hasExtraBody], otherwise opens a
 * block and emits [body] ahead of the shared style and children.
 */
private fun KotlinSourceBuilder.appendStructured(
    header: String,
    component: Component,
    hasExtraBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit,
) {
    val hasStyle = hasDslOutput(component.style())
    if (!hasExtraBody && !hasStyle && component.children().isEmpty()) {
        line(header)
        return
    }

    block(header) {
        body()
        appendStyle(component.style())
        component.children().forEach { appendComponent(it) }
    }
}

/**
 * Emits a translatable argument. Adventure wraps either a component or a primitive ([Boolean] / [Number]); the core
 * `translatable` DSL has an `arg(...)` overload for each.
 */
private fun KotlinSourceBuilder.appendArgument(argument: TranslationArgument) {
    when (val value = argument.value()) {
        is Component -> appendComponentArgument("arg", value)
        is Boolean -> line("arg($value)")
        is Number -> line("arg($value)")
        else ->
            conversionError(
                "miniToDsl cannot represent the ${value::class.qualifiedName} translatable argument.",
            )
    }
}

/**
 * Emits `$label { ... }`, recursing through [appendRoot] for the wrapped component.
 */
private fun KotlinSourceBuilder.appendComponentArgument(
    label: String,
    component: Component,
) {
    block(label) { appendRoot(component) }
}
