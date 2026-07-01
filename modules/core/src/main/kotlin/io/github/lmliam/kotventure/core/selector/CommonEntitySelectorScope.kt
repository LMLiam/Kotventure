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

    /**
     * Sets selector origin coordinates (vanilla `x`, `y`, `z`): `origin(12.5.x, 64.y)`.
     *
     * Each coordinate binds once across the whole selector.
     *
     * @throws IllegalStateException if a supplied coordinate is already set
     * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
     */
    public fun origin(
        first: OriginCoordinate,
        vararg rest: OriginCoordinate,
    )

    /**
     * Sets selector bounding-volume deltas (vanilla `dx`, `dy`, `dz`): `volume(16.dx, 8.dy)`.
     *
     * Each delta binds once across the whole selector.
     *
     * @throws IllegalStateException if a supplied delta is already set
     * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
     */
    public fun volume(
        first: VolumeDelta,
        vararg rest: VolumeDelta,
    )

    /**
     * This number as the origin `x` coordinate.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.x: OriginCoordinate get() = originCoordinate("x", this)

    /**
     * This number as the origin `y` coordinate.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.y: OriginCoordinate get() = originCoordinate("y", this)

    /**
     * This number as the origin `z` coordinate.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.z: OriginCoordinate get() = originCoordinate("z", this)

    /**
     * This number as the bounding-volume `dx` delta.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.dx: VolumeDelta get() = volumeDelta("dx", this)

    /**
     * This number as the bounding-volume `dy` delta.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.dy: VolumeDelta get() = volumeDelta("dy", this)

    /**
     * This number as the bounding-volume `dz` delta.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.dz: VolumeDelta get() = volumeDelta("dz", this)

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
