package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.EntitySelector
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorHead
import io.github.lmliam.kotventure.core.selector.SelectorAdvancementProgress
import io.github.lmliam.kotventure.core.selector.SelectorCoordinate
import io.github.lmliam.kotventure.core.selector.SelectorEntityType
import io.github.lmliam.kotventure.core.selector.SelectorIntRange
import io.github.lmliam.kotventure.core.selector.SelectorNegatable
import io.github.lmliam.kotventure.core.selector.SelectorRange
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument
import io.github.lmliam.kotventure.core.selector.SelectorStringCondition

/**
 * Emits the typed selector expression for [selector].
 *
 * Arguments retain model order. Coordinates in the same group are combined into one `origin` or `volume` call at the
 * position of that group's first coordinate.
 */
internal fun KotlinSourceBuilder.appendEntitySelector(selector: EntitySelector) {
    val factory = selector.head.dslFactoryName

    if (selector.arguments.isEmpty()) {
        line("$factory()")
    } else {
        block(factory) {
            appendSelectorArguments(selector.arguments)
        }
    }
}

private fun KotlinSourceBuilder.appendSelectorArguments(arguments: List<EntitySelectorArgument>) {
    val coordinatesByGroup =
        arguments
            .filterIsInstance<EntitySelectorArgument.Coordinate>()
            .groupBy { it.coordinate.groupFunctionName }
    val emittedCoordinateGroups = mutableSetOf<String>()

    arguments.forEach { argument ->
        when (argument) {
            is EntitySelectorArgument.Coordinate -> {
                val group = argument.coordinate.groupFunctionName

                if (emittedCoordinateGroups.add(group)) {
                    appendCoordinateGroup(
                        function = group,
                        coordinates = coordinatesByGroup.getValue(group),
                    )
                }
            }

            is EntitySelectorArgument.Range ->
                line(
                    "${argument.argument.dslFunctionName}(${argument.range.toDslArgument()})",
                )

            is EntitySelectorArgument.Level ->
                line("level(${argument.range.toDslArgument()})")

            is EntitySelectorArgument.Limit ->
                line("limit(${argument.value})")

            is EntitySelectorArgument.Sort ->
                line("sort(${argument.value.name.lowercase()})")

            is EntitySelectorArgument.GameMode ->
                line(
                    argument.withNegation(
                        "gamemode(${argument.value.name.lowercase()})",
                    ),
                )

            is EntitySelectorArgument.Name ->
                line(
                    argument.withNegation(
                        "name(${quoted(argument.value)})",
                    ),
                )

            is EntitySelectorArgument.Type ->
                line(
                    argument.withNegation(
                        argument.target.toDslCall(),
                    ),
                )

            is EntitySelectorArgument.Tag ->
                appendStringCondition(
                    function = "tag",
                    condition = argument.condition,
                )

            is EntitySelectorArgument.Team ->
                appendStringCondition(
                    function = "team",
                    condition = argument.condition,
                )

            is EntitySelectorArgument.Nbt ->
                appendNbtFilter(argument)

            is EntitySelectorArgument.Predicate ->
                line(
                    argument.withNegation(
                        "predicate(${keyLiteral(argument.key)})",
                    ),
                )

            is EntitySelectorArgument.Scores ->
                appendScores(argument)

            is EntitySelectorArgument.Advancements ->
                appendAdvancements(argument)
        }
    }
}

/** Emits one grouped `origin` or `volume` call. */
private fun KotlinSourceBuilder.appendCoordinateGroup(
    function: String,
    coordinates: List<EntitySelectorArgument.Coordinate>,
) {
    val arguments =
        coordinates.joinToString(separator = ", ") {
            "${it.value.toCoordinateLiteral()}.${it.coordinate.argumentName}"
        }

    line("$function($arguments)")
}

/** Emits a named or presence-based tag or team condition. */
private fun KotlinSourceBuilder.appendStringCondition(
    function: String,
    condition: SelectorStringCondition,
) {
    when (condition) {
        is SelectorStringCondition.Named ->
            line(
                condition.withNegation(
                    "$function(${quoted(condition.value)})",
                ),
            )

        is SelectorStringCondition.Presence ->
            line(
                "$function(${condition.value.name.lowercase()})",
            )
    }
}

private fun KotlinSourceBuilder.appendNbtFilter(argument: EntitySelectorArgument.Nbt) {
    val snbt = argument.snbt.value
    val body =
        snbtToDslBody(snbt)
            ?: conversionError(
                "miniToDsl cannot represent selector SNBT $snbt",
            )
    val expression =
        if (body.isEmpty()) {
            "nbt { }"
        } else {
            "nbt { $body }"
        }

    line(argument.withNegation(expression))
}

private fun KotlinSourceBuilder.appendScores(argument: EntitySelectorArgument.Scores) {
    appendEntriesBlock(
        function = "scores",
        entries = argument.scores,
    ) { score ->
        line(
            "${quoted(score.objective)} eq ${score.range.toDslArgument()}",
        )
    }
}

private fun KotlinSourceBuilder.appendAdvancements(argument: EntitySelectorArgument.Advancements) {
    appendEntriesBlock(
        function = "advancements",
        entries = argument.advancements,
    ) { requirement ->
        val advancement = keyLiteral(requirement.advancement)

        when (val progress = requirement.progress) {
            is SelectorAdvancementProgress.Completion ->
                line("$advancement eq ${progress.completed}")

            is SelectorAdvancementProgress.Criteria ->
                appendAdvancementCriteria(
                    advancement = advancement,
                    progress = progress,
                )
        }
    }
}

private fun KotlinSourceBuilder.appendAdvancementCriteria(
    advancement: String,
    progress: SelectorAdvancementProgress.Criteria,
) {
    if (progress.criteria.isEmpty()) {
        line("$advancement eq { }")
        return
    }

    block("$advancement eq") {
        progress.criteria.forEach { criterion ->
            line(
                "${quoted(criterion.name)} eq ${criterion.completed}",
            )
        }
    }
}

private fun <T> KotlinSourceBuilder.appendEntriesBlock(
    function: String,
    entries: List<T>,
    appendEntry: KotlinSourceBuilder.(T) -> Unit,
) {
    if (entries.isEmpty()) {
        line("$function { }")
        return
    }

    block(function) {
        entries.forEach { entry ->
            appendEntry(entry)
        }
    }
}

private val EntitySelectorHead.dslFactoryName: String
    get() =
        when (this) {
            EntitySelectorHead.NEAREST_PLAYER -> "nearestPlayer"
            EntitySelectorHead.ALL_PLAYERS -> "allPlayers"
            EntitySelectorHead.RANDOM_PLAYER -> "randomPlayer"
            EntitySelectorHead.SELF -> "self"
            EntitySelectorHead.ENTITIES -> "entities"
            EntitySelectorHead.NEAREST_ENTITY -> "nearestEntity"
        }

private val SelectorCoordinate.groupFunctionName: String
    get() =
        when (this) {
            SelectorCoordinate.X, SelectorCoordinate.Y, SelectorCoordinate.Z -> "origin"

            SelectorCoordinate.DX, SelectorCoordinate.DY, SelectorCoordinate.DZ -> "volume"
        }

private val SelectorRangeArgument.dslFunctionName: String
    get() =
        when (this) {
            SelectorRangeArgument.DISTANCE -> "distance"
            SelectorRangeArgument.X_ROTATION -> "pitch"
            SelectorRangeArgument.Y_ROTATION -> "yaw"
        }

private val SelectorEntityType.dslFunctionName: String
    get() =
        when (this) {
            is SelectorEntityType.Direct -> "type"
            is SelectorEntityType.Tag -> "typeTag"
        }

private fun SelectorEntityType.toDslCall(): String = "$dslFunctionName(${keyLiteral(key)})"

private fun SelectorNegatable.withNegation(expression: String): String =
    if (isNegated) {
        "!$expression"
    } else {
        expression
    }

private fun SelectorRange.toDslArgument(): String =
    rangeDslArgument(
        minimum = minimum,
        maximum = maximum,
        render = ::kotlinDoubleLiteral,
    )

private fun SelectorIntRange.toDslArgument(): String =
    rangeDslArgument(
        minimum = minimum,
        maximum = maximum,
        render = ::kotlinIntLiteral,
    )

private fun <T : Any> rangeDslArgument(
    minimum: T?,
    maximum: T?,
    render: (T) -> String,
): String =
    when {
        minimum != null && minimum == maximum ->
            "exactly(${render(minimum)})"

        minimum != null && maximum != null ->
            "${render(minimum)}..${render(maximum)}"

        minimum != null ->
            "atLeast(${render(minimum)})"

        else ->
            "atMost(${render(checkNotNull(maximum))})"
    }

private fun Double.toCoordinateLiteral(): String {
    val literal = toString()
    val requiresParentheses =
        literal.startsWith('-') || 'E' in literal

    return if (requiresParentheses) {
        "($literal)"
    } else {
        literal
    }
}
