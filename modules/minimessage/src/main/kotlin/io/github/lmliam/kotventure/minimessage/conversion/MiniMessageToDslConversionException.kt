package io.github.lmliam.kotventure.minimessage.conversion

internal class MiniMessageToDslConversionException(
    message: String,
) : IllegalArgumentException(message)

internal fun conversionError(message: String): Nothing = throw MiniMessageToDslConversionException(message)
