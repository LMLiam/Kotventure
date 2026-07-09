package io.github.lmliam.kotventure.core.bossbar.timed

import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration

/**
 * Linear progress endpoints for a managed bar, each validated in Adventure's inclusive
 * [[BossBar.MIN_PROGRESS], [BossBar.MAX_PROGRESS]] range.
 */
internal data class TimedBossBarProgress(
    val from: Float,
    val to: Float,
) {
    init {
        from.requireBossBarProgress(label = "from")
        to.requireBossBarProgress(label = "to")
    }

    /**
     * Interpolates fill for the given [remaining] time within [over].
     *
     * Lands exactly on [to] when [remaining] is zero.
     */
    fun at(
        remaining: Duration,
        over: Duration,
    ): Float {
        if (remaining == Duration.ZERO) return to
        val elapsedFraction = 1.0 - (remaining / over)
        return from + ((to - from) * elapsedFraction).toFloat()
    }
}

private fun Float.requireBossBarProgress(label: String): Float =
    also {
        require(this in BossBar.MIN_PROGRESS..BossBar.MAX_PROGRESS) {
            "'progress' $label must be in ${BossBar.MIN_PROGRESS}..${BossBar.MAX_PROGRESS}, got $this."
        }
    }
