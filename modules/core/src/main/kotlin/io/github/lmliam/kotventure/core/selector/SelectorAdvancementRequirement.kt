package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * One advancement entry in an `advancements={...}` argument.
 *
 * @property advancement advancement key
 * @property progress whole-advancement or criterion-level requirement
 */
public data class SelectorAdvancementRequirement(
    public val advancement: Key,
    public val progress: SelectorAdvancementProgress,
)
