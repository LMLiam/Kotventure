package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
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
    val components = item.dataComponents()
    val arguments =
        buildList {
            add { line("key = $itemKey") }
            if (item.count() != 1) add { line("count = ${item.count()}") }
        }

    when {
        components.isEmpty() && arguments.size == 1 -> line("item($itemKey)")
        components.isEmpty() -> {
            openArguments("item(", arguments)
            line(")")
        }
        arguments.size == 1 -> block("item($itemKey)") { appendDataComponents(components) }
        else -> {
            openArguments("item(", arguments)
            block(")") { appendDataComponents(components) }
        }
    }
}

private fun KotlinSourceBuilder.appendDataComponents(components: Map<Key, DataComponentValue>) {
    components.entries
        .sortedBy { (key, _) -> key.asString() }
        .forEach { (key, value) -> appendDataComponent(keyLiteral(key), value) }
}

private fun KotlinSourceBuilder.appendDataComponent(
    keyLiteral: String,
    value: DataComponentValue,
) {
    when (value) {
        is DataComponentValue.Removed -> line("removed($keyLiteral)")
        is BinaryTagHolder -> appendNbtComponent(keyLiteral, value.string())
        is DataComponentValue.TagSerializable -> appendNbtComponent(keyLiteral, value.asBinaryTag().string())
        else -> conversionError("miniToDsl cannot represent data component value ${value::class.qualifiedName}.")
    }
}

private fun KotlinSourceBuilder.appendNbtComponent(
    keyLiteral: String,
    snbt: String,
) {
    when (val body = snbtToDslBody(snbt)) {
        null -> line("component($keyLiteral, nbt(\"${escapeKotlinString(snbt)}\"))")
        "" -> line("component($keyLiteral) { }")
        else -> line("component($keyLiteral) { $body }")
    }
}

private fun KotlinSourceBuilder.appendShowEntity(entity: HoverEvent.ShowEntity) {
    val arguments: List<() -> Unit> =
        listOf(
            { line("type = ${keyLiteral(entity.type())}") },
            { line("id = uuid(\"${entity.id()}\")") },
        )

    openArguments("entity(", arguments)

    val name = entity.name()
    if (name == null) {
        line(")")
    } else {
        block(")") { appendRoot(name) }
    }
}
