package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

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

    /** Excludes entities in this game mode. */
    public fun gamemode(mode: GameMode)
}
