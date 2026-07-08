package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.text.text
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

private class RecordingMemberAudience : Audience {
    val messages = mutableListOf<Component>()

    override fun sendMessage(message: Component) {
        messages += message
    }
}

class AudienceTest :
    StringSpec(
        {
            "emptyAudience returns the audience that ignores everything" {
                emptyAudience() shouldBeSameInstanceAs Audience.empty()
            }

            "audienceOf forwards to every member" {
                val first = RecordingMemberAudience()
                val second = RecordingMemberAudience()

                audienceOf(first, second).message { text("Broadcast") }

                first.messages shouldHaveSize 1
                second.messages shouldHaveSize 1
                first.messages.single() shouldBe second.messages.single()
            }
        },
    )
