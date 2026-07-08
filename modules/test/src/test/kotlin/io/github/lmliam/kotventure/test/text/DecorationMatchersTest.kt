package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

class DecorationMatchersTest :
    StringSpec(
        {
            "matches root decorations" {
                text("Title") { bold(true) } shouldHaveDecoration TextDecoration.BOLD
            }

            "matches missing root decorations" {
                text("Title") shouldNotHaveDecoration TextDecoration.BOLD
            }

            "reports decoration mismatch with expected and actual state" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Title") shouldHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <TRUE>, " +
                            "but was <NOT_SET>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected root decorations" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Title") { bold(true) } shouldNotHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <NOT_SET>, " +
                            "but was <TRUE>."

                failure.message shouldContain expectedMessage
            }

            "does not treat explicitly disabled decorations as missing" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Title") { bold(false) } shouldNotHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <NOT_SET>, " +
                            "but was <FALSE>."

                failure.message shouldContain expectedMessage
            }

            "matches explicit decoration states" {
                text("Title") { italic(false) }
                    .shouldHaveDecoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            }

            "matches each decoration through its own assertion" {
                text("x") { bold(true) }.shouldBeBold()
                text("x") { italic(true) }.shouldBeItalic()
                text("x") { underlined(true) }.shouldBeUnderlined()
                text("x") { strikethrough(true) }.shouldBeStrikethrough()
                text("x") { obfuscated(true) }.shouldBeObfuscated()
            }

            "matches the absence of each decoration through its own assertion" {
                text("plain").shouldNotBeBold()
                text("plain").shouldNotBeItalic()
                text("plain").shouldNotBeUnderlined()
                text("plain").shouldNotBeStrikethrough()
                text("plain").shouldNotBeObfuscated()
            }

            "treats an explicitly disabled decoration as not enabled" {
                text("x") { bold(false) }.shouldNotBeBold()
            }

            "reports a component that is not bold" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("plain").shouldBeBold()
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <TRUE>, but was <NOT_SET>."

                failure.message shouldContain expectedMessage
            }

            "reports a component that is unexpectedly italic" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("x") { italic(true) }.shouldNotBeItalic()
                    }
                val expectedMessage = "Expected component decoration <${TextDecoration.ITALIC}> not to be <TRUE>."

                failure.message shouldContain expectedMessage
            }
        },
    )
