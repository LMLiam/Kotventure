package io.github.lmliam.kotventure.core.platform

import io.github.lmliam.kotventure.core.registry.AdventureDsl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.kyori.adventure.audience.Audience

class PlatformAdapterRegistrationTest :
    StringSpec(
        {
            beforeTest {
                AdventureDsl.reset()
            }

            afterTest {
                AdventureDsl.reset()
            }

            "registers the active platform adapter through the register extension" {
                val adapter = TestPlatformAdapter("paper")

                val returned = adapter.register()

                returned shouldBeSameInstanceAs adapter
                platformAdapter() shouldBe adapter
            }

            "replaces the active platform adapter on re-registration" {
                val first = TestPlatformAdapter("paper")
                val second = TestPlatformAdapter("velocity")

                first.register()
                second.register()

                platformAdapter() shouldBeSameInstanceAs second
            }

            "returns null when no platform adapter is registered" {
                platformAdapter().shouldBeNull()
            }
        },
    )

private class TestPlatformAdapter(
    override val name: String,
) : PlatformAdapter {
    override fun console(): Audience = Audience.empty()

    override fun players(): Iterable<Audience> = emptyList()

    override fun all(): Audience = Audience.empty()

    override fun schedule(task: () -> Unit): PlatformTask {
        task()
        return PlatformTask { }
    }
}
