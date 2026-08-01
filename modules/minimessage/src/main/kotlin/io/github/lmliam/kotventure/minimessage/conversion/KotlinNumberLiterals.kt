package io.github.lmliam.kotventure.minimessage.conversion

/** Returns a [Byte] literal and parenthesises a negative value. */
internal fun kotlinByteLiteral(value: Byte): String =
    narrowingLiteral(
        value = value.toLong(),
        type = "Byte",
    )

/** Returns a [Short] literal and parenthesises a negative value. */
internal fun kotlinShortLiteral(value: Short): String =
    narrowingLiteral(
        value = value.toLong(),
        type = "Short",
    )

/** Returns an [Int] literal, including the [Int.MIN_VALUE] constant. */
internal fun kotlinIntLiteral(value: Int): String =
    if (value == Int.MIN_VALUE) {
        "Int.MIN_VALUE"
    } else {
        value.toString()
    }

/** Returns a [Long] literal, including the [Long.MIN_VALUE] constant. */
internal fun kotlinLongLiteral(value: Long): String =
    if (value == Long.MIN_VALUE) {
        "Long.MIN_VALUE"
    } else {
        "${value}L"
    }

/** Returns a [Float] literal, including non-finite constants. */
internal fun kotlinFloatLiteral(value: Float): String =
    when {
        value.isNaN() -> "Float.NaN"
        value == Float.POSITIVE_INFINITY -> "Float.POSITIVE_INFINITY"
        value == Float.NEGATIVE_INFINITY -> "Float.NEGATIVE_INFINITY"
        else -> "${value}f"
    }

/** Returns a [Double] literal, including non-finite constants. */
internal fun kotlinDoubleLiteral(value: Double): String =
    when {
        value.isNaN() -> "Double.NaN"
        value == Double.POSITIVE_INFINITY -> "Double.POSITIVE_INFINITY"
        value == Double.NEGATIVE_INFINITY -> "Double.NEGATIVE_INFINITY"
        else -> value.toString()
    }

/** Returns [value] as the applicable Kotlin numeric literal. */
internal fun kotlinNumberLiteral(value: Number): String =
    when (value) {
        is Byte -> kotlinByteLiteral(value)
        is Short -> kotlinShortLiteral(value)
        is Int -> kotlinIntLiteral(value)
        is Long -> kotlinLongLiteral(value)
        is Float -> kotlinFloatLiteral(value)
        is Double -> kotlinDoubleLiteral(value)
        else -> value.toString()
    }

private fun narrowingLiteral(
    value: Long,
    type: String,
): String {
    val receiver =
        if (value < 0) {
            "($value)"
        } else {
            value.toString()
        }
    return "$receiver.to$type()"
}
