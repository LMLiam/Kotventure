package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.EntitySelector
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorHead
import io.github.lmliam.kotventure.core.selector.SelectorAdvancementProgress
import io.github.lmliam.kotventure.core.selector.SelectorCoordinate
import io.github.lmliam.kotventure.core.selector.SelectorEntityType
import io.github.lmliam.kotventure.core.selector.SelectorIntRange
import io.github.lmliam.kotventure.core.selector.SelectorRange
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument
import io.github.lmliam.kotventure.core.selector.SelectorStringCondition

/**
 * Emit [selector] as the typed selector-DSL expression that reconstructs it.
 *
 * The factory name (e.g. `entities`, `self`, ...) is emitted, and each argument becomes one scope
 * call in model order. Coordinates for a group are collapsed into a single `origin(...)` or
 * `volume(...)` call, emitted the first time that group appears.
 */
internal fun KotlinSourceBuilder.appendEntitySelector(selector: EntitySelector) {
    val factory = selector.head.factoryName
    if (selector.arguments.isEmpty()) {
        line("$factory()")
    } else {
        block(factory) { appendArguments(selector.arguments) }
    }
}

private val EntitySelectorHead.factoryName: String
    get() =
        when (this) {
        EntitySelectorHead.NEAREST_PLAYER -> "nearestPlayer"
        EntitySelectorHead.ALL_PLAYERS -> "allPlayers"
        EntitySelectorHead.RANDOM_PLAYER -> "randomPlayer"
        EntitySelectorHead.SELF -> "self"
        EntitySelectorHead.ENTITIES -> "entities"
        EntitySelectorHead.NEAREST_ENTITY -> "nearestEntity"
    }

/**
 * Emit selector arguments preserving model order. Coordinates are emitted as grouped calls the
 * first time their coordinate-group appears.
 */
private fun KotlinSourceBuilder.appendArguments(arguments: List<EntitySelectorArgument>) {
    val emittedCoordinateGroups = mutableSetOf<String>()

    arguments.forEach { argument ->
        when (argument) {
            is EntitySelectorArgument.Coordinate ->
                emitCoordinateGroupIfFirstSeen(argument.coordinate.groupFunction, arguments, emittedCoordinateGroups)

            is EntitySelectorArgument.Range ->
                line("${argument.argument.dslFunction}(${argument.range.toDslArgument()})")

            is EntitySelectorArgument.Level -> line("level(${argument.range.toDslArgument()})")
            is EntitySelectorArgument.Limit -> line("limit(${argument.value})")
            is EntitySelectorArgument.Sort -> line("sort(${argument.value.name.lowercase()})")

            is EntitySelectorArgument.GameMode ->
                line("${argument.negation}gamemode(${argument.value.name.lowercase()})")

            is EntitySelectorArgument.Name ->
                line("${argument.negation}name(${quoted(argument.value)})")

            is EntitySelectorArgument.Type ->
                line("${argument.negation}${argument.target.dslFunction}(${keyLiteral(argument.target.key)})")

            is EntitySelectorArgument.Tag -> appendStringCondition("tag", argument.condition)
            is EntitySelectorArgument.Team -> appendStringCondition("team", argument.condition)

            is EntitySelectorArgument.Nbt -> appendNbtFilter(argument)
            is EntitySelectorArgument.Predicate ->
                line("${argument.negation}predicate(${keyLiteral(argument.key)})")

            is EntitySelectorArgument.Scores -> appendScores(argument)
            is EntitySelectorArgument.Advancements -> appendAdvancements(argument)
        }
    }
}

/**
 * Emit one grouped coordinate call (e.g. `origin(x, y, z)`) the first time the group is seen.
 */
private fun KotlinSourceBuilder.emitCoordinateGroupIfFirstSeen(
    groupFunction: String,
    arguments: List<EntitySelectorArgument>,
    emittedGroups: MutableSet<String>,
) {
    if (!emittedGroups.add(groupFunction)) return

    val coordinates =
        arguments
        .filterIsInstance<EntitySelectorArgument.Coordinate>()
        .filter { it.coordinate.groupFunction == groupFunction }
        .joinToString(", ") { "${it.value.toCoordinateLiteral()}.${it.coordinate.argumentName}" }

    line("$groupFunction($coordinates)")
}

private val SelectorCoordinate.groupFunction: String
    get() =
        when (this) {
        SelectorCoordinate.X, SelectorCoordinate.Y, SelectorCoordinate.Z -> "origin"
        SelectorCoordinate.DX, SelectorCoordinate.DY, SelectorCoordinate.DZ -> "volume"
    }

private val SelectorRangeArgument.dslFunction: String
    get() =
        when (this) {
        SelectorRangeArgument.DISTANCE -> "distance"
        SelectorRangeArgument.X_ROTATION -> "pitch"
        SelectorRangeArgument.Y_ROTATION -> "yaw"
    }

private val SelectorEntityType.dslFunction: String
    get() =
        when (this) {
        is SelectorEntityType.Direct -> "type"
        is SelectorEntityType.Tag -> "typeTag"
    }

private val EntitySelectorArgument.Negatable.negation: String
    get() = if (isNegated) "!" else ""

/** Emit name/team/tag conditions (negation and presence variants). */
private fun KotlinSourceBuilder.appendStringCondition(
    function: String,
    condition: SelectorStringCondition,
) {
    when (condition) {
        is SelectorStringCondition.Named ->
            line("${if (condition.isNegated) "!" else ""}$function(${quoted(condition.value)})")

        is SelectorStringCondition.Presence ->
            line("$function(${condition.value.name.lowercase()})")
    }
}

private fun KotlinSourceBuilder.appendNbtFilter(argument: EntitySelectorArgument.Nbt) {
    val body =
        snbtToDslBody(argument.snbt.value)
        ?: conversionError("miniToDsl cannot represent selector SNBT ${argument.snbt.value}")
    val call = if (body.isEmpty()) "nbt { }" else "nbt { $body }"
    line("${argument.negation}$call")
}

private fun KotlinSourceBuilder.appendScores(argument: EntitySelectorArgument.Scores) =
    appendEntryBlock("scores", argument.scores) { score ->
        line("${quoted(score.objective)} eq ${score.range.toDslArgument()}")
    }

private fun KotlinSourceBuilder.appendAdvancements(argument: EntitySelectorArgument.Advancements) =
    appendEntryBlock("advancements", argument.advancements) { requirement ->
        val advancement = keyLiteral(requirement.advancement)
        when (val progress = requirement.progress) {
            is SelectorAdvancementProgress.Completion -> line("$advancement eq ${progress.completed}")
            is SelectorAdvancementProgress.Criteria -> appendCriteria(advancement, progress)
        }
    }

private fun KotlinSourceBuilder.appendCriteria(
    advancement: String,
    progress: SelectorAdvancementProgress.Criteria,
) {
    if (progress.criteria.isEmpty()) {
        line("$advancement eq { }")
    } else {
        block("$advancement eq") {
            progress.criteria.forEach { line("${quoted(it.name)} eq ${it.completed}") }
        }
    }
}

private fun <T> KotlinSourceBuilder.appendEntryBlock(
    function: String,
    entries: List<T>,
    appendEntry: KotlinSourceBuilder.(T) -> Unit,
) {
    if (entries.isEmpty()) {
        line("$function { }")
    } else {
        block(function) { entries.forEach { appendEntry(it) } }
    }
}

private fun SelectorRange.toDslArgument(): String = rangeDslArgument(minimum, maximum, Double::toString)

private fun SelectorIntRange.toDslArgument(): String = rangeDslArgument(minimum, maximum, Int::toString)

private fun <T> rangeDslArgument(
    minimum: T?,
    maximum: T?,
    render: (T) -> String,
): String =
    when {
    minimum != null && minimum == maximum -> "exactly(${render(minimum)})"
    minimum != null && maximum != null -> "${render(minimum)}..${render(maximum)}"
    minimum != null -> "atLeast(${render(minimum)})"
    else -> "atMost(${render(checkNotNull(maximum))})"
}

private fun Double.toCoordinateLiteral(): String {
    val literal = toString()
    return if (this < 0 || 'E' in literal) "($literal)" else literal
}
