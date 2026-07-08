package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun actionBarSample() {
    val audience = emptyAudience()

    audience.actionBar {
        text("+10 XP") { color(gold) }
    }
}
