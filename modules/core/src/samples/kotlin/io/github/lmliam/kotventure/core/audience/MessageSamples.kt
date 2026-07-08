package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun messageSample() {
    val audience = emptyAudience()

    audience.message {
        text("Welcome to the server") { color(gold) }
    }
}
