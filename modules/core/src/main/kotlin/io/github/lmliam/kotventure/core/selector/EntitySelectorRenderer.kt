package io.github.lmliam.kotventure.core.selector

internal object EntitySelectorRenderer {
    fun render(
        head: String,
        builder: EntitySelectorBuilder,
    ): EntitySelector {
        val arguments =
            buildList {
                builder.typeFilters.entries.forEach { entry -> add("type=${entry.renderValue { it }}") }
                builder.nameFilters.entries.forEach { add("name=${it.renderValue(::renderName)}") }
                (OriginAxis.entries + VolumeAxis.entries).forEach { axis ->
                    builder.coordinates[axis]?.let { add("${axis.argument}=${formatSelectorNumber(it)}") }
                }
                builder.distance?.let { add("distance=${it.rendered}") }
                builder.pitch?.let { add("x_rotation=${it.rendered}") }
                builder.yaw?.let { add("y_rotation=${it.rendered}") }
                builder.level?.let { add("level=${it.rendered}") }
                builder.gamemodeFilters.entries.forEach { add("gamemode=${it.renderValue { mode -> mode.value }}") }
                builder.teamFilters.entries.forEach { entry -> add("team=${entry.renderValue { it }}") }
                builder.limit?.let { add("limit=$it") }
                builder.sort?.let { add("sort=${it.value}") }
                builder.tagFilters.entries.forEach { entry -> add("tag=${entry.renderValue { it }}") }
            }
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("$head$suffix")
    }

    private fun <T> SelectorFilterEntry<T>.renderValue(render: (T) -> String): String {
        val prefix = if (polarity == SelectorFilterPolarity.NEGATIVE) "!" else ""
        return "$prefix${render(value)}"
    }

    private fun renderName(value: String): String = if (needsQuoting(value)) "\"${escapeQuotes(value)}\"" else value

    private fun needsQuoting(value: String): Boolean = value.any { !it.isAllowedInUnquotedSelectorToken() }

    private fun escapeQuotes(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
}
