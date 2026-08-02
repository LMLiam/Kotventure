package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.SelectorCoordinate
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument
import io.github.lmliam.kotventure.core.selector.SelectorSort

internal fun SelectorReader.readCoordinateArgument(coordinate: SelectorCoordinate): EntitySelectorArgument.Coordinate =
    EntitySelectorArgument.Coordinate(coordinate, readSelectorDouble())

internal fun SelectorReader.readRangeArgument(argument: SelectorRangeArgument): EntitySelectorArgument.Range =
    EntitySelectorArgument.Range(argument, readDoubleRange(argument))

internal fun SelectorReader.readLevelArgument(): EntitySelectorArgument.Level =
    EntitySelectorArgument.Level(readIntRange("level", nonNegative = true))

/** Reads a positive `limit` value. */
internal fun SelectorReader.readLimitArgument(): EntitySelectorArgument.Limit =
    EntitySelectorArgument.Limit(
        readValidatedInt("Selector limit must be positive") { it > 0 },
    )

/** Reads a [SelectorSort] value. */
internal fun SelectorReader.readSortArgument(): EntitySelectorArgument.Sort {
    val start = offset
    val token = readValueToken()
    return EntitySelectorArgument.Sort(
        SelectorSort.fromValue(token) ?: failAt(start, "Unsupported selector sort '$token'"),
    )
}

/**
 * Reads an integer and applies [isValid] at the integer's source offset.
 */
private fun SelectorReader.readValidatedInt(
    message: String,
    isValid: (Int) -> Boolean,
): Int {
    val start = offset
    val value = readSelectorInt()
    if (!isValid(value)) failAt(start, message)
    return value
}
