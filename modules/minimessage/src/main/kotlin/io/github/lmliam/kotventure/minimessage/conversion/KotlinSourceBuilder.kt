package io.github.lmliam.kotventure.minimessage.conversion

/**
 * Collects Kotlin source lines and controls their indentation.
 */
internal class KotlinSourceBuilder {
    private val lines = mutableListOf<String>()
    private var depth = 0

    /** Appends [text] at the current indentation. */
    fun line(text: String) {
        lines += INDENT.repeat(depth) + text
    }

    /** Emits a block with [header] and an indented [body]. */
    fun block(
        header: String,
        body: KotlinSourceBuilder.() -> Unit,
    ) {
        line("$header {")
        indented(body)
        line("}")
    }

    /** Emits [body] one indentation level deeper. */
    fun indented(body: KotlinSourceBuilder.() -> Unit) {
        depth++
        body()
        depth--
    }

    /**
     * Emits [opener] and the indented [arguments] without a trailing comma.
     *
     * The caller must emit the closing parenthesis.
     */
    fun openArguments(
        opener: String,
        arguments: List<() -> Unit>,
    ) {
        line(opener)
        indented { commaSeparated(arguments) }
    }

    override fun toString(): String = lines.joinToString(separator = "\n")

    private fun commaSeparated(parts: List<() -> Unit>) {
        parts.forEachIndexed { index, part ->
            part()
            if (index != parts.lastIndex) {
                lines[lines.lastIndex] += ","
            }
        }
    }

    private companion object {
        const val INDENT: String = "    "
    }
}
