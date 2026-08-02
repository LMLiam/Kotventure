package io.github.lmliam.kotventure.minimessage.conversion

/**
 * Builds deterministic, indented Kotlin source
 */
internal class KotlinSourceBuilder {
    private val source = StringBuilder()
    private var indentationDepth = 0
    private var lineCount = 0

    /** Appends one source line at the current indentation. */
    fun line(text: String) {
        if (lineCount > 0) source.append('\n')
        source.append(INDENTATION_UNIT.repeat(indentationDepth))
        source.append(text)
        lineCount++
    }

    /** Appends [header] followed by an indented Kotlin block. */
    fun block(
        header: String,
        body: KotlinSourceBuilder.() -> Unit,
    ) = delimited("$header {", "}", body)

    /**
     * Appends a multiline function call and an optional trailing-lambda [body].
     *
     * Each argument must emit at least one source line.
     * Commas are placed after the final line emitted by each argument except the last.
     */
    fun call(
        function: String,
        arguments: List<KotlinSourceBuilder.() -> Unit>,
        body: (KotlinSourceBuilder.() -> Unit)? = null,
    ) {
        line("$function(")
        indented { appendArguments(arguments) }

        if (body == null) {
            line(")")
        } else {
            delimited(") {", "}", body)
        }
    }

    /** Returns the generated source. */
    fun build(): String = source.toString()

    private fun delimited(
        opening: String,
        closing: String,
        body: KotlinSourceBuilder.() -> Unit,
    ) {
        line(opening)
        indented(body)
        line(closing)
    }

    private fun appendArguments(arguments: List<KotlinSourceBuilder.() -> Unit>) {
        arguments.forEachIndexed { index, argument ->
            val initialLineCount = lineCount
            argument(this)

            check(lineCount > initialLineCount) {
                "A Kotlin source argument must emit at least one line."
            }

            if (index < arguments.lastIndex) {
                source.append(',')
            }
        }
    }

    private inline fun indented(body: KotlinSourceBuilder.() -> Unit) {
        indentationDepth++

        try {
            body(this)
        } finally {
            indentationDepth--
        }
    }

    private companion object {
        const val INDENTATION_UNIT = "    "
    }
}

internal fun buildKotlinSource(body: KotlinSourceBuilder.() -> Unit): String = KotlinSourceBuilder().apply(body).build()
