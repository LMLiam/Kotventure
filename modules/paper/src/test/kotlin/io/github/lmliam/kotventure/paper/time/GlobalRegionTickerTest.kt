package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
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

private fun serverWith(scheduler: GlobalRegionScheduler): Server {
    val server = mockk<Server>()
    every { server.globalRegionScheduler } returns scheduler
    return server
}

private fun schedulerReturning(task: ScheduledTask): GlobalRegionScheduler {
    val scheduler = mockk<GlobalRegionScheduler>()
    every {
        scheduler.runAtFixedRate(any<Plugin>(), any<Consumer<ScheduledTask>>(), any<Long>(), any<Long>())
    } returns task
    return scheduler
}

private fun rejects(interval: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker().repeating(interval) { }
    }
}

class GlobalRegionTickerTest :
    StringSpec(
        {
            "schedules repeating work with the interval as the global region delay and period" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))

                plugin.ticker().repeating(1.seconds) { }

                verify { scheduler.runAtFixedRate(plugin, any<Consumer<ScheduledTask>>(), 20L, 20L) }
            }

            "never routes through the bukkit scheduler" {
                val bukkitScheduler = mockk<BukkitScheduler>()
                val server = serverWith(schedulerReturning(mockk()))
                every { server.scheduler } returns bukkitScheduler
                val plugin = pluginWith(server)

                plugin.ticker().repeating(1.seconds) { }

                verify { bukkitScheduler wasNot Called }
            }

            "accepts an interval written in ticks" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))

                plugin.ticker().repeating(3.ticks) { }

                verify { scheduler.runAtFixedRate(plugin, any<Consumer<ScheduledTask>>(), 3L, 3L) }
            }

            "a scheduled fire sends through the audience DSL" {
                val consumer = slot<Consumer<ScheduledTask>>()
                val scheduler = mockk<GlobalRegionScheduler>()
                every {
                    scheduler.runAtFixedRate(any<Plugin>(), capture(consumer), any<Long>(), any<Long>())
                } returns mockk()
                val plugin = pluginWith(serverWith(scheduler))
                val sent = slot<Component>()
                val player = mockk<Player>()
                every { player.sendMessage(capture(sent)) } just Runs

                plugin.ticker().repeating(1.seconds) {
                    player.message { text("Meteor incoming") }
                }
                consumer.captured.accept(mockk())

                sent.captured shouldHaveContent "Meteor incoming"
            }

            "cancel delegates to the scheduled task on every call" {
                val scheduledTask = mockk<ScheduledTask>(relaxed = true)
                val plugin = pluginWith(serverWith(schedulerReturning(scheduledTask)))

                val task = plugin.ticker().repeating(1.seconds) { }
                task.cancel()
                task.cancel()

                verify(exactly = 2) { scheduledTask.cancel() }
            }

            "rejects an interval that is not a whole number of ticks" { rejects(75.milliseconds) }

            "rejects a sub-millisecond remainder" { rejects(50.milliseconds + 1.nanoseconds) }

            "rejects a zero interval" { rejects(Duration.ZERO) }

            "rejects a negative interval" { rejects((-1).seconds) }
        },
    )
