package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.ComponentLike

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
     * Configures fade-in, stay, and fade-out durations via [TitleTimesScope].
     *
     * Each timing may be set independently; any left unset uses Adventure's default for that slot
     * (`Title.DEFAULT_TIMES`). When this block is omitted entirely, those same defaults apply.
     *
     * @throws IllegalStateException when times are already configured in this title block, or when
     *   a timing slot is set twice inside [init].
     */
    public fun times(init: TitleTimesScope.() -> Unit)
}
