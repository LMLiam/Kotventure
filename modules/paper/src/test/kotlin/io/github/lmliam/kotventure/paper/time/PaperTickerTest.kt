package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.test.text.haveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private fun pluginWith(scheduler: BukkitScheduler): Plugin {
    val plugin = mockk<Plugin>()
    every { plugin.server.scheduler } returns scheduler
    return plugin
}

private fun schedulerReturning(task: BukkitTask): BukkitScheduler {
    val scheduler = mockk<BukkitScheduler>()
    every { scheduler.runTaskTimer(any<Plugin>(), any<Runnable>(), any<Long>(), any<Long>()) } returns task
    return scheduler
}

class PaperTickerTest :
    StringSpec(
        {
            "schedules repeating work with the interval as the bukkit delay and period" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(scheduler)

                plugin.ticker().repeating(1.seconds) { }

                verify { scheduler.runTaskTimer(plugin, any<Runnable>(), 20L, 20L) }
            }

            "accepts an interval written in ticks" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(scheduler)

                plugin.ticker().repeating(3.ticks) { }

                verify { scheduler.runTaskTimer(plugin, any<Runnable>(), 3L, 3L) }
            }

            "a scheduled fire sends through the audience DSL" {
                val runnable = slot<Runnable>()
                val scheduler = mockk<BukkitScheduler>()
                every { scheduler.runTaskTimer(any<Plugin>(), capture(runnable), any<Long>(), any<Long>()) } returns
                        mockk()
                val plugin = pluginWith(scheduler)
                val sent = slot<Component>()
                val player = mockk<Player>()
                every { player.sendMessage(capture(sent)) } just Runs

                plugin.ticker().repeating(1.seconds) {
                    player.message { text("Meteor incoming") }
                }
                runnable.captured.run()

                sent.captured should haveContent("Meteor incoming")
            }

            "cancel delegates to the bukkit task on every call" {
                val bukkitTask = mockk<BukkitTask>(relaxed = true)
                val plugin = pluginWith(schedulerReturning(bukkitTask))

                val task = plugin.ticker().repeating(1.seconds) { }
                task.cancel()
                task.cancel()

                verify(exactly = 2) { bukkitTask.cancel() }
            }

            "rejects an interval that is not a whole number of ticks" {
                val plugin = pluginWith(schedulerReturning(mockk()))

                shouldThrow<IllegalArgumentException> {
                    plugin.ticker().repeating(75.milliseconds) { }
                }
            }

            "rejects a sub-millisecond remainder" {
                val plugin = pluginWith(schedulerReturning(mockk()))

                shouldThrow<IllegalArgumentException> {
                    plugin.ticker().repeating(50.milliseconds + 1.nanoseconds) { }
                }
            }

            "rejects a zero interval" {
                val plugin = pluginWith(schedulerReturning(mockk()))

                shouldThrow<IllegalArgumentException> {
                    plugin.ticker().repeating(Duration.ZERO) { }
                }
            }

            "rejects a negative interval" {
                val plugin = pluginWith(schedulerReturning(mockk()))

                shouldThrow<IllegalArgumentException> {
                    plugin.ticker().repeating((-1).seconds) { }
                }
            }
        },
    )
