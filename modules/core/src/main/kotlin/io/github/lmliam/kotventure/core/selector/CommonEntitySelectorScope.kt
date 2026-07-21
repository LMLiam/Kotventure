package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key

/**
 * Provides the arguments that all typed entity-selector heads share.
 *
 * `origin`, `volume`, `distance`, `pitch`, `yaw`, `level`, `scores`, and `advancements` are singleton arguments. Each
 * coordinate in `origin` and `volume` is also a separate singleton. The scope throws [IllegalStateException] when a
 * singleton occurs more than one time.
 *
 * `tag`, `nbt`, and `predicate` are repeatable. They accept positive and negative filters together. `name`, `gamemode`,
 * and `team` accept multiple exclusions or one positive filter. They do not accept a positive filter together with an
 * exclusion. Filter arguments keep their call order.
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
     * Each coordinate can occur one time in the complete selector. If one call contains a duplicate, the call adds no
     * coordinates.
     *
     * @throws IllegalStateException when a supplied coordinate is already set or occurs more than one time in this
     * call.
     * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
     */
    public fun origin(
        first: OriginCoordinate,
        vararg rest: OriginCoordinate,
    )

    /**
     * Sets selector bounding-volume deltas (vanilla `dx`, `dy`, `dz`): `volume(16.dx, 8.dy)`.
     *
     * Each delta can occur one time in the complete selector. If one call contains a duplicate, the call adds no
     * deltas.
     *
     * @throws IllegalStateException when a supplied delta is already set or occurs more than one time in this call.
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
    public val Number.x: OriginCoordinate get() = originCoordinate(SelectorCoordinate.X, this)

    /**
     * This number as the origin `y` coordinate.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.y: OriginCoordinate get() = originCoordinate(SelectorCoordinate.Y, this)

    /**
     * This number as the origin `z` coordinate.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.z: OriginCoordinate get() = originCoordinate(SelectorCoordinate.Z, this)

    /**
     * This number as the bounding-volume `dx` delta.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.dx: VolumeDelta get() = volumeDelta(SelectorCoordinate.DX, this)

    /**
     * This number as the bounding-volume `dy` delta.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.dy: VolumeDelta get() = volumeDelta(SelectorCoordinate.DY, this)

    /**
     * This number as the bounding-volume `dz` delta.
     *
     * @throws IllegalArgumentException if the value is not finite
     */
    public val Number.dz: VolumeDelta get() = volumeDelta(SelectorCoordinate.DZ, this)

    /**
     * Sets the distance filter.
     *
     * @throws IllegalArgumentException when a bound is negative or the minimum is greater than the maximum.
     * @throws IllegalStateException when `distance` is already set.
     */
    public fun distance(range: SelectorRange)

    /**
     * Sets the distance filter from a Kotlin [ClosedFloatingPointRange].
     *
     * @throws IllegalArgumentException when a bound is not finite, a bound is negative, or the minimum is greater than
     * the maximum.
     * @throws IllegalStateException when `distance` is already set.
     */
    public fun distance(range: ClosedFloatingPointRange<Double>)

    /**
     * Filters by vertical look angle in degrees with vanilla `x_rotation` and a [SelectorRange]:
     * `pitch(atMost(-45.0))`.
     *
     * Angle mapping: `-90°` means straight up, `0°` means level, and `90°` means straight down.
     *
     * @throws IllegalStateException when `x_rotation` is already set.
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun pitch(range: SelectorRange)

    /**
     * Filters by vertical look angle in degrees with vanilla `x_rotation` and a Kotlin range:
     * `pitch(-90.0..-45.0)`.
     *
     * Angle mapping: `-90°` means straight up, `0°` means level, and `90°` means straight down.
     *
     * @throws IllegalArgumentException when a bound is not finite.
     * @throws IllegalStateException when `x_rotation` is already set.
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun pitch(range: ClosedFloatingPointRange<Double>)

    /**
     * Filters by horizontal look angle in degrees with vanilla `y_rotation` and a [SelectorRange]:
     * `yaw(atLeast(90.0))`.
     *
     * Angle mapping: `-180°` means north, `-90°` means east, `0°` means south, and `90°` means west.
     *
     * @throws IllegalStateException when `y_rotation` is already set.
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun yaw(range: SelectorRange)

    /**
     * Filters by horizontal look angle in degrees with vanilla `y_rotation` and a Kotlin range:
     * `yaw(0.0..90.0)`.
     *
     * Angle mapping: `-180°` means north, `-90°` means east, `0°` means south, and `90°` means west.
     *
     * Descending ranges like `yaw(170.0..-170.0)` wrap around ±180°, matching vanilla semantics.
     *
     * @throws IllegalArgumentException when a bound is not finite.
     * @throws IllegalStateException when `y_rotation` is already set.
     * @sample io.github.lmliam.kotventure.core.selector.selectorRotationSample
     */
    public fun yaw(range: ClosedFloatingPointRange<Double>)

    /**
     * Filters by scoreboard tag. Prefix the call with `!` to exclude the tag.
     *
     * @throws IllegalArgumentException if the tag name is empty (use `tag(any)` or `tag(none)` instead)
     * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
     */
    public fun tag(tag: String): SelectorFilterExpression

    /** Filters by whether any scoreboard tag is present. */
    public fun tag(presence: SelectorPresence)

    /**
     * Filters by a structured NBT compound. Prefix the call with `!` to exclude matching NBT.
     *
     * Repeated calls accumulate in declaration order.
     *
     * @throws IllegalStateException when [init] sets the same compound key more than one time.
     * @sample io.github.lmliam.kotventure.core.selector.selectorNbtSample
     */
    public fun nbt(init: NbtCompoundScope.() -> Unit): SelectorFilterExpression

    /**
     * Filters by a datapack predicate (vanilla `predicate`): `predicate(key("my_pack", "flying"))`.
     *
     * Prefix the call with `!` to require the predicate to fail. Repeated calls accumulate in
     * declaration order and must all match.
     *
     * There is deliberately no string overload: predicate IDs are datapack-defined, so a default
     * namespace would usually be wrong. Build IDs with
     * [key][io.github.lmliam.kotventure.core.key.key]. Validate complete selector source from
     * string interop with [parseSelector].
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorPredicateSample
     */
    public fun predicate(predicate: Key): SelectorFilterExpression

    /**
     * Filters by entity name. Prefix the call with `!` to exclude the name.
     *
     * A selector accepts one positive name or multiple excluded names. It does not accept both polarities.
     *
     * @throws IllegalStateException when the selector already has a positive name, or when positive and negative name
     * filters are combined.
     * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
     */
    public fun name(name: String): SelectorFilterExpression

    /**
     * Filters by experience level using a [SelectorIntRange]: `level(atLeast(30))`.
     *
     * @throws IllegalArgumentException when a bound is negative.
     * @throws IllegalStateException when `level` is already set.
     */
    public fun level(range: SelectorIntRange)

    /**
     * Filters by experience level using a Kotlin [IntRange]: `level(5..30)`.
     *
     * @throws IllegalArgumentException when the range is descending or a bound is negative.
     * @throws IllegalStateException when `level` is already set.
     */
    public fun level(range: IntRange)

    /**
     * Filters by scoreboard objective values (vanilla `scores={...}`):
     * `scores { "kills" eq atLeast(10) }`.
     *
     * Objectives render in declaration order. Each objective binds one time in the block. The complete argument binds
     * one time in the selector. Vanilla does not support negating `scores`,
     * so the block is not prefix-negatable.
     *
     * @throws IllegalArgumentException when an objective name is empty or invalid, or an [IntRange] is descending.
     * @throws IllegalStateException when `scores` is already set or an objective occurs more than one time.
     * @sample io.github.lmliam.kotventure.core.selector.selectorScoreSample
     */
    public fun scores(init: SelectorScoresScope.() -> Unit)

    /**
     * Filters by advancement progress (vanilla `advancements={...}`):
     * `advancements { key("minecraft", "story/smelt_iron") eq true }`.
     *
     * Advancements render in declaration order. Each advancement binds one time in the block. The complete argument
     * binds one time in the selector. Vanilla does not support negating
     * `advancements`. Thus, the block is not prefix-negatable. Require an incomplete advancement
     * with `eq false`.
     *
     * @throws IllegalArgumentException when a criterion name is empty or invalid.
     * @throws IllegalStateException when `advancements`, an advancement, or a criterion occurs more than one time.
     * @sample io.github.lmliam.kotventure.core.selector.selectorAdvancementsSample
     */
    public fun advancements(init: SelectorAdvancementsScope.() -> Unit)

    /**
     * Filters by game mode. Prefix the call with `!` to exclude the mode.
     *
     * A selector accepts one positive game mode or multiple excluded game modes. It does not accept both polarities.
     *
     * @throws IllegalStateException when the selector already has a positive game mode, or when positive and negative
     * game-mode filters are combined.
     * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
     */
    public fun gamemode(mode: GameMode): SelectorFilterExpression

    /**
     * Filters by team membership: `team("red")`. Prefix the call with `!` to exclude the team.
     *
     * A selector has at most one positive team.
     *
     * @throws IllegalArgumentException when the team name is empty or contains characters outside vanilla's
     * unquoted-token syntax. Use `team(none)` to match no team.
     * @throws IllegalStateException when a positive team is already set, or when positive and negative team filters
     * are combined.
     * @sample io.github.lmliam.kotventure.core.selector.selectorTeamSample
     */
    public fun team(team: String): SelectorFilterExpression

    /**
     * Filters by team presence. `team(any)` matches entities on a team with vanilla `team=!`. `team(none)` matches
     * entities without a team with vanilla `team=`.
     *
     * @throws IllegalStateException when a positive team is already set, or when positive and negative team filters
     * are combined.
     * @sample io.github.lmliam.kotventure.core.selector.selectorTeamSample
     */
    public fun team(presence: SelectorPresence)

    /**
     * Negates a filter expression created by this selector: `!tag("muted")`.
     *
     * @throws IllegalStateException when the expression belongs to another selector, its selector block has returned,
     * or it is already negated.
     * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
     */
    public operator fun SelectorFilterExpression.not(): Unit
}
