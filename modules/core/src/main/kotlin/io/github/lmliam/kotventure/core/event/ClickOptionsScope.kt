package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration

/**
 * Configures the limits of a server-side click callback.
 *
 * Defaults when a slot is left unset: one use, and Adventure's default callback lifetime of twelve hours.
 *
 * @sample io.github.lmliam.kotventure.core.event.clickOptionsSample
 */
@KotventureDslMarker
public interface ClickOptionsScope {
    /**
     * The [uses] count that lets a callback be clicked any number of times.
     * [ClickCallback.UNLIMITED_USES] under a clearer name.
     */
    public val unlimited: Int
        get() = ClickCallback.UNLIMITED_USES

    /**
     * Sets how many times the callback may be clicked. Defaults to one use.
     *
     * @throws IllegalStateException when the use count is already set in this block.
     * @throws IllegalArgumentException when [count] is neither positive nor [unlimited].
     */
    public fun uses(count: Int)

    /**
     * Sets how long the callback stays clickable. Defaults to
     * [Adventure's twelve hours][net.kyori.adventure.text.event.ClickCallback.DEFAULT_LIFETIME].
     *
     * @throws IllegalStateException when the lifetime is already set in this block.
     * @throws IllegalArgumentException when [duration] is not positive.
     */
    public fun lifetime(duration: Duration)
}
