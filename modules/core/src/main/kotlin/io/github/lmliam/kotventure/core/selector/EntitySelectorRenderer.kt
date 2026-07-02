package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.renderCompound

internal object EntitySelectorRenderer {
    fun render(
        head: EntitySelectorHead,
        builder: EntitySelectorBuilder,
    ): EntitySelector {
        val arguments =
            buildList {
                addAll(builder.typeFilters.rendered { it })
                addAll(builder.nameFilters.rendered(::renderName))
                (OriginAxis.entries + VolumeAxis.entries).forEach { axis ->
                    builder.coordinates[axis]?.let { add("${axis.argument}=${formatSelectorNumber(it)}") }
                }
                builder.distance?.let { add("distance=${it.rendered}") }
                builder.pitch?.let { add("x_rotation=${it.rendered}") }
                builder.yaw?.let { add("y_rotation=${it.rendered}") }
                builder.level?.let { add("level=${it.rendered}") }
                builder.scores?.let { scores ->
                    add(scores.entries.joinToString(",", "scores={", "}", transform = ::renderScore))
                }
                builder.advancements?.let { advancements ->
                    add(advancements.entries.joinToString(",", "advancements={", "}", transform = ::renderAdvancement))
                }
                addAll(builder.gamemodeFilters.rendered { it.value })
                addAll(builder.teamFilters.rendered { it })
                builder.limit?.let { add("limit=$it") }
                builder.sort?.let { add("sort=${it.value}") }
                addAll(builder.tagFilters.rendered { it })
                addAll(builder.nbtFilters.rendered(::renderCompound))
                addAll(builder.predicateFilters.rendered { it })
            }
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("${head.token}$suffix")
    }

    private fun <T> SelectorFilterGroup<T>.rendered(renderValue: (T) -> String): List<String> =
        entries.map { entry ->
            val prefix = if (entry.polarity == SelectorFilterPolarity.NEGATIVE) "!" else ""
            "$argument=$prefix${renderValue(entry.value)}"
        }

    private fun renderScore(score: Map.Entry<String, SelectorIntRange>): String = "${score.key}=${score.value.rendered}"

    private fun renderAdvancement(advancement: Map.Entry<String, AdvancementCondition>): String =
        "${advancement.key}=${renderAdvancementCondition(advancement.value)}"

    private fun renderAdvancementCondition(condition: AdvancementCondition): String =
        when (condition) {
            is AdvancementCondition.Completed -> condition.completed.toString()
            is AdvancementCondition.Criteria ->
                condition.criteria.entries.joinToString(",", "{", "}") { "${it.key}=${it.value}" }
        }

    private fun renderName(value: String): String = if (needsQuoting(value)) "\"${escapeQuotes(value)}\"" else value

    private fun needsQuoting(value: String): Boolean = value.any { !it.isAllowedInUnquotedSelectorToken() }

    private fun escapeQuotes(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
}
