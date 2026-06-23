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
 * The tree is serialised with the Adventure JSON serializer (lossless for every component type, including NBT, score,
 * and selector), then re-laid-out with stable two-space indentation so committed snapshots produce reviewable
 * line-by-line diffs. Member order is preserved end-to-end, keeping the output deterministic across runs.
 */
internal fun Component.toSnapshotJson(compact: Boolean = false): String {
    val component = if (compact) compact() else this
    return prettyPrinter.toJson(JsonParser.parseString(component.toJson()))
}

/**
 * Normalises a snapshot string for comparison: collapses Windows line endings and drops trailing blank lines, so a
 * checkout that rewrites newlines or an editor that adds a final newline does not register as a mismatch.
 */
internal fun String.normalizeSnapshot(): String = replace("\r\n", "\n").trimEnd('\n')
