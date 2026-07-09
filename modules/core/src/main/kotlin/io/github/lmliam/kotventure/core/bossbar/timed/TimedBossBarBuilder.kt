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

internal class TimedBossBarBuilder(
    private val appearance: BossBarAppearanceBuilder = BossBarAppearanceBuilder(),
) : TimedBossBarScope,
    BossBarAppearanceScope by appearance {
    private var name: ((Duration) -> Component)? by once()
    private var progressRange: ProgressRange? by once { "'progress' is already set." }
    private var every: Duration? by once()
    private var onTick: (TimedBossBar.(Duration) -> Unit)? by once()
    private var onFinish: (TimedBossBar.() -> Unit)? by once()
    private var onCancel: (TimedBossBar.() -> Unit)? by once()

    override fun name(init: ComponentScope.() -> Unit): Unit = name(component(init))

    override fun <T : ComponentLike> name(component: T) {
        val fixed = component.asComponent()
        name = { fixed }
    }

    override fun name(render: TimedBossBarName) {
        name = { remaining -> component { with(render) { render(remaining) } } }
    }

    override fun progress(
        from: Float,
        to: Float,
    ) {
        requireProgress(from, "from")
        requireProgress(to, "to")
        progressRange = ProgressRange(from, to)
    }

    override fun every(interval: Duration) {
        require(interval.isPositive()) { "'every' must be positive, got $interval." }
        every = interval
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
    ): TimedBossBar = TimedBossBar(ticker, buildConfig(over), initialViewer)

    private fun buildConfig(over: Duration): TimedBossBarConfig {
        require(over.isPositive()) { "'over' must be positive, got $over." }
        val name = checkNotNull(name) { "'name' is not set." }
        val range = progressRange
        return TimedBossBarConfig(
            name = name,
            progressFrom = range?.from ?: BossBar.MAX_PROGRESS,
            progressTo = range?.to ?: BossBar.MIN_PROGRESS,
            appearance = appearance.build(),
            every = every ?: 1.ticks,
            over = over,
            onTick = onTick,
            onFinish = onFinish,
            onCancel = onCancel,
        )
    }

    private data class ProgressRange(
        val from: Float,
        val to: Float,
    )
}

private fun requireProgress(
    value: Float,
    label: String,
) {
    require(value in BossBar.MIN_PROGRESS..BossBar.MAX_PROGRESS) {
        "'progress' $label must be in ${BossBar.MIN_PROGRESS}..${BossBar.MAX_PROGRESS}, got $value."
    }
}
