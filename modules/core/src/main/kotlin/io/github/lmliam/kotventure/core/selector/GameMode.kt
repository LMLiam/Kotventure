package io.github.lmliam.kotventure.core.selector

/**
 * A Minecraft game mode, used to filter an entity selector.
 *
 * Access via scoped constants inside [EntitySelectorScope]: `gamemode(survival)`.
 */
public enum class GameMode(
    internal val value: String,
) {
    SURVIVAL("survival"),
    CREATIVE("creative"),
    ADVENTURE("adventure"),
    SPECTATOR("spectator"),
}
