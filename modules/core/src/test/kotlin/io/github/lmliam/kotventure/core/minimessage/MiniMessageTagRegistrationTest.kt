package io.github.lmliam.kotventure.core.minimessage

import io.github.lmliam.kotventure.core.registry.AdventureDsl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class MiniMessageTagRegistrationTest :
    StringSpec(
        {
            beforeTest {
                AdventureDsl.reset()
            }

            afterTest {
                AdventureDsl.reset()
            }

            "registers tag providers through the register extension" {
                val provider = TestMiniMessageTagProvider("player")

                val returned = provider.register()

                returned shouldBeSameInstanceAs provider
                miniMessageTag("player") shouldBe provider
            }

            "replaces a tag provider registered with the same name" {
                val first = TestMiniMessageTagProvider("player")
                val second = TestMiniMessageTagProvider("player")

                first.register()
                second.register()

                miniMessageTag("player") shouldBeSameInstanceAs second
            }

            "returns null for unknown tag provider names" {
                miniMessageTag("missing").shouldBeNull()
            }
        },
    )

private class TestMiniMessageTagProvider(
    override val name: String,
) : MiniMessageTagProvider
