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

    /**
     * Sets supplied selector origin coordinates.
     *
     * @throws IllegalArgumentException if no coordinate is supplied or a supplied value is not finite
     * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
     */
    public fun origin(
        x: Double? = null,
        y: Double? = null,
        z: Double? = null,
    )

    /**
     * Sets supplied selector bounding-volume deltas.
     *
     * @throws IllegalArgumentException if no delta is supplied or a supplied value is not finite
     * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
     */
    public fun volume(
        dx: Double? = null,
        dy: Double? = null,
        dz: Double? = null,
    )

    /** Filters by scoreboard tag. */
    public fun tag(tag: String)

    /** Filters by whether any scoreboard tag is present. */
    public fun tag(presence: SelectorPresence)

    /** Filters by entity name. */
    public fun name(name: String)

    /** Filters by experience level using a [LevelRange]. */
    public fun level(range: LevelRange)

    /** Filters by experience level using a Kotlin [IntRange]. */
    public fun level(range: IntRange)

    /** Filters by game mode. */
    public fun gamemode(mode: GameMode)
}
