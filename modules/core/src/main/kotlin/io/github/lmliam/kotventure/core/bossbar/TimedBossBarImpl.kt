package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.time.Duration

internal class TimedBossBarImpl(
    private val ticker: Ticker,
    private val config: TimedBossBarConfig,
    creator: Audience,
) : TimedBossBar {
    override val bar: BossBar =
        BossBar.bossBar(
            renderName(config.over),
            config.progressFrom,
            config.color,
            config.overlay,
            config.flags,
        )

    private val viewers: MutableSet<Audience> =
        Collections.newSetFromMap(IdentityHashMap())

    private val lock = Any()

    @Volatile
    private var elapsed: Duration = Duration.ZERO

    @Volatile
    override var remaining: Duration = config.over
        private set

    @Volatile
    override var isRunning: Boolean = true
        private set

    @Volatile
    override var isPaused: Boolean = false
        private set

    @Volatile
    private var task: TickerTask? = null

    init {
        show(creator)
        startTicking()
    }

    override fun pause() {
        synchronized(lock) {
            check(isRunning) { "Cannot pause a finished or cancelled TimedBossBar." }
            check(!isPaused) { "TimedBossBar is already paused." }
            isPaused = true
            stopTicking()
        }
    }

    override fun resume() {
        synchronized(lock) {
            check(isRunning) { "Cannot resume a finished or cancelled TimedBossBar." }
            check(isPaused) { "TimedBossBar is not paused." }
            isPaused = false
            startTicking()
        }
    }

    override fun cancel() {
        val shouldFireCancel: Boolean
        synchronized(lock) {
            if (!isRunning) {
                return
            }
            isRunning = false
            isPaused = false
            remaining = Duration.ZERO
            stopTicking()
            hideAllViewers()
            shouldFireCancel = true
        }
        if (shouldFireCancel) {
            config.onCancel?.invoke(this)
        }
    }

    override fun show(audience: Audience) {
        synchronized(lock) {
            if (!isRunning) {
                return
            }
            viewers.add(audience)
            audience.showBossBar(bar)
        }
    }

    override fun hide(audience: Audience) {
        synchronized(lock) {
            viewers.remove(audience)
            audience.hideBossBar(bar)
        }
    }

    private fun startTicking() {
        task =
            ticker.repeating(config.every) {
                onInterval()
            }
    }

    private fun stopTicking() {
        task?.cancel()
        task = null
    }

    private fun onInterval() {
        val finished: Boolean
        val remainingAfter: Duration
        synchronized(lock) {
            if (!isRunning || isPaused) {
                return
            }
            elapsed += config.every
            if (elapsed >= config.over) {
                elapsed = config.over
                remaining = Duration.ZERO
                bar.progress(config.progressTo)
                applyName(Duration.ZERO)
                finished = true
                remainingAfter = Duration.ZERO
            } else {
                remaining = config.over - elapsed
                bar.progress(interpolatedProgress(elapsed))
                applyName(remaining)
                finished = false
                remainingAfter = remaining
            }
        }

        // Hooks run outside the lock so re-entrant handle calls can take it.
        config.onTick?.invoke(this, remainingAfter)

        if (finished) {
            completeNaturally()
        }
    }

    private fun completeNaturally() {
        synchronized(lock) {
            if (!isRunning) {
                return
            }
            isRunning = false
            isPaused = false
            stopTicking()
            hideAllViewers()
        }
        config.onFinish?.invoke(this)
    }

    private fun hideAllViewers() {
        val snapshot = viewers.toList()
        viewers.clear()
        for (audience in snapshot) {
            audience.hideBossBar(bar)
        }
    }

    private fun applyName(remaining: Duration) {
        when (val spec = config.name) {
            is BossBarNameSpec.Static -> Unit
            is BossBarNameSpec.Dynamic -> bar.name(spec.render(remaining))
        }
    }

    private fun renderName(remaining: Duration): Component =
        when (val spec = config.name) {
            is BossBarNameSpec.Static -> spec.component
            is BossBarNameSpec.Dynamic -> spec.render(remaining)
        }

    private fun interpolatedProgress(elapsed: Duration): Float {
        val overNanos = config.over.inWholeNanoseconds.toDouble()
        val fraction = (elapsed.inWholeNanoseconds.toDouble() / overNanos).toFloat()
        return config.progressFrom + (config.progressTo - config.progressFrom) * fraction
    }
}
