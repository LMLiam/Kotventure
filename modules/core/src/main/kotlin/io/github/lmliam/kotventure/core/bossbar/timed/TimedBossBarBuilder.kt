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
    private var progressValue: ProgressEndpoints? by once { "'progress' is already set." }
    private var every: Duration? by once()
    private var onTick: (TimedBossBar.(Duration) -> Unit)? by once()
    private var onFinish: (TimedBossBar.() -> Unit)? by once()
    private var onCancel: (TimedBossBar.() -> Unit)? by once()

    override fun name(init: ComponentScope.() -> Unit): Unit = name(component(init))

    override fun <T : ComponentLike> name(component: T) {
        name = component.asComponent().asFixedName()
    }

    override fun name(render: TimedBossBarName) {
        name = render.asDynamicName()
    }

    override fun progress(
        from: Float,
        to: Float,
    ) {
        progressValue = ProgressEndpoints(from, to)
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
        val endpoints = progressValue
        val lifetime = over.requirePositive(label = "over")
        val interval = every ?: 1.ticks
        // Each tick subtracts `every` from remaining; a larger cadence would miss the lifetime
        // entirely and complete only after the first (late) fire.
        require(interval <= lifetime) {
            "'every' ($interval) must not exceed 'over' ($lifetime)."
        }
        return TimedBossBarConfig(
            name = checkNotNull(name) { "'name' is not set." },
            progressFrom = endpoints?.from ?: BossBar.MAX_PROGRESS,
            progressTo = endpoints?.to ?: BossBar.MIN_PROGRESS,
            appearance = appearance.build(),
            every = interval,
            over = lifetime,
            onTick = onTick,
            onFinish = onFinish,
            onCancel = onCancel,
        )
    }

    /** Linear progress endpoints; both ends must be legal Adventure boss-bar progress. */
    private data class ProgressEndpoints(
        val from: Float,
        val to: Float,
    ) {
        init {
            from.requireBossBarProgress(label = "from")
            to.requireBossBarProgress(label = "to")
        }
    }
}

/** Fixed name: ignore remaining time; change-detection on the bar skips redundant pushes. */
private fun Component.asFixedName(): (Duration) -> Component = { _ -> this }

/** Dynamic name: re-enter a component scope each tick with [TimedBossBarName] as the renderer. */
private fun TimedBossBarName.asDynamicName(): (Duration) -> Component =
    { remaining ->
        component {
            with(this@asDynamicName) {
                render(remaining)
            }
        }
    }

private fun Float.requireBossBarProgress(label: String): Float =
    also {
        require(this in BossBar.MIN_PROGRESS..BossBar.MAX_PROGRESS) {
            "'progress' $label must be in ${BossBar.MIN_PROGRESS}..${BossBar.MAX_PROGRESS}, got $this."
        }
    }

private fun Duration.requirePositive(label: String): Duration =
    also {
        require(isPositive()) { "'$label' must be positive, got $this." }
    }
