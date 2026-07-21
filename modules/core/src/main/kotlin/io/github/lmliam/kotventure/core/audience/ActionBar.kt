package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.audience.Audience

/**
 * Creates a component from [init] and sends it to this [Audience]'s action bar.
 *
 * The action bar is the temporary line above a player's hotbar. To send a persistent chat line, use [message].
 *
 * Works for a player, the console, or a forwarding audience. An audience without an action bar ignores it.
 *
 * @throws IllegalStateException when [init] assigns a write-once component slot more than once.
 * @sample io.github.lmliam.kotventure.core.audience.actionBarSample
 */
public fun Audience.actionBar(init: ComponentScope.() -> Unit): Unit = sendActionBar(component(init))
