package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun tabListSample() {
    val audience = emptyAudience()

    audience.tabList {
        header {
            text("Welcome") { color(gold) }
        }
        footer { text("play.example.org") }
    }
}
