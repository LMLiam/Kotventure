package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.selector.allPlayers
import io.github.lmliam.kotventure.core.selector.entities
import io.github.lmliam.kotventure.core.selector.entitySelector
import io.github.lmliam.kotventure.core.selector.nearestPlayer
import io.github.lmliam.kotventure.core.selector.randomPlayer
import io.github.lmliam.kotventure.core.selector.self
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
                val path = nbtPath("Inventory")[0]["tag"]["display"]["Name"]

                val component = entityNbt(nearestPlayer(), path).shouldBeEntityNbtComponent()

                component shouldHaveEntitySelector "@p"
                component shouldHaveNbtPath "Inventory[0].tag.display.Name"
                component.shouldNotInterpret()
                component.shouldNotHaveNbtSeparator()
            }

            "accepts a parsed selector and path" {
                val component =
                    entityNbt(
                        entitySelector("@e[type=minecraft:zombie,limit=1]"),
                        nbtPath("CustomName"),
                    ).shouldBeEntityNbtComponent()

                component shouldHaveEntitySelector "@e[type=minecraft:zombie,limit=1]"
                component shouldHaveNbtPath "CustomName"
            }

            "applies style to the entity nbt root" {
                val component =
                    entityNbt(randomPlayer(), nbtPath("CustomName")) {
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
                    entityNbt(nearestPlayer(), nbtPath("CustomName")) {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "sets interpret true" {
                val component =
                    entityNbt(nearestPlayer(), nbtPath("CustomName")) {
                        interpret(true)
                    }

                component.shouldBeEntityNbtComponent().shouldInterpret()
            }

            "sets a component separator" {
                val separator = Component.text(", ")
                val path = nbtPath("Inventory")[all]["id"]

                val component =
                    entityNbt(allPlayers(), path) {
                        separator(separator)
                    }

                component.shouldBeEntityNbtComponent() shouldHaveNbtSeparator separator
            }

            "sets an inline text separator" {
                val component =
                    entityNbt(allPlayers(), nbtPath("Inventory[].id")) {
                        separator {
                            content(" | ")
                            color(NamedTextColor.GRAY)
                        }
                    }

                val separator = checkNotNull(component.shouldBeEntityNbtComponent().separator())

                separator shouldHaveColor NamedTextColor.GRAY
                separator shouldContainText " | "
            }

            "uses self selector" {
                val component = entityNbt(self(), nbtPath("Health")).shouldBeEntityNbtComponent()

                component shouldHaveEntitySelector "@s"
            }

            "uses entities with arguments" {
                val selector =
                    entities {
                        type("armor_stand")
                        limit(1)
                    }

                val component = entityNbt(selector, nbtPath("CustomName")).shouldBeEntityNbtComponent()

                component shouldHaveEntitySelector "@e[type=minecraft:armor_stand,limit=1]"
            }
        },
    )
