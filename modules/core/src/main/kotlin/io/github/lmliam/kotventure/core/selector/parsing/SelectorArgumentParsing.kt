package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorHead
import io.github.lmliam.kotventure.core.selector.SelectorCoordinate
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument
import io.github.lmliam.kotventure.core.selector.supportsArgument

internal fun SelectorReader.readArgumentValue(
    head: EntitySelectorHead,
    name: String,
    nameOffset: Int,
): EntitySelectorArgument {
    if (!head.supportsArgument(name)) {
        failAt(nameOffset, "Selector ${head.token} does not support '$name'")
    }
    val coordinate = SelectorCoordinate.entries.firstOrNull { it.argumentName == name }
    if (coordinate != null) return readCoordinateArgument(coordinate)
    val rangeArgument = SelectorRangeArgument.entries.firstOrNull { it.argumentName == name }
    if (rangeArgument != null) return readRangeArgument(rangeArgument)
    return when (name) {
        "level" -> readLevelArgument()
        "limit" -> readLimitArgument()
        "sort" -> readSortArgument()
        "gamemode" -> readGamemodeArgument()
        "name" -> readNameArgument()
        "type" -> readTypeArgument()
        "tag" -> readTagArgument()
        "team" -> readTeamArgument()
        "nbt" -> readNbtArgument()
        "scores" -> readScoresArgument()
        "predicate" -> readPredicateArgument()
        "advancements" -> readAdvancementsArgument()
        else -> failAt(nameOffset, "Unsupported selector argument '$name'")
    }
}
