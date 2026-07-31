package io.github.lmliam.kotventure.core.selector

/**
 * Contains each keyword selector-argument name. These arguments are not coordinates
 * ([SelectorCoordinate]) or floating-point ranges ([SelectorRangeArgument]).
 *
 * This enum is the one source for keyword spellings. The parser dispatches on it, the head policy restricts by it, and
 * the renderer prints through it.
 *
 * @property sourceName vanilla selector-source spelling, such as `limit` in `limit=1`
 * @property isSingleton whether this keyword argument can only occur one time in a selector
 */
internal enum class SelectorArgumentKeyword(
    val sourceName: String,
    val isSingleton: Boolean = false
) {
    LEVEL("level", isSingleton = true),
    LIMIT("limit", isSingleton = true),
    SORT("sort", isSingleton = true),
    GAMEMODE("gamemode"),
    NAME("name"),
    TYPE("type"),
    TAG("tag"),
    TEAM("team"),
    NBT("nbt"),
    SCORES("scores", isSingleton = true),
    PREDICATE("predicate"),
    ADVANCEMENTS("advancements", isSingleton = true),
    ;

    companion object {
        private val bySourceName = entries.associateBy(SelectorArgumentKeyword::sourceName)

        fun fromSourceName(name: String): SelectorArgumentKeyword? = bySourceName[name]
    }
}
