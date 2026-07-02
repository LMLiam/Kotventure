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
    public data class Gamemode(
        public val value: GameMode,
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
     * @property key entity type or tag key
     * @property isTag whether [key] identifies an entity-type tag
     * @property isNegated whether the filter excludes this type or tag
     */
    public data class Type(
        public val key: Key,
        public val isTag: Boolean,
        override val isNegated: Boolean,
    ) : Negatable

    /**
     * A scoreboard-tag filter.
     *
     * @property condition named tag or explicit presence condition
     * @property isNegated whether a named condition is negated
     */
    public data class Tag(
        public val condition: SelectorStringCondition,
        override val isNegated: Boolean,
    ) : Negatable {
        init {
            require(condition is SelectorStringCondition.Named || !isNegated) {
                "Selector presence conditions cannot be prefix-negated."
            }
        }
    }

    /**
     * A scoreboard-team filter.
     *
     * @property condition named team or explicit presence condition
     * @property isNegated whether a named condition is negated
     */
    public data class Team(
        public val condition: SelectorStringCondition,
        override val isNegated: Boolean,
    ) : Negatable {
        init {
            require(condition is SelectorStringCondition.Named || !isNegated) {
                "Selector presence conditions cannot be prefix-negated."
            }
        }
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

    /** An immutable collection of scoreboard objective ranges. */
    public class Scores(
        scores: Collection<ParsedSelectorScore>,
    ) : EntitySelectorArgument {
        /** Score requirements in source order. */
        public val scores: List<ParsedSelectorScore> =
            buildList(scores.size) {
                addAll(scores)
            }

        /** Returns a score argument with the supplied requirements. */
        public fun copy(scores: Collection<ParsedSelectorScore> = this.scores): Scores = Scores(scores)

        public override fun equals(other: Any?): Boolean = other is Scores && scores == other.scores

        public override fun hashCode(): Int = scores.hashCode()

        public override fun toString(): String = "Scores(scores=$scores)"
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

    /** An immutable collection of advancement requirements. */
    public class Advancements(
        advancements: Collection<ParsedSelectorAdvancement>,
    ) : EntitySelectorArgument {
        /** Advancement requirements in source order. */
        public val advancements: List<ParsedSelectorAdvancement> =
            buildList(advancements.size) {
                addAll(advancements)
            }

        /** Returns an advancement argument with the supplied requirements. */
        public fun copy(advancements: Collection<ParsedSelectorAdvancement> = this.advancements): Advancements =
            Advancements(advancements)

        public override fun equals(other: Any?): Boolean = other is Advancements && advancements == other.advancements

        public override fun hashCode(): Int = advancements.hashCode()

        public override fun toString(): String = "Advancements(advancements=$advancements)"
    }
}
