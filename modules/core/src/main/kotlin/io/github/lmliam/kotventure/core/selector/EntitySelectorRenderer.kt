package io.github.lmliam.kotventure.core.selector

internal object EntitySelectorRenderer {
    fun render(
        head: String,
        builder: EntitySelectorBuilder,
    ): EntitySelector {
        val arguments =
            buildList {
                builder.type?.renderValues { it }?.forEach { add("type=$it") }
                builder.name?.renderValues(::renderName)?.forEach { add("name=$it") }
                (OriginAxis.entries + VolumeAxis.entries).forEach { axis ->
                    builder.coordinates[axis]?.let { add("${axis.argument}=${formatSelectorNumber(it)}") }
                }
                builder.distance?.let { add("distance=${it.rendered}") }
                builder.pitch?.let { add("x_rotation=${it.rendered}") }
                builder.yaw?.let { add("y_rotation=${it.rendered}") }
                builder.level?.let { add("level=${it.rendered}") }
                builder.gamemode?.renderValues { it.value }?.forEach { add("gamemode=$it") }
                builder.team?.renderValues { it }?.forEach { add("team=$it") }
                builder.limit?.let { add("limit=$it") }
                builder.sort?.let { add("sort=${it.value}") }
                builder.tags.forEach { add("tag=$it") }
            }
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("$head$suffix")
    }

    private fun <T> SelectorFilter<T>.renderValues(render: (T) -> String): List<String> =
        when (this) {
            is SelectorFilter.Positive -> listOf(render(value))
            is SelectorFilter.Negative -> values.map { value -> "!${render(value)}" }
        }

    private fun renderName(value: String): String = if (needsQuoting(value)) "\"${escapeQuotes(value)}\"" else value

    private fun needsQuoting(value: String): Boolean = value.any { !it.isAllowedInUnquotedSelectorToken() }

    private fun escapeQuotes(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
}
