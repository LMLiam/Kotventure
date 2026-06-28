package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component

internal fun miniTemplateRenderSample() {
    val template =
        object : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
        val player = placeholder<Component>("player")
        val count = placeholder<Int>("count")
    }

    val forAlex =
        template {
        player bind component { text("Alex") }
        count bind 3
    }
}
