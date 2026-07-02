package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.list
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeSelectorComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorPattern
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorSeparator
import io.github.lmliam.kotventure.test.text.shouldNotHaveSelectorSeparator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class SelectorDslTest :
    StringSpec(
        {
            "builds a selector component with a typed selector" {
                val component = selector(nearestPlayer()).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@p"
                component.shouldNotHaveSelectorSeparator()
            }

            "builds a selector component with a configured nearest entity" {
                val component =
                    selector(
                        nearestEntity {
                            type("minecraft:zombie")
                            distance(atMost(8.0))
                            limit(1)
                        },
                    ).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@n[type=minecraft:zombie,distance=..8,limit=1]"
            }

            "builds a selector component with typed negated filters" {
                val component =
                    selector(
                        entities {
                            typeTag(key("minecraft", "raiders"))
                            !tag("hidden")
                        },
                    ).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@e[type=#minecraft:raiders,tag=!hidden]"
            }

            "builds a selector component with typed NBT filters" {
                val component =
                    selector(
                        entities {
                            nbt {
                                "Tags" eq list("boss")
                            }
                            !nbt { "Silent" eq true }
                        },
                    ).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@e[nbt={Tags:[\"boss\"]},nbt=!{Silent:1b}]"
            }

            "builds a selector component with typed score filters" {
                val component =
                    selector(
                        allPlayers {
                            scores {
                                "kills" eq atLeast(10)
                                "deaths" eq 0..5
                            }
                        },
                    ).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@a[scores={kills=10..,deaths=0..5}]"
            }

            "builds a selector component with typed predicate filters" {
                val component =
                    selector(
                        entities {
                            predicate(key("my_pack", "on_fire"))
                            !predicate(key("my_pack", "hidden"))
                        },
                    ).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@e[predicate=my_pack:on_fire,predicate=!my_pack:hidden]"
            }

            "builds a selector component with a typed origin and volume" {
                val component =
                    selector(
                        nearestEntity {
                            origin(10.x, (-4).z)
                            volume(0.dx, 2.dy)
                        },
                    ).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@n[x=10,z=-4,dx=0,dy=2]"
            }

            "builds a selector component with the escape hatch" {
                val component = selector(entitySelector("@e[distance=..10]")).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@e[distance=..10]"
            }

            "sets a component separator" {
                val separator = Component.text(", ")

                val component =
                    selector(allPlayers()) {
                        separator(separator)
                    }

                component.shouldBeSelectorComponent() shouldHaveSelectorSeparator separator
            }

            "sets an inline text separator" {
                val component =
                    selector(allPlayers()) {
                        separator {
                            content(" | ")
                            color(NamedTextColor.GRAY)
                        }
                    }

                val separator = checkNotNull(component.shouldBeSelectorComponent().separator())

                separator shouldHaveColor NamedTextColor.GRAY
                separator shouldContainText " | "
            }

            "applies style to the selector root" {
                val component =
                    selector(randomPlayer()) {
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
                    selector(nearestPlayer()) {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "uses entities with arguments" {
                val component =
                    selector(
                        entities {
                            type("zombie")
                            limit(5)
                        },
                    ).shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@e[type=minecraft:zombie,limit=5]"
            }
        },
    )
