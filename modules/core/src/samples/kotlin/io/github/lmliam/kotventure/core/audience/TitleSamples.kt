package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.ticks
import kotlin.time.Duration.Companion.seconds

internal fun titleSample() {
    val audience = emptyAudience()

    audience.title {
        title {
            text("Welcome") { color(gold) }
        }
        subtitle { text("to the server") }
        times(fadeIn = 1.ticks, stay = 3.seconds, fadeOut = 1.ticks)
    }
}
