package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration
import kotlin.time.toJavaDuration

internal class ClickOptionsBuilder : ClickOptionsScope {
    private var uses: Int? by once()
    private var lifetime: Duration? by once()

    override fun uses(count: Int) {
        require(count > 0 || count == unlimited) {
            "'uses' must be positive or 'unlimited', was $count."
        }
        uses = count
    }

    override fun lifetime(duration: Duration) {
        require(duration.isPositive()) { "'lifetime' must be positive, was $duration." }
        lifetime = duration
    }

    fun build(): ClickCallback.Options =
        ClickCallback.Options
                .builder()
                .apply { uses?.let(::uses) }
                .apply { lifetime?.let { lifetime(it.toJavaDuration()) } }
                .build()
}
