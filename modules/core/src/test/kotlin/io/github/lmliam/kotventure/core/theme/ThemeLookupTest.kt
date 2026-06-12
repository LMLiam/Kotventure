package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.registry.AdventureDsl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.format.Style

class ThemeLookupTest :
    StringSpec(
        {
            beforeTest {
                AdventureDsl.reset()
            }

            afterTest {
                AdventureDsl.reset()
            }

            "resolves registered themes by name" {
                val provider = TestThemeProvider("brand")

                provider.register()

                theme("brand") shouldBe provider
            }

            "returns null for unknown theme names" {
                theme("missing").shouldBeNull()
            }

            "resolves the registered default theme" {
                val provider = TestThemeProvider("server")

                provider.register(default = true)

                defaultTheme() shouldBe provider
            }

            "returns null when no default theme is registered" {
                TestThemeProvider("brand").register()

                defaultTheme().shouldBeNull()
            }
        },
    )

private class TestThemeProvider(
    override val name: String,
) : ThemeProvider {
    override fun style(name: String): Style? = null
}
