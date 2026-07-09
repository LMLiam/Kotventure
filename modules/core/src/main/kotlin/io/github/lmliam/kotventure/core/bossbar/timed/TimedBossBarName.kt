package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.text.Component
import kotlin.time.Duration

/**
 * Re-renders a managed boss bar's name each tick from the time remaining until completion.
 *
 * The block is a regular [ComponentScope], so the dynamic form composes exactly like the static
 * `name { }` form — child builders, styling, and the string shorthands all apply. The SAM keeps
 * `name { remaining -> text("…") }` distinct from the static [TimedBossBarScope.name] overloads
 * during overload resolution.
 */
public fun interface TimedBossBarName {
    /**
     * Appends the name's content to the receiving component scope for the given remaining
     * duration.
     *
     * @param remaining time left until the bar completes.
     */
    public fun ComponentScope.render(remaining: Duration)
}

/** Fixed name: ignore remaining time; change-detection on the bar skips redundant pushes. */
internal fun Component.asFixedTimedName(): (Duration) -> Component = { _ -> this }

/** Dynamic name: re-enter a component scope each tick with [TimedBossBarName] as the renderer. */
internal fun TimedBossBarName.asDynamicTimedName(): (Duration) -> Component =
    { remaining ->
        component {
            with(this@asDynamicTimedName) {
                render(remaining)
            }
        }
    }
