package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.paper.dialog.fixture.builtBase
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class DialogBodyTest :
    StringSpec(
        {
            "accumulates plain-message bodies in call order" {
                val base =
                    builtBase {
                        title { text("t") }
                        message { text("first") }
                        message(Component.text("second"))
                    }

                base.body() shouldHaveSize 2
                base.body()[0].shouldBeInstanceOf<PlainMessageDialogBody>().contents() shouldHaveContent "first"
                base.body()[1].shouldBeInstanceOf<PlainMessageDialogBody>().contents() shouldHaveContent "second"
            }

            "wires an item body with framing knobs and description" {
                val stack = mockk<ItemStack>()
                val base =
                    builtBase {
                        title { text("t") }
                        item(stack) {
                            description { text("shiny") }
                            decorations(false)
                            tooltip(false)
                            width(32)
                            height(48)
                        }
                    }

                val body = base.body().single().shouldBeInstanceOf<ItemDialogBody>()
                body.item() shouldBe stack
                body.showDecorations() shouldBe false
                body.showTooltip() shouldBe false
                body.width() shouldBe 32
                body.height() shouldBe 48
                body.description().shouldNotBeNull().contents() shouldHaveContent "shiny"
            }

            "sets decorations and tooltip to true with no argument" {
                val stack = mockk<ItemStack>()
                val base =
                    builtBase {
                        title { text("t") }
                        item(stack) {
                            decorations()
                            tooltip()
                        }
                    }

                val body = base.body().single().shouldBeInstanceOf<ItemDialogBody>()
                body.showDecorations() shouldBe true
                body.showTooltip() shouldBe true
            }

            "adds a bare item body with default framing" {
                val stack = mockk<ItemStack>()
                val base =
                    builtBase {
                        title { text("t") }
                        item(stack)
                    }

                base
                    .body()
                    .single()
                    .shouldBeInstanceOf<ItemDialogBody>()
                    .item() shouldBe stack
            }

            "throws when an item body width exceeds 256" {
                val stack = mockk<ItemStack>()

                shouldThrow<IllegalArgumentException> {
                    builtBase {
                        title { text("t") }
                        item(stack) { width(257) }
                    }
                }
            }
        },
    )
