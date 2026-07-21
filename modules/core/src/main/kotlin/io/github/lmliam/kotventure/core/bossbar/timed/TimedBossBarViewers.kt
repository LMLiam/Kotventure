package io.github.lmliam.kotventure.core.bossbar.timed

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Tracks managed boss-bar viewers by object identity.
 *
 * This class is not thread-safe. [TimedBossBarRuntime] serialises access with its state lock.
 */
internal class TimedBossBarViewers {
    private val viewers: MutableSet<Audience> =
        Collections.newSetFromMap(IdentityHashMap())

    fun add(audience: Audience): Boolean = viewers.add(audience)

    fun remove(audience: Audience): Boolean = viewers.remove(audience)

    operator fun contains(audience: Audience): Boolean = audience in viewers

    /** Removes and returns all current viewers so later shutdown work cannot target them twice. */
    fun snapshotAndClear(): List<Audience> {
        val snapshot = viewers.toList()
        viewers.clear()
        return snapshot
    }

    /**
     * Attempts to hide [bar] from every entry in [audiences].
     *
     * A failure does not stop later attempts. After all attempts, the function throws the first failure and attaches
     * later failures as suppressed exceptions.
     */
    fun hideAll(
        bar: BossBar,
        audiences: List<Audience>,
    ) {
        var firstError: Throwable? = null
        for (audience in audiences) {
            try {
                audience.hideBossBar(bar)
            } catch (error: Throwable) {
                if (firstError == null) {
                    firstError = error
                } else {
                    firstError.addSuppressed(error)
                }
            }
        }
        firstError?.let { throw it }
    }
}
