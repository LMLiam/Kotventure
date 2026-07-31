package io.github.lmliam.kotventure.core.replacement

import net.kyori.adventure.text.ComponentLike

internal class ReplacementState(
    override val match: TextMatch,
) : ReplacementScope {
    override val remove: ComponentLike? = null
}
