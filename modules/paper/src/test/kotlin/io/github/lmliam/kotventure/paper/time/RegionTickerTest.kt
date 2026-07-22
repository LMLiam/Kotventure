package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.papermc.paper.threadedregions.scheduler.RegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import java.util.function.Consumer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private fun pluginWith(server: Server): Plugin {
    val plugin = mockk<Plugin>()
    every { plugin.server } returns server
    return plugin
}

private fun serverWith(scheduler: RegionScheduler): Server {
    val server = mockk<Server>()
    every { server.regionScheduler } returns scheduler
    return server
}

private fun schedulerReturning(task: ScheduledTask): RegionScheduler {
    val scheduler = mockk<RegionScheduler>()
    every {
        scheduler.runAtFixedRate(
            any<Plugin>(),
            any<Location>(),
            any<Consumer<ScheduledTask>>(),
            any<Long>(),
            any<Long>(),
        )
    } returns task
    return scheduler
}

private fun onceSchedulerReturning(task: ScheduledTask): RegionScheduler {
    val scheduler = mockk<RegionScheduler>()
    every { scheduler.run(any<Plugin>(), any<Location>(), any<Consumer<ScheduledTask>>()) } returns task
    every {
        scheduler.runDelayed(any<Plugin>(), any<Location>(), any<Consumer<ScheduledTask>>(), any<Long>())
    } returns task
    return scheduler
}

private fun rejects(interval: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker(mockk<Location>()).repeating(interval) { }
    }
}

private fun rejectsOnce(delay: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker(mockk<Location>()).once(delay) { }
    }
}

class RegionTickerTest :
    StringSpec(
        {
            "schedules repeating work with the interval as the region delay and period" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))
                val location = mockk<Location>()

                plugin.ticker(location).repeating(1.seconds) { }

                verify {
                    scheduler.runAtFixedRate(plugin, location, any<Consumer<ScheduledTask>>(), 20L, 20L)
                }
            }

            "never routes through the bukkit scheduler" {
                val bukkitScheduler = mockk<BukkitScheduler>()
                val server = serverWith(schedulerReturning(mockk()))
                every { server.scheduler } returns bukkitScheduler
                val plugin = pluginWith(server)

                plugin.ticker(mockk<Location>()).repeating(1.seconds) { }

                verify { bukkitScheduler wasNot Called }
            }

            "accepts an interval written in ticks" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))
                val location = mockk<Location>()

                plugin.ticker(location).repeating(3.ticks) { }

                verify {
                    scheduler.runAtFixedRate(plugin, location, any<Consumer<ScheduledTask>>(), 3L, 3L)
                }
            }

            "a scheduled fire sends through the audience DSL" {
                val consumer = slot<Consumer<ScheduledTask>>()
                val scheduler = mockk<RegionScheduler>()
                every {
                    scheduler.runAtFixedRate(
                        any<Plugin>(),
                        any<Location>(),
                        capture(consumer),
                        any<Long>(),
                        any<Long>(),
                    )
                } returns mockk()
                val plugin = pluginWith(serverWith(scheduler))
                val location = mockk<Location>()
                val sent = slot<Component>()
                val player = mockk<Player>()
                every { player.sendMessage(capture(sent)) } just Runs

                plugin.ticker(location).repeating(1.seconds) {
                    player.message { text("Meteor incoming") }
                }
                consumer.captured.accept(mockk())

                sent.captured shouldHaveContent "Meteor incoming"
            }

            "cancel delegates to the scheduled task on every call" {
                val scheduledTask = mockk<ScheduledTask>(relaxed = true)
                val plugin = pluginWith(serverWith(schedulerReturning(scheduledTask)))
                val location = mockk<Location>()

                val task = plugin.ticker(location).repeating(1.seconds) { }
                task.cancel()
                task.cancel()

                verify(exactly = 2) { scheduledTask.cancel() }
            }

            "rejects an interval that is not a whole number of ticks" { rejects(75.milliseconds) }

            "rejects a sub-millisecond remainder" { rejects(50.milliseconds + 1.nanoseconds) }

            "rejects a zero interval" { rejects(Duration.ZERO) }

            "rejects a negative interval" { rejects((-1).seconds) }

            "schedules a zero delay on the next tick of the region" {
                val scheduler = onceSchedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))
                val location = mockk<Location>()

                plugin.ticker(location).once { }

                verify { scheduler.run(plugin, location, any<Consumer<ScheduledTask>>()) }
                verify(exactly = 0) {
                    scheduler.runDelayed(any<Plugin>(), any<Location>(), any<Consumer<ScheduledTask>>(), any<Long>())
                }
            }

            "schedules a positive delay as region ticks" {
                val scheduler = onceSchedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))
                val location = mockk<Location>()

                plugin.ticker(location).once(3.ticks) { }

                verify { scheduler.runDelayed(plugin, location, any<Consumer<ScheduledTask>>(), 3L) }
                verify(exactly = 0) {
                    scheduler.run(any<Plugin>(), any<Location>(), any<Consumer<ScheduledTask>>())
                }
            }

            "rejects a one-shot delay that is not a whole number of ticks" { rejectsOnce(75.milliseconds) }

            "rejects a negative one-shot delay" { rejectsOnce((-1).seconds) }

            "reads thread ownership from the region that contains the location" {
                val location = mockk<Location>()
                val server = mockk<Server>()
                every { server.isOwnedByCurrentRegion(location) } returns true
                val plugin = pluginWith(server)

                plugin.ticker(location).ownsCurrentThread shouldBe true

                verify { server.isOwnedByCurrentRegion(location) }
            }
        },
    )
