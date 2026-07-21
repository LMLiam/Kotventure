package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.requireBossBarProgress
import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration

/**
 * Validated start and end progress for a linear managed-bar transition.
 */
internal data class TimedBossBarProgress(
    val from: Float,
    val to: Float,
) {
    init {
        from.requireBossBarProgress(label = "progress from")
        to.requireBossBarProgress(label = "progress to")
    }

    /**
     * Returns the linear progress for [remaining] within the positive lifetime [over].
     *
     * The result is [to] when [remaining] is zero and [from] when it equals [over].
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
