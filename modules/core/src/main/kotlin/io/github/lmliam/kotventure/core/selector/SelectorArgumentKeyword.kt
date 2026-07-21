package io.github.lmliam.kotventure.core.selector

/**
 * Contains each keyword selector-argument name. These arguments are not coordinates
 * ([SelectorCoordinate]) or floating-point ranges ([SelectorRangeArgument]).
 *
 * This enum is the one source for keyword spellings. The parser dispatches on it, the head policy restricts by it, and
 * the renderer prints through it.
 *
 * @property sourceName vanilla selector-source spelling, such as `limit` in `limit=1`
 */
internal enum class SelectorArgumentKeyword(
    val sourceName: String,
) {
    LEVEL("level"),
    LIMIT("limit"),
    SORT("sort"),
    GAMEMODE("gamemode"),
    NAME("name"),
    TYPE("type"),
    TAG("tag"),
    TEAM("team"),
    NBT("nbt"),
    SCORES("scores"),
    PREDICATE("predicate"),
    ADVANCEMENTS("advancements"),
    ;

    companion object {
        fun fromSourceName(name: String): SelectorArgumentKeyword? = entries.firstOrNull { it.sourceName == name }
    }
}
