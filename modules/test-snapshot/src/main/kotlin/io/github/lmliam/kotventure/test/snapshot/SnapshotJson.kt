package io.github.lmliam.kotventure.test.snapshot

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lmliam.kotventure.serializer.toJson
import net.kyori.adventure.text.Component

private val prettyPrinter =
    GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

/**
 * Serialises this component to the JSON form that snapshots store.
 *
 * The function optionally compacts the component. It then uses the project JSON serialiser and
 * applies stable two-space indentation.
 */
internal fun Component.toSnapshotJson(compact: Boolean = false): String {
    val component = if (compact) compact() else this
    return prettyPrinter.toJson(JsonParser.parseString(component.toJson()))
}

/**
 * Normalises line endings and removes trailing newline characters from a snapshot.
 */
internal fun String.normalizeSnapshot(): String = replace("\r\n", "\n").trimEnd('\n')
