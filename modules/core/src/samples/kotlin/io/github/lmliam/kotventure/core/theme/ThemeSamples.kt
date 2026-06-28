package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.color.hex
import net.kyori.adventure.text.format.Style

internal fun themeSample() {
    object : Theme("brand") {
        val primary = hex("#5865F2")

        val header: Style by style {
            color(primary)
            bold()
        }
    }
}
