package io.github.lmliam.kotventure.minimessage.conversion

/** Returns [value] as an escaped, double-quoted Kotlin string literal. */
internal fun quoted(value: String): String =
    buildString(value.length + 2) {
        append('"')
        appendEscapedKotlinString(value)
        append('"')
    }

/** Escapes [value] for use inside a double-quoted Kotlin string literal. */
internal fun escapeKotlinString(value: String): String =
    buildString(value.length) {
        appendEscapedKotlinString(value)
    }

private fun StringBuilder.appendEscapedKotlinString(value: String) {
    value.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '$' -> append('\\').append('$')

            else ->
                if (character.isISOControl()) {
                    append("\\u")
                    append(
                        character.code
                        .toString(16)
                        .uppercase()
                        .padStart(4, '0'),
                            )
                } else {
                    append(character)
                }
        }
    }
}
