package io.github.lmliam.kotventure.core.animation

import io.github.lmliam.kotventure.core.registry.AdventureDsl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class AnimationDriverRegistrationTest :
    StringSpec(
        {
            beforeTest {
                AdventureDsl.reset()
            }

            afterTest {
                AdventureDsl.reset()
            }

            "registers animation drivers through the register extension" {
                val driver = TestAnimationDriver("test")

                val returned = driver.register()

                returned shouldBeSameInstanceAs driver
                animationDriver("test") shouldBe driver
            }

            "replaces an animation driver registered with the same name" {
                val first = TestAnimationDriver("test")
                val second = TestAnimationDriver("test")

                first.register()
                second.register()

                animationDriver("test") shouldBeSameInstanceAs second
            }

            "returns null for unknown animation driver names" {
                animationDriver("missing").shouldBeNull()
            }
        },
    )

private class TestAnimationDriver(
    override val name: String,
) : AnimationDriver {
    override fun start(
        animationId: String,
        onTick: () -> Unit,
    ): Unit = onTick()

    override fun tick(animationId: String): Unit = Unit

    override fun stop(animationId: String): Unit = Unit
}
