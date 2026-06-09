package io.github.lmliam.kotventure.core.keybind

import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveKeybind
import io.github.lmliam.kotventure.test.text.shouldHaveScoreName
import io.github.lmliam.kotventure.test.text.shouldHaveScoreObjective
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class KeybindDslTest :
    StringSpec(
        {
            "builds a keybind component with a key" {
                val component = keybind("key.jump")

                component shouldHaveKeybind "key.jump"
            }

            "applies style to the keybind root" {
                val component =
                    keybind("key.inventory") {
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
                val suffix = Component.text(" to open inventory")

                val component =
                    keybind("key.inventory") {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "appends nested child builders from component scope" {
                val component =
                    keybind("key.jump") {
                        text(" pressed")
                        score("Alex", "kills") {
                            bold()
                        }
                    }

                component shouldHaveChildCount 2
                component.childAt(0) shouldContainText " pressed"
                component.childAt(1) shouldHaveScoreName "Alex"
                component.childAt(1) shouldHaveScoreObjective "kills"
                component.childAt(1) shouldHaveDecoration TextDecoration.BOLD
            }
        },
    )
