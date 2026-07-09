package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.text.Component
import kotlin.time.Duration

/**
 * How a managed boss bar resolves its name: fixed once, or re-rendered each tick from remaining
 * time.
 */
internal sealed interface BossBarNameSpec {
    /** Resolves the name component for the given [remaining] time. */
    fun resolve(remaining: Duration): Component

    data class Static(
        val component: Component,
    ) : BossBarNameSpec {
        override fun resolve(remaining: Duration): Component = component
    }

    data class Dynamic(
        val render: TimedBossBarName,
    ) : BossBarNameSpec {
        override fun resolve(remaining: Duration): Component = component { with(render) { render(remaining) } }
    }
}
