package io.github.lmliam.kotventure.core.nbt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.BlockNBTComponent

class BlockPosDslTest :
    StringSpec(
        {
            "builds an absolute block position from integer coordinates" {
                val pos = blockPos(1, 64, -3)

                pos shouldBe
                    BlockNBTComponent.WorldPos.worldPos(
                        BlockNBTComponent.WorldPos.Coordinate.absolute(1),
                        BlockNBTComponent.WorldPos.Coordinate.absolute(64),
                        BlockNBTComponent.WorldPos.Coordinate.absolute(-3),
                    )
                pos.asString() shouldBe "1 64 -3"
            }

            "builds a relative block position from integer offsets" {
                val pos = relativeBlockPos(1, 2, -2)

                pos shouldBe
                    BlockNBTComponent.WorldPos.worldPos(
                        BlockNBTComponent.WorldPos.Coordinate.relative(1),
                        BlockNBTComponent.WorldPos.Coordinate.relative(2),
                        BlockNBTComponent.WorldPos.Coordinate.relative(-2),
                    )
                pos.asString() shouldBe "~1 ~2 ~-2"
            }

            "builds a relative block position from zero offsets by default" {
                val pos = relativeBlockPos()

                pos shouldBe
                    BlockNBTComponent.WorldPos.worldPos(
                        BlockNBTComponent.WorldPos.Coordinate.relative(0),
                        BlockNBTComponent.WorldPos.Coordinate.relative(0),
                        BlockNBTComponent.WorldPos.Coordinate.relative(0),
                    )
                pos.asString() shouldBe "~0 ~0 ~0"
            }

            "parses a block position from a coordinate string" {
                val pos = blockPos("~1 ~2 ~3")

                pos shouldBe BlockNBTComponent.Pos.fromString("~1 ~2 ~3")
                pos.asString() shouldBe "~1 ~2 ~3"
            }

            "throws when parsing an invalid coordinate string" {
                shouldThrow<IllegalArgumentException> {
                    blockPos("not coordinates")
                }
            }
        },
    )
