package io.github.lmliam.kotventure.core.nbt

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

                val component = blockNbt(pos, "Items[0].tag.display.Name").shouldBeBlockNbtComponent()

                component shouldHaveBlockPos pos
                component shouldHaveNbtPath "Items[0].tag.display.Name"
                component.shouldNotInterpret()
                component.shouldNotHaveNbtSeparator()
            }

            "applies style to the block nbt root" {
                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), "CustomName") {
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
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), "CustomName") {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "sets interpret true" {
                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), "CustomName") {
                        interpret(true)
                    }

                component.shouldBeBlockNbtComponent().shouldInterpret()
            }

            "sets a component separator" {
                val separator = Component.text(", ")

                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), "Items[].id") {
                        separator(separator)
                    }

                component.shouldBeBlockNbtComponent() shouldHaveNbtSeparator separator
            }

            "sets an inline text separator" {
                val component =
                    blockNbt(BlockNBTComponent.Pos.fromString("1 2 3"), "Items[].id") {
                        separator {
                            content(" | ")
                            color(NamedTextColor.GRAY)
                        }
                    }

                val separator = checkNotNull(component.shouldBeBlockNbtComponent().separator())

                separator shouldHaveColor NamedTextColor.GRAY
                separator shouldContainText " | "
            }
        },
    )
