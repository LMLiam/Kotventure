package io.github.lmliam.kotventure.core.selector

internal object EntitySelectorRenderer {
    fun render(
        head: String,
        state: EntitySelectorState,
    ): EntitySelector {
        val arguments =
            buildList {
                state.type?.renderValues { it }?.forEach { add("type=$it") }
                state.name?.renderValues(::renderName)?.forEach { add("name=$it") }
                state.x?.let { add("x=${formatSelectorNumber(it)}") }
                state.y?.let { add("y=${formatSelectorNumber(it)}") }
                state.z?.let { add("z=${formatSelectorNumber(it)}") }
                state.dx?.let { add("dx=${formatSelectorNumber(it)}") }
                state.dy?.let { add("dy=${formatSelectorNumber(it)}") }
                state.dz?.let { add("dz=${formatSelectorNumber(it)}") }
                state.distance?.let { add("distance=${it.rendered}") }
                state.xRotation?.let { add("x_rotation=${it.rendered}") }
                state.yRotation?.let { add("y_rotation=${it.rendered}") }
                state.level?.let { add("level=${it.rendered}") }
                state.gamemode?.renderValues { it.value }?.forEach { add("gamemode=$it") }
                state.limit?.let { add("limit=$it") }
                state.sort?.let { add("sort=${it.value}") }
                state.tags.forEach { add("tag=$it") }
            }
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("$head$suffix")
    }

    private fun renderName(value: String): String = if (needsQuoting(value)) "\"${escapeQuotes(value)}\"" else value

    private fun needsQuoting(value: String): Boolean = value.any { !it.isAllowedInUnquotedString() }

    private fun Char.isAllowedInUnquotedString(): Boolean =
        this in '0'..'9' ||
            this in 'A'..'Z' ||
            this in 'a'..'z' ||
            this == '_' ||
            this == '-' ||
            this == '.' ||
            this == '+'

    private fun escapeQuotes(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
}
