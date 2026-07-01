package io.github.lmliam.kotventure.core.selector

internal fun parseSelectorArgument(
    head: EntitySelectorHead,
    name: String,
    value: String,
    nameOffset: Int,
    valueOffset: Int,
): EntitySelectorArgument {
    validateSelectorCapability(head, name, nameOffset)
    return when (name) {
        "x", "y", "z", "dx", "dy", "dz" -> parseCoordinateArgument(name, value, valueOffset)
        "distance", "x_rotation", "y_rotation" ->
            parseFloatingRangeArgument(name, value, valueOffset)
        "level" -> EntitySelectorArgument.Level(parseSelectorIntRange(value, valueOffset, true))
        "limit" -> parseLimitArgument(value, valueOffset)
        "sort" -> parseSortArgument(value, valueOffset)
        "gamemode" -> parseGamemodeArgument(value, valueOffset)
        "name" -> parseNameArgument(value, valueOffset)
        "type" -> parseTypeArgument(value, valueOffset)
        "tag" -> parseTagArgument(value, valueOffset)
        "team" -> parseTeamArgument(value, valueOffset)
        "nbt" -> parseNbtArgument(value, valueOffset)
        "scores" -> parseScoresArgument(value, valueOffset)
        "predicate" -> parsePredicateArgument(value, valueOffset)
        "advancements" -> parseAdvancementsArgument(value, valueOffset)
        else -> fail(nameOffset, "Unsupported selector argument '$name'")
    }
}

private fun validateSelectorCapability(
    head: EntitySelectorHead,
    name: String,
    nameOffset: Int,
) {
    if (
        name == "type" &&
        head in
        setOf(
            EntitySelectorHead.NEAREST_PLAYER,
            EntitySelectorHead.ALL_PLAYERS,
            EntitySelectorHead.RANDOM_PLAYER,
        )
    ) {
        fail(nameOffset, "Selector ${head.token} does not support 'type'")
    }
    if (head == EntitySelectorHead.SELF && (name == "limit" || name == "sort")) {
        fail(nameOffset, "Selector ${head.token} does not support '$name'")
    }
}
