package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.emptyComponent
import net.kyori.adventure.audience.Audience

/**
 * Builds a player list (tab list) header and footer from a [TabListScope] block and sends them to
 * this [Audience] via [Audience.sendPlayerListHeaderAndFooter].
 *
 * Adventure has a single primitive that always sets **both** sides. An unset slot in this block
 * is sent as empty and therefore **clears** any previous value on that side. Set at least one of
 * `header` or `footer` (either alone is fine — the other is sent empty). To clear both sides
 * explicitly, set both to [emptyComponent].
 *
 * Works for a player, the console, or a forwarding audience. An audience without a player-list surface ignores it.
 *
 * @throws IllegalStateException when the block sets neither slot, or sets any slot twice.
 * @sample io.github.lmliam.kotventure.core.audience.tabListSample
 */
public fun Audience.tabList(init: TabListScope.() -> Unit) {
    TabListBuilder().apply(init).sendTo(this)
}
