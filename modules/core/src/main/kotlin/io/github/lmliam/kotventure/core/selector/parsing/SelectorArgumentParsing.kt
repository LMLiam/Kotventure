package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorHead
import io.github.lmliam.kotventure.core.selector.SelectorArgumentKeyword
import io.github.lmliam.kotventure.core.selector.SelectorCoordinate
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument
import io.github.lmliam.kotventure.core.selector.supports

internal fun SelectorReader.readArgumentValue(
    head: EntitySelectorHead,
    name: String,
    nameOffset: Int,
): EntitySelectorArgument {
    val coordinate = SelectorCoordinate.entries.firstOrNull { it.argumentName == name }
    if (coordinate != null) return readCoordinateArgument(coordinate)
    val rangeArgument = SelectorRangeArgument.entries.firstOrNull { it.argumentName == name }
    if (rangeArgument != null) return readRangeArgument(rangeArgument)
    val keyword =
        SelectorArgumentKeyword.fromSourceName(name)
            ?: failAt(nameOffset, "Unsupported selector argument '$name'")
    if (!head.supports(keyword)) {
        failAt(nameOffset, "Selector ${head.token} does not support '$name'")
    }
    return when (keyword) {
        SelectorArgumentKeyword.LEVEL -> readLevelArgument()
        SelectorArgumentKeyword.LIMIT -> readLimitArgument()
        SelectorArgumentKeyword.SORT -> readSortArgument()
        SelectorArgumentKeyword.GAMEMODE -> readGamemodeArgument()
        SelectorArgumentKeyword.NAME -> readNameArgument()
        SelectorArgumentKeyword.TYPE -> readTypeArgument()
        SelectorArgumentKeyword.TAG -> readTagArgument()
        SelectorArgumentKeyword.TEAM -> readTeamArgument()
        SelectorArgumentKeyword.NBT -> readNbtArgument()
        SelectorArgumentKeyword.SCORES -> readScoresArgument()
        SelectorArgumentKeyword.PREDICATE -> readPredicateArgument()
        SelectorArgumentKeyword.ADVANCEMENTS -> readAdvancementsArgument()
    }
}
