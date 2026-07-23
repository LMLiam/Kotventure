package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Computes the replacement for one accepted match inside a [ReplaceScope.replacement] block.
 *
 * The block returns any [ComponentLike] computed from [match], or the scoped [remove] to delete this match.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceRemoveSample
 */
@KotventureDslMarker
public interface ReplacementScope {
    /** The snapshotted match that this block replaces. */
    public val match: TextMatch

    /** Deletes this match. Return this value from the block instead of a component. */
    public val remove: ComponentLike?
}
