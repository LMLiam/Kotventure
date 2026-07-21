package io.github.lmliam.kotventure.core.selector

/**
 * A Minecraft game mode for an entity-selector filter.
 *
 * Access via scoped constants inside any selector scope (see [CommonEntitySelectorScope]): `gamemode(survival)`.
 */
public enum class GameMode(
    internal val value: String,
) {
    /** Survival mode. */
    SURVIVAL("survival"),

    /** Creative mode. */
    CREATIVE("creative"),

    /** Adventure mode. */
    ADVENTURE("adventure"),

    /** Spectator mode. */
    SPECTATOR("spectator"),
}
