package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.bossbar.bossBar
import io.github.lmliam.kotventure.core.text.text

internal fun audienceBossBarSample() {
    val audience = emptyAudience()

    audience.bossBar {
        name { text("Raid") }
    }
}

internal fun audienceShowHideBossBarSample() {
    val audience = emptyAudience()
    val bar =
        bossBar {
            name { text("Ender Dragon") }
            progress(0.25f)
            color(purple)
        }

    audience.show(bar)
    audience.hide(bar)
}
