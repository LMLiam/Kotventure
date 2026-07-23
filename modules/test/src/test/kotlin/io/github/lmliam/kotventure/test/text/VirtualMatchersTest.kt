package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
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

            "reports context type mismatch with expected and actual types" {
                val failure =
                    shouldThrow<AssertionError> {
                        localeComponent().shouldBeVirtualComponent().shouldHaveContextType<String>()
                    }
                val expectedMessage = "Expected context type <java.lang.String>, but was <java.util.Locale>."

                failure.message shouldContain expectedMessage
            }

            "reports fallback string mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        localeComponent().shouldBeVirtualComponent() shouldHaveFallbackString "console"
                    }
                val expectedMessage = "Expected fallback string <console>, but was <player>."

                failure.message shouldContain expectedMessage
            }

            "reports a context type that should not be the actual type" {
                val failure =
                    shouldThrow<AssertionError> {
                        localeComponent().shouldBeVirtualComponent() shouldNot haveContextType<Locale>()
                    }
                val expectedMessage = "Expected context type not to be <java.util.Locale>."

                failure.message shouldContain expectedMessage
            }

            "reports a fallback string that should not be the actual value" {
                val failure =
                    shouldThrow<AssertionError> {
                        localeComponent().shouldBeVirtualComponent() shouldNot haveFallbackString("player")
                    }
                val expectedMessage = "Expected fallback string not to be <player>."

                failure.message shouldContain expectedMessage
            }

            "reports non-virtual components before virtual assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldBeVirtualComponent()
                    }
                val expectedMessage = "Expected virtual component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }
        },
    )
