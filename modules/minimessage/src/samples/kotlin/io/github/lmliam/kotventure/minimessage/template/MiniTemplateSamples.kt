package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component

internal fun miniTemplateRenderSample() {
    val template =
        object : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
            val player by placeholder<Component>()
            val count by placeholder<Int>()
        }

    val forAlex =
        template {
            player bind component { text("Alex") }
            count bind 3
        }
}
