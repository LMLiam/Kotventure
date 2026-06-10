package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeEntityNbtComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveEntitySelector
import io.github.lmliam.kotventure.test.text.shouldHaveNbtPath
import io.github.lmliam.kotventure.test.text.shouldHaveNbtSeparator
import io.github.lmliam.kotventure.test.text.shouldInterpret
import io.github.lmliam.kotventure.test.text.shouldNotHaveNbtSeparator
import io.github.lmliam.kotventure.test.text.shouldNotInterpret
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class EntityNbtDslTest :
    StringSpec(
        {
            "builds an entity nbt component with a selector and path" {
                val component = entityNbt("@p", "Inventory[0].tag.display.Name").shouldBeEntityNbtComponent()

                component shouldHaveEntitySelector "@p"
                component shouldHaveNbtPath "Inventory[0].tag.display.Name"
                component.shouldNotInterpret()
                component.shouldNotHaveNbtSeparator()
            }

            "applies style to the entity nbt root" {
                val component =
                    entityNbt("@r", "CustomName") {
                        color(NamedTextColor.AQUA)
                        bold()
                        style {
                            underlined()
                        }
                    }

                component shouldHaveColor NamedTextColor.AQUA
                component shouldHaveDecoration TextDecoration.BOLD
                component shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "appends child components" {
                val suffix = Component.text(" entity")

                val component =
                    entityNbt("@p", "CustomName") {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "sets interpret true" {
                val component =
                    entityNbt("@p", "CustomName") {
                        interpret(true)
                    }

                component.shouldBeEntityNbtComponent().shouldInterpret()
            }

            "sets a component separator" {
                val separator = Component.text(", ")

                val component =
                    entityNbt("@a", "Inventory[].id") {
                        separator(separator)
                    }

                component.shouldBeEntityNbtComponent() shouldHaveNbtSeparator separator
            }

            "sets an inline text separator" {
                val component =
                    entityNbt("@a", "Inventory[].id") {
                        separator {
                            content(" | ")
                            color(NamedTextColor.GRAY)
                        }
                    }

                val separator = checkNotNull(component.shouldBeEntityNbtComponent().separator())

                separator shouldHaveColor NamedTextColor.GRAY
                separator shouldContainText " | "
            }
        },
    )
