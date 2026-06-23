package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.objectcomponent.display
import io.github.lmliam.kotventure.core.objectcomponent.sprite
import io.github.lmliam.kotventure.core.translatable.translatable
import io.github.lmliam.kotventure.test.text.shouldBeObjectComponent
import io.github.lmliam.kotventure.test.text.shouldHaveObjectContents
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent

class ComponentSequenceTest :
    StringSpec(
        {
            fun Sequence<Component>.textContents(): List<String> =
                mapNotNull { (it as? TextComponent)?.content()?.takeIf(String::isNotEmpty) }.toList()

            "asSequence yields the root first, then children depth-first in declaration order" {
                val message =
                    component {
                        text("A") {
                            text("B")
                            text("C")
                        }
                        text("D")
                    }

                message.asSequence().first() shouldBe message
                message.asSequence().textContents() shouldContainExactly listOf("A", "B", "C", "D")
            }

            "asSequence visits a single leaf component exactly once" {
                val solo = text("solo")

                solo.asSequence().toList() shouldContainExactly listOf(solo)
            }

            "asSequence visits an empty-content root together with its children" {
                val message =
                    component {
                        text("only-child")
                    }

                val nodes = message.asSequence().toList()

                nodes shouldHaveSize 2
                nodes.first() shouldBe message
                (nodes.first() as TextComponent).content() shouldBe ""
                message.asSequence().textContents() shouldContainExactly listOf("only-child")
            }

            "asSequence descends every level of a deeply nested tree" {
                val message =
                    component {
                        text("one") {
                            text("two") {
                                text("three") {
                                    text("four")
                                }
                            }
                        }
                    }

                message.asSequence().textContents() shouldContainExactly listOf("one", "two", "three", "four")
            }

            "asSequence preserves object components rather than dropping them" {
                val stone = sprite(key("minecraft", "block/stone"))
                val message =
                    component {
                        text("Block: ")
                        display(stone) {
                            fallback { text("[stone]") }
                        }
                    }

                message.asSequence().filterIsInstance<ObjectComponent>().toList() shouldHaveSize 1
            }

            "asSequence descends into an object component's own children" {
                val message =
                    component {
                        display(sprite(key("minecraft", "block/stone"))) {
                            text("child-of-object")
                        }
                    }

                val nodes = message.asSequence().toList()

                nodes shouldHaveSize 3
                nodes[0] shouldBe message
                nodes[1].shouldBeObjectComponent()
                (nodes[2] as TextComponent).content() shouldBe "child-of-object"
            }

            "asSequence composes with standard-library search operators" {
                val stone = sprite(key("minecraft", "block/stone"))
                val message =
                    component {
                        display(stone)
                        text("trailing")
                    }

                message.asSequence().any { it is ObjectComponent } shouldBe true
                checkNotNull(message.asSequence().firstOrNull { it is ObjectComponent })
                    .shouldBeObjectComponent() shouldHaveObjectContents stone
            }

            "asSequence count returns one for a single leaf component" {
                text("solo").asSequence().count() shouldBe 1
            }

            "asSequence count includes the root and every nested child of a deep tree" {
                val message =
                    component {
                        text("one") {
                            text("two") {
                                text("three")
                            }
                        }
                    }

                message.asSequence().count() shouldBe 4
            }

            "asSequence count includes the root and every child of a wide tree" {
                val message =
                    component {
                        text("a")
                        text("b")
                        text("c")
                    }

                message.asSequence().count() shouldBe 4
            }

            "asSequence count tallies text, translatable, and object nodes alike" {
                val message =
                    component {
                        text("hi")
                        translatable("item.minecraft.diamond") { fallback("Diamond") }
                        display(sprite(key("minecraft", "block/stone")))
                    }

                message.asSequence().count() shouldBe 4
                message.asSequence().filterIsInstance<TranslatableComponent>().toList() shouldHaveSize 1
                message.asSequence().filterIsInstance<ObjectComponent>().toList() shouldHaveSize 1
            }

            "asSequence forEach visits every node in traversal order" {
                val message =
                    component {
                        text("A") {
                            text("B")
                        }
                        display(sprite(key("minecraft", "block/stone")))
                    }

                val visited = buildList { message.asSequence().forEach { add(it) } }

                visited shouldContainExactly message.asSequence().toList()
            }

            "asSequence forEach visits object components" {
                val message =
                    component {
                        text("Block: ")
                        display(sprite(key("minecraft", "block/stone")))
                    }

                var sawObject = false
                message.asSequence().forEach { if (it is ObjectComponent) sawObject = true }

                sawObject shouldBe true
            }
        },
    )
