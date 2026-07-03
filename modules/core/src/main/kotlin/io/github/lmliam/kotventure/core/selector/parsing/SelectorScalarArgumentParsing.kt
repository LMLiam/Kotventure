package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelector
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

/**
 * Read and validate a limit argument. The limit must be positive (> 0).
 *
 * Throws [EntitySelectorParseException][io.github.lmliam.kotventure.core.selector.EntitySelectorParseException] if the limit is not positive.
 */
internal fun SelectorReader.readLimitArgument(): EntitySelectorArgument.Limit {
    val limit = readValidatedInt("Selector limit must be positive") { it > 0 }
    return EntitySelectorArgument.Limit(limit)
}

/**
 * Read and validate a sort argument against the known [SelectorSort] enum.
 *
 * Throws [EntitySelectorParseException][io.github.lmliam.kotventure.core.selector.EntitySelectorParseException] if the sort value is unsupported.
 */
internal fun SelectorReader.readSortArgument(): EntitySelectorArgument.Sort {
    val start = offset
    val token = readValueToken()
    val sort =
        SelectorSort.entries.firstOrNull { it.value == token }
            ?: failAt(start, "Unsupported selector sort '$token'")
    return EntitySelectorArgument.Sort(sort)
}

/**
 * Generic helper to read and validate an integer.
 *
 * Captures the offset before parsing and fails with [message] if [isValid] returns false.
 *
 * @param message the error message if validation fails
 * @param isValid predicate on the parsed value; returns true if valid
 * @return the parsed and validated integer
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
