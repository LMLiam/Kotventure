package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key

/**
 * The advancement-to-condition entries of one vanilla `advancements={...}` selector argument.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorAdvancementsSample
 */
@KotventureDslMarker
public sealed interface SelectorAdvancementsScope {
    /**
     * Requires this advancement to be complete (`true`) or incomplete (`false`):
     * `key("minecraft", "story/smelt_iron") eq true`.
     *
     * @throws IllegalStateException if this advancement already has a condition
     */
    public infix fun Key.eq(completed: Boolean)

    /**
     * Requires per-criterion completion states within this advancement:
     * `key("my_pack", "boss") eq { "kill_dragon" eq true }`. An empty block renders the valid
     * vanilla form `{}`.
     *
     * @throws IllegalStateException if this advancement already has a condition
     */
    public infix fun Key.eq(criteria: AdvancementCriteriaScope.() -> Unit)
}
