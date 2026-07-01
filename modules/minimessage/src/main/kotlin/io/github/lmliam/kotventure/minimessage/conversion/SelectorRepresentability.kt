package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.LevelRange
import io.github.lmliam.kotventure.core.selector.ParsedAdvancementProgress
import io.github.lmliam.kotventure.core.selector.ParsedEntitySelector
import io.github.lmliam.kotventure.core.selector.SelectorCoordinate
import io.github.lmliam.kotventure.core.selector.SelectorRange
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument
import java.math.BigDecimal

internal fun ParsedEntitySelector.isLosslesslyRepresentable(
    pattern: String,
    nbtSources: SelectorNbtSources,
): Boolean {
    if (hasExplicitArgumentList && arguments.isEmpty()) return false
    if (!arguments.haveCanonicalOrder()) return false
    if (!arguments.haveRepresentableCardinality()) return false
    return canonicalPattern(nbtSources) == pattern
}

private fun List<EntitySelectorArgument>.haveCanonicalOrder(): Boolean =
    zipWithNext().all { (left, right) -> left.canonicalRank <= right.canonicalRank }

private fun List<EntitySelectorArgument>.haveRepresentableCardinality(): Boolean {
    if (!filterIsInstance<EntitySelectorArgument.Type>().hasCanonicalFilterOrder { it.isNegated }) return false
    if (!filterIsInstance<EntitySelectorArgument.Name>().hasCanonicalFilterOrder { it.isNegated }) return false
    if (!filterIsInstance<EntitySelectorArgument.Gamemode>().hasCanonicalFilterOrder { it.isNegated }) return false
    if (!filterIsInstance<EntitySelectorArgument.Team>().hasCanonicalTeamOrder()) return false
    if (filterIsInstance<EntitySelectorArgument.Coordinate>().map { it.coordinate }.hasDuplicates()) return false
    if (filterIsInstance<EntitySelectorArgument.Range>().map { it.argument }.hasDuplicates()) return false
    if (count { it is EntitySelectorArgument.Level } > 1) return false
    if (count { it is EntitySelectorArgument.Limit } > 1) return false
    if (count { it is EntitySelectorArgument.Sort } > 1) return false

    val scores = filterIsInstance<EntitySelectorArgument.Scores>()
    if (scores.size > 1 || scores.any { it.scores.isEmpty() }) return false
    if (scores.any { it.scores.map { score -> score.objective }.hasDuplicates() }) return false

    val advancements = filterIsInstance<EntitySelectorArgument.Advancements>()
    if (advancements.size > 1 || advancements.any { it.advancements.isEmpty() }) return false
    return advancements.none { argument ->
        argument.advancements.map { it.advancement }.hasDuplicates() ||
            argument.advancements.any { advancement ->
                val progress = advancement.progress
                progress is ParsedAdvancementProgress.Criteria &&
                    progress.criteria.map { it.name }.hasDuplicates()
            }
    }
}

private fun <T> List<T>.hasCanonicalFilterOrder(isNegated: (T) -> Boolean): Boolean =
    count { !isNegated(it) } <= 1 &&
        (none(isNegated) || all(isNegated))

private fun List<EntitySelectorArgument.Team>.hasCanonicalTeamOrder(): Boolean =
    count { !it.isEffectiveNegation } <= 1 && isNegatedThenPositive { it.isEffectiveNegation }

private val EntitySelectorArgument.Team.isEffectiveNegation: Boolean
    get() = isNegated && value.isNotEmpty()

private fun <T> List<T>.isNegatedThenPositive(isNegated: (T) -> Boolean): Boolean {
    var sawPositive = false
    forEach {
        if (!isNegated(it)) {
            sawPositive = true
        } else if (sawPositive) {
            return false
        }
    }
    return true
}

private fun <T> List<T>.hasDuplicates(): Boolean = size != distinct().size

private fun ParsedEntitySelector.canonicalPattern(nbtSources: SelectorNbtSources): String? {
    if (arguments.isEmpty()) return head.token
    val renderedArguments = arguments.map { it.canonicalSource(nbtSources) ?: return null }
    return renderedArguments.joinToString(",", "${head.token}[", "]")
}

private fun EntitySelectorArgument.canonicalSource(nbtSources: SelectorNbtSources): String? =
    when (this) {
        is EntitySelectorArgument.Coordinate ->
            "${coordinate.argumentName}=${formatSelectorNumber(value)}"
        is EntitySelectorArgument.Range ->
            "${argument.argumentName}=${range.canonicalSource()}"
        is EntitySelectorArgument.Limit -> "limit=$value"
        is EntitySelectorArgument.Sort -> "sort=${value.name.lowercase()}"
        is EntitySelectorArgument.Level -> "level=${range.canonicalSource()}"
        is EntitySelectorArgument.Gamemode ->
            "gamemode=${if (isNegated) "!" else ""}${value.name.lowercase()}"
        is EntitySelectorArgument.Name ->
            "name=${if (isNegated) "!" else ""}${value.renderSelectorName()}"
        is EntitySelectorArgument.Type ->
            "type=${if (isNegated) "!" else ""}${if (isTag) "#" else ""}${key.asString()}"
        is EntitySelectorArgument.Tag -> "tag=${if (isNegated) "!" else ""}$value"
        is EntitySelectorArgument.Team -> "team=${if (isNegated) "!" else ""}$value"
        is EntitySelectorArgument.Nbt ->
            nbtSources[this]?.let { "nbt=${if (isNegated) "!" else ""}${it.rendered}" }
        is EntitySelectorArgument.Scores ->
            scores.joinToString(",", "scores={", "}") {
                "${it.objective}=${it.range.canonicalSource()}"
            }
        is EntitySelectorArgument.Predicate ->
            "predicate=${if (isNegated) "!" else ""}${key.asString()}"
        is EntitySelectorArgument.Advancements ->
            advancements.joinToString(",", "advancements={", "}") {
                "${it.advancement.asString()}=${it.progress.canonicalSource()}"
            }
    }

private fun ParsedAdvancementProgress.canonicalSource(): String =
    when (this) {
        is ParsedAdvancementProgress.Completion -> completed.toString()
        is ParsedAdvancementProgress.Criteria ->
            criteria.joinToString(",", "{", "}") { "${it.name}=${it.completed}" }
    }

private fun String.renderSelectorName(): String =
    if (all(Char::isSelectorTokenCharacter)) {
        this
    } else {
        "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
    }

private fun Char.isSelectorTokenCharacter(): Boolean =
    this in '0'..'9' ||
        this in 'A'..'Z' ||
        this in 'a'..'z' ||
        this == '_' ||
        this == '-' ||
        this == '.' ||
        this == '+'

private fun SelectorRange.canonicalSource(): String =
    rangeSource(
        original = toString(),
        minimum = minimum,
        maximum = maximum,
        render = ::formatSelectorNumber,
    )

private fun LevelRange.canonicalSource(): String =
    rangeSource(
        original = toString(),
        minimum = minimum,
        maximum = maximum,
        render = Int::toString,
    )

private fun <T> rangeSource(
    original: String,
    minimum: T?,
    maximum: T?,
    render: (T) -> String,
): String =
    if (".." !in original) {
        render(requireNotNull(minimum))
    } else {
        "${minimum?.let(render).orEmpty()}..${maximum?.let(render).orEmpty()}"
    }

private fun formatSelectorNumber(value: Double): String =
    BigDecimal
        .valueOf(value)
        .stripTrailingZeros()
        .toPlainString()

private val EntitySelectorArgument.canonicalRank: Int
    get() =
        when (this) {
            is EntitySelectorArgument.Type -> 0
            is EntitySelectorArgument.Name -> 1
            is EntitySelectorArgument.Coordinate ->
                when (coordinate) {
                    SelectorCoordinate.X -> 2
                    SelectorCoordinate.Y -> 3
                    SelectorCoordinate.Z -> 4
                    SelectorCoordinate.DX -> 5
                    SelectorCoordinate.DY -> 6
                    SelectorCoordinate.DZ -> 7
                }
            is EntitySelectorArgument.Range ->
                when (argument) {
                    SelectorRangeArgument.DISTANCE -> 8
                    SelectorRangeArgument.X_ROTATION -> 9
                    SelectorRangeArgument.Y_ROTATION -> 10
                }
            is EntitySelectorArgument.Level -> 11
            is EntitySelectorArgument.Gamemode -> 12
            is EntitySelectorArgument.Limit -> 13
            is EntitySelectorArgument.Sort -> 14
            is EntitySelectorArgument.Tag -> 15
            is EntitySelectorArgument.Team -> 16
            is EntitySelectorArgument.Nbt -> 17
            is EntitySelectorArgument.Scores -> 18
            is EntitySelectorArgument.Predicate -> 19
            is EntitySelectorArgument.Advancements -> 20
        }
