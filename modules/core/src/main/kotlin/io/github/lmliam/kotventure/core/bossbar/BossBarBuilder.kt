package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class BossBarBuilder(
    private val appearance: BossBarAppearanceBuilder = BossBarAppearanceBuilder(),
) : BossBarScope,
    BossBarAppearanceScope by appearance {
    private var name: Component? by once()

    // BossBarAppearanceScope already uses `progress` for its overlay value.
    private var progressValue: Float? by once { "'progress' is already set." }

    override fun name(init: ComponentScope.() -> Unit): Unit = name(component(init))

    override fun <T : ComponentLike> name(component: T) {
        name = component.asComponent()
    }

    override fun progress(progress: Float) {
        // Claim the slot before range validation. A second call then always throws IllegalStateException.
        progressValue = progress
        progress.requireBossBarProgress()
    }

    internal fun build(): BossBar {
        val name = checkNotNull(name) { "'name' is not set." }
        val appearance = appearance.build()
        return BossBar.bossBar(
            name,
            progressValue ?: BossBar.MAX_PROGRESS,
            appearance.color,
            appearance.overlay,
            appearance.flags,
        )
    }
}
