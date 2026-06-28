package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.format.Style

internal fun styleSample() {
    val heading =
        style {
        color(gold)
        bold()
    }
    val title = text("Welcome") { style(heading) }
}

internal fun styledSample() {
    val heading =
        style {
        color(gold)
        bold()
    }
    val highlighted = component { text("important") } styled heading
}

internal fun styleScopeSample() {
    style {
        color(gold)
        bold()
        italic(false)
    }
}
