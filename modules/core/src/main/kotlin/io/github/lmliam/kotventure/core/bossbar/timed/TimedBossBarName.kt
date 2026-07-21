package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.text.Component
import kotlin.time.Duration

/**
 * Produces a managed boss-bar name from its remaining lifetime.
 *
 * [render] runs after each progress update. The receiving [ComponentScope] supports the same child and style operations
 * as a fixed name.
 */
public fun interface TimedBossBarName {
    /**
     * Appends the name content for [remaining] to the receiving component scope.
     *
     * @param remaining time left until the bar completes.
     */
    public fun ComponentScope.render(remaining: Duration)
}

/** Adapts a fixed component to the runtime name function. */
internal fun Component.asFixedTimedName(): (Duration) -> Component = { _ -> this }

/** Creates a component for each runtime call by invoking this renderer in a new component scope. */
internal fun TimedBossBarName.asDynamicTimedName(): (Duration) -> Component =
    { remaining ->
        component {
            with(this@asDynamicTimedName) {
                render(remaining)
            }
        }
    }
