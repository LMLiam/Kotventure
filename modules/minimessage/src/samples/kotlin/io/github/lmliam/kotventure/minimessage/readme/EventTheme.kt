package io.github.lmliam.kotventure.minimessage.readme

import io.github.lmliam.kotventure.core.color.hex
import io.github.lmliam.kotventure.core.theme.Theme
import net.kyori.adventure.text.format.Style

internal object EventTheme : Theme("event") {
    val accent = hex("#55FFAA")

    val header: Style by style {
        color(accent)
        bold()
    }
}
