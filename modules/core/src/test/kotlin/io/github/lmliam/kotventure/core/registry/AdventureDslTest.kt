package io.github.lmliam.kotventure.core.registry

import io.github.lmliam.kotventure.core.animation.AnimationDriver
import io.github.lmliam.kotventure.core.minimessage.MiniMessageTagProvider
import io.github.lmliam.kotventure.core.platform.PlatformAdapter
import io.github.lmliam.kotventure.core.platform.PlatformTask
import io.github.lmliam.kotventure.core.theme.ThemeProvider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style

class AdventureDslTest :
    StringSpec(
        {
            beforeTest {
                AdventureDsl.reset()
            }

            afterTest {
                AdventureDsl.reset()
            }

            "registers and retrieves MiniMessage tag providers by name" {
                val provider = TestMiniMessageTagProvider("player")

                AdventureDsl.registerMiniMessageTag(provider)

                AdventureDsl.miniMessageTag("player") shouldBe provider
                AdventureDsl.miniMessageTags() shouldContainExactly mapOf("player" to provider)
            }

            "registers and retrieves theme providers by name" {
                val provider =
                    TestThemeProvider(
                        name = "brand",
                        styles = mapOf("header" to Style.style(NamedTextColor.AQUA)),
                    )

                AdventureDsl.registerTheme(provider)

                AdventureDsl.theme("brand") shouldBe provider
                AdventureDsl.themes() shouldContainExactly mapOf("brand" to provider)
            }

            "registers and retrieves animation drivers by name" {
                val driver = TestAnimationDriver("test")

                AdventureDsl.registerAnimationDriver(driver)

                AdventureDsl.animationDriver("test") shouldBe driver
                AdventureDsl.animationDrivers() shouldContainExactly mapOf("test" to driver)
            }

            "registers and retrieves the active platform adapter" {
                val adapter = TestPlatformAdapter("paper")

                AdventureDsl.registerPlatformAdapter(adapter)

                AdventureDsl.platformAdapter() shouldBe adapter
            }

            "returns null for unregistered extension lookups" {
                AdventureDsl.miniMessageTag("missing").shouldBeNull()
                AdventureDsl.theme("missing").shouldBeNull()
                AdventureDsl.animationDriver("missing").shouldBeNull()
                AdventureDsl.platformAdapter().shouldBeNull()
            }

            "returns immutable MiniMessage tag snapshots" {
                val tagProvider = TestMiniMessageTagProvider("player")

                AdventureDsl.registerMiniMessageTag(tagProvider)
                val snapshot = AdventureDsl.miniMessageTags()
                AdventureDsl.reset()

                snapshot shouldContainExactly mapOf("player" to tagProvider)
                AdventureDsl.miniMessageTags() shouldContainExactly emptyMap()
            }

            "returns immutable theme snapshots" {
                val themeProvider =
                    TestThemeProvider(
                        name = "brand",
                        styles = mapOf("header" to Style.style(NamedTextColor.AQUA)),
                    )

                AdventureDsl.registerTheme(themeProvider)
                val snapshot = AdventureDsl.themes()
                AdventureDsl.reset()

                snapshot shouldContainExactly mapOf("brand" to themeProvider)
                AdventureDsl.themes() shouldContainExactly emptyMap()
            }

            "returns immutable animation driver snapshots" {
                val driver = TestAnimationDriver("test")

                AdventureDsl.registerAnimationDriver(driver)
                val snapshot = AdventureDsl.animationDrivers()
                AdventureDsl.reset()

                snapshot shouldContainExactly mapOf("test" to driver)
                AdventureDsl.animationDrivers() shouldContainExactly emptyMap()
            }
        },
    )

private data class TestMiniMessageTagProvider(
    override val name: String,
) : MiniMessageTagProvider

private data class TestThemeProvider(
    override val name: String,
    private val styles: Map<String, Style>,
) : ThemeProvider {
    override fun style(name: String): Style? = styles[name]
}

private data class TestAnimationDriver(
    override val name: String,
) : AnimationDriver {
    override fun start(
        animationId: String,
        onTick: () -> Unit,
    ): Unit = onTick()

    override fun tick(animationId: String): Unit = Unit

    override fun stop(animationId: String): Unit = Unit
}

private data class TestPlatformAdapter(
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
