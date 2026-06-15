package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
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

/** Emits [component], unwrapping a content-less, style-less root into a bare sequence of its children. */
private fun KotlinSourceBuilder.appendRoot(component: Component) {
    if (component is TextComponent &&
        component.content().isEmpty() &&
        !MiniMessageToDslSupport.hasDslOutput(component.style())
    ) {
        component.children().forEach { appendComponent(it as TextComponent) }
        return
    }

    appendComponent(component as TextComponent)
}

private fun KotlinSourceBuilder.appendComponent(component: TextComponent) {
    val text = component.content()
    val hasBlockBody = MiniMessageToDslSupport.hasDslOutput(component.style()) || component.children().isNotEmpty()

    if (!hasBlockBody) {
        line("text(\"${escapeKotlinString(text)}\")")
        return
    }

    val header = if (text.isEmpty()) "text" else "text(\"${escapeKotlinString(text)}\")"
    block(header) {
        appendStyle(component.style())
        component.children().forEach { appendComponent(it as TextComponent) }
    }
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
        dataComponents.entries.map { (key, value) ->
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
