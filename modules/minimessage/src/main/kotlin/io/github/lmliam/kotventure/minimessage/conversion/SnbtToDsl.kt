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

/**
 * Converts an SNBT compound string into the body of a Kotventure NBT DSL block: the `"key" eq value`
 * calls that go inside `nbt { ... }` or `component(key) { ... }`. An empty compound renders to `""`.
 *
 * The SNBT is parsed with Adventure's [TagStringIO], then each entry is rendered to its DSL form.
 * Keys are emitted in alphabetical order so the output is deterministic and JDK-independent (NBT
 * compounds are unordered, so reordering is semantically transparent).
 *
 * Returns `null` when the SNBT is malformed, has trailing content, or contains a tag type the typed
 * DSL cannot express, signalling the caller to fall back to `nbt("raw")`.
 */
internal fun snbtToDslBody(snbt: String): String? {
    val compound =
        try {
            TagStringIO.tagStringIO().asCompound(snbt)
        } catch (e: IOException) {
            return null
        }
    return renderCompoundBody(compound)
}

private fun renderCompoundBody(compound: CompoundBinaryTag): String? {
    val entries =
        compound.keySet().sorted().map { key ->
            val value = renderValue(compound.get(key) ?: return null) ?: return null
            "\"${escapeKotlinString(key)}\" eq $value"
        }
    return entries.joinToString("; ")
}

private fun renderValue(tag: BinaryTag): String? =
    when (tag) {
        is ByteBinaryTag -> renderByteLiteral(tag.value())
        is ShortBinaryTag -> renderShortLiteral(tag.value())
        is IntBinaryTag -> renderIntLiteral(tag.value())
        is LongBinaryTag -> renderLongLiteral(tag.value())
        is FloatBinaryTag -> "${tag.value()}f"
        is DoubleBinaryTag -> "${tag.value()}"
        is StringBinaryTag -> "\"${escapeKotlinString(tag.value())}\""
        is ByteArrayBinaryTag -> "byteArrayOf(${tag.value().joinToString(", ")})"
        is IntArrayBinaryTag -> "intArrayOf(${tag.value().joinToString(", ") { renderIntLiteral(it) }})"
        is LongArrayBinaryTag -> "longArrayOf(${tag.value().joinToString(", ") { renderLongLiteral(it) }})"
        is CompoundBinaryTag -> renderCompoundBody(tag)?.let { if (it.isEmpty()) "{ }" else "{ $it }" }
        is ListBinaryTag -> renderListLiteral(tag)
        else -> null // Any other tag has no typed DSL form yet → raw fallback.
    }

private fun renderListLiteral(list: ListBinaryTag): String? {
    val first = list.firstOrNull() ?: return "list()"
    if (first is CompoundBinaryTag) return renderCompoundElementList(list)
    val elements = list.map { renderValue(it) ?: return null }
    return "list(${elements.joinToString(", ")})"
}

private fun renderCompoundElementList(list: ListBinaryTag): String? {
    val elements =
        list.map { element ->
            val body = renderCompoundBody(element as CompoundBinaryTag) ?: return null
            if (body.isEmpty()) "{ }" else "{ $body }"
        }
    return "list(${elements.joinToString(", ")})"
}

/** Emits a [Byte] literal, parenthesising negatives so `(-5).toByte()` keeps the `Byte` type. */
private fun renderByteLiteral(value: Byte): String = if (value < 0) "($value).toByte()" else "$value.toByte()"

/** Emits a [Short] literal, parenthesising negatives so `(-5).toShort()` keeps the `Short` type. */
private fun renderShortLiteral(value: Short): String = if (value < 0) "($value).toShort()" else "$value.toShort()"

/** Emits an [Int] literal, using `Int.MIN_VALUE` (which has no valid negated-literal form). */
private fun renderIntLiteral(value: Int): String = if (value == Int.MIN_VALUE) "Int.MIN_VALUE" else "$value"

/** Emits a [Long] literal, using `Long.MIN_VALUE` (which has no valid negated-literal form). */
private fun renderLongLiteral(value: Long): String = if (value == Long.MIN_VALUE) "Long.MIN_VALUE" else "${value}L"
