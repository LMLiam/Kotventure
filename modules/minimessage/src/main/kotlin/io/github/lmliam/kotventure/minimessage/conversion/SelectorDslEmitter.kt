package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.LevelRange
import io.github.lmliam.kotventure.core.selector.ParsedAdvancementProgress
import io.github.lmliam.kotventure.core.selector.ParsedEntitySelector
import io.github.lmliam.kotventure.core.selector.ParsedSelectorAdvancement
import io.github.lmliam.kotventure.core.selector.SelectorCoordinate
import io.github.lmliam.kotventure.core.selector.SelectorRange
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument

private data class EmittedSelectorArgument(
    val node: SelectorDslNode,
    val isNegated: Boolean = false,
)

internal fun ParsedEntitySelector.toDslBody(): List<SelectorDslNode> {
    val emitted = mutableListOf<EmittedSelectorArgument>()
    var emittedOrigin = false
    var emittedVolume = false

    arguments.forEach { argument ->
        if (argument is EntitySelectorArgument.Coordinate) {
            if (argument.coordinate in ORIGIN_COORDINATES && !emittedOrigin) {
                emitted += EmittedSelectorArgument(coordinateGroup("origin", ORIGIN_COORDINATES))
                emittedOrigin = true
            } else if (argument.coordinate in VOLUME_COORDINATES && !emittedVolume) {
                emitted += EmittedSelectorArgument(coordinateGroup("volume", VOLUME_COORDINATES))
                emittedVolume = true
            }
        } else {
            emitted += argument.toEmittedArgument()
        }
    }
    return emitted.groupNegatedArguments()
}

private fun ParsedEntitySelector.coordinateGroup(
    function: String,
    coordinates: Set<SelectorCoordinate>,
): SelectorDslNode {
    val values =
        arguments
            .filterIsInstance<EntitySelectorArgument.Coordinate>()
            .filter { it.coordinate in coordinates }
            .joinToString(", ") {
                "${it.coordinate.argumentName} = ${it.value.toKotlinDoubleSource()}"
            }
    return SelectorDslNode.Line("$function($values)")
}

private fun EntitySelectorArgument.toEmittedArgument(): EmittedSelectorArgument =
    when (this) {
        is EntitySelectorArgument.Coordinate -> error("Coordinates are emitted in groups")
        is EntitySelectorArgument.Range ->
            EmittedSelectorArgument(
                SelectorDslNode.Line("${argument.dslFunction}(${range.toKotlinSource()})"),
            )
        is EntitySelectorArgument.Limit -> EmittedSelectorArgument(SelectorDslNode.Line("limit($value)"))
        is EntitySelectorArgument.Sort ->
            EmittedSelectorArgument(SelectorDslNode.Line("sort(${value.name.lowercase()})"))
        is EntitySelectorArgument.Level ->
            EmittedSelectorArgument(SelectorDslNode.Line("level(${range.toKotlinSource()})"))
        is EntitySelectorArgument.Gamemode ->
            EmittedSelectorArgument(
                SelectorDslNode.Line("gamemode(${value.name.lowercase()})"),
                isNegated,
            )
        is EntitySelectorArgument.Name ->
            EmittedSelectorArgument(
                SelectorDslNode.Line("name(\"${escapeKotlinString(value)}\")"),
                isNegated,
            )
        is EntitySelectorArgument.Type ->
            EmittedSelectorArgument(
                SelectorDslNode.Line("${if (isTag) "typeTag" else "type"}(${keyLiteral(key)})"),
                isNegated,
            )
        is EntitySelectorArgument.Tag -> tagArgument()
        is EntitySelectorArgument.Team -> teamArgument()
        is EntitySelectorArgument.Nbt ->
            EmittedSelectorArgument(
                SelectorDslNode.Block(
                    "nbt",
                    requireNotNull(snbtToDslSource(snbt)).bodyLines.map(SelectorDslNode::Line),
                ),
                isNegated,
            )
        is EntitySelectorArgument.Scores ->
            EmittedSelectorArgument(
                SelectorDslNode.Group(
                    scores.map {
                        SelectorDslNode.Line(
                            "score(\"${escapeKotlinString(it.objective)}\", ${it.range.toKotlinSource()})",
                        )
                    },
                ),
            )
        is EntitySelectorArgument.Predicate ->
            EmittedSelectorArgument(
                SelectorDslNode.Line("predicate(${keyLiteral(key)})"),
                isNegated,
            )
        is EntitySelectorArgument.Advancements ->
            EmittedSelectorArgument(
                SelectorDslNode.Group(advancements.map(ParsedSelectorAdvancement::toDslNode)),
            )
    }

private fun EntitySelectorArgument.Tag.tagArgument(): EmittedSelectorArgument =
    if (value.isEmpty()) {
        EmittedSelectorArgument(SelectorDslNode.Line("tag(${if (isNegated) "any" else "none"})"))
    } else {
        EmittedSelectorArgument(
            SelectorDslNode.Line("tag(\"${escapeKotlinString(value)}\")"),
            isNegated,
        )
    }

private fun EntitySelectorArgument.Team.teamArgument(): EmittedSelectorArgument =
    if (value.isEmpty()) {
        EmittedSelectorArgument(SelectorDslNode.Line("team(${if (isNegated) "any" else "none"})"))
    } else {
        EmittedSelectorArgument(
            SelectorDslNode.Line("team(\"${escapeKotlinString(value)}\")"),
            isNegated,
        )
    }

private fun ParsedSelectorAdvancement.toDslNode(): SelectorDslNode =
    when (val requirement = progress) {
        is ParsedAdvancementProgress.Completion ->
            SelectorDslNode.Line(
                "advancement(${keyLiteral(advancement)}, completed = ${requirement.completed})",
            )
        is ParsedAdvancementProgress.Criteria ->
            SelectorDslNode.Block(
                "advancement(${keyLiteral(advancement)})",
                requirement.criteria.map {
                    SelectorDslNode.Line(
                        "criterion(\"${escapeKotlinString(it.name)}\", completed = ${it.completed})",
                    )
                },
            )
    }

private fun List<EmittedSelectorArgument>.groupNegatedArguments(): List<SelectorDslNode> {
    val result = mutableListOf<SelectorDslNode>()
    var index = 0
    while (index < size) {
        val argument = this[index]
        if (!argument.isNegated) {
            result += argument.node
            index++
            continue
        }
        val negated = mutableListOf<SelectorDslNode>()
        while (index < size && this[index].isNegated) {
            negated += this[index].node
            index++
        }
        result += SelectorDslNode.Block("not", negated)
    }
    return result
}

private fun SelectorRange.toKotlinSource(): String =
    when {
        ".." !in toString() -> "exactly(${requireNotNull(minimum).toKotlinDoubleSource()})"
        minimum == null -> "atMost(${requireNotNull(maximum).toKotlinDoubleSource()})"
        maximum == null -> "atLeast(${requireNotNull(minimum).toKotlinDoubleSource()})"
        else ->
            "${requireNotNull(minimum).toKotlinDoubleSource()}.." +
                requireNotNull(maximum).toKotlinDoubleSource()
    }

private fun LevelRange.toKotlinSource(): String =
    when {
        ".." !in toString() -> "exactly(${requireNotNull(minimum)})"
        minimum == null -> "atMost(${requireNotNull(maximum)})"
        maximum == null -> "atLeast($minimum)"
        else -> "$minimum..$maximum"
    }

private fun Double.toKotlinDoubleSource(): String {
    val source = toString()
    return if (source.any { it == '.' || it == 'E' || it == 'e' }) source else "$source.0"
}

private val SelectorRangeArgument.dslFunction: String
    get() =
        when (this) {
            SelectorRangeArgument.DISTANCE -> "distance"
            SelectorRangeArgument.X_ROTATION -> "xRotation"
            SelectorRangeArgument.Y_ROTATION -> "yRotation"
        }

private val ORIGIN_COORDINATES: Set<SelectorCoordinate> =
    setOf(SelectorCoordinate.X, SelectorCoordinate.Y, SelectorCoordinate.Z)

private val VOLUME_COORDINATES: Set<SelectorCoordinate> =
    setOf(SelectorCoordinate.DX, SelectorCoordinate.DY, SelectorCoordinate.DZ)
