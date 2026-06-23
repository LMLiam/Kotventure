package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent

internal fun KotlinSourceBuilder.appendClickEvent(event: ClickEvent<*>) {
    block("click") {
        when (event.action()) {
            ClickEvent.Action.OPEN_URL -> line("openUrl(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.OPEN_FILE -> line("openFile(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.RUN_COMMAND -> line("run(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.SUGGEST_COMMAND -> line("suggest(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.CHANGE_PAGE -> line("changePage(${event.intPayload()})")
            ClickEvent.Action.COPY_TO_CLIPBOARD -> line("copy(\"${escapeKotlinString(event.textPayload())}\")")
            else -> conversionError("miniToDsl cannot represent the ${event.action().name()} click action.")
        }
    }
}

private fun ClickEvent<*>.textPayload(): String = (payload() as ClickEvent.Payload.Text).value()

private fun ClickEvent<*>.intPayload(): Int = (payload() as ClickEvent.Payload.Int).integer()

internal fun KotlinSourceBuilder.appendHoverEvent(event: HoverEvent<*>) {
    block("hover") {
        when (event.action()) {
            HoverEvent.Action.SHOW_TEXT -> block("text") { appendRoot(event.value() as Component) }
            HoverEvent.Action.SHOW_ITEM -> appendShowItem(event.value() as HoverEvent.ShowItem)
            HoverEvent.Action.SHOW_ENTITY -> appendShowEntity(event.value() as HoverEvent.ShowEntity)
            else -> conversionError("miniToDsl cannot represent the ${event.action().name()} hover action.")
        }
    }
}

private fun KotlinSourceBuilder.appendShowItem(item: HoverEvent.ShowItem) {
    val itemKey = keyLiteral(item.item())
    val arguments =
        buildList<() -> Unit> {
            add { line("key = $itemKey") }
            if (item.count() != 1) add { line("count = ${item.count()}") }
            if (item.dataComponents().isNotEmpty()) add { appendDataComponents(item.dataComponents()) }
        }

    if (arguments.size == 1) {
        line("item($itemKey)")
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
