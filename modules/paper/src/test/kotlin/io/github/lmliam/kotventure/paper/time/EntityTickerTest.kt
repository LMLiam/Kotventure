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
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import org.bukkit.Server
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

private fun onceSchedulerReturning(task: ScheduledTask?): EntityScheduler {
    val scheduler = mockk<EntityScheduler>()
    every { scheduler.run(any<Plugin>(), any<Consumer<ScheduledTask>>(), isNull()) } returns task
    every {
        scheduler.runDelayed(any<Plugin>(), any<Consumer<ScheduledTask>>(), isNull(), any<Long>())
    } returns task
    return scheduler
}

private fun rejects(interval: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker(mockk<Entity>()).every(interval) { }
    }
}

private fun rejectsOnce(delay: Duration) {
    shouldThrow<IllegalArgumentException> {
        mockk<Plugin>().ticker(mockk<Entity>()).after(delay) { }
    }
}

class EntityTickerTest :
    StringSpec(
        {
            "schedules repeating work with the interval as the entity delay and period" {
                val scheduler = schedulerReturning(mockk())
                val plugin = mockk<Plugin>()
                val entity = entityWith(scheduler)

                plugin.ticker(entity).every(1.seconds) { }

                verify {
                    scheduler.runAtFixedRate(plugin, any<Consumer<ScheduledTask>>(), isNull(), 20L, 20L)
                }
            }

            "never routes through the bukkit scheduler" {
                val bukkitScheduler = mockk<BukkitScheduler>()
                val plugin = mockk<Plugin>()
                every { plugin.server.scheduler } returns bukkitScheduler
                val entity = entityWith(schedulerReturning(mockk()))

                plugin.ticker(entity).every(1.seconds) { }

                verify { bukkitScheduler wasNot Called }
            }

            "accepts an interval written in ticks" {
                val scheduler = schedulerReturning(mockk())
                val plugin = mockk<Plugin>()
                val entity = entityWith(scheduler)

                plugin.ticker(entity).every(3.ticks) { }

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

                plugin.ticker(entity).every(1.seconds) {
                    player.message { text("Meteor incoming") }
                }
                consumer.captured.accept(mockk())

                sent.captured shouldHaveContent "Meteor incoming"
            }

            "cancel delegates to the scheduled task on every call" {
                val scheduledTask = mockk<ScheduledTask>(relaxed = true)
                val plugin = mockk<Plugin>()
                val entity = entityWith(schedulerReturning(scheduledTask))

                val task = plugin.ticker(entity).every(1.seconds) { }
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
                    plugin.ticker(entity).every(1.seconds) { }
                }
            }

            "schedules a zero delay on the next tick of the entity region" {
                val scheduler = onceSchedulerReturning(mockk())
                val plugin = mockk<Plugin>()
                val entity = entityWith(scheduler)

                plugin.ticker(entity).after { }

                verify { scheduler.run(plugin, any<Consumer<ScheduledTask>>(), isNull()) }
                verify(exactly = 0) {
                    scheduler.runDelayed(any<Plugin>(), any<Consumer<ScheduledTask>>(), isNull(), any<Long>())
                }
            }

            "schedules a positive delay as entity region ticks" {
                val scheduler = onceSchedulerReturning(mockk())
                val plugin = mockk<Plugin>()
                val entity = entityWith(scheduler)

                plugin.ticker(entity).after(3.ticks) { }

                verify { scheduler.runDelayed(plugin, any<Consumer<ScheduledTask>>(), isNull(), 3L) }
                verify(exactly = 0) { scheduler.run(any<Plugin>(), any<Consumer<ScheduledTask>>(), isNull()) }
            }

            "rejects a one-shot delay that is not a whole number of ticks" { rejectsOnce(75.milliseconds) }

            "rejects a negative one-shot delay" { rejectsOnce((-1).seconds) }

            "fails a one-shot schedule when the entity scheduler rejects a removed entity" {
                val plugin = mockk<Plugin>()
                val entity = entityWith(onceSchedulerReturning(null))

                shouldThrow<IllegalStateException> {
                    plugin.ticker(entity).after { }
                }
            }

            "reads thread ownership from the region that owns the entity" {
                val scheduler = onceSchedulerReturning(mockk())
                val entity = entityWith(scheduler)
                val server = mockk<Server>()
                every { server.isOwnedByCurrentRegion(entity) } returns true
                val plugin = mockk<Plugin>()
                every { plugin.server } returns server

                plugin.ticker(entity).isCurrent shouldBe true

                verify { server.isOwnedByCurrentRegion(entity) }
            }
        },
    )
