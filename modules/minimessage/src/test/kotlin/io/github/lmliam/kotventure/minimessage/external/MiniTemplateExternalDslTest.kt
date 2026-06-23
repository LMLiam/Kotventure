package io.github.lmliam.kotventure.minimessage.external

import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import io.github.lmliam.kotventure.minimessage.template.bind
import io.github.lmliam.kotventure.minimessage.template.invoke
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.kotest.core.spec.style.StringSpec
import net.kyori.adventure.text.Component

private object ExternalWelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
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
