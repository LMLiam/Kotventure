package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration

/**
 * Configures labels, an indicator, and callback limits for a [Pagination] navigation row.
 *
 * Defaults when a slot is left unset: `« Previous` and `Next »` labels, a `page/pageCount`
 * indicator, unlimited callback uses, and Adventure's default callback lifetime of twelve hours.
 *
 * @sample io.github.lmliam.kotventure.core.pagination.audiencePaginateSample
 */
@KotventureDslMarker
public interface NavScope {
    /**
     * The [uses] count that lets a navigation button be clicked any number of times.
     * [ClickCallback.UNLIMITED_USES], and what applies when [uses] is never called.
     */
    public val unlimited: Int
        get() = ClickCallback.UNLIMITED_USES

    /**
     * Creates and sets the preceding-page label from a component DSL block.
     *
     * @throws IllegalStateException when the previous label is already set in this block.
     */
    public fun previous(init: ComponentScope.() -> Unit)

    /**
     * Sets the label of the button that navigates to the preceding page.
     *
     * @throws IllegalStateException when the previous label is already set in this block.
     */
    public fun <C : ComponentLike> previous(component: C)

    /**
     * Creates and sets the following-page label from a component DSL block.
     *
     * @throws IllegalStateException when the next label is already set in this block.
     */
    public fun next(init: ComponentScope.() -> Unit)

    /**
     * Sets the label of the button that navigates to the following page.
     *
     * @throws IllegalStateException when the next label is already set in this block.
     */
    public fun <C : ComponentLike> next(component: C)

    /**
     * Shows the default `page/pageCount` indicator when [shown], or hides the indicator entirely
     * when not.
     *
     * @throws IllegalStateException when the indicator is already set in this block.
     */
    public fun indicator(shown: Boolean)

    /**
     * Sets how the page-position indicator is rendered from the shown page and the page count.
     *
     * @throws IllegalStateException when the indicator is already set in this block.
     */
    public fun indicator(render: (page: Int, pageCount: Int) -> ComponentLike)

    /**
     * Sets how many times each nav button may be clicked. Defaults to [unlimited].
     *
     * @throws IllegalStateException when the use count is already set in this block.
     * @throws IllegalArgumentException when [count] is neither positive nor [unlimited].
     */
    public fun uses(count: Int)

    /**
     * Sets how long each nav button stays clickable after its page is rendered. Defaults to
     * [Adventure's twelve hours][net.kyori.adventure.text.event.ClickCallback.DEFAULT_LIFETIME].
     *
     * @throws IllegalStateException when the lifetime is already set in this block.
     * @throws IllegalArgumentException when [duration] is not positive.
     */
    public fun lifetime(duration: Duration)
}
