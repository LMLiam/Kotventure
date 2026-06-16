package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.objectcomponent.display
import io.github.lmliam.kotventure.core.objectcomponent.sprite
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class CompactedTest :
    StringSpec(
        {
            "merges adjacent same-style text while preserving its content" {
                val message =
                    component {
                        text("Hello, ")
                        text("world")
                    }

                val compacted = message.compacted()

                compacted.count() shouldBeLessThan message.count()
                compacted shouldContainText "Hello, world"
            }

            "preserves styling through compaction" {
                val message =
                    component {
                        text("warn") {
                            color(NamedTextColor.RED)
                            bold()
                        }
                    }

                val compacted = message.compacted()

                compacted shouldContainText "warn"
                compacted shouldHaveColor NamedTextColor.RED
                compacted shouldHaveDecoration TextDecoration.BOLD
            }

            "handles an empty component gracefully" {
                val compacted = component {}.compacted()

                compacted.count() shouldBe 1
                (compacted as TextComponent).content() shouldBe ""
            }

            "preserves object components rather than dropping them" {
                val message =
                    component {
                        text("Block: ")
                        display(sprite(key("minecraft", "block/stone"))) {
                            fallback { text("[stone]") }
                        }
                    }

                val compacted = message.compacted()

                compacted shouldContainText "Block: "
                compacted.asSequence().filterIsInstance<ObjectComponent>().toList() shouldHaveSize 1
            }
        },
    )
