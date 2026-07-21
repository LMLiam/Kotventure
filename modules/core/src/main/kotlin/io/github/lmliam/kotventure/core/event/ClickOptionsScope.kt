package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration

/**
 * Configures the limits of a server-side click callback.
 *
 * Unset slots keep the Adventure defaults: one use, and a lifetime of twelve hours.
 *
 * @sample io.github.lmliam.kotventure.core.event.clickOptionsSample
 */
@KotventureDslMarker
public interface ClickOptionsScope {
    /**
     * The [uses] count that permits an unlimited number of clicks.
     * It gives [ClickCallback.UNLIMITED_USES] a clearer name.
     */
    public val unlimited: Int
        get() = ClickCallback.UNLIMITED_USES

    /**
     * Sets how many times a player can click the callback. The default is one use.
     *
     * @throws IllegalStateException when the use count is already set in this block.
     * @throws IllegalArgumentException when [count] is neither positive nor [unlimited].
     */
    public fun uses(count: Int)

    /**
     * Sets how long the callback stays clickable. The default is
     * [Adventure's twelve hours][net.kyori.adventure.text.event.ClickCallback.DEFAULT_LIFETIME].
     *
     * @throws IllegalStateException when the lifetime is already set in this block.
     * @throws IllegalArgumentException when [duration] is not positive.
     */
    public fun lifetime(duration: Duration)
}
