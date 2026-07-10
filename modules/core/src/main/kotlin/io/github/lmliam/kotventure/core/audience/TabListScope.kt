package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for configuring the player list (tab list) header and footer shown via
 * [Audience.tabList].
 *
 * At least one of [header] or [footer] must be set before the block ends.
 */
@KotventureDslMarker
public interface TabListScope {
    /**
     * Builds the tab list header from a component DSL block.
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
     * Builds the tab list footer from a component DSL block.
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
