package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Arguments shared by every typed entity-selector head.
 *
 * @sample io.github.lmliam.kotventure.core.selector.commonEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface CommonEntitySelectorScope {
    /** Requires at least one scoreboard tag. */
    public val any: SelectorPresence

    /** Requires no scoreboard tags. */
    public val none: SelectorPresence

    /** Survival mode. */
    public val survival: GameMode

    /** Creative mode. */
    public val creative: GameMode

    /** Adventure mode. */
    public val adventure: GameMode

    /** Spectator mode. */
    public val spectator: GameMode

    /** Filters by distance using a [SelectorRange]. */
    public fun distance(range: SelectorRange)

    /** Filters by distance using a Kotlin [ClosedFloatingPointRange]. */
    public fun distance(range: ClosedFloatingPointRange<Double>)

    /** Filters by scoreboard tag. */
    public fun tag(tag: String)

    /** Filters by whether any scoreboard tag is present. */
    public fun tag(presence: SelectorPresence)

    /**
     * Excludes entities with this scoreboard tag: `tag(!"muted")`. Repeatable.
     *
     * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
     */
    public fun tag(tag: Excluded<String>)

    /** Filters by entity name. */
    public fun name(name: String)

    /**
     * Excludes entities with this name: `name(!"Boss")`. Exclusions accumulate.
     *
     * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
     */
    public fun name(name: Excluded<String>)

    /** Filters by experience level using a [LevelRange]. */
    public fun level(range: LevelRange)

    /** Filters by experience level using a Kotlin [IntRange]. */
    public fun level(range: IntRange)

    /** Filters by game mode. */
    public fun gamemode(mode: GameMode)

    /**
     * Excludes entities in this game mode: `gamemode(!survival)`. Exclusions accumulate.
     *
     * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
     */
    public fun gamemode(mode: Excluded<GameMode>)

    /** Marks a string argument value as excluded: `tag(!"muted")`. */
    public operator fun String.not(): Excluded<String> = Excluded(this)

    /** Marks a game mode as excluded: `gamemode(!survival)`. */
    public operator fun GameMode.not(): Excluded<GameMode> = Excluded(this)
}
