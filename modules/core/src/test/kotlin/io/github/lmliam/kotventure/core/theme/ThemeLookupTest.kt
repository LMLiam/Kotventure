package io.github.lmliam.kotventure.core.theme

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.format.Style

class ThemeLookupTest :
    StringSpec(
        {
            "resolves registered themes by name" {
                val registry = ThemeRegistry()
                val provider = TestThemeProvider("brand")

                registry.register(provider)

                registry.theme("brand") shouldBe provider
            }

            "returns null for unknown theme names" {
                ThemeRegistry().theme("missing").shouldBeNull()
            }

            "resolves the registered default theme" {
                val registry = ThemeRegistry()
                val provider = TestThemeProvider("server")

                registry.register(provider, default = true)

                registry.defaultTheme() shouldBe provider
            }

            "returns null when no default theme is registered" {
                val registry = ThemeRegistry()

                registry.register(TestThemeProvider("brand"))

                registry.defaultTheme().shouldBeNull()
            }

            "rejects duplicate theme registrations" {
                val registry = ThemeRegistry()

                registry.register(TestThemeProvider("brand"))

                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    registry.register(TestThemeProvider("brand"))
                }
            }

            "rejects registering a second default theme" {
                val registry = ThemeRegistry()

                registry.register(TestThemeProvider("brand"), default = true)

                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    registry.register(TestThemeProvider("server"), default = true)
                }
            }
        },
    )

private class TestThemeProvider(
    override val name: String,
) : ThemeProvider {
    override fun style(name: String): Style? = null
}
