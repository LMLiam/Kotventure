package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

internal class NavBuilder : NavScope {
    private var previous: Component? by once()
    private var next: Component? by once()
    private var indicator: ((page: Int, pageCount: Int) -> ComponentLike?)? by once()
    private var uses: Int? by once()
    private var lifetime: Duration? by once()

    override fun previous(init: ComponentScope.() -> Unit) {
        previous = component(init)
    }

    override fun <C : ComponentLike> previous(component: C) {
        previous = component.asComponent()
    }

    override fun next(init: ComponentScope.() -> Unit) {
        next = component(init)
    }

    override fun <C : ComponentLike> next(component: C) {
        next = component.asComponent()
    }

    override fun indicator(shown: Boolean) {
        indicator = if (shown) ::defaultIndicator else hiddenIndicator
    }

    override fun indicator(render: (page: Int, pageCount: Int) -> ComponentLike) {
        indicator = render
    }

    override fun uses(count: Int) {
        require(count > 0 || count == ClickCallback.UNLIMITED_USES) {
            "'uses' must be positive or ClickCallback.UNLIMITED_USES, was $count."
        }
        uses = count
    }

    override fun lifetime(duration: Duration) {
        require(duration.isPositive()) { "'lifetime' must be positive, was $duration." }
        lifetime = duration
    }

    internal fun build(): NavSettings =
        NavSettings(
            previous = previous ?: text("« Previous"),
            next = next ?: text("Next »"),
            indicator = indicator ?: ::defaultIndicator,
            uses = uses ?: ClickCallback.UNLIMITED_USES,
            lifetime = lifetime ?: ClickCallback.DEFAULT_LIFETIME.toKotlinDuration(),
        )
}

private val hiddenIndicator: (page: Int, pageCount: Int) -> ComponentLike? = { _, _ -> null }

private fun defaultIndicator(
    page: Int,
    pageCount: Int,
): ComponentLike = text("$page/$pageCount")
