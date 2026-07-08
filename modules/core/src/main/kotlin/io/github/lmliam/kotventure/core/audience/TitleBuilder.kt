package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.title.Title
import kotlin.time.Duration
import kotlin.time.toJavaDuration

internal class TitleBuilder : TitleScope {
    private var title: Component? by once()
    private var subtitle: Component? by once()
    private var times: Title.Times? by once()

    override fun title(init: ComponentScope.() -> Unit) = title(component(init))

    override fun <T : ComponentLike> title(component: T) {
        title = component.asComponent()
    }

    override fun subtitle(init: ComponentScope.() -> Unit) = subtitle(component(init))

    override fun <T : ComponentLike> subtitle(component: T) {
        subtitle = component.asComponent()
    }

    override fun times(
        fadeIn: Duration,
        stay: Duration,
        fadeOut: Duration,
    ) {
        times =
            Title.Times.times(
                fadeIn.toJavaDuration(),
                stay.toJavaDuration(),
                fadeOut.toJavaDuration(),
            )
    }

    internal fun build(): Title {
        check(title != null || subtitle != null) {
            "At least one of 'title' or 'subtitle' must be set."
        }
        return Title.title(
            title ?: Component.empty(),
            subtitle ?: Component.empty(),
            times ?: Title.DEFAULT_TIMES,
        )
    }
}
