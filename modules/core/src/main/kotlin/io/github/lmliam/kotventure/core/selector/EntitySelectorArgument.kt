package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * One immutable, typed Java Edition entity-selector argument.
 */
public sealed interface EntitySelectorArgument {
    /**
     * An argument that vanilla syntax can prefix-negate with `!`.
     */
    public sealed interface Negatable : EntitySelectorArgument {
        /** Whether the argument excludes matching entities instead of requiring them. */
        public val isNegated: Boolean
    }

    /**
     * A selector coordinate or bounding-volume delta.
     *
     * @property coordinate coordinate argument name
     * @property value finite coordinate value
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

    /**
     * A game-mode filter.
     *
     * @property value parsed game mode
     * @property isNegated whether the filter excludes this game mode
     */
    public data class GameMode(
        public val value: io.github.lmliam.kotventure.core.selector.GameMode,
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
     * A scoreboard-tag filter. Negation lives inside [condition]; presence conditions carry their
     * polarity and are never additionally negated.
     *
     * @property condition named tag or explicit presence condition
     */
    public data class Tag(
        public val condition: SelectorStringCondition,
    ) : Negatable {
        override val isNegated: Boolean get() = condition.isNegated
    }

    /**
     * A scoreboard-team filter. Negation lives inside [condition]; presence conditions carry their
     * polarity and are never additionally negated.
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
     * An immutable collection of scoreboard objective ranges.
     *
     * @property scores score requirements in source order
     */
    @ConsistentCopyVisibility
    public data class Scores private constructor(
        public val scores: List<SelectorScoreRequirement>,
    ) : EntitySelectorArgument {
        /** Builds a scores argument from a defensive snapshot of [scores]. */
        public constructor(scores: Collection<SelectorScoreRequirement>) : this(
            buildList(scores.size) { addAll(scores) },
        )
    }

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

    /**
     * An immutable collection of advancement requirements.
     *
     * @property advancements advancement requirements in source order
     */
    @ConsistentCopyVisibility
    public data class Advancements private constructor(
        public val advancements: List<SelectorAdvancementRequirement>,
    ) : EntitySelectorArgument {
        /** Builds an advancements argument from a defensive snapshot of [advancements]. */
        public constructor(advancements: Collection<SelectorAdvancementRequirement>) : this(
            buildList(advancements.size) { addAll(advancements) },
        )
    }
}

/**
 * The vanilla selector-source name of this argument, such as `limit` in `limit=1`. Exhaustive so a
 * new argument cannot be added without declaring its syntax name.
 */
internal val EntitySelectorArgument.argumentName: String
    get() =
        when (this) {
            is EntitySelectorArgument.Coordinate -> coordinate.argumentName
            is EntitySelectorArgument.Range -> argument.argumentName
            is EntitySelectorArgument.Limit -> "limit"
            is EntitySelectorArgument.Sort -> "sort"
            is EntitySelectorArgument.Level -> "level"
            is EntitySelectorArgument.GameMode -> "gamemode"
            is EntitySelectorArgument.Name -> "name"
            is EntitySelectorArgument.Type -> "type"
            is EntitySelectorArgument.Tag -> "tag"
            is EntitySelectorArgument.Team -> "team"
            is EntitySelectorArgument.Nbt -> "nbt"
            is EntitySelectorArgument.Scores -> "scores"
            is EntitySelectorArgument.Predicate -> "predicate"
            is EntitySelectorArgument.Advancements -> "advancements"
        }
