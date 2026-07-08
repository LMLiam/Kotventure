package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.audience.Audience

/**
 * Builds a component from a Kotventure component DSL block and sends it to this [Audience] as a
 * system chat message.
 *
 * Works for any audience — a player, the console, or a forwarding audience over many members.
 */
public fun Audience.message(init: ComponentScope.() -> Unit): Unit = sendMessage(component(init))
