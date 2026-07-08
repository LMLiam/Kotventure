package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.audience.Audience

/**
 * Builds a component from a Kotventure component DSL block and shows it in this [Audience]'s
 * action bar.
 *
 * The action bar is the transient line above a player's hotbar; to send a persistent chat line
 * instead, use [message].
 *
 * Works for any audience — a player, the console, or a forwarding audience over many members;
 * audiences without an action bar ignore it.
 *
 * @sample io.github.lmliam.kotventure.core.audience.actionBarSample
 */
public fun Audience.actionBar(init: ComponentScope.() -> Unit): Unit = sendActionBar(component(init))
