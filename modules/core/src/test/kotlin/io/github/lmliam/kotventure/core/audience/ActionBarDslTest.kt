package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

private class ActionBarRecordingAudience : Audience {
    val actionBars = mutableListOf<Component>()

    override fun sendActionBar(message: Component) {
        actionBars += message
    }
}

class ActionBarDslTest :
    StringSpec(
        {
            "builds and shows the component" {
                val audience = ActionBarRecordingAudience()

                audience.actionBar {
                    text("+10 XP") {
                        color(red)
                        bold()
                    }
                }

                audience.actionBars shouldHaveSize 1
                val actionBar = audience.actionBars.single()
                actionBar shouldHaveChildCount 1
                actionBar.childAt(0) shouldContainText "+10 XP"
                actionBar.childAt(0) shouldHaveColor red
                actionBar.childAt(0) shouldHaveDecoration TextDecoration.BOLD
            }

            "shows the same component to every member of a forwarding audience" {
                val first = ActionBarRecordingAudience()
                val second = ActionBarRecordingAudience()

                audienceOf(first, second).actionBar {
                    text("Broadcast")
                }

                first.actionBars shouldHaveSize 1
                second.actionBars shouldHaveSize 1
                first.actionBars.single() shouldBe second.actionBars.single()
            }

            "shows an empty component from an empty block" {
                val audience = ActionBarRecordingAudience()

                audience.actionBar {}

                audience.actionBars.single() shouldBe Component.empty()
            }
        },
    )
