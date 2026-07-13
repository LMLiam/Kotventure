package io.github.lmliam.kotventure.test.snapshot.readme

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.snapshot.shouldMatchSnapshot
import io.github.lmliam.kotventure.test.text.shouldBeBold
import io.github.lmliam.kotventure.test.text.shouldHaveClickAction
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor

internal fun readmeTestingTourSample() {
    val nameplate =
        text("Alex") {
            color(aqua)
            bold()
            click { suggest("/friend add Alex") }
        }

    nameplate shouldHaveColor NamedTextColor.AQUA
    nameplate.shouldBeBold()
    nameplate shouldHaveClickAction ClickEvent.Action.SUGGEST_COMMAND
    nameplate shouldMatchSnapshot "join-nameplate"
}
