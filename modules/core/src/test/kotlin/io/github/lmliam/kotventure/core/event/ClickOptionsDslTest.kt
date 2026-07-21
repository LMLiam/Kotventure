package io.github.lmliam.kotventure.core.event

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

class ClickOptionsDslTest :
    StringSpec(
        {
            "builds Adventure options from both slots" {
                val options =
                    clickOptions {
                        uses(3)
                        lifetime(10.minutes)
                    }

                options.uses() shouldBe 3
                options.lifetime() shouldBe JavaDuration.ofMinutes(10)
            }

            "keeps the Adventure defaults for unset slots" {
                val options = clickOptions { }
                val defaults = ClickCallback.Options.builder().build()

                options.uses() shouldBe defaults.uses()
                options.lifetime() shouldBe defaults.lifetime()
            }

            "accepts unlimited uses" {
                clickOptions { uses(unlimited) }.uses() shouldBe ClickCallback.UNLIMITED_USES
            }

            "rejects a use count that is neither positive nor unlimited" {
                shouldThrow<IllegalArgumentException> {
                    clickOptions { uses(0) }
                }
            }

            "rejects a lifetime that is not positive" {
                shouldThrow<IllegalArgumentException> {
                    clickOptions { lifetime(0.minutes) }
                }
            }

            "rejects a second use count in one block" {
                shouldThrow<IllegalStateException> {
                    clickOptions {
                        uses(1)
                        uses(2)
                    }
                }
            }

            "rejects a second lifetime in one block" {
                shouldThrow<IllegalStateException> {
                    clickOptions {
                        lifetime(10.minutes)
                        lifetime(20.minutes)
                    }
                }
            }
        },
    )
