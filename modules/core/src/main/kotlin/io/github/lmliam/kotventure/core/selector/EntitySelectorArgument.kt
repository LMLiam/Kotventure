package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * One immutable, typed Java Edition entity-selector argument.
 */
public sealed interface EntitySelectorArgument {
    /** A selector coordinate or bounding-volume delta. */
    public data class Coordinate(
        public val coordinate: SelectorCoordinate,
        public val value: Double,
    ) : EntitySelectorArgument

    /** A floating-point selector range. */
    public data class Range(
        public val argument: SelectorRangeArgument,
        public val range: SelectorRange,
    ) : EntitySelectorArgument

    /** A result limit. */
    public data class Limit(
        public val value: Int,
    ) : EntitySelectorArgument

    /** A result sort order. */
    public data class Sort(
        public val value: SelectorSort,
    ) : EntitySelectorArgument

    /** An integral experience-level range. */
    public data class Level(
        public val range: LevelRange,
    ) : EntitySelectorArgument

    /** A game-mode filter. */
    public data class Gamemode(
        public val value: GameMode,
        public val isNegated: Boolean,
    ) : EntitySelectorArgument

    /** An entity-name filter, retaining the original quote style when present. */
    public data class Name(
        public val value: String,
        public val quote: Char?,
        public val isNegated: Boolean,
    ) : EntitySelectorArgument

    /** An entity type or entity-type-tag filter. */
    public data class Type(
        public val key: Key,
        public val isTag: Boolean,
        public val isNegated: Boolean,
    ) : EntitySelectorArgument

    /** A scoreboard-tag filter; an empty value represents a presence condition. */
    public data class Tag(
        public val value: String,
        public val isNegated: Boolean,
    ) : EntitySelectorArgument

    /** A scoreboard-team filter; an empty value represents a presence condition. */
    public data class Team(
        public val value: String,
        public val isNegated: Boolean,
    ) : EntitySelectorArgument

    /** A structured NBT filter retained as validated SNBT source. */
    public data class Nbt(
        public val snbt: String,
        public val isNegated: Boolean,
    ) : EntitySelectorArgument

    /** An immutable collection of scoreboard objective ranges. */
    public class Scores(
        scores: Collection<ParsedSelectorScore>,
    ) : EntitySelectorArgument {
        /** Score requirements in source order. */
        public val scores: List<ParsedSelectorScore> = scores.immutableSnapshot()

        /** Returns a score argument with the supplied requirements. */
        public fun copy(scores: Collection<ParsedSelectorScore> = this.scores): Scores = Scores(scores)

        public override fun equals(other: Any?): Boolean = other is Scores && scores == other.scores

        public override fun hashCode(): Int = scores.hashCode()

        public override fun toString(): String = "Scores(scores=$scores)"
    }

    /** A datapack predicate filter. */
    public data class Predicate(
        public val key: Key,
        public val isNegated: Boolean,
    ) : EntitySelectorArgument

    /** An immutable collection of advancement requirements. */
    public class Advancements(
        advancements: Collection<ParsedSelectorAdvancement>,
    ) : EntitySelectorArgument {
        /** Advancement requirements in source order. */
        public val advancements: List<ParsedSelectorAdvancement> =
            advancements.immutableSnapshot()

        /** Returns an advancement argument with the supplied requirements. */
        public fun copy(advancements: Collection<ParsedSelectorAdvancement> = this.advancements): Advancements =
            Advancements(advancements)

        public override fun equals(other: Any?): Boolean = other is Advancements && advancements == other.advancements

        public override fun hashCode(): Int = advancements.hashCode()

        public override fun toString(): String = "Advancements(advancements=$advancements)"
    }
}

/**
 * Coordinate names supported by Java Edition selectors.
 */
public enum class SelectorCoordinate(
    public val argumentName: String,
) {
    /** Origin X coordinate. */
    X("x"),

    /** Origin Y coordinate. */
    Y("y"),

    /** Origin Z coordinate. */
    Z("z"),

    /** Bounding-volume X delta. */
    DX("dx"),

    /** Bounding-volume Y delta. */
    DY("dy"),

    /** Bounding-volume Z delta. */
    DZ("dz"),
}

/**
 * Floating-point range arguments supported by Java Edition selectors.
 */
public enum class SelectorRangeArgument(
    public val argumentName: String,
) {
    /** Distance from the selector origin. */
    DISTANCE("distance"),

    /** Vertical rotation. */
    X_ROTATION("x_rotation"),

    /** Horizontal rotation. */
    Y_ROTATION("y_rotation"),
}

/**
 * One objective entry in a parsed `scores={...}` argument.
 */
public data class ParsedSelectorScore(
    public val objective: String,
    public val range: LevelRange,
)

/**
 * One advancement entry in a parsed `advancements={...}` argument.
 */
public data class ParsedSelectorAdvancement(
    public val advancement: Key,
    public val progress: ParsedAdvancementProgress,
)

/**
 * Whole-advancement or criterion-level progress.
 */
public sealed interface ParsedAdvancementProgress {
    /** Whole-advancement completion. */
    public data class Completion(
        public val completed: Boolean,
    ) : ParsedAdvancementProgress

    /** Criterion-level completion. */
    public class Criteria(
        criteria: Collection<ParsedAdvancementCriterion>,
    ) : ParsedAdvancementProgress {
        /** Criterion requirements in source order. */
        public val criteria: List<ParsedAdvancementCriterion> = criteria.immutableSnapshot()

        /** Returns criterion progress with the supplied requirements. */
        public fun copy(criteria: Collection<ParsedAdvancementCriterion> = this.criteria): Criteria = Criteria(criteria)

        public override fun equals(other: Any?): Boolean = other is Criteria && criteria == other.criteria

        public override fun hashCode(): Int = criteria.hashCode()

        public override fun toString(): String = "Criteria(criteria=$criteria)"
    }
}

/**
 * One criterion entry in parsed advancement progress.
 */
public data class ParsedAdvancementCriterion(
    public val name: String,
    public val completed: Boolean,
)
