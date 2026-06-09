package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

class ComponentMatchersTest :
    StringSpec(
        {
            "matches text content on Adventure text components" {
                Component.text("Hello") shouldContainText "Hello"
            }

            "matches text content nested in child components" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                component shouldContainText "world"
            }

            "reports text mismatch with expected and actual content" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Hello") shouldContainText "Bye"
                    }
                val expectedMessage = "Expected component text to contain <Bye>, but was <Hello>."

                failure.message shouldContain expectedMessage
            }

            "reports nested text mismatch with the complete extracted content" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .append(Component.text("!"))
                        .build()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldContainText "missing"
                    }
                val expectedMessage =
                    "Expected component text to contain <missing>, but was <Hello world!>."

                failure.message shouldContain expectedMessage
            }

            "matches root component colors" {
                Component.text("Warning", NamedTextColor.RED) shouldHaveColor NamedTextColor.RED
            }

            "reports color mismatch with expected and actual colors" {
                val component = Component.text("Warning", NamedTextColor.RED)

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveColor NamedTextColor.BLUE
                    }
                val expectedMessage =
                    "Expected component color <${NamedTextColor.BLUE}>, " +
                            "but was <${NamedTextColor.RED}>."

                failure.message shouldContain expectedMessage
            }

            "reports missing root color with expected and actual colors" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Warning") shouldHaveColor NamedTextColor.RED
                    }
                val expectedMessage =
                    "Expected component color <${NamedTextColor.RED}>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "matches complete Adventure styles" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                Component.text("Title").style(style) shouldHaveStyle style
            }

            "matches root decorations" {
                Component
                    .text("Title")
                    .decoration(TextDecoration.BOLD, true) shouldHaveDecoration TextDecoration.BOLD
            }

            "matches missing root decorations" {
                Component.text("Title") shouldNotHaveDecoration TextDecoration.BOLD
            }

            "reports decoration mismatch with expected and actual state" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Title") shouldHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <TRUE>, " +
                            "but was <NOT_SET>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected root decorations" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Title")
                            .decoration(TextDecoration.BOLD, true) shouldNotHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <NOT_SET>, " +
                            "but was <TRUE>."

                failure.message shouldContain expectedMessage
            }

            "does not treat explicitly disabled decorations as missing" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Title")
                            .decoration(TextDecoration.BOLD, false) shouldNotHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <NOT_SET>, " +
                            "but was <FALSE>."

                failure.message shouldContain expectedMessage
            }

            "matches child count and retrieves children by index" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "world"
            }

            "reports missing child indexes clearly" {
                val failure =
                    shouldThrow<IllegalStateException> {
                        Component.text("Hello").childAt(0)
                    }
                val expectedMessage = "Expected child at index <0>, but component has <0> children."

                failure.message shouldContain expectedMessage
            }

            "matches translatable component keys" {
                Component.translatable("item.minecraft.diamond") shouldHaveTranslationKey "item.minecraft.diamond"
            }

            "reports translation key mismatch with expected and actual keys" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("item.minecraft.diamond") shouldHaveTranslationKey
                            "item.minecraft.emerald"
                    }
                val expectedMessage =
                    "Expected translation key <item.minecraft.emerald>, " +
                            "but was <item.minecraft.diamond>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable fallback text" {
                Component.translatable("missing.key", "Missing key") shouldHaveFallback "Missing key"
            }

            "reports fallback mismatch with expected and actual fallback text" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("missing.key", "Missing key") shouldHaveFallback "Other fallback"
                    }
                val expectedMessage =
                    "Expected translatable fallback <Other fallback>, " +
                            "but was <Missing key>."

                failure.message shouldContain expectedMessage
            }

            "matches absent translatable fallback text" {
                Component.translatable("missing.key").shouldNotHaveFallback()
            }

            "reports unexpected translatable fallback text" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("missing.key", "Missing key").shouldNotHaveFallback()
                    }
                val expectedMessage = "Expected translatable fallback to be absent, but was <Missing key>."

                failure.message shouldContain expectedMessage
            }

            "reports absent fallback checks on non-translatable components" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldNotHaveFallback()
                    }
                val expectedMessage = "Expected translatable fallback to be absent, but was <not translatable>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable argument counts" {
                val argument = TranslationArgument.component(Component.text("Diamond"))

                Component.translatable("item.count", null as String?, argument) shouldHaveArgumentCount 1
            }

            "reports argument count mismatch with expected and actual counts" {
                val argument = TranslationArgument.component(Component.text("Diamond"))

                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("item.count", null as String?, argument) shouldHaveArgumentCount 2
                    }
                val expectedMessage = "Expected <2> translation arguments, but found <1>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable arguments exactly" {
                val item = TranslationArgument.component(Component.text("Diamond"))
                val amount = TranslationArgument.numeric(3)

                Component.translatable("item.count", null as String?, item, amount).shouldHaveArguments(item, amount)
            }

            "reports argument mismatch with expected and actual arguments" {
                val item = TranslationArgument.component(Component.text("Diamond"))
                val actualAmount = TranslationArgument.numeric(3)
                val expectedAmount = TranslationArgument.numeric(4)

                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .translatable("item.count", null as String?, item, actualAmount)
                            .shouldHaveArguments(item, expectedAmount)
                    }
                val expectedMessage =
                    "Expected translation arguments <${listOf(item, expectedAmount)}>, " +
                            "but found <${listOf(item, actualAmount)}>."

                failure.message shouldContain expectedMessage
            }
        },
    )
