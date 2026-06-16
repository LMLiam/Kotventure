package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.text.asSequence
import io.github.lmliam.kotventure.core.text.compacted
import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.core.text.count
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor

/**
 * Verifies the core component utilities ([compacted] + [asSequence]/[count]) interoperate with the serializers:
 * compaction preserves rendered meaning, and both normalised and traversed trees survive a JSON round trip.
 */
class ComponentUtilityRoundTripTest :
    StringSpec(
        {
            "compaction preserves rendered text and round-trips losslessly through json" {
                val message =
                    component {
                        text("Hello, ") { color(NamedTextColor.GOLD) }
                        text("world") { color(NamedTextColor.GOLD) }
                    }

                val compacted = message.compacted()

                // Compaction never changes how the tree renders.
                compacted.toPlain() shouldBe "Hello, world"
                compacted.toPlain() shouldBe message.toPlain()

                // The normalised tree survives a serialize/deserialize round trip unchanged.
                val roundTripped = compacted.toJson().asJsonComponent()
                roundTripped shouldBe compacted
                roundTripped shouldContainText "Hello, world"
            }

            "a deeply nested tree keeps every node across a json round trip" {
                val message =
                    component {
                        text("a") {
                            text("b") {
                                text("c")
                            }
                        }
                    }

                val roundTripped = message.toJson().asJsonComponent()

                roundTripped.count() shouldBe message.count()
            }

            "a translatable node survives a json round trip" {
                val message =
                    component {
                        text("You found ")
                        translatable("item.minecraft.diamond") { fallback("Diamond") }
                    }

                val roundTripped = message.toJson().asJsonComponent()

                roundTripped.count() shouldBe message.count()
                roundTripped.asSequence().filterIsInstance<TranslatableComponent>().toList() shouldHaveSize 1
            }
        },
    )
