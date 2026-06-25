package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument

internal fun KotlinSourceBuilder.appendArgument(argument: TranslationArgument) {
    when (val value = argument.value()) {
        is Component -> appendComponentArgument("arg", value)
        is Boolean -> line("arg($value)")
        is Number -> line("arg(${value.toKotlinSource()})")
        else ->
            conversionError(
                "miniToDsl cannot represent ${value::class.qualifiedName} translatable argument.",
            )
    }
}

private fun Number.toKotlinSource(): String =
    when (this) {
        is Double -> nonFiniteSource() ?: toString()
        is Float -> nonFiniteSource() ?: "${this}f"
        is Long -> toKotlinSource()
        is Byte -> toKotlinSource()
        is Short -> toKotlinSource()
        else -> toString()
    }

private fun Long.toKotlinSource(): String = if (this == Long.MIN_VALUE) "Long.MIN_VALUE" else "${this}L"

private fun Byte.toKotlinSource(): String = "($this).toByte()"

private fun Short.toKotlinSource(): String = "($this).toShort()"

private fun Double.nonFiniteSource(): String? =
    when {
        isNaN() -> "Double.NaN"
        this == Double.POSITIVE_INFINITY -> "Double.POSITIVE_INFINITY"
        this == Double.NEGATIVE_INFINITY -> "Double.NEGATIVE_INFINITY"
        else -> null
    }

private fun Float.nonFiniteSource(): String? =
    when {
        isNaN() -> "Float.NaN"
        this == Float.POSITIVE_INFINITY -> "Float.POSITIVE_INFINITY"
        this == Float.NEGATIVE_INFINITY -> "Float.NEGATIVE_INFINITY"
        else -> null
    }
