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

private val snbtReader = TagStringIO.tagStringIO()

/**
 * Converts an SNBT compound into the body of a Kotventure NBT block.
 *
 * The result contains the `"key" eq value` expressions. Compound keys are sorted alphabetically because NBT compounds
 * do not define an iteration order. An empty compound produces an empty string.
 *
 * @return the generated DSL body, or `null` when [snbt] is malformed, contains trailing input, or contains an
 * unsupported tag type.
 */
internal fun snbtToDslBody(snbt: String): String? {
    val compound =
        try {
            snbtReader.asCompound(snbt)
        } catch (_: Exception) {
            null
        }

    return compound?.toDslBody()
}

private fun CompoundBinaryTag.toDslBody(): String? {
    val entries = ArrayList<String>(keySet().size)

    for (key in keySet().sorted()) {
        val tag = get(key) ?: return null
        val value = tag.toDslLiteral() ?: return null

        entries += "${quoted(key)} eq $value"
    }

    return entries.joinToString(separator = "; ")
}

private fun BinaryTag.toDslLiteral(): String? =
    when (this) {
        is ByteBinaryTag ->
            kotlinByteLiteral(value())

        is ShortBinaryTag ->
            kotlinShortLiteral(value())

        is IntBinaryTag ->
            kotlinIntLiteral(value())

        is LongBinaryTag ->
            kotlinLongLiteral(value())

        is FloatBinaryTag ->
            kotlinFloatLiteral(value())

        is DoubleBinaryTag ->
            kotlinDoubleLiteral(value())

        is StringBinaryTag ->
            quoted(value())

        is ByteArrayBinaryTag ->
            value().toDslLiteral()

        is IntArrayBinaryTag ->
            value().toDslLiteral()

        is LongArrayBinaryTag ->
            value().toDslLiteral()

        is CompoundBinaryTag ->
            toDslBody()?.toCompoundLiteral()

        is ListBinaryTag ->
            toDslLiteral()

        else ->
            null
    }

private fun ListBinaryTag.toDslLiteral(): String? {
    val elements = ArrayList<String>(size())

    for (element in this) {
        elements += element.toDslLiteral() ?: return null
    }

    return elements.joinToString(
        separator = ", ",
        prefix = "list(",
        postfix = ")",
    )
}

private fun ByteArray.toDslLiteral(): String =
    joinToString(
        separator = ", ",
        prefix = "byteArrayOf(",
        postfix = ")",
    )

private fun IntArray.toDslLiteral(): String =
    joinToString(
        separator = ", ",
        prefix = "intArrayOf(",
        postfix = ")",
        transform = ::kotlinIntLiteral,
    )

private fun LongArray.toDslLiteral(): String =
    joinToString(
        separator = ", ",
        prefix = "longArrayOf(",
        postfix = ")",
        transform = ::kotlinLongLiteral,
    )

private fun String.toCompoundLiteral(): String =
    if (isEmpty()) {
        "{ }"
    } else {
        "{ $this }"
    }
