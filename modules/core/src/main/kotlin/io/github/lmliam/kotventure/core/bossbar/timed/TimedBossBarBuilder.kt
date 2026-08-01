package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.BossBarAppearanceBuilder
import io.github.lmliam.kotventure.core.bossbar.BossBarAppearanceScope
import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.dsl.positive
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.ticks
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import kotlin.time.Duration

private typealias TimedNameRenderer = (Duration) -> Component
private typealias TickHandler = TimedBossBar.(remaining: Duration) -> Unit
private typealias LifecycleHandler = TimedBossBar.() -> Unit

private val DefaultTickInterval: Duration = 1.ticks

/**
 * Validates [TimedBossBarScope] state and starts a [TimedBossBar] for one initial viewer.
 *
 * [build] takes an immutable configuration snapshot before it constructs the runtime.
 */
internal class TimedBossBarBuilder(
    private val appearance: BossBarAppearanceBuilder = BossBarAppearanceBuilder(),
) : TimedBossBarScope,
    BossBarAppearanceScope by appearance {
    private var name: TimedNameRenderer? by once()

    // The appearance scope owns `progress` as an overlay name, so this slot uses the endpoint name.
    private var progressEndpoints: TimedBossBarProgress? by once { "'progress' is already set." }
    private var every: Duration? by once()
    private var onTick: TickHandler? by once()
    private var onFinish: LifecycleHandler? by once()
    private var onCancel: LifecycleHandler? by once()

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
        require(interval.isFinite()) { "'every' must be finite, was $interval" }
        require(interval.isPositive()) { "'every' must be positive, was $interval" }
        every = interval
    }

    override fun onTick(handler: TickHandler) {
        onTick = handler
    }

    override fun onFinish(handler: LifecycleHandler) {
        onFinish = handler
    }

    override fun onCancel(handler: LifecycleHandler) {
        onCancel = handler
    }

    internal fun build(
        over: Duration,
        ticker: Ticker,
        initialViewer: Audience,
    ): TimedBossBar = TimedBossBar(ticker, over.toConfig(), initialViewer)

    private fun Duration.toConfig(): TimedBossBarConfig {
        val lifetime =
            also {
                require(isFinite()) { "'over' must be finite, was $this" }
                require(isPositive()) { "'over' must be positive, was $this" }
            }
        val interval = every ?: DefaultTickInterval

        // A cadence longer than the lifetime would make the first update late.
        require(interval <= lifetime) {
            "'every' ($interval) must not exceed 'over' ($lifetime)."
        }

        return TimedBossBarConfig(
            name = checkNotNull(name) { "'name' is not set." },
            progress = progressEndpoints ?: TimedBossBarProgress.Countdown,
            appearance = appearance.build(),
            every = interval,
            over = lifetime,
            onTick = onTick,
            onFinish = onFinish,
            onCancel = onCancel,
        )
    }
}
