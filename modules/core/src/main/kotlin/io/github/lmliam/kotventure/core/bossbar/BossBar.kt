package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar

/**
 * Builds an Adventure [BossBar] from a [BossBarScope] block without showing it.
 *
 * Use this when one bar is shared across audiences or held for later
 * [io.github.lmliam.kotventure.core.audience.show] /
 * [io.github.lmliam.kotventure.core.audience.hide]. For a one-shot build-and-show, prefer
 * [io.github.lmliam.kotventure.core.audience.bossBar].
 *
 * @throws IllegalStateException when `name` is missing or any slot/flag is set twice.
 * @throws IllegalArgumentException when `progress` is outside `0f..1f`.
 * @sample io.github.lmliam.kotventure.core.bossbar.bossBarSample
 */
public fun bossBar(init: BossBarScope.() -> Unit): BossBar = BossBarBuilder().apply(init).build()
