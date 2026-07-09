package io.github.lmliam.kotventure.core.bossbar.timed

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Identity-tracked audiences watching a managed boss bar.
 *
 * Not thread-safe on its own — the owning [TimedBossBar] serializes access.
 */
internal class TimedBossBarViewers {
    private val viewers: MutableSet<Audience> =
        Collections.newSetFromMap(IdentityHashMap())

    fun add(audience: Audience): Boolean = viewers.add(audience)

    fun remove(audience: Audience): Boolean = viewers.remove(audience)

    operator fun contains(audience: Audience): Boolean = audience in viewers

    /** Copies current viewers and clears the set so auto-hide cannot double-target them. */
    fun snapshotAndClear(): List<Audience> {
        val snapshot = viewers.toList()
        viewers.clear()
        return snapshot
    }

    /**
     * Hides [bar] from every audience, isolating per-viewer failures so one throw cannot leave
     * the rest visible. Rethrows the first error with the others suppressed.
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
