package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import kotlin.time.Duration

/**
 * Configures a title's fade-in, stay, and fade-out durations.
 *
 * Any slot left unset falls back to Adventure's default for that slot
 * (`Title.DEFAULT_TIMES`).
 */
@KotventureDslMarker
public interface TitleTimesScope {
    /**
     * Sets how long the title takes to fade in.
     *
     * @throws IllegalStateException when fade-in is already set in this block.
     */
    public fun fadeIn(duration: Duration)

    /**
     * Sets how long the title stays fully visible.
     *
     * @throws IllegalStateException when stay is already set in this block.
     */
    public fun stay(duration: Duration)

    /**
     * Sets how long the title takes to fade out.
     *
     * @throws IllegalStateException when fade-out is already set in this block.
     */
    public fun fadeOut(duration: Duration)
}
