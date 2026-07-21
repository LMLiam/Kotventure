package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the player-list header and footer for [Audience.tabList].
 *
 * At least one of [header] or [footer] must be set before the block ends.
 */
@KotventureDslMarker
public interface TabListScope {
    /**
     * Creates and sets the player-list header from a component DSL block.
     *
     * @throws IllegalStateException when the header is already set in this block.
     */
    public fun header(init: ComponentScope.() -> Unit)

    /**
     * Sets the tab list header.
     *
     * @throws IllegalStateException when the header is already set in this block.
     */
    public fun <T : ComponentLike> header(component: T)

    /**
     * Creates and sets the player-list footer from a component DSL block.
     *
     * @throws IllegalStateException when the footer is already set in this block.
     */
    public fun footer(init: ComponentScope.() -> Unit)

    /**
     * Sets the tab list footer.
     *
     * @throws IllegalStateException when the footer is already set in this block.
     */
    public fun <T : ComponentLike> footer(component: T)
}
