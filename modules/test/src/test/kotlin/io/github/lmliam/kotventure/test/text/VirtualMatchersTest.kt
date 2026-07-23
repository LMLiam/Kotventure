package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.VirtualComponentRenderer
import java.util.Locale

private fun localeComponent(fallback: String = "player") =
    Component.virtual(
        Locale::class.java,
        object : VirtualComponentRenderer<Locale> {
            override fun apply(context: Locale): ComponentLike = Component.text(context.language)

            override fun fallbackString(): String = fallback
        },
    )

class VirtualMatchersTest :
    StringSpec(
        {
            "matches the context type and fallback string" {
                val component = localeComponent().shouldBeVirtualComponent()

                component.shouldHaveContextType<Locale>()
                component shouldHaveFallbackString "player"
            }

            "fails when the context type differs" {
                shouldThrow<AssertionError> {
                    localeComponent().shouldBeVirtualComponent().shouldHaveContextType<String>()
                }
            }

            "fails when the fallback string differs" {
                shouldThrow<AssertionError> {
                    localeComponent().shouldBeVirtualComponent() shouldHaveFallbackString "console"
                }
            }

            "fails when shouldNot is given the actual context type" {
                shouldThrow<AssertionError> {
                    localeComponent().shouldBeVirtualComponent() shouldNot haveContextType<Locale>()
                }
            }

            "fails when shouldNot is given the actual fallback string" {
                shouldThrow<AssertionError> {
                    localeComponent().shouldBeVirtualComponent() shouldNot haveFallbackString("player")
                }
            }

            "fails on a non-virtual component" {
                shouldThrow<AssertionError> {
                    Component.text("plain").shouldBeVirtualComponent()
                }
            }
        },
    )
