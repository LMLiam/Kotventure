package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.ComponentLike
import kotlin.time.Duration

/**
 * Scope for configuring a screen title shown via [Audience.title]: the main [title] line, an
 * optional [subtitle], and optional fade [times].
 *
 * At least one of [title] or [subtitle] must be set before the block ends.
 */
@KotventureDslMarker
public interface TitleScope {
    /**
     * Builds the main title line from a component DSL block.
     *
     * @throws IllegalStateException when the title is already set in this block.
     */
    public fun title(init: ComponentScope.() -> Unit)

    /**
     * Sets the main title line.
     *
     * @throws IllegalStateException when the title is already set in this block.
     */
    public fun <T : ComponentLike> title(component: T)

    /**
     * Builds the subtitle line from a component DSL block.
     *
     * @throws IllegalStateException when the subtitle is already set in this block.
     */
    public fun subtitle(init: ComponentScope.() -> Unit)

    /**
     * Sets the subtitle line.
     *
     * @throws IllegalStateException when the subtitle is already set in this block.
     */
    public fun <T : ComponentLike> subtitle(component: T)

    /**
     * Sets how long the title fades in, stays on screen, and fades out.
     *
     * When not set, Adventure's default timings (`Title.DEFAULT_TIMES`) are used.
     *
     * @throws IllegalStateException when times are already set in this block.
     */
    public fun times(
        fadeIn: Duration,
        stay: Duration,
        fadeOut: Duration,
    )
}
