package io.github.lmliam.kotventure.minimessage.external

import io.github.lmliam.kotventure.minimessage.MiniTemplate
import io.github.lmliam.kotventure.minimessage.bind
import io.github.lmliam.kotventure.minimessage.invoke
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.kotest.core.spec.style.StringSpec
import net.kyori.adventure.text.Component

private object ExternalWelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages") {
    val player = placeholder<Component>("player")
    val count = placeholder<Int>("count")
}

class MiniTemplateExternalDslTest :
    StringSpec(
        {
            "renders with unqualified placeholders from another package" {
                val rendered =
                    ExternalWelcomeTemplate {
                        bind(player, Component.text("Alex"))
                        bind(count, 3)
                    }

                rendered shouldContainText "Alex"
                rendered shouldContainText "3"
            }
        },
    )
