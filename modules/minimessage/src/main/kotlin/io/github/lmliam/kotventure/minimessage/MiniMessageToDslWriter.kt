package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration.State

/**
 * Walks a MiniMessage-parsed component tree and emits the equivalent Kotventure component DSL source.
 *
 * The walker decides *what* to emit; [KotlinSourceBuilder] owns indentation and [MiniMessageToDslLiterals] renders leaf
 * values, so each function reads as the DSL it produces.
 */
internal object MiniMessageToDslWriter {
    fun write(component: Component): String {
        MiniMessageToDslSupport.requireSupported(component)

        if (component.isEmptyComponent()) {
            return "component {}"
        }

        return KotlinSourceBuilder()
            .apply { block("component") { appendRoot(component) } }
            .toString()
    }

    private fun Component.isEmptyComponent(): Boolean =
        this is TextComponent &&
                content().isEmpty() &&
                children().isEmpty() &&
                !MiniMessageToDslSupport.hasDslOutput(style())
}

/** Emits [component], unwrapping a content-less, style-less text root into a bare sequence of its children. */
private fun KotlinSourceBuilder.appendRoot(component: Component) {
    if (component is TextComponent &&
        component.content().isEmpty() &&
        !MiniMessageToDslSupport.hasDslOutput(component.style())
    ) {
        component.children().forEach { appendComponent(it) }
        return
    }

    appendComponent(component)
}

/**
 * Dispatches to the emitter for [component]'s concrete type. Every emission is a self-contained call expression, so it
 * reads the same whether it appends a child inside a scope or stands alone as a translatable argument or separator.
 *
 * [MiniMessageToDslSupport.requireSupported] runs before any emission, so an unrecognised type here is a broken
 * invariant rather than user input.
 */
private fun KotlinSourceBuilder.appendComponent(component: Component) {
    when (component) {
        is TextComponent -> appendText(component)
        is TranslatableComponent -> appendTranslatable(component)
        is KeybindComponent -> appendKeybind(component)
        is ScoreComponent -> appendScore(component)
        is SelectorComponent -> appendSelector(component)
        else -> error("miniToDsl reached an unvalidated ${component::class.simpleName} component.")
    }
}

private fun KotlinSourceBuilder.appendText(component: TextComponent) {
    val text = component.content()
    val hasBlockBody = MiniMessageToDslSupport.hasDslOutput(component.style()) || component.children().isNotEmpty()

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

/**
 * Emits [header] as a bare call when [component] carries no style, children, or [hasExtraBody], otherwise opens a block
 * and emits [body] (component-specific configuration such as fallback, arguments, or a separator) ahead of the shared
 * style and children.
 */
private fun KotlinSourceBuilder.appendStructured(
    header: String,
    component: Component,
    hasExtraBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit,
) {
    val hasStyle = MiniMessageToDslSupport.hasDslOutput(component.style())
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

private fun KotlinSourceBuilder.appendArgument(argument: TranslationArgument) {
    val value = argument.value()
    check(value is Component) {
        "miniToDsl reached an unvalidated ${value::class.simpleName} translatable argument."
    }
    appendComponentArgument("arg", value)
}

/**
 * Emits `$label { ... }`, recursing through [appendRoot] for the wrapped component.
 *
 * The builder-block form (`arg { … }`, `separator { … }`) is used rather than a value argument because the per-type
 * builders (`text`, `translatable`, …) are members of
 * [io.github.lmliam.kotventure.core.component.ComponentScope] that return `Unit`, so an unwrapped `text("…")` could not
 * type-check as a [net.kyori.adventure.text.ComponentLike] value. Both [io.github.lmliam.kotventure.core.translatable]
 * `arg` and [io.github.lmliam.kotventure.core.selector] `separator` expose a `TextScope` block overload for exactly this.
 */
private fun KotlinSourceBuilder.appendComponentArgument(
    label: String,
    component: Component,
) {
    block(label) { appendRoot(component) }
}

private fun KotlinSourceBuilder.appendStyle(style: Style) {
    style.color()?.let { color -> line("color(${colorLiteral(color)})") }

    MiniMessageToDslSupport.decorations.forEach { (decoration, functionName) ->
        if (style.decoration(decoration) == State.TRUE) {
            line("$functionName()")
        }
    }

    val disabledDecorations =
        MiniMessageToDslSupport.decorations.filter { (decoration) -> style.decoration(decoration) == State.FALSE }
    if (disabledDecorations.isNotEmpty()) {
        block("style") {
            disabledDecorations.forEach { (_, functionName) -> line("$functionName(false)") }
        }
    }

    style.clickEvent()?.let { event -> appendClickEvent(event) }
    style.hoverEvent()?.let { event -> appendHoverEvent(event) }
}

private fun KotlinSourceBuilder.appendClickEvent(event: ClickEvent<*>) {
    block("click") {
        when (event.action()) {
            ClickEvent.Action.OPEN_URL -> line("openUrl(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.OPEN_FILE -> line("openFile(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.RUN_COMMAND -> line("run(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.SUGGEST_COMMAND -> line("suggest(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.CHANGE_PAGE -> line("changePage(${event.intPayload()})")
            ClickEvent.Action.COPY_TO_CLIPBOARD -> line("copy(\"${escapeKotlinString(event.textPayload())}\")")
            else -> line("// callback not representable")
        }
    }
}

private fun ClickEvent<*>.textPayload(): String = (payload() as ClickEvent.Payload.Text).value()

private fun ClickEvent<*>.intPayload(): Int = (payload() as ClickEvent.Payload.Int).integer()

private fun KotlinSourceBuilder.appendHoverEvent(event: HoverEvent<*>) {
    block("hover") {
        when (event.action()) {
            HoverEvent.Action.SHOW_TEXT -> block("text") { appendRoot(event.value() as Component) }
            HoverEvent.Action.SHOW_ITEM -> appendShowItem(event.value() as HoverEvent.ShowItem)
            HoverEvent.Action.SHOW_ENTITY -> appendShowEntity(event.value() as HoverEvent.ShowEntity)
            else -> throw IllegalArgumentException(
                "miniToDsl does not yet support the ${event.action().name()} hover action.",
            )
        }
    }
}

private fun KotlinSourceBuilder.appendShowItem(item: HoverEvent.ShowItem) {
    require(item.nbt() == null) {
        "miniToDsl does not yet support legacy show-item NBT payloads."
    }

    val arguments =
        buildList<() -> Unit> {
            add { line("key = ${keyLiteral(item.item())}") }
            if (item.count() != 1) add { line("count = ${item.count()}") }
            if (item.dataComponents().isNotEmpty()) add { appendDataComponents(item.dataComponents()) }
        }

    if (arguments.size == 1) {
        line("item(${keyLiteral(item.item())})")
        return
    }

    openArguments("item(", arguments)
    line(")")
}

private fun KotlinSourceBuilder.appendDataComponents(dataComponents: Map<Key, DataComponentValue>) {
    val entries: List<() -> Unit> =
        dataComponents.entries
            .sortedBy { (key, _) -> key.asString() }
            .map { (key, value) ->
                { line("${keyLiteral(key)} to ${dataComponentValueLiteral(value)}") }
            }

    openArguments("dataComponents = mapOf(", entries)
    line(")")
}

private fun KotlinSourceBuilder.appendShowEntity(entity: HoverEvent.ShowEntity) {
    val arguments: List<() -> Unit> =
        listOf(
            { line("type = ${keyLiteral(entity.type())}") },
            { line("id = UUID.fromString(\"${entity.id()}\")") },
        )

    openArguments("entity(", arguments)

    val name = entity.name()
    if (name == null) {
        line(")")
    } else {
        block(")") { appendRoot(name) }
    }
}
