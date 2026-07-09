package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.text.Component

/**
 * How a managed boss bar resolves its name: fixed once, or re-rendered each tick from remaining
 * time.
 */
internal sealed class BossBarNameSpec {
    data class Static(
        val component: Component,
    ) : BossBarNameSpec()

    data class Dynamic(
        val render: TimedBossBarName,
    ) : BossBarNameSpec()
}
