package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.ClickCallback

/**
 * Creates callback limits from the slots that [init] sets.
 *
 * Slots that [init] does not set keep the Adventure defaults: one use, and a lifetime of twelve hours.
 *
 * @throws IllegalStateException when [init] sets a slot more than once.
 * @throws IllegalArgumentException when [init] supplies an invalid use count or lifetime.
 * @sample io.github.lmliam.kotventure.core.event.clickOptionsSample
 */
public fun clickOptions(init: ClickOptionsScope.() -> Unit): ClickCallback.Options =
    ClickOptionsBuilder()
        .apply(init)
        .build()
