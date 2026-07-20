package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.audience.Audience

/**
 * Builds a component from a Kotventure component DSL block and shows it in this [Audience]'s
 * action bar.
 *
 * The action bar is the temporary line above a player's hotbar. To send a persistent chat line, use [message].
 *
 * Works for a player, the console, or a forwarding audience. An audience without an action bar ignores it.
 *
 * @sample io.github.lmliam.kotventure.core.audience.actionBarSample
 */
public fun Audience.actionBar(init: ComponentScope.() -> Unit): Unit = sendActionBar(component(init))
