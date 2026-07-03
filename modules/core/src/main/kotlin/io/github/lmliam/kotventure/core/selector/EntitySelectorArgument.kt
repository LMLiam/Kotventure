package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key
import io.github.lmliam.kotventure.core.selector.GameMode as SelectorGameMode

/**
 * One immutable, typed Java Edition entity-selector argument.
 *
 * Arguments form a closed hierarchy: simple values (Coordinate, Range, Level, Limit, Sort),
 * negatable filters (GameMode, Name, Type, Tag, Team, Nbt, Predicate), and compound collections
 * (Scores, Advancements).
 */
public sealed interface EntitySelectorArgument {
    /**
     * An argument that vanilla syntax can prefix-negate with `!`.
     *
     * Subtypes carry an [isNegated] flag that models the leading `!` operator.
     */
    public sealed interface Negatable :
        EntitySelectorArgument,
        SelectorNegatable

    //region Simple value arguments

    /**
     * A selector coordinate or bounding-volume delta.
     *
     * @property coordinate coordinate argument name
     * @property value finite coordinate value
     * @throws IllegalArgumentException if [value] is NaN or infinite
     */
    public data class Coordinate(
        public val coordinate: SelectorCoordinate,
        public val value: Double,
    ) : EntitySelectorArgument {
        init {
            require(value.isFinite()) { "Selector coordinate must be finite, got: $value" }
        }
    }

    /**
     * A floating-point selector range.
     *
     * @property argument range argument name
     * @property range parsed range bounds
     */
    public data class Range(
        public val argument: SelectorRangeArgument,
        public val range: SelectorRange,
    ) : EntitySelectorArgument

    /**
     * A result limit.
     *
     * @property value positive maximum result count
     * @throws IllegalArgumentException if [value] is not positive
     */
    public data class Limit(
        public val value: Int,
    ) : EntitySelectorArgument {
        init {
            require(value > 0) { "Selector limit must be positive, got: $value" }
        }
    }

    /**
     * A result sort order.
     *
     * @property value parsed sort order
     */
    public data class Sort(
        public val value: SelectorSort,
    ) : EntitySelectorArgument

    /**
     * An integral experience-level range.
     *
     * @property range parsed level bounds
     */
    public data class Level(
        public val range: SelectorIntRange,
    ) : EntitySelectorArgument

    //endregion
    //region Negatable filter arguments

    /**
     * A game-mode filter.
     *
     * @property value parsed game mode
     * @property isNegated whether the filter excludes this game mode
     */
    public data class GameMode(
        public val value: SelectorGameMode,
        override val isNegated: Boolean,
    ) : Negatable

    /**
     * An entity-name filter.
     *
     * @property value decoded entity name
     * @property isNegated whether the filter excludes this name
     */
    public data class Name(
        public val value: String,
        override val isNegated: Boolean,
    ) : Negatable

    /**
     * An entity type or entity-type-tag filter.
     *
     * @property target concrete entity type or entity-type tag
     * @property isNegated whether the filter excludes this type or tag
     */
    public data class Type(
        public val target: SelectorEntityType,
        override val isNegated: Boolean,
    ) : Negatable

    /**
     * A scoreboard-tag filter.
     *
     * Negation lives inside [condition]; presence conditions carry their polarity and are never
     * additionally negated.
     *
     * @property condition named tag or explicit presence condition
     */
    public data class Tag(
        public val condition: SelectorStringCondition,
    ) : Negatable {
        override val isNegated: Boolean get() = condition.isNegated
    }

    /**
     * A scoreboard-team filter.
     *
     * Negation lives inside [condition]; presence conditions carry their polarity and are never
     * additionally negated.
     *
     * @property condition named team or explicit presence condition
     */
    public data class Team(
        public val condition: SelectorStringCondition,
    ) : Negatable {
        override val isNegated: Boolean get() = condition.isNegated
    }

    /**
     * A structured NBT filter retained as validated SNBT source.
     *
     * @property snbt validated compound SNBT
     * @property isNegated whether the filter excludes matching NBT
     */
    public data class Nbt(
        public val snbt: SnbtCompoundSource,
        override val isNegated: Boolean,
    ) : Negatable

    /**
     * A datapack predicate filter.
     *
     * @property key datapack predicate key
     * @property isNegated whether the filter excludes matching predicates
     */
    public data class Predicate(
        public val key: Key,
        override val isNegated: Boolean,
    ) : Negatable

    //endregion
    //region Compound collection arguments

    /**
     * An immutable collection of scoreboard objective ranges.
     *
     * @property scores score requirements in source order
     */
    @ConsistentCopyVisibility
    public data class Scores private constructor(
        public val scores: List<SelectorScoreRequirement>,
    ) : EntitySelectorArgument {
        public constructor(scores: Collection<SelectorScoreRequirement>) : this(scores.toList())
    }

    /**
     * An immutable collection of advancement requirements.
     *
     * @property advancements advancement requirements in source order
     */
    @ConsistentCopyVisibility
    public data class Advancements private constructor(
        public val advancements: List<SelectorAdvancementRequirement>,
    ) : EntitySelectorArgument {
        /** Builds an advancements argument from a defensive immutable snapshot of [advancements]. */
        public constructor(advancements: Collection<SelectorAdvancementRequirement>) : this(advancements.toList())
    }

    //endregion
}

//region Extension properties - exhaustive keyword and name mapping

/**
 * The keyword of this argument, or `null` for coordinates and floating-point ranges (whose names
 * are owned by [SelectorCoordinate] and [SelectorRangeArgument]).
 *
 * Exhaustive: a new argument subtype cannot be added without updating this mapping. This ensures
 * that rendering and parsing stay in sync.
 */
internal val EntitySelectorArgument.keyword: SelectorArgumentKeyword?
    get() =
        when (this) {
            is EntitySelectorArgument.Coordinate, is EntitySelectorArgument.Range -> null
            is EntitySelectorArgument.Level -> SelectorArgumentKeyword.LEVEL
            is EntitySelectorArgument.Limit -> SelectorArgumentKeyword.LIMIT
            is EntitySelectorArgument.Sort -> SelectorArgumentKeyword.SORT
            is EntitySelectorArgument.GameMode -> SelectorArgumentKeyword.GAMEMODE
            is EntitySelectorArgument.Name -> SelectorArgumentKeyword.NAME
            is EntitySelectorArgument.Type -> SelectorArgumentKeyword.TYPE
            is EntitySelectorArgument.Tag -> SelectorArgumentKeyword.TAG
            is EntitySelectorArgument.Team -> SelectorArgumentKeyword.TEAM
            is EntitySelectorArgument.Nbt -> SelectorArgumentKeyword.NBT
            is EntitySelectorArgument.Scores -> SelectorArgumentKeyword.SCORES
            is EntitySelectorArgument.Predicate -> SelectorArgumentKeyword.PREDICATE
            is EntitySelectorArgument.Advancements -> SelectorArgumentKeyword.ADVANCEMENTS
        }

/**
 * The vanilla selector-source name of this argument, such as `limit` in `limit=1`.
 *
 * Coordinates and ranges are named by their own argument types; keyword arguments resolve their
 * names from the [keyword] property.
 */
internal val EntitySelectorArgument.argumentName: String
    get() =
        when (this) {
            is EntitySelectorArgument.Coordinate -> coordinate.argumentName
            is EntitySelectorArgument.Range -> argument.argumentName
            else -> checkNotNull(keyword) { "Keyword arguments always declare a keyword" }.sourceName
        }

//endregion
