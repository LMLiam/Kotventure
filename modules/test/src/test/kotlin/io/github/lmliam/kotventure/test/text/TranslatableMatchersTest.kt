package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.TranslationArgument

class TranslatableMatchersTest :
    StringSpec(
        {
            "matches translatable component keys" {
                translatable("item.minecraft.diamond") shouldHaveTranslationKey "item.minecraft.diamond"
            }

            "reports translation key mismatch with expected and actual keys" {
                val failure =
                    shouldThrow<AssertionError> {
                        translatable("item.minecraft.diamond") shouldHaveTranslationKey
                                "item.minecraft.emerald"
                    }
                val expectedMessage =
                    "Expected translation key <item.minecraft.emerald>, " +
                            "but was <item.minecraft.diamond>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable fallback text" {
                translatable("missing.key") { fallback("Missing key") } shouldHaveFallback "Missing key"
            }

            "reports fallback mismatch with expected and actual fallback text" {
                val failure =
                    shouldThrow<AssertionError> {
                        translatable("missing.key") { fallback("Missing key") } shouldHaveFallback "Other fallback"
                    }
                val expectedMessage =
                    "Expected translatable fallback <Other fallback>, " +
                            "but was <Missing key>."

                failure.message shouldContain expectedMessage
            }

            "reports fallback checks on non-translatable components" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("plain") shouldHaveFallback "Missing key"
                    }
                val expectedMessage = "Expected translatable fallback <Missing key>, but was <not translatable>."

                failure.message shouldContain expectedMessage
            }

            "matches absent translatable fallback text" {
                translatable("missing.key").shouldNotHaveFallback()
            }

            "reports unexpected translatable fallback text" {
                val failure =
                    shouldThrow<AssertionError> {
                        translatable("missing.key") { fallback("Missing key") }.shouldNotHaveFallback()
                    }
                val expectedMessage = "Expected translatable fallback to be absent, but was <Missing key>."

                failure.message shouldContain expectedMessage
            }

            "reports absent fallback checks on non-translatable components" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("plain").shouldNotHaveFallback()
                    }
                val expectedMessage = "Expected translatable fallback to be absent, but was <not translatable>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable argument counts" {
                translatable("item.count") {
                    arg {
                        content("Diamond")
                    }
                } shouldHaveArgumentCount 1
            }

            "reports argument count mismatch with expected and actual counts" {
                val failure =
                    shouldThrow<AssertionError> {
                        translatable("item.count") {
                            arg {
                                content("Diamond")
                            }
                        } shouldHaveArgumentCount 2
                    }
                val expectedMessage = "Expected <2> translation arguments, but found <1>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable arguments exactly" {
                val item = TranslationArgument.component(text("Diamond"))
                val amount = TranslationArgument.numeric(3)

                translatable("item.count") {
                    arg {
                        content("Diamond")
                    }
                    arg(3)
                }.shouldHaveArguments(item, amount)
            }

            "reports argument mismatch with expected and actual arguments" {
                val item = TranslationArgument.component(text("Diamond"))
                val actualAmount = TranslationArgument.numeric(3)
                val expectedAmount = TranslationArgument.numeric(4)

                val failure =
                    shouldThrow<AssertionError> {
                        translatable("item.count") {
                            arg {
                                content("Diamond")
                            }
                            arg(3)
                        }.shouldHaveArguments(item, expectedAmount)
                    }
                val expectedMessage =
                    "Expected translation arguments <${listOf(item, expectedAmount)}>, " +
                            "but found <${listOf(item, actualAmount)}>."

                failure.message shouldContain expectedMessage
            }
        },
    )
