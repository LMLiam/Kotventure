package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeBlockNbtComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveBlockPos
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveNbtPath
import io.github.lmliam.kotventure.test.text.shouldHaveNbtSeparator
import io.github.lmliam.kotventure.test.text.shouldInterpret
import io.github.lmliam.kotventure.test.text.shouldNotHaveNbtSeparator
import io.github.lmliam.kotventure.test.text.shouldNotInterpret
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class BlockNbtDslTest :
    StringSpec(
        {
            "builds a block nbt component with a position and path" {
                val pos = BlockNBTComponent.Pos.fromString("~1 ~2 ~3")
                val path = nbtPath("Items")[0]["tag"]["display"]["Name"]

                val component = blockNbt(pos, path).shouldBeBlockNbtComponent()

                component shouldHaveBlockPos pos
                component shouldHaveNbtPath "Items[0].tag.display.Name"
                component.shouldNotInterpret()
                component.shouldNotHaveNbtSeparator()
            }

            "accepts an nbt path from the string escape hatch" {
                val pos = blockPos(1, 2, 3)

                val component = blockNbt(pos, nbtPath("CustomName")).shouldBeBlockNbtComponent()

                component shouldHaveNbtPath "CustomName"
            }

            "applies style to the block nbt root" {
                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), nbtPath("CustomName")) {
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
                val suffix = Component.text(" block")

                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), nbtPath("CustomName")) {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "sets interpret true" {
                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), nbtPath("CustomName")) {
                        interpret(true)
                    }

                component.shouldBeBlockNbtComponent().shouldInterpret()
            }

            "sets a component separator" {
                val separator = Component.text(", ")
                val path = nbtPath("Items")[all]["id"]

                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), path) {
                        separator(separator)
                    }

                component.shouldBeBlockNbtComponent() shouldHaveNbtSeparator separator
            }

            "sets an inline text separator" {
                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), nbtPath("Items[].id")) {
                        separator {
                            content(" | ")
                            color(NamedTextColor.GRAY)
                        }
                    }

                val separator = checkNotNull(component.shouldBeBlockNbtComponent().separator())

                separator shouldHaveColor NamedTextColor.GRAY
                separator shouldContainText " | "
            }

            "rejects setting interpret twice in one block" {
                shouldThrow<IllegalStateException> {
                    blockNbt(blockPos(1, 2, 3), nbtPath("CustomName")) {
                        interpret(true)
                        interpret(false)
                    }
                }
            }

            "rejects setting the separator twice in one block" {
                val comma = text(", ")

                shouldThrow<IllegalStateException> {
                    blockNbt(blockPos(1, 2, 3), nbtPath("Items[].id")) {
                        separator(comma)
                        separator {
                            content(" | ")
                        }
                    }
                }
            }
        },
    )
