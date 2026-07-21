package io.github.lmliam.kotventure.core.bossbar.timed

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar

internal class TimedBossBarRecordingAudience : Audience {
    val shown: MutableList<BossBar> = mutableListOf()
    val hidden: MutableList<BossBar> = mutableListOf()

    override fun showBossBar(bar: BossBar) {
        shown += bar
    }

    override fun hideBossBar(bar: BossBar) {
        hidden += bar
    }
}
