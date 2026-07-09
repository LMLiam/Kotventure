package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.bossbar.BossBarScope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import io.github.lmliam.kotventure.core.bossbar.bossBar as buildBossBar

/**
 * Shows [bar] to this [Audience].
 *
 * Type-overloaded [show] so future showable types (books, …) can share the verb.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceShowHideBossBarSample
 */
public fun Audience.show(bar: BossBar): Unit = showBossBar(bar)

/**
 * Hides [bar] from this [Audience].
 *
 * Type-overloaded [hide] so future hideable types can share the verb.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceShowHideBossBarSample
 */
public fun Audience.hide(bar: BossBar): Unit = hideBossBar(bar)

/**
 * Builds a [BossBar] from [init], shows it on this [Audience], and returns it for later
 * [hide] or live updates.
 *
 * Works for any audience — a player, the console, or a forwarding audience over many members;
 * audiences without a boss-bar surface ignore it.
 *
 * @throws IllegalStateException when `name` is missing or any slot/flag is set twice.
 * @throws IllegalArgumentException when `progress` is outside `0f..1f`.
 * @sample io.github.lmliam.kotventure.core.audience.audienceBossBarSample
 */
public fun Audience.bossBar(init: BossBarScope.() -> Unit): BossBar = buildBossBar(init).also(::show)
