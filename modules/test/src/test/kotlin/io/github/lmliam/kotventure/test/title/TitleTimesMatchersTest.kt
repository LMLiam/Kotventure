package io.github.lmliam.kotventure.test.title

import io.github.lmliam.kotventure.core.time.ticks
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.title.Title
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

class TitleTimesMatchersTest :
    StringSpec(
        {
            "matches individual timing slots from Kotlin durations" {
                val times =
                    Title.Times.times(
                        1.ticks.toJavaDuration(),
                        3.seconds.toJavaDuration(),
                        1.ticks.toJavaDuration(),
                    )

                times shouldHaveFadeIn 1.ticks
                times shouldHaveStay 3.seconds
                times shouldHaveFadeOut 1.ticks
            }

            "matches Adventure values converted at the call site" {
                val times = Title.DEFAULT_TIMES

                times shouldHaveFadeIn Title.DEFAULT_TIMES.fadeIn().toKotlinDuration()
                times shouldHaveStay Title.DEFAULT_TIMES.stay().toKotlinDuration()
                times shouldHaveFadeOut Title.DEFAULT_TIMES.fadeOut().toKotlinDuration()
            }

            "reports a fade-in mismatch with expected and actual kotlin durations" {
                val times =
                    Title.Times.times(
                        1.ticks.toJavaDuration(),
                        3.seconds.toJavaDuration(),
                        1.ticks.toJavaDuration(),
                    )

                val failure =
                    shouldThrow<AssertionError> {
                        times shouldHaveFadeIn 2.ticks
                    }

                failure.message shouldContain "Expected title fade-in <100ms>, but was <50ms>."
            }

            "matches the absence of a given stay duration" {
                val times =
                    Title.Times.times(
                        1.ticks.toJavaDuration(),
                        3.seconds.toJavaDuration(),
                        1.ticks.toJavaDuration(),
                    )

                times shouldNotHaveStay 1.seconds
            }
        },
    )
