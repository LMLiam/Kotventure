package io.github.lmliam.kotventure.test.bossbar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

class BossBarMatchersTest :
    StringSpec(
        {
            fun bar(
                progress: Float = BossBar.MAX_PROGRESS,
                color: BossBar.Color = BossBar.Color.PINK,
                overlay: BossBar.Overlay = BossBar.Overlay.PROGRESS,
                flags: Set<BossBar.Flag> = emptySet(),
            ): BossBar =
                BossBar.bossBar(
                    Component.text("test"),
                    progress,
                    color,
                    overlay,
                    flags,
                )

            "matches progress, colour, overlay, and flags" {
                val subject =
                    bar(
                        progress = 0.25f,
                        color = BossBar.Color.RED,
                        overlay = BossBar.Overlay.NOTCHED_10,
                        flags =
                            setOf(
                                BossBar.Flag.DARKEN_SCREEN,
                                BossBar.Flag.PLAY_BOSS_MUSIC,
                            ),
                    )

                subject shouldHaveProgress 0.25f
                subject shouldHaveColor BossBar.Color.RED
                subject shouldHaveOverlay BossBar.Overlay.NOTCHED_10
                subject shouldHaveFlags
                        setOf(
                            BossBar.Flag.DARKEN_SCREEN,
                            BossBar.Flag.PLAY_BOSS_MUSIC,
                        )
                subject shouldHaveFlag BossBar.Flag.DARKEN_SCREEN
                subject shouldNotHaveFlag BossBar.Flag.CREATE_WORLD_FOG
            }

            "matches an empty flag set" {
                bar().shouldHaveNoFlags()
            }

            "matches the absence of a given progress" {
                bar(progress = 0.5f) shouldNotHaveProgress 0.25f
            }

            "reports a progress mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        bar(progress = 0.25f) shouldHaveProgress 0.5f
                    }

                failure.message shouldContain
                        "Expected boss bar progress <0.5>, but was <0.25>."
            }

            "reports a colour mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        bar(color = BossBar.Color.BLUE) shouldHaveColor BossBar.Color.RED
                    }

                failure.message shouldContain
                        "Expected boss bar color <RED>, but was <BLUE>."
            }

            "reports a missing flag with the actual flag set" {
                val failure =
                    shouldThrow<AssertionError> {
                        bar() shouldHaveFlag BossBar.Flag.CREATE_WORLD_FOG
                    }

                failure.message shouldContain
                        "Expected boss bar to have flag <CREATE_WORLD_FOG>, but flags were <[]>."
            }
        },
    )
