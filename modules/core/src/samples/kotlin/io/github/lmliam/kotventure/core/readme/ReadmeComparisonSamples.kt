package io.github.lmliam.kotventure.core.readme

import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

internal fun readmeComparisonRawAdventure() {
    val warning =
        Component
            .text()
            .content("Watch out!")
            .color(NamedTextColor.RED)
            .decorate(TextDecoration.BOLD)
            .clickEvent(ClickEvent.runCommand("/duck"))
            .hoverEvent(HoverEvent.showText(Component.text("Quack")))
            .build()
}

internal fun readmeComparisonKotventure() {
    val warning =
        text("Watch out!") {
            color(red)
            bold()
            click { run("/duck") }
            hover { text("Quack") }
        }
}
