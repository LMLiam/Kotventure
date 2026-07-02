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

internal fun SelectorReader.readLimitArgument(): EntitySelectorArgument.Limit {
    val start = offset
    val limit = readSelectorInt()
    if (limit <= 0) failAt(start, "Selector limit must be positive")
    return EntitySelectorArgument.Limit(limit)
}

internal fun SelectorReader.readSortArgument(): EntitySelectorArgument.Sort {
    val start = offset
    val token = readValueToken()
    val sort =
        SelectorSort.entries.firstOrNull { it.value == token }
            ?: failAt(start, "Unsupported selector sort '$token'")
    return EntitySelectorArgument.Sort(sort)
}
