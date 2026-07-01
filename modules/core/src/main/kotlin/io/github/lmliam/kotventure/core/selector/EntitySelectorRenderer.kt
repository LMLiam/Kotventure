package io.github.lmliam.kotventure.core.selector

internal object EntitySelectorRenderer {
    fun render(
        head: String,
        state: EntitySelectorState,
    ): EntitySelector {
        val arguments =
            buildList {
                state.type?.let { add("type=$it") }
                state.name?.let { add("name=${renderName(it)}") }
                state.distance?.let { add("distance=${it.rendered}") }
                state.level?.let { add("level=${it.rendered}") }
                state.gamemode?.let { add("gamemode=${it.value}") }
                state.limit?.let { add("limit=$it") }
                state.sort?.let { add("sort=${it.value}") }
                state.tags.forEach { add("tag=$it") }
            }
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("$head$suffix")
    }

    private fun renderName(value: String): String = if (needsQuoting(value)) "\"${escapeQuotes(value)}\"" else value

    private fun needsQuoting(value: String): Boolean = value.any { it in ",[]{}\" " }

    private fun escapeQuotes(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
}
