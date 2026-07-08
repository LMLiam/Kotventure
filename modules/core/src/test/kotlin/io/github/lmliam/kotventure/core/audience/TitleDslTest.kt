package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.title.shouldHaveFadeIn
import io.github.lmliam.kotventure.test.title.shouldHaveFadeOut
import io.github.lmliam.kotventure.test.title.shouldHaveStay
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

/**
 * Captures titles via [sendTitlePart], matching Adventure's default [Audience.showTitle]
 * (which fans out parts — including through [audienceOf] — rather than calling [Audience.showTitle]
 * on each member).
 */
private class TitleRecordingAudience : Audience {
    val titles = mutableListOf<Title>()
    private var titlePart: Component = Component.empty()
    private var subtitlePart: Component = Component.empty()
    private var timesPart: Title.Times? = null

    override fun <T : Any> sendTitlePart(
        part: TitlePart<T>,
        value: T,
    ) {
        when (part) {
            TitlePart.TITLE -> {
                titlePart = value as Component
                titles += Title.title(titlePart, subtitlePart, timesPart)
                titlePart = Component.empty()
                subtitlePart = Component.empty()
                timesPart = null
            }
            TitlePart.SUBTITLE -> subtitlePart = value as Component
            TitlePart.TIMES -> timesPart = value as Title.Times
            else -> error("Unexpected title part: $part")
        }
    }
}

class TitleDslTest :
    StringSpec(
        {
            "builds and shows a title with subtitle and times" {
                val audience = TitleRecordingAudience()

                audience.title {
                    title {
                        text("Welcome") { color(gold) }
                    }
                    subtitle { text("to the server") }
                    times {
                        fadeIn(1.ticks)
                        stay(3.seconds)
                        fadeOut(1.ticks)
                    }
                }

                audience.titles shouldHaveSize 1
                val shown = audience.titles.single()
                shown.title().childAt(0) shouldContainText "Welcome"
                shown.title().childAt(0) shouldHaveColor gold
                shown.subtitle().childAt(0) shouldContainText "to the server"
                val times = shown.times().shouldNotBeNull()
                times shouldHaveFadeIn 1.ticks
                times shouldHaveStay 3.seconds
                times shouldHaveFadeOut 1.ticks
            }

            "defaults unset timing slots to DEFAULT_TIMES values" {
                val audience = TitleRecordingAudience()
                val defaults = Title.DEFAULT_TIMES

                audience.title {
                    title { text("Partial times") }
                    times {
                        stay(1.seconds)
                    }
                }

                val times =
                    audience.titles
                    .single()
                    .times()
                    .shouldNotBeNull()
                times shouldHaveFadeIn defaults.fadeIn().toKotlinDuration()
                times shouldHaveStay 1.seconds
                times shouldHaveFadeOut defaults.fadeOut().toKotlinDuration()
            }

            "defaults subtitle to empty and times to DEFAULT_TIMES when only title is set" {
                val audience = TitleRecordingAudience()

                audience.title {
                    title { text("Solo") }
                }

                val shown = audience.titles.single()
                shown.title().childAt(0) shouldContainText "Solo"
                shown.subtitle() shouldBe Component.empty()
                shown.times() shouldBe Title.DEFAULT_TIMES
            }

            "allows a subtitle-only title" {
                val audience = TitleRecordingAudience()

                audience.title {
                    subtitle { text("only subtitle") }
                }

                val shown = audience.titles.single()
                shown.title() shouldBe Component.empty()
                shown.subtitle().childAt(0) shouldContainText "only subtitle"
                shown.times() shouldBe Title.DEFAULT_TIMES
            }

            "accepts existing components for title and subtitle" {
                val audience = TitleRecordingAudience()
                val main = Component.text("Main")
                val sub = Component.text("Sub")

                audience.title {
                    title(main)
                    subtitle(sub)
                }

                val shown = audience.titles.single()
                shown.title() shouldBe main
                shown.subtitle() shouldBe sub
            }

            "shows the same title to every member of a forwarding audience" {
                val first = TitleRecordingAudience()
                val second = TitleRecordingAudience()

                audienceOf(first, second).title {
                    title { text("Broadcast") }
                }

                first.titles shouldHaveSize 1
                second.titles shouldHaveSize 1
                first.titles.single().title() shouldBe second.titles.single().title()
            }

            "rejects a block with neither title nor subtitle" {
                shouldThrow<IllegalStateException> {
                    TitleRecordingAudience().title {}
                }
            }

            "rejects a duplicate title" {
                shouldThrow<IllegalStateException> {
                    TitleRecordingAudience().title {
                        title { text("a") }
                        title { text("b") }
                    }
                }
            }

            "rejects a duplicate subtitle" {
                shouldThrow<IllegalStateException> {
                    TitleRecordingAudience().title {
                        subtitle { text("a") }
                        subtitle { text("b") }
                    }
                }
            }

            "rejects a duplicate times block" {
                shouldThrow<IllegalStateException> {
                    TitleRecordingAudience().title {
                        title { text("a") }
                        times { stay(1.seconds) }
                        times { stay(2.seconds) }
                    }
                }
            }

            "rejects a duplicate timing slot inside times" {
                shouldThrow<IllegalStateException> {
                    TitleRecordingAudience().title {
                        title { text("a") }
                        times {
                            fadeIn(1.ticks)
                            fadeIn(2.ticks)
                        }
                    }
                }
            }
        },
    )
