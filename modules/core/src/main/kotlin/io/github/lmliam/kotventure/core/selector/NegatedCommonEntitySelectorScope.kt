package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key

/**
 * Negated filters shared by every typed selector head.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedCommonEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface NegatedCommonEntitySelectorScope {
    /** Survival mode. */
    public val survival: GameMode

    /** Creative mode. */
    public val creative: GameMode

    /** Adventure mode. */
    public val adventure: GameMode

    /** Spectator mode. */
    public val spectator: GameMode

    /** Excludes entities with this name. */
    public fun name(name: String)

    /** Excludes entities with this scoreboard tag. */
    public fun tag(tag: String)

    /**
     * Excludes entities in this named scoreboard team.
     *
     * Repeated calls preserve each named exclusion.
     *
     * @throws IllegalArgumentException if [team] is empty or is not a vanilla unquoted token
     * @sample io.github.lmliam.kotventure.core.selector.selectorTeamSample
     */
    public fun team(team: String)

    /**
     * Adds a negated structured NBT compound filter.
     *
     * Repeated positive and negated calls preserve every compound in call order.
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorNbtSample
     */
    public fun nbt(init: NbtCompoundScope.() -> Unit)

    /**
     * Adds a negated datapack predicate filter.
     *
     * Repeated positive and negated calls preserve every [predicate] key in call order.
     *
     * @sample io.github.lmliam.kotventure.core.selector.selectorPredicateSample
     */
    public fun predicate(predicate: Key)

    /** Excludes entities in this game mode. */
    public fun gamemode(mode: GameMode)
}
