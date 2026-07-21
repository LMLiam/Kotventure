package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.emptyComponent
import net.kyori.adventure.audience.Audience

/**
 * Creates a player-list header and footer from [init] and sends both values to
 * this [Audience] via [Audience.sendPlayerListHeaderAndFooter].
 *
 * Adventure's operation always sets **both** values. An unset slot in this block
 * is sent as empty and therefore **clears** any previous value on that side. Set at least one of
 * `header` or `footer`. If you set only one value, the operation sends an empty value for the other side. To clear both sides
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
