package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Advancements
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Coordinate
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.GameMode
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Level
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Limit
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Name
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Nbt
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Predicate
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Range
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Scores
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Sort
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Tag
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Team
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Type

/**
 * Returns the keyword of this argument, or null when its argument type owns the name.
 */
internal val EntitySelectorArgument.keyword: SelectorArgumentKeyword?
    get() =
        when (this) {
            is Coordinate, is Range -> null
            is Level -> SelectorArgumentKeyword.LEVEL
            is Limit -> SelectorArgumentKeyword.LIMIT
            is Sort -> SelectorArgumentKeyword.SORT
            is GameMode -> SelectorArgumentKeyword.GAMEMODE
            is Name -> SelectorArgumentKeyword.NAME
            is Type -> SelectorArgumentKeyword.TYPE
            is Tag -> SelectorArgumentKeyword.TAG
            is Team -> SelectorArgumentKeyword.TEAM
            is Nbt -> SelectorArgumentKeyword.NBT
            is Scores -> SelectorArgumentKeyword.SCORES
            is Predicate -> SelectorArgumentKeyword.PREDICATE
            is Advancements -> SelectorArgumentKeyword.ADVANCEMENTS
        }

/**
 * Returns the vanilla source name of this argument, such as `limit` in `limit=1`.
 *
 * Coordinate and range types supply their argument names. Keyword arguments get their names from [keyword].
 */
internal val EntitySelectorArgument.argumentName: String
    get() =
        when (this) {
            is Coordinate -> coordinate.argumentName
            is Range -> argument.argumentName
            else -> keyword?.sourceName ?: error("Keyword arguments always declare a keyword")
        }

/**
 * The argument names that can occur one time in a selector.
 */
internal val singletonSelectorArgumentNames: Set<String> =
    buildSet {
        addAll(SelectorCoordinate.entries.map(SelectorCoordinate::argumentName))
        addAll(SelectorRangeArgument.entries.map(SelectorRangeArgument::argumentName))
        addAll(
            SelectorArgumentKeyword.entries
                .filter(SelectorArgumentKeyword::isSingleton)
                .map(SelectorArgumentKeyword::sourceName),
        )
    }
