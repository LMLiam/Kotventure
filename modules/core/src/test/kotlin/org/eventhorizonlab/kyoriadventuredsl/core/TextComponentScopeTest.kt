package org.eventhorizonlab.kyoriadventuredsl.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class TextComponentScopeTest :
    StringSpec({

        // Use the concrete Factory to create components
        val factory = TextComponentScope.Factory()

        "builds a simple text component with content" {
            val content = "Hello World!"
            val component =
                factory.create {
                    content(content)
                }

            component.content() shouldBe content
            component.color() shouldBe null
            component.decorations().forEach { (_, state) ->
                state shouldBe TextDecoration.State.NOT_SET
            }
        }

        "applies a color to the text" {
            val color = NamedTextColor.RED
            val component =
                factory.create {
                    color(color)
                }

            component.color() shouldBe color
        }

        "applies multiple decorations" {
            val decorations = arrayOf(TextDecoration.BOLD, TextDecoration.ITALIC)
            val component =
                factory.create {
                    decorate(*decorations)
                }

            decorations.forEach {
                component.hasDecoration(it) shouldBe true
                component.decoration(it) shouldBe TextDecoration.State.TRUE
            }
        }

        "supports nested text components" {
            val component =
                factory.create {
                    content("Parent")
                    text {
                        content(" Child")
                        color(NamedTextColor.GREEN)
                    }
                }

            component.children().size shouldBe 1
            val child = component.children().first()
            child shouldBe instanceOf<TextComponent>()
            val textComponent = child as TextComponent
            textComponent.content() shouldBe " Child"
            textComponent.color() shouldBe NamedTextColor.GREEN
        }
    })