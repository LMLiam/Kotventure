package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.audience.Audience

/**
 * Creates a component from [init] and sends it to this [Audience] as a system chat message.
 *
 * A system message appears as a plain, unattributed line from the server. To send player-styled chat with a sender name,
 * use [chat].
 *
 * Works for any audience, such as a player, the console, or a forwarding audience over many members.
 *
 * @throws IllegalStateException when [init] assigns a write-once component slot more than once.
 * @sample io.github.lmliam.kotventure.core.audience.messageSample
 */
public fun Audience.message(init: ComponentScope.() -> Unit): Unit = sendMessage(component(init))
