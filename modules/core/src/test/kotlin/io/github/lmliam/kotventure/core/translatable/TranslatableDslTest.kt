package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldHaveArgumentCount
import io.github.lmliam.kotventure.test.text.shouldHaveArguments
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveFallback
import io.github.lmliam.kotventure.test.text.shouldHaveTranslationKey
import io.github.lmliam.kotventure.test.text.shouldNotHaveFallback
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class TranslatableDslTest :
    StringSpec(
        {
            "builds a translatable component with a key" {
                val component = translatable("item.minecraft.diamond")

                component shouldHaveTranslationKey "item.minecraft.diamond"
                component.shouldNotHaveFallback()
                component shouldHaveArgumentCount 0
            }

            "applies fallback text" {
                val component =
                    translatable("missing.key") {
                        fallback("Missing translation")
                    }

                component shouldHaveTranslationKey "missing.key"
                component shouldHaveFallback "Missing translation"
            }

            "adds a component argument" {
                val item = Component.text("Diamond")

                val component =
                    translatable("item.count") {
                        arg(item)
                    }

                component.shouldHaveArguments(TranslationArgument.component(item))
            }

            "adds boolean and numeric arguments" {
                val component =
                    translatable("settings.toggle") {
                        arg(true)
                        arg(3)
                    }

                component.shouldHaveArguments(
                    TranslationArgument.bool(true),
                    TranslationArgument.numeric(3),
                )
            }

            "adds multiple component arguments at once" {
                val player = Component.text("Alex")
                val item = Component.text("Diamond")

                val component =
                    translatable("chat.type.item") {
                        args(player, item)
                    }

                component.shouldHaveArguments(
                    TranslationArgument.component(player),
                    TranslationArgument.component(item),
                )
            }

            "applies style to the translatable root" {
                val component =
                    translatable("menu.title") {
                        color(NamedTextColor.GOLD)
                        bold()
                        style {
                            underlined()
                        }
                    }

                component shouldHaveColor NamedTextColor.GOLD
                component shouldHaveDecoration TextDecoration.BOLD
                component shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "appends child components" {
                val suffix = Component.text(" unlocked")

                val component =
                    translatable("advancements.toast.task") {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "combines fallback arguments style and children" {
                val player = Component.text("Alex")
                val suffix = Component.text("!")

                val component =
                    translatable("quest.progress") {
                        fallback("Quest progress")
                        arg(player)
                        arg(false)
                        arg(12.5)
                        color(NamedTextColor.AQUA)
                        italic()
                        append(suffix)
                    }

                component shouldHaveTranslationKey "quest.progress"
                component shouldHaveFallback "Quest progress"
                component.shouldHaveArguments(
                    TranslationArgument.component(player),
                    TranslationArgument.bool(false),
                    TranslationArgument.numeric(12.5),
                )
                component shouldHaveColor NamedTextColor.AQUA
                component shouldHaveDecoration TextDecoration.ITALIC
                component.childAt(0) shouldBe suffix
            }
        },
    )
