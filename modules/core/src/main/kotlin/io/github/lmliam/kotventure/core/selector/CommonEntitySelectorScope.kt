package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key

/**
 * Arguments shared by every typed entity-selector head.
 *
 * @sample io.github.lmliam.kotventure.core.selector.commonEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface CommonEntitySelectorScope {
    /** Requires at least one value for a presence-aware selector argument. */
    public val any: SelectorPresence

    /** Requires no value for a presence-aware selector argument. */
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
     * Filters by vertical rotation using a [SelectorRange].
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun xRotation(range: SelectorRange)

    /**
     * Filters by vertical rotation using a Kotlin range, including descending wrap-around ranges.
     *
     * @throws IllegalArgumentException if either bound is not finite
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun xRotation(range: ClosedFloatingPointRange<Double>)

    /**
     * Filters by horizontal rotation using a [SelectorRange].
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun yRotation(range: SelectorRange)

    /**
     * Filters by horizontal rotation using a Kotlin range, including descending wrap-around ranges.
     *
     * @throws IllegalArgumentException if either bound is not finite
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun yRotation(range: ClosedFloatingPointRange<Double>)

    /**
     * Sets supplied selector origin coordinates.
     *
     * Repeated calls replace only the coordinates supplied by the later call.
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
     * Repeated calls replace only the deltas supplied by the later call.
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

    /**
     * Filters by a named scoreboard team.
     *
     * Repeated calls replace the previous positive team filter.
     *
     * @throws IllegalArgumentException if [team] is empty or is not a vanilla unquoted token
     * @sample io.github.lmliam.kotventure.core.selector.selectorTeamSample
     */
    public fun team(team: String)

    /**
     * Filters by whether any scoreboard team is present.
     *
     * Repeated calls replace the previous positive team filter.
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorTeamSample
     */
    public fun team(presence: SelectorPresence)

    /**
     * Adds a structured NBT compound filter.
     *
     * Repeated calls preserve every compound in call order.
     * Raw SNBT is intentionally unsupported; use [entitySelector] for raw selector interop.
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorNbtSample
     */
    public fun nbt(init: NbtCompoundScope.() -> Unit)

    /**
     * Filters a scoreboard [objective] using an integral [range].
     *
     * Repeated objectives replace their range without changing insertion order.
     *
     * @throws IllegalArgumentException if [objective] is empty or is not a vanilla unquoted token
     * @sample io.github.lmliam.kotventure.core.selector.selectorScoreSample
     */
    public fun score(
        objective: String,
        range: LevelRange,
    )

    /**
     * Filters a scoreboard [objective] using a closed Kotlin [IntRange].
     *
     * Repeated objectives replace their range without changing insertion order.
     *
     * @throws IllegalArgumentException if [objective] is invalid or [range] is descending
     * @sample io.github.lmliam.kotventure.core.selector.selectorScoreSample
     */
    public fun score(
        objective: String,
        range: IntRange,
    )

    /**
     * Adds a datapack predicate filter.
     *
     * Repeated calls preserve every [predicate] key in call order.
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorPredicateSample
     */
    public fun predicate(predicate: Key)

    /** Filters by entity name. */
    public fun name(name: String)

    /** Filters by experience level using a [LevelRange]. */
    public fun level(range: LevelRange)

    /** Filters by experience level using a Kotlin [IntRange]. */
    public fun level(range: IntRange)

    /** Filters by game mode. */
    public fun gamemode(mode: GameMode)
}
