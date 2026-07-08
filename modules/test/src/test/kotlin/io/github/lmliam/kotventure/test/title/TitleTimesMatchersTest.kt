package io.github.lmliam.kotventure.test.title

import io.github.lmliam.kotventure.core.time.ticks
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.title.Title
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class TitleTimesMatchersTest :
    StringSpec(
        {
            "matches times built from Kotlin durations" {
                val times =
                    Title.Times.times(
                        1.ticks.toJavaDuration(),
                        3.seconds.toJavaDuration(),
                        1.ticks.toJavaDuration(),
                    )

                times.shouldHaveTimes(fadeIn = 1.ticks, stay = 3.seconds, fadeOut = 1.ticks)
            }

            "reports a timing mismatch with expected and actual kotlin durations" {
                val times =
                    Title.Times.times(
                        1.ticks.toJavaDuration(),
                        3.seconds.toJavaDuration(),
                        1.ticks.toJavaDuration(),
                    )

                val failure =
                    shouldThrow<AssertionError> {
                        times.shouldHaveTimes(fadeIn = 2.ticks, stay = 3.seconds, fadeOut = 1.ticks)
                    }

                failure.message shouldContain
                    "Expected title times <fadeIn=100ms, stay=3s, fadeOut=50ms>"
                failure.message shouldContain "fadeIn=50ms"
            }

            "matches the absence of the given times" {
                val times =
                    Title.Times.times(
                        1.ticks.toJavaDuration(),
                        3.seconds.toJavaDuration(),
                        1.ticks.toJavaDuration(),
                    )

                times.shouldNotHaveTimes(fadeIn = 2.ticks, stay = 3.seconds, fadeOut = 1.ticks)
            }
        },
    )
