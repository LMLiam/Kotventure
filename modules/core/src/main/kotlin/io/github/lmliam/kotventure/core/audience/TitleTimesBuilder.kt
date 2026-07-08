package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.title.Title
import kotlin.time.Duration
import kotlin.time.toJavaDuration

internal class TitleTimesBuilder : TitleTimesScope {
    private var fadeIn: Duration? by once()
    private var stay: Duration? by once()
    private var fadeOut: Duration? by once()

    override fun fadeIn(duration: Duration) {
        fadeIn = duration
    }

    override fun stay(duration: Duration) {
        stay = duration
    }

    override fun fadeOut(duration: Duration) {
        fadeOut = duration
    }

    internal fun build(): Title.Times {
        val defaults = Title.DEFAULT_TIMES
        return Title.Times.times(
            fadeIn?.toJavaDuration() ?: defaults.fadeIn(),
            stay?.toJavaDuration() ?: defaults.stay(),
            fadeOut?.toJavaDuration() ?: defaults.fadeOut(),
        )
    }
}
