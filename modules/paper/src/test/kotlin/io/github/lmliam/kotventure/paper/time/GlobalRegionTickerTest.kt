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

private fun onceSchedulerReturning(task: ScheduledTask): GlobalRegionScheduler {
    val scheduler = mockk<GlobalRegionScheduler>()
    every { scheduler.run(any<Plugin>(), any<Consumer<ScheduledTask>>()) } returns task
    every { scheduler.runDelayed(any<Plugin>(), any<Consumer<ScheduledTask>>(), any<Long>()) } returns task
    return scheduler
}

private fun rejects(interval: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker().every(interval) { }
    }
}

private fun rejectsOnce(delay: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker().after(delay) { }
    }
}

class GlobalRegionTickerTest :
    StringSpec(
        {
            "schedules repeating work with the interval as the global region delay and period" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))

                plugin.ticker().every(1.seconds) { }

                verify { scheduler.runAtFixedRate(plugin, any<Consumer<ScheduledTask>>(), 20L, 20L) }
            }

            "never routes through the bukkit scheduler" {
                val bukkitScheduler = mockk<BukkitScheduler>()
                val server = serverWith(schedulerReturning(mockk()))
                every { server.scheduler } returns bukkitScheduler
                val plugin = pluginWith(server)

                plugin.ticker().every(1.seconds) { }

                verify { bukkitScheduler wasNot Called }
            }

            "accepts an interval written in ticks" {
                val scheduler = schedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))

                plugin.ticker().every(3.ticks) { }

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

                plugin.ticker().every(1.seconds) {
                    player.message { text("Meteor incoming") }
                }
                consumer.captured.accept(mockk())

                sent.captured shouldHaveContent "Meteor incoming"
            }

            "cancel delegates to the scheduled task on every call" {
                val scheduledTask = mockk<ScheduledTask>(relaxed = true)
                val plugin = pluginWith(serverWith(schedulerReturning(scheduledTask)))

                val task = plugin.ticker().every(1.seconds) { }
                task.cancel()
                task.cancel()

                verify(exactly = 2) { scheduledTask.cancel() }
            }

            "rejects an interval that is not a whole number of ticks" { rejects(75.milliseconds) }

            "rejects a sub-millisecond remainder" { rejects(50.milliseconds + 1.nanoseconds) }

            "rejects a zero interval" { rejects(Duration.ZERO) }

            "rejects a negative interval" { rejects((-1).seconds) }

            "schedules a zero delay on the next global tick" {
                val scheduler = onceSchedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))

                plugin.ticker().after { }

                verify { scheduler.run(plugin, any<Consumer<ScheduledTask>>()) }
                verify(exactly = 0) {
                    scheduler.runDelayed(any<Plugin>(), any<Consumer<ScheduledTask>>(), any<Long>())
                }
            }

            "schedules a positive delay as global region ticks" {
                val scheduler = onceSchedulerReturning(mockk())
                val plugin = pluginWith(serverWith(scheduler))

                plugin.ticker().after(3.ticks) { }

                verify { scheduler.runDelayed(plugin, any<Consumer<ScheduledTask>>(), 3L) }
                verify(exactly = 0) { scheduler.run(any<Plugin>(), any<Consumer<ScheduledTask>>()) }
            }

            "a one-shot fire sends through the audience DSL" {
                val consumer = slot<Consumer<ScheduledTask>>()
                val scheduler = mockk<GlobalRegionScheduler>()
                every { scheduler.run(any<Plugin>(), capture(consumer)) } returns mockk()
                val plugin = pluginWith(serverWith(scheduler))
                val sent = slot<Component>()
                val player = mockk<Player>()
                every { player.sendMessage(capture(sent)) } just Runs

                plugin.ticker().after { player.message { text("Meteor landed") } }
                consumer.captured.accept(mockk())

                sent.captured shouldHaveContent "Meteor landed"
            }

            "cancel delegates to the one-shot scheduled task on every call" {
                val scheduledTask = mockk<ScheduledTask>(relaxed = true)
                val plugin = pluginWith(serverWith(onceSchedulerReturning(scheduledTask)))

                val task = plugin.ticker().after(1.seconds) { }
                task.cancel()
                task.cancel()

                verify(exactly = 2) { scheduledTask.cancel() }
            }

            "rejects a one-shot delay that is not a whole number of ticks" { rejectsOnce(75.milliseconds) }

            "rejects a negative one-shot delay" { rejectsOnce((-1).seconds) }

            "reads thread ownership from the global tick thread" {
                val server = mockk<Server>()
                every { server.isGlobalTickThread } returns true
                val plugin = pluginWith(server)

                plugin.ticker().isCurrent shouldBe true

                verify { server.isGlobalTickThread }
            }
        },
    )
