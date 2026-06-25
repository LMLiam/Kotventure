package io.github.lmliam.kotventure.minimessage.conversion

/**
 * Accumulates indented lines of Kotlin source, owning indentation so callers describe the structure to emit rather than
 * the whitespace in front of it.
 */
internal class KotlinSourceBuilder {
    private val lines = mutableListOf<String>()
    private var depth = 0

    /** Appends [text] as one line at the current indentation. */
    fun line(text: String) {
        lines += INDENT.repeat(depth) + text
    }

    /** Emits `$header {`, runs [body] one level deeper, then the closing brace. */
    fun block(
        header: String,
        body: KotlinSourceBuilder.() -> Unit,
    ) {
        line("$header {")
        indented(body)
        line("}")
    }

    /** Runs [body] one indentation level deeper. */
    fun indented(body: KotlinSourceBuilder.() -> Unit) {
        depth++
        body()
        depth--
    }

    /**
     * Emits [opener], then each lambda in [arguments] on its own indented line, comma-separated with no trailing comma.
     * The caller emits the closer so the choice between `)` and `) {` stays theirs.
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
