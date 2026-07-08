package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.audience.audienceOf
import io.github.lmliam.kotventure.core.audience.bossBar
import io.github.lmliam.kotventure.core.audience.hide
import io.github.lmliam.kotventure.core.audience.show
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.bossbar.shouldHaveColor
import io.github.lmliam.kotventure.test.bossbar.shouldHaveFlags
import io.github.lmliam.kotventure.test.bossbar.shouldHaveNoFlags
import io.github.lmliam.kotventure.test.bossbar.shouldHaveOverlay
import io.github.lmliam.kotventure.test.bossbar.shouldHaveProgress
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

private class BossBarRecordingAudience : Audience {
    val shown = mutableListOf<BossBar>()
    val hidden = mutableListOf<BossBar>()

    override fun showBossBar(bar: BossBar) {
        shown += bar
    }

    override fun hideBossBar(bar: BossBar) {
        hidden += bar
    }
}

class BossBarDslTest :
    StringSpec(
        {
            "builds a fully configured boss bar" {
                val bar =
                    bossBar {
                        name {
                            text("Ender Dragon") { color(gold) }
                        }
                        progress(0.25f)
                        color(red)
                        overlay(notched10)
                        darkenScreen()
                        playBossMusic()
                        createWorldFog()
                    }

                bar.name().childAt(0) shouldContainText "Ender Dragon"
                bar.name().childAt(0) shouldHaveColor gold
                bar shouldHaveProgress 0.25f
                bar shouldHaveColor BossBar.Color.RED
                bar shouldHaveOverlay BossBar.Overlay.NOTCHED_10
                bar shouldHaveFlags
                        setOf(
                            BossBar.Flag.DARKEN_SCREEN,
                            BossBar.Flag.PLAY_BOSS_MUSIC,
                            BossBar.Flag.CREATE_WORLD_FOG,
                        )
            }

            "defaults produce a full pink progress bar with no flags" {
                val bar =
                    bossBar {
                        name { text("Raid") }
                    }

                bar.name().childAt(0) shouldContainText "Raid"
                bar shouldHaveProgress BossBar.MAX_PROGRESS
                bar shouldHaveColor BossBar.Color.PINK
                bar shouldHaveOverlay BossBar.Overlay.PROGRESS
                bar.shouldHaveNoFlags()
            }

            "accepts an existing component for the name" {
                val name = Component.text("Named")

                val bar = bossBar { name(name) }

                bar.name() shouldBe name
            }

            "scope-bound colour and overlay vals match Adventure enums" {
                val bar =
                    bossBar {
                        name { text("Palette") }
                        color(blue)
                        overlay(notched20)
                    }

                bar shouldHaveColor BossBar.Color.BLUE
                bar shouldHaveOverlay BossBar.Overlay.NOTCHED_20
            }

            "progress overlay property coexists with progress function" {
                val bar =
                    bossBar {
                        name { text("Continuous") }
                        progress(0.5f)
                        overlay(progress)
                    }

                bar shouldHaveProgress 0.5f
                bar shouldHaveOverlay BossBar.Overlay.PROGRESS
            }

            "shows and hides a built bar" {
                val audience = BossBarRecordingAudience()
                val bar = bossBar { name { text("Shown") } }

                audience.show(bar)
                audience.hide(bar)

                audience.shown shouldContainExactly listOf(bar)
                audience.hidden shouldContainExactly listOf(bar)
            }

            "Audience.bossBar builds, shows, and returns the bar" {
                val audience = BossBarRecordingAudience()

                val bar =
                    audience.bossBar {
                        name { text("Raid") }
                        color(green)
                    }

                audience.shown shouldContainExactly listOf(bar)
                bar.name().childAt(0) shouldContainText "Raid"
                bar shouldHaveColor BossBar.Color.GREEN
                bar shouldHaveProgress BossBar.MAX_PROGRESS
            }

            "shows the same bar to every member of a forwarding audience" {
                val first = BossBarRecordingAudience()
                val second = BossBarRecordingAudience()

                val bar =
                    audienceOf(first, second).bossBar {
                        name { text("Broadcast") }
                    }

                first.shown shouldHaveSize 1
                second.shown shouldHaveSize 1
                first.shown.single() shouldBe bar
                second.shown.single() shouldBe bar
            }

            "rejects a missing name" {
                shouldThrow<IllegalStateException> {
                    bossBar { progress(0.5f) }
                }.message shouldBe "'name' is not set."
            }

            "rejects out-of-range progress without clamping" {
                shouldThrow<IllegalArgumentException> {
                    bossBar {
                        name { text("Bad") }
                        progress(1.5f)
                    }
                }
                shouldThrow<IllegalArgumentException> {
                    bossBar {
                        name { text("Bad") }
                        progress(-0.1f)
                    }
                }
            }

            listOf(
                "rejects a duplicate name" to {
                    bossBar {
                        name { text("a") }
                        name { text("b") }
                    }
                },
                "rejects a duplicate progress" to {
                    bossBar {
                        name { text("a") }
                        progress(0.1f)
                        progress(0.2f)
                    }
                },
                "rejects a duplicate color" to {
                    bossBar {
                        name { text("a") }
                        color(red)
                        color(blue)
                    }
                },
                "rejects a duplicate overlay" to {
                    bossBar {
                        name { text("a") }
                        overlay(notched6)
                        overlay(notched12)
                    }
                },
                "rejects a duplicate darkenScreen" to {
                    bossBar {
                        name { text("a") }
                        darkenScreen()
                        darkenScreen()
                    }
                },
                "rejects a duplicate playBossMusic" to {
                    bossBar {
                        name { text("a") }
                        playBossMusic()
                        playBossMusic()
                    }
                },
                "rejects a duplicate createWorldFog" to {
                    bossBar {
                        name { text("a") }
                        createWorldFog()
                        createWorldFog()
                    }
                },
            ).forEach { (name, action) ->
                name {
                    shouldThrow<IllegalStateException> { action() }
                }
            }

            "duplicate progress message names the progress slot" {
                shouldThrow<IllegalStateException> {
                    bossBar {
                        name { text("a") }
                        progress(0.1f)
                        progress(0.2f)
                    }
                }.message shouldBe "'progress' is already set."
            }
        },
    )
