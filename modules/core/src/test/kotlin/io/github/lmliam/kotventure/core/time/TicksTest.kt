package io.github.lmliam.kotventure.core.time

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TicksTest :
    StringSpec(
        {
            "maps one tick to 50 milliseconds" {
                1.ticks shouldBe 50.milliseconds
            }

            "maps 20 ticks to one second" {
                20.ticks shouldBe 1.seconds
            }

            "maps Long ticks the same way" {
                1L.ticks shouldBe 50.milliseconds
                20L.ticks shouldBe 1.seconds
            }
        },
    )
