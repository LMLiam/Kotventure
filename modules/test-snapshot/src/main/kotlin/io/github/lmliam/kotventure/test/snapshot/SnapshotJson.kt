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
 * Serialises this component to the canonical form stored in snapshots.
 *
 * The Adventure JSON serialiser serialises the tree without loss for all component types. These types include NBT,
 * score, and selector components. The function then applies stable two-space indentation. It preserves member order so
 * that the output is the same for each run and produces clear line-by-line diffs.
 */
internal fun Component.toSnapshotJson(compact: Boolean = false): String {
    val component = if (compact) compact() else this
    return prettyPrinter.toJson(JsonParser.parseString(component.toJson()))
}

/**
 * Normalises a snapshot string for comparison. It changes Windows line endings and removes trailing blank lines. Thus,
 * newline changes from a checkout or editor do not cause a mismatch.
 */
internal fun String.normalizeSnapshot(): String = replace("\r\n", "\n").trimEnd('\n')
