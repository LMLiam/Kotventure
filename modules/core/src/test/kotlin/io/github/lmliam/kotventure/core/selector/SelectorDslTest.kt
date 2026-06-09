package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorPattern
import io.github.lmliam.kotventure.test.text.shouldHaveSeparator
import io.github.lmliam.kotventure.test.text.shouldNotHaveSeparator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class SelectorDslTest :
    StringSpec(
        {
            "builds a selector component with a pattern" {
                val component = selector("@p")

                component shouldHaveSelectorPattern "@p"
                component.shouldNotHaveSeparator()
            }

            "sets a component separator" {
                val separator = Component.text(", ")

                val component =
                    selector("@a") {
                        separator(separator)
                    }

                component shouldHaveSeparator separator
            }

            "sets an inline text separator" {
                val component =
                    selector("@a") {
                        separator {
                            content(" | ")
                            color(NamedTextColor.GRAY)
                        }
                    }

                val separator = checkNotNull((component as SelectorComponent).separator())

                separator shouldHaveColor NamedTextColor.GRAY
                separator shouldContainText " | "
            }

            "applies style to the selector root" {
                val component =
                    selector("@r") {
                        color(NamedTextColor.AQUA)
                        obfuscated()
                        style {
                            underlined()
                        }
                    }

                component shouldHaveColor NamedTextColor.AQUA
                component shouldHaveDecoration TextDecoration.OBFUSCATED
                component shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "appends child components" {
                val suffix = Component.text(" joined")

                val component =
                    selector("@p") {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }
        },
    )
