package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeObjectComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveObjectContents
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.TextComponent

class ObjectComponentRendererTest :
    StringSpec(
        {
            "renders object components with fallback replacements" {
                val contents = sprite(key("minecraft", "block/stone"))
                val message =
                    display(contents) {
                        fallback(Component.text("[stone]"))
                        text(" item")
                    }

                val rendered = message.renderObjectFallbacks()

                rendered.containsObjectComponent() shouldBe false
                val fallback = rendered as TextComponent
                fallback.content() shouldBe "[stone]"
                fallback shouldHaveChildCount 1
                fallback.childAt(0) shouldContainText " item"
            }

            "preserves object components without fallback while rendering children" {
                val contents = sprite(key("minecraft", "block/stone"))
                val message =
                    display(contents) {
                        text(" child")
                    }

                val rendered = message.renderObjectFallbacks().shouldBeObjectComponent()

                rendered shouldHaveObjectContents contents
                rendered shouldHaveChildCount 1
                rendered.childAt(0) shouldContainText " child"
            }

            "preserves empty object components without fallback or children" {
                val contents = sprite(key("minecraft", "block/stone"))
                val message = display(contents)

                val rendered = message.renderObjectFallbacks().shouldBeObjectComponent()

                rendered shouldHaveObjectContents contents
                rendered shouldHaveChildCount 0
            }

            "renders nested object fallbacks in component trees" {
                val contents = sprite(key("minecraft", "block/stone"))
                val message =
                    component {
                        text("Block: ")
                        display(contents) {
                            fallback(Component.text("[stone]"))
                        }
                    }

                val rendered = message.renderObjectFallbacks()

                rendered shouldHaveChildCount 2
                rendered.childAt(0) shouldContainText "Block: "
                rendered.childAt(1) shouldContainText "[stone]"
            }

            "preserves fallback children before object children" {
                val contents = sprite(key("minecraft", "block/stone"))
                val message =
                    display(contents) {
                        fallback {
                            text("[stone]")
                            text(" fallback-child")
                        }
                        text(" object-child")
                    }

                val rendered = message.renderObjectFallbacks()

                rendered.containsObjectComponent() shouldBe false
                rendered shouldHaveChildCount 3
                rendered.childAt(0) shouldContainText "[stone]"
                rendered.childAt(1) shouldContainText " fallback-child"
                rendered.childAt(2) shouldContainText " object-child"
            }
        },
    )

private fun Component.containsObjectComponent(): Boolean =
    this is ObjectComponent || children().any { child -> child.containsObjectComponent() }
