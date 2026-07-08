package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.asSequence
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor

/**
 * Verifies core component normalisation and traversal interoperate with the serializers:
 * compaction preserves rendered meaning, and both normalised and traversed trees survive a JSON round trip.
 */
class ComponentUtilityRoundTripTest :
    StringSpec(
        {
            "compaction preserves rendered text and round-trips losslessly through json" {
                val message =
                    component {
                        text("Hello, ") { color(gold) }
                        text("world") { color(gold) }
                    }

                val compacted = message.compact()

                // Compaction never changes how the tree renders.
                compacted.toPlainText() shouldBe "Hello, world"
                compacted.toPlainText() shouldBe message.toPlainText()

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

                roundTripped.asSequence().count() shouldBe message.asSequence().count()
            }

            "a translatable node survives a json round trip" {
                val message =
                    component {
                        text("You found ")
                        translatable("item.minecraft.diamond") { fallback("Diamond") }
                    }

                val roundTripped = message.toJson().asJsonComponent()

                roundTripped.asSequence().count() shouldBe message.asSequence().count()
                roundTripped.asSequence().filterIsInstance<TranslatableComponent>().toList() shouldHaveSize 1
            }
        },
    )
