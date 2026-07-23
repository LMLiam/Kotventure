package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope

/**
 * Modifies the matched text inside a [ReplaceScope.modify] block.
 *
 * This scope starts with the [TextScope] content, style, and children of a text component that Adventure
 * pre-populates from [match]. Change [TextScope.content] to keep only part of the match, apply a style, or append
 * children. Leave the content unchanged to keep the matched text as-is.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceModifySample
 */
@KotventureDslMarker
public interface ModifyScope : TextScope {
    /** The snapshotted match that this block modifies. */
    public val match: TextMatch
}
