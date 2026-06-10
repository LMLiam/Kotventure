package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeKeybindComponent
import io.github.lmliam.kotventure.test.text.shouldBeObjectComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveKeybind
import io.github.lmliam.kotventure.test.text.shouldHaveObjectContents
import io.github.lmliam.kotventure.test.text.shouldHaveObjectFallback
import io.github.lmliam.kotventure.test.text.shouldNotHaveObjectFallback
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents

class ObjectComponentDslTest :
    StringSpec(
        {
            "builds sprite object contents with the default atlas" {
                val spriteKey = key("minecraft", "block/stone")

                val contents = sprite(spriteKey)

                contents shouldBe ObjectContents.sprite(spriteKey)
            }

            "builds sprite object contents with a custom atlas" {
                val atlas = key("minecraft", "blocks")
                val spriteKey = key("minecraft", "block/stone")

                val contents = sprite(atlas, spriteKey)

                contents shouldBe ObjectContents.sprite(atlas, spriteKey)
            }

            "builds an object component with sprite contents" {
                val contents = sprite(key("minecraft", "block/stone"))

                val component = display(contents).shouldBeObjectComponent()

                component shouldHaveObjectContents contents
                component.shouldNotHaveObjectFallback()
            }

            "applies style and fallback to the object root" {
                val contents = sprite(key("minecraft", "block/stone"))
                val fallback = Component.text("[stone]")

                val component =
                    display(contents) {
                        color(NamedTextColor.GOLD)
                        bold()
                        fallback(fallback)
                    }.shouldBeObjectComponent()

                component shouldHaveObjectContents contents
                component shouldHaveColor NamedTextColor.GOLD
                component shouldHaveDecoration TextDecoration.BOLD
                component shouldHaveObjectFallback fallback
            }

            "builds fallback text from a component DSL block" {
                val contents = sprite(key("minecraft", "block/stone"))

                val component =
                    display(contents) {
                        fallback {
                            text("stone") {
                                color(NamedTextColor.GRAY)
                            }
                        }
                    }.shouldBeObjectComponent()

                val fallback = checkNotNull(component.fallback())
                fallback shouldHaveChildCount 1
                fallback.childAt(0) shouldContainText "stone"
                fallback.childAt(0) shouldHaveColor NamedTextColor.GRAY
            }

            "uses the last configured fallback" {
                val contents = sprite(key("minecraft", "block/stone"))
                val fallback = Component.text("[stone]")

                val component =
                    display(contents) {
                        fallback(Component.text("[old]"))
                        fallback(fallback)
                    }.shouldBeObjectComponent()

                component shouldHaveObjectFallback fallback
            }

            "appends child component builders" {
                val contents = sprite(key("minecraft", "block/stone"))

                val component =
                    display(contents) {
                        text(" item")
                        keybind("key.attack") {
                            underlined()
                        }
                    }

                component shouldHaveChildCount 2
                component.childAt(0) shouldContainText " item"
                val keybind = component.childAt(1).shouldBeKeybindComponent()
                keybind shouldHaveKeybind "key.attack"
                keybind shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "appends object components from component scope" {
                val contents = sprite(key("minecraft", "block/stone"))

                val component =
                    component {
                        text("Block: ")
                        display(contents) {
                            fallback(Component.text("[stone]"))
                        }
                    }

                component shouldHaveChildCount 2
                component.childAt(0) shouldContainText "Block: "
                val objectChild = component.childAt(1).shouldBeObjectComponent()
                objectChild shouldHaveObjectContents contents
                objectChild shouldHaveObjectFallback Component.text("[stone]")
            }
        },
    )
