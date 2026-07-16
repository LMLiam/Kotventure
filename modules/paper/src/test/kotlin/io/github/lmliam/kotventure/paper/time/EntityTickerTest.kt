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
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import java.util.function.Consumer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private fun entityWith(scheduler: EntityScheduler): Entity {
    val entity = mockk<Entity>()
    every { entity.scheduler } returns scheduler
    return entity
}

private fun schedulerReturning(task: ScheduledTask?): EntityScheduler {
    val scheduler = mockk<EntityScheduler>()
    every {
        scheduler.runAtFixedRate(any<Plugin>(), any<Consumer<ScheduledTask>>(), isNull(), any<Long>(), any<Long>())
    } returns task
    return scheduler
}

private fun rejects(interval: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker(mockk<Entity>()).repeating(interval) { }
    }
}

class EntityTickerTest :
    StringSpec(
        {
            "schedules repeating work with the interval as the entity delay and period" {
                val scheduler = schedulerReturning(mockk())
                val plugin = mockk<Plugin>()
                val entity = entityWith(scheduler)

                plugin.ticker(entity).repeating(1.seconds) { }

                verify {
                    scheduler.runAtFixedRate(plugin, any<Consumer<ScheduledTask>>(), isNull(), 20L, 20L)
                }
            }

            "never routes through the bukkit scheduler" {
                val bukkitScheduler = mockk<BukkitScheduler>()
                val plugin = mockk<Plugin>()
                every { plugin.server.scheduler } returns bukkitScheduler
                val entity = entityWith(schedulerReturning(mockk()))

                plugin.ticker(entity).repeating(1.seconds) { }

                verify { bukkitScheduler wasNot Called }
            }

            "accepts an interval written in ticks" {
                val scheduler = schedulerReturning(mockk())
                val plugin = mockk<Plugin>()
                val entity = entityWith(scheduler)

                plugin.ticker(entity).repeating(3.ticks) { }

                verify {
                    scheduler.runAtFixedRate(plugin, any<Consumer<ScheduledTask>>(), isNull(), 3L, 3L)
                }
            }

            "a scheduled fire sends through the audience DSL" {
                val consumer = slot<Consumer<ScheduledTask>>()
                val scheduler = mockk<EntityScheduler>()
                every {
                    scheduler.runAtFixedRate(any<Plugin>(), capture(consumer), isNull(), any<Long>(), any<Long>())
                } returns mockk()
                val plugin = mockk<Plugin>()
                val entity = entityWith(scheduler)
                val sent = slot<Component>()
                val player = mockk<Player>()
                every { player.sendMessage(capture(sent)) } just Runs

                plugin.ticker(entity).repeating(1.seconds) {
                    player.message { text("Meteor incoming") }
                }
                consumer.captured.accept(mockk())

                sent.captured shouldHaveContent "Meteor incoming"
            }

            "cancel delegates to the scheduled task on every call" {
                val scheduledTask = mockk<ScheduledTask>(relaxed = true)
                val plugin = mockk<Plugin>()
                val entity = entityWith(schedulerReturning(scheduledTask))

                val task = plugin.ticker(entity).repeating(1.seconds) { }
                task.cancel()
                task.cancel()

                verify(exactly = 2) { scheduledTask.cancel() }
            }

            "rejects an interval that is not a whole number of ticks" { rejects(75.milliseconds) }

            "rejects a sub-millisecond remainder" { rejects(50.milliseconds + 1.nanoseconds) }

            "rejects a zero interval" { rejects(Duration.ZERO) }

            "rejects a negative interval" { rejects((-1).seconds) }

            "fails when the entity scheduler rejects a removed entity" {
                val plugin = mockk<Plugin>()
                val entity = entityWith(schedulerReturning(null))

                shouldThrow<IllegalStateException> {
                    plugin.ticker(entity).repeating(1.seconds) { }
                }
            }
        },
    )
