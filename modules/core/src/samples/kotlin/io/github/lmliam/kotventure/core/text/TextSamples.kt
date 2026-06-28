package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.gold

internal fun textSample() {
    val greeting =
        text("Hello") {
        color(gold)
        bold()
    }
}
