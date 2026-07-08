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

private class RecordingAudience : Audience {
    val messages = mutableListOf<Component>()

    override fun sendMessage(message: Component) {
        messages += message
    }
}

class MessageDslTest :
    StringSpec(
        {
            "builds and sends the component" {
                val audience = RecordingAudience()

                audience.message {
                    text("Hello") {
                        color(red)
                        bold()
                    }
                }

                audience.messages shouldHaveSize 1
                val message = audience.messages.single()
                message shouldHaveChildCount 1
                message.childAt(0) shouldContainText "Hello"
                message.childAt(0) shouldHaveColor red
                message.childAt(0) shouldHaveDecoration TextDecoration.BOLD
            }

            "sends the same component to every member of a forwarding audience" {
                val first = RecordingAudience()
                val second = RecordingAudience()

                Audience.audience(first, second).message {
                    text("Broadcast")
                }

                first.messages shouldHaveSize 1
                second.messages shouldHaveSize 1
                first.messages.single() shouldBe second.messages.single()
            }

            "sends an empty component from an empty block" {
                val audience = RecordingAudience()

                audience.message {}

                audience.messages.single() shouldBe Component.empty()
            }
        },
    )
