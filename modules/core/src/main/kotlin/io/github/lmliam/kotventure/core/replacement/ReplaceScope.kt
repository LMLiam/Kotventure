package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * Configures one [Component.replace] operation.
 *
 * Set exactly one replacement action: [modify], one form of [replacement], or [remove].
 * Set at most one match policy: [once], [times], or [condition].
 * [insideHoverEvents] is independent of those slots.
 * Every slot is write-once.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceLiteralSample
 */
@KotventureDslMarker
public interface ReplaceScope {
    /**
     * Replaces every accepted match with a text component containing [value] and
     * configured by [init].
     *
     * The replacement is built once.
     * Use [modify] to retain and edit the matched text, or the receiver-block
     * form of [replacement] to calculate a different component for each accepted match.
     *
     * @throws IllegalStateException when this block already contains a replacement action.
     */
    public fun replacement(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Replaces every accepted match with [component].
     *
     * Kotventure converts and stores the component once rather than rebuilding it for
    each match.
     *
     * @sample io.github.lmliam.kotventure.core.replacement.replaceComponentSample
     *
     * @throws IllegalStateException when this block already contains a replacement
    action.
     */
    public fun replacement(component: ComponentLike)

    /**
     * Replaces each accepted match with the component returned by [init].
     *
     * [init] is evaluated separately for every accepted match. Its receiver exposes the
    snapshotted match through
     * [ReplacementScope.match]. Return any [ComponentLike], or return
    [ReplacementScope.remove] to delete only the
     * current match.
     *
     * @throws IllegalStateException when this block already contains a replacement
    action.
     */
    public fun replacement(init: ReplacementScope.() -> ComponentLike?)

    /**
     * Modifies each accepted match through [init].
     *
     * Adventure supplies a text-component builder pre-populated with the matched text.
    [init] can change its content,
     * style, and children without reconstructing the match. The snapshotted match is
    available through
     * [ModifyScope.match].
     *
     * @throws IllegalStateException when this block already contains a replacement
    action.
     */
    public fun modify(init: ModifyScope.() -> Unit)

    /**
     * Deletes every accepted match.
     *
     * @throws IllegalStateException when this block already contains a replacement
    action.
     */
    public fun remove()

    /**
     * Replaces only the first match.
     *
     * This function shares one write-once match-policy slot with [times] and [condition].
     *
     * @throws IllegalStateException when this block already contains a match policy.
     */
    public fun once()

    /**
     * Replaces only the first [count] matches.
     *
     * This function shares one write-once match-policy slot with [once] and [condition].
     *
     * @sample io.github.lmliam.kotventure.core.replacement.replaceLimitSample
     *
     * @throws IllegalArgumentException when [count] is less than `1`.
     * @throws IllegalStateException when this block already contains a match policy.
     */
    public fun times(count: Int)

    /**
     * Uses [predicate] to decide the outcome of each match.
     *
     * This function shares one write-once match-policy slot with [once] and [times].
     *
     * @throws IllegalStateException when this block already contains a match policy.
     */
    public fun condition(predicate: ConditionScope.() -> MatchAction)

    /**
     * Sets whether replacement also searches text inside hover events.
     *
     * Adventure enables this behaviour by default.
     *
     * @throws IllegalStateException when this setting is already present in the block.
     */
    public fun insideHoverEvents(enabled: Boolean)
}
