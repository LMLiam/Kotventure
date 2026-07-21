package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.bossbar.BossBarScope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import io.github.lmliam.kotventure.core.bossbar.bossBar as buildBossBar

/**
 * Shows [bar] to this [Audience].
 *
 * [BossBar] is mutable. The audience observes later changes to the same bar. An audience without a boss-bar surface
 * ignores the operation.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceShowHideBossBarSample
 */
public fun Audience.show(bar: BossBar): Unit = showBossBar(bar)

/**
 * Hides [bar] from this [Audience].
 *
 * This operation does not dispose of the mutable bar. Other audiences can continue to see it.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceShowHideBossBarSample
 */
public fun Audience.hide(bar: BossBar): Unit = hideBossBar(bar)

/**
 * Creates a [BossBar] from [init], shows it on this [Audience], and returns the same mutable bar for later
 * [hide] or live updates.
 *
 * Works for a player, the console, or a forwarding audience. An audience without a boss-bar surface ignores it.
 *
 * @throws IllegalStateException when `name` is missing or any slot/flag is set twice.
 * @throws IllegalArgumentException when `progress` is outside `0f..1f`.
 * @sample io.github.lmliam.kotventure.core.audience.audienceBossBarSample
 */
public fun Audience.bossBar(init: BossBarScope.() -> Unit): BossBar = buildBossBar(init).also(::show)
