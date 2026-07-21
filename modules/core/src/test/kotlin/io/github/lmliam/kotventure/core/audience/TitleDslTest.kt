package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.emptyComponent
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

private class TitleRecordingAudience : Audience {
    val titles = mutableListOf<Title>()
    private var titlePart: Component = emptyComponent()
    private var subtitlePart: Component = emptyComponent()
    private var timesPart: Title.Times? = null

    override fun <T : Any> sendTitlePart(
        part: TitlePart<T>,
        value: T,
    ) {
        when (part) {
            TitlePart.TITLE -> {
                titlePart = value as Component
                titles += Title.title(titlePart, subtitlePart, timesPart)
                titlePart = emptyComponent()
                subtitlePart = emptyComponent()
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
            fun record(block: TitleScope.() -> Unit): Title =
                TitleRecordingAudience().also { it.title(block) }.titles.single()

            "builds and shows a title with subtitle and times" {
                val shown =
                    record {
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

                shown.title().childAt(0) shouldContainText "Welcome"
                shown.title().childAt(0) shouldHaveColor gold
                shown.subtitle().childAt(0) shouldContainText "to the server"

                val times = shown.times().shouldNotBeNull()
                times shouldHaveFadeIn 1.ticks
                times shouldHaveStay 3.seconds
                times shouldHaveFadeOut 1.ticks
            }

            "defaults unset timing slots to DEFAULT_TIMES values" {
                val defaults = Title.DEFAULT_TIMES

                val times =
                    record {
                        title { text("Partial times") }
                        times { stay(1.seconds) }
                    }.times().shouldNotBeNull()

                times shouldHaveFadeIn defaults.fadeIn().toKotlinDuration()
                times shouldHaveStay 1.seconds
                times shouldHaveFadeOut defaults.fadeOut().toKotlinDuration()
            }

            "defaults subtitle to empty and times to DEFAULT_TIMES when only title is set" {
                val shown = record { title { text("Solo") } }

                shown.title().childAt(0) shouldContainText "Solo"
                shown.subtitle() shouldBe Component.empty()
                shown.times() shouldBe Title.DEFAULT_TIMES
            }

            "allows a subtitle-only title" {
                val shown = record { subtitle { text("only subtitle") } }

                shown.title() shouldBe Component.empty()
                shown.subtitle().childAt(0) shouldContainText "only subtitle"
                shown.times() shouldBe Title.DEFAULT_TIMES
            }

            "accepts existing components for title and subtitle" {
                val main = Component.text("Main")
                val sub = Component.text("Sub")

                val shown =
                    record {
                        title(main)
                        subtitle(sub)
                    }

                shown.title() shouldBe main
                shown.subtitle() shouldBe sub
            }

            "shows the same title to every member of a forwarding audience" {
                val first = TitleRecordingAudience()
                val second = TitleRecordingAudience()

                audienceOf(first, second).title { title { text("Broadcast") } }

                first.titles shouldHaveSize 1
                second.titles shouldHaveSize 1
                first.titles.single().title() shouldBe second.titles.single().title()
            }

            listOf(
                "rejects a block with neither title nor subtitle" to { TitleRecordingAudience().title {} },
                "rejects a times-only block" to {
                    TitleRecordingAudience().title {
                        times { stay(1.seconds) }
                    }
                },
                "rejects a duplicate title" to {
                    TitleRecordingAudience().title {
                        title { text("a") }
                        title { text("b") }
                    }
                },
                "rejects a duplicate subtitle" to {
                    TitleRecordingAudience().title {
                        subtitle { text("a") }
                        subtitle { text("b") }
                    }
                },
                "rejects a duplicate times block" to {
                    TitleRecordingAudience().title {
                        title { text("a") }
                        times { stay(1.seconds) }
                        times { stay(2.seconds) }
                    }
                },
                "rejects a duplicate timing slot inside times" to {
                    TitleRecordingAudience().title {
                        title { text("a") }
                        times {
                            fadeIn(1.ticks)
                            fadeIn(2.ticks)
                        }
                    }
                },
            ).forEach { (name, action) ->
                name {
                    shouldThrow<IllegalStateException> { action() }
                }
            }
        },
    )
