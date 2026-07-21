package io.github.lmliam.kotventure.core.bossbar.timed

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar

internal class ThrowingHideAudience : Audience {
    override fun showBossBar(bar: BossBar): Unit = Unit

    override fun hideBossBar(bar: BossBar) {
        error("hide failed")
    }
}
