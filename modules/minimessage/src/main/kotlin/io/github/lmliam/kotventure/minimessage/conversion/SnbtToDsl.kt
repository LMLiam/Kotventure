package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.ByteArrayBinaryTag
import net.kyori.adventure.nbt.ByteBinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.DoubleBinaryTag
import net.kyori.adventure.nbt.FloatBinaryTag
import net.kyori.adventure.nbt.IntArrayBinaryTag
import net.kyori.adventure.nbt.IntBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.kyori.adventure.nbt.LongArrayBinaryTag
import net.kyori.adventure.nbt.LongBinaryTag
import net.kyori.adventure.nbt.ShortBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import java.io.IOException

private val tagStringIO = TagStringIO.tagStringIO()

/**
 * Converts an SNBT compound string into the body of a Kotventure NBT DSL block.
 *
 * The result is the sequence of `"key" eq value` calls that go inside `nbt { ... }` or
 * `component(key) { ... }`. An empty compound renders to `""`.
 *
 * Keys are emitted in alphabetical order for deterministic, JDK-independent output.
 * Since NBT compounds are unordered, reordering is semantically transparent.
 *
 * @return the DSL body, or `null` if the SNBT is malformed, has trailing content,
 * or contains a tag type the typed DSL cannot express (triggering a raw fallback).
 */
internal fun snbtToDslBody(snbt: String): String? {
    val compound =
        try {
            tagStringIO.asCompound(snbt)
        } catch (_: Exception) {
            // Catches IOException and underlying parser exceptions (e.g., IllegalStateException)
            return null
        }
    return renderCompoundBody(compound)
}

private fun renderCompoundBody(compound: CompoundBinaryTag): String? =
    compound
        .keySet()
        .sorted()
        .map { key ->
            val tag = compound.get(key) ?: return null
            val value = renderTag(tag) ?: return null
            "\"${escapeKotlinString(key)}\" eq $value"
        }.joinToString("; ")

private fun renderTag(tag: BinaryTag): String? =
    when (tag) {
        is ByteBinaryTag -> kotlinByteLiteral(tag.value())
        is ShortBinaryTag -> kotlinShortLiteral(tag.value())
        is IntBinaryTag -> kotlinIntLiteral(tag.value())
        is LongBinaryTag -> kotlinLongLiteral(tag.value())
        is FloatBinaryTag -> kotlinFloatLiteral(tag.value())
        is DoubleBinaryTag -> kotlinDoubleLiteral(tag.value())
        is StringBinaryTag -> "\"${escapeKotlinString(tag.value())}\""
        is ByteArrayBinaryTag ->
            tag.value().joinToString(", ", prefix = "byteArrayOf(", postfix = ")")

        is IntArrayBinaryTag ->
            tag.value().joinToString(", ", prefix = "intArrayOf(", postfix = ")") { kotlinIntLiteral(it) }

        is LongArrayBinaryTag ->
            tag.value().joinToString(", ", prefix = "longArrayOf(", postfix = ")") { kotlinLongLiteral(it) }

        is CompoundBinaryTag ->
            renderCompoundBody(tag)?.toCompoundLiteral()

        is ListBinaryTag ->
            renderListLiteral(tag)

        else -> null // No typed DSL representation; fall back to raw SNBT.
    }

private fun renderListLiteral(list: ListBinaryTag): String? {
    if (list.size() == 0) return "list()"

    val elements = list.map { renderTag(it) ?: return null }
    return elements.joinToString(", ", prefix = "list(", postfix = ")")
}

private fun String.toCompoundLiteral(): String = if (isEmpty()) "{ }" else "{ $this }"
