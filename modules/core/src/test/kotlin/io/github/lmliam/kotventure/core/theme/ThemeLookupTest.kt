package io.github.lmliam.kotventure.core.theme

import io.kotest.assertions.throwables.shouldThrow
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

            "rejects blank provider names" {
                shouldThrow<IllegalArgumentException> {
                    ThemeRegistry().register(TestThemeProvider(" "))
                }
            }

            "rejects duplicate theme registrations" {
                val registry = ThemeRegistry()

                registry.register(TestThemeProvider("brand"))

                shouldThrow<IllegalArgumentException> {
                    registry.register(TestThemeProvider("brand"))
                }
            }

            "rejects registering a second default theme" {
                val registry = ThemeRegistry()

                registry.register(TestThemeProvider("brand"), default = true)

                shouldThrow<IllegalArgumentException> {
                    registry.register(TestThemeProvider("server"), default = true)
                }
            }

            "unregisters a theme by object reference and returns the removed provider" {
                val registry = ThemeRegistry()
                val provider = TestThemeProvider("brand")

                registry.register(provider)

                registry.unregister(provider) shouldBe provider
                registry.theme("brand").shouldBeNull()
            }

            "unregisters a theme by name and returns the removed provider" {
                val registry = ThemeRegistry()
                val provider = TestThemeProvider("brand")

                registry.register(provider)

                registry.unregister("brand") shouldBe provider
                registry.theme("brand").shouldBeNull()
            }

            "unregister by object returns null when a different instance owns the name" {
                val registry = ThemeRegistry()
                val original = TestThemeProvider("brand")
                val reloaded = TestThemeProvider("brand")

                registry.register(original)
                registry.replace(reloaded)

                registry.unregister(original).shouldBeNull()
                registry.theme("brand") shouldBe reloaded
            }

            "unregister by object returns null when the provider was never registered" {
                ThemeRegistry().unregister(TestThemeProvider("brand")).shouldBeNull()
            }

            "unregister by name returns null when the name is not registered" {
                ThemeRegistry().unregister("missing").shouldBeNull()
            }

            "unregister by object clears the default when the default theme is removed" {
                val registry = ThemeRegistry()
                val provider = TestThemeProvider("brand")

                registry.register(provider, default = true)

                registry.unregister(provider) shouldBe provider
                registry.defaultTheme().shouldBeNull()
            }

            "unregister by name clears the default when the default theme is removed" {
                val registry = ThemeRegistry()
                val provider = TestThemeProvider("brand")

                registry.register(provider, default = true)

                registry.unregister("brand") shouldBe provider
                registry.defaultTheme().shouldBeNull()
            }

            "unregister leaves another default intact when removing a non-default theme" {
                val registry = ThemeRegistry()
                val brand = TestThemeProvider("brand")
                val server = TestThemeProvider("server")

                registry.register(brand, default = true)
                registry.register(server)

                registry.unregister(server) shouldBe server
                registry.defaultTheme() shouldBe brand
            }

            "replace swaps a theme under the same name" {
                val registry = ThemeRegistry()
                val original = TestThemeProvider("brand")
                val reloaded = TestThemeProvider("brand")

                registry.register(original)
                registry.replace(reloaded)

                registry.theme("brand") shouldBe reloaded
            }

            "replace inserts a theme that was not previously registered" {
                val registry = ThemeRegistry()
                val provider = TestThemeProvider("brand")

                registry.replace(provider)

                registry.theme("brand") shouldBe provider
            }

            "replace with default promotes the provider to the sole default" {
                val registry = ThemeRegistry()
                val brand = TestThemeProvider("brand")
                val server = TestThemeProvider("server")

                registry.register(brand, default = true)
                registry.replace(server, default = true)

                registry.defaultTheme() shouldBe server
                registry.theme("brand") shouldBe brand
            }

            "replace without default clears default when replacing the default theme" {
                val registry = ThemeRegistry()
                val original = TestThemeProvider("brand")
                val reloaded = TestThemeProvider("brand")

                registry.register(original, default = true)
                registry.replace(reloaded)

                registry.theme("brand") shouldBe reloaded
                registry.defaultTheme().shouldBeNull()
            }

            "replace without default leaves a different default theme intact" {
                val registry = ThemeRegistry()
                val brand = TestThemeProvider("brand")
                val server = TestThemeProvider("server")
                val serverReload = TestThemeProvider("server")

                registry.register(brand, default = true)
                registry.register(server)
                registry.replace(serverReload)

                registry.theme("server") shouldBe serverReload
                registry.defaultTheme() shouldBe brand
            }

            "replace rejects blank provider names" {
                shouldThrow<IllegalArgumentException> {
                    ThemeRegistry().replace(TestThemeProvider(" "))
                }
            }

            "hot-reload keeps a theme as default when replace sets default true" {
                val registry = ThemeRegistry()
                val original = TestThemeProvider("brand")
                val reloaded = TestThemeProvider("brand")

                registry.register(original, default = true)
                registry.replace(reloaded, default = true)

                registry.theme("brand") shouldBe reloaded
                registry.defaultTheme() shouldBe reloaded
            }
        },
    )

private class TestThemeProvider(
    override val name: String,
) : ThemeProvider {
    override fun style(name: String): Style? = null
}
