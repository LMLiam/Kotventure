package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.BossBarAppearanceBuilder
import io.github.lmliam.kotventure.core.bossbar.BossBarAppearanceScope
import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.ticks
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import kotlin.time.Duration

/**
 * Collects [TimedBossBarScope] slots into an immutable [TimedBossBarConfig], then starts a
 * [TimedBossBar] against the given [Ticker] and initial viewer.
 */
internal class TimedBossBarBuilder(
    private val appearance: BossBarAppearanceBuilder = BossBarAppearanceBuilder(),
) : TimedBossBarScope,
    BossBarAppearanceScope by appearance {
    private var name: ((Duration) -> Component)? by once()

    // Not named `progress`: BossBarAppearanceScope already has `val progress: Overlay`.
    private var progressEndpoints: TimedBossBarProgress? by once { "'progress' is already set." }
    private var every: Duration? by once()
    private var onTick: (TimedBossBar.(Duration) -> Unit)? by once()
    private var onFinish: (TimedBossBar.() -> Unit)? by once()
    private var onCancel: (TimedBossBar.() -> Unit)? by once()

    override fun name(init: ComponentScope.() -> Unit): Unit = name(component(init))

    override fun <T : ComponentLike> name(component: T) {
        name = component.asComponent().asFixedTimedName()
    }

    override fun name(render: TimedBossBarName) {
        name = render.asDynamicTimedName()
    }

    override fun progress(
        from: Float,
        to: Float,
    ) {
        progressEndpoints = TimedBossBarProgress(from = from, to = to)
    }

    override fun every(interval: Duration) {
        every = interval.requirePositive(label = "every")
    }

    override fun onTick(handler: TimedBossBar.(remaining: Duration) -> Unit) {
        onTick = handler
    }

    override fun onFinish(handler: TimedBossBar.() -> Unit) {
        onFinish = handler
    }

    override fun onCancel(handler: TimedBossBar.() -> Unit) {
        onCancel = handler
    }

    internal fun build(
        over: Duration,
        ticker: Ticker,
        initialViewer: Audience,
    ): TimedBossBar = TimedBossBar(ticker, toConfig(over), initialViewer)

    private fun toConfig(over: Duration): TimedBossBarConfig {
        val lifetime = over.requirePositive(label = "over")
        val interval = every ?: 1.ticks
        // Each tick subtracts `every` from remaining; a larger cadence would miss the lifetime
        // entirely and complete only after the first (late) fire.
        require(interval <= lifetime) {
            "'every' ($interval) must not exceed 'over' ($lifetime)."
        }
        return TimedBossBarConfig(
            name = checkNotNull(name) { "'name' is not set." },
            progress =
                progressEndpoints
                    ?: TimedBossBarProgress(from = BossBar.MAX_PROGRESS, to = BossBar.MIN_PROGRESS),
            appearance = appearance.build(),
            every = interval,
            over = lifetime,
            onTick = onTick,
            onFinish = onFinish,
            onCancel = onCancel,
        )
    }
}

private fun Duration.requirePositive(label: String): Duration =
    also {
        require(isPositive()) { "'$label' must be positive, got $this." }
    }
