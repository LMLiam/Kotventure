package io.github.lmliam.kotventure.core.selector

internal fun SelectorReader.readArgumentValue(
    head: EntitySelectorHead,
    name: String,
    nameOffset: Int,
): EntitySelectorArgument {
    requireSupportedByHead(head, name, nameOffset)
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

private fun SelectorReader.requireSupportedByHead(
    head: EntitySelectorHead,
    name: String,
    nameOffset: Int,
) {
    val supported =
        when (name) {
            "type" -> head.acceptsTypeFilters
            "limit", "sort" -> head.acceptsResultControls
            else -> true
        }
    if (!supported) failAt(nameOffset, "Selector ${head.token} does not support '$name'")
}
