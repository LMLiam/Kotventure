package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.text.Component
import kotlin.time.Duration

/**
 * Renders a managed boss bar's name from the time remaining until completion.
 *
 * Used as a SAM for [TimedBossBarScope.name], so call sites stay as
 * `name { remaining -> text("…") }` while remaining distinct from the static
 * [BossBarBaseScope.name] overloads on the JVM.
 */
public fun interface TimedBossBarName {
    /**
     * Builds the name component for the given remaining duration.
     *
     * @param remaining time left until the bar completes.
     */
    public operator fun invoke(remaining: Duration): Component
}
