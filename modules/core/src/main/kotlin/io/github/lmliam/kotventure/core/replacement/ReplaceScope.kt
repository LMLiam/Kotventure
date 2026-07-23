package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * Configures one [Component.replace] call.
 *
 * Choose exactly one replacement action: [modify], one form of [replacement], or [remove]. Choose at most one
 * match limit: [once], [times], or [condition]. [insideHoverEvents] is independent of both. Each slot is
 * write-once.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceLiteralSample
 */
@KotventureDslMarker
public interface ReplaceScope {
    /**
     * Sets the replacement to a text component with literal [value], configured by [init].
     *
     * This is the short form for a fixed text replacement. Use [modify] to keep the matched text and only change
     * its style or children. Use the [replacement] block form to compute a different replacement for each match.
     * Kotventure builds the replacement one time. It does not rebuild the value for each match.
     *
     * @throws IllegalStateException when a replacement action is already set in this block.
     */
    public fun replacement(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the replacement to [component] for every match.
     *
     * Kotventure stores [component] one time. It does not rebuild the value for each match.
     *
     * @sample io.github.lmliam.kotventure.core.replacement.replaceComponentSample
     *
     * @throws IllegalStateException when a replacement action is already set in this block.
     */
    public fun <T : ComponentLike> replacement(component: T)

    /**
     * Sets the replacement to the result of [build], computed for each accepted match.
     *
     * [build] receives the snapshotted match through [ReplacementScope.match]. It returns any component, or the
     * scoped [ReplacementScope.remove] to delete this match.
     *
     * @throws IllegalStateException when a replacement action is already set in this block.
     */
    public fun replacement(build: ReplacementScope.() -> ComponentLike?)

    /**
     * Modifies the matched text through [build].
     *
     * [build] starts with a text component that Adventure pre-populates from the match. The block can change the
     * content, the style, and the children of that component without retyping the matched text. The snapshotted
     * match is available as [ModifyScope.match].
     *
     * @throws IllegalStateException when a replacement action is already set in this block.
     */
    public fun modify(build: ModifyScope.() -> Unit)

    /**
     * Deletes every accepted match.
     *
     * @throws IllegalStateException when a replacement action is already set in this block.
     */
    public fun remove()

    /**
     * Replaces only the first match.
     *
     * This form and [times] and [condition] share one write-once slot.
     *
     * @throws IllegalStateException when a match limit is already set in this block.
     */
    public fun once()

    /**
     * Replaces only the first [count] matches.
     *
     * This form and [once] and [condition] share one write-once slot.
     *
     * @sample io.github.lmliam.kotventure.core.replacement.replaceLimitSample
     *
     * @throws IllegalArgumentException when [count] is below `1`.
     * @throws IllegalStateException when a match limit is already set in this block.
     */
    public fun times(count: Int)

    /**
     * Decides the outcome for each match through [predicate].
     *
     * This form and [once] and [times] share one write-once slot.
     *
     * @throws IllegalStateException when a match limit is already set in this block.
     */
    public fun condition(predicate: ConditionScope.() -> MatchAction)

    /**
     * Sets whether a replacement also changes text inside hover events.
     *
     * Adventure applies this setting with a default of `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun insideHoverEvents(replace: Boolean)
}
