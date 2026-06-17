package io.github.lmliam.kotventure.test.snapshot

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lmliam.kotventure.core.text.compacted
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
 * The tree is first [compacted] so structurally different but visually identical components normalise to the same
 * shape, then serialised with the Adventure JSON serializer (lossless for every component type, including NBT, score,
 * and selector), and finally re-laid-out with stable two-space indentation so committed snapshots produce reviewable
 * line-by-line diffs. Member order is preserved end-to-end, keeping the output deterministic across runs.
 */
internal fun Component.toSnapshotJson(): String = prettyPrinter.toJson(JsonParser.parseString(compacted().toJson()))

/**
 * Normalises a snapshot string for comparison: collapses Windows line endings and drops trailing blank lines, so a
 * checkout that rewrites newlines or an editor that adds a final newline does not register as a mismatch.
 */
internal fun String.normalizeSnapshot(): String = replace("\r\n", "\n").trimEnd('\n')
