package io.github.lmliam.kotventure.core.bossbar.timed

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

/** Records successive boss-bar name updates for change-detection tests. */
internal class BossBarNameChangeRecorder : BossBar.Listener {
    val names: MutableList<Component> = mutableListOf()

    override fun bossBarNameChanged(
        bar: BossBar,
        oldName: Component,
        newName: Component,
    ) {
        names += newName
    }
}
