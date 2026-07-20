package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.audience.Audience

/**
 * Builds a screen [net.kyori.adventure.title.Title] from a [TitleScope] block and shows it on this
 * [Audience].
 *
 * Set at least one of `title` or `subtitle` (either alone is fine — the other defaults to empty).
 * When `times` is omitted, Adventure's default timings are used. To clear or reset a shown title,
 * call Adventure's [Audience.clearTitle] or [Audience.resetTitle] directly.
 *
 * Works for a player, the console, or a forwarding audience. An audience without a title surface ignores it.
 *
 * @throws IllegalStateException when the block sets neither `title` nor `subtitle`, or sets any
 *   slot twice.
 * @sample io.github.lmliam.kotventure.core.audience.titleSample
 */
public fun Audience.title(init: TitleScope.() -> Unit) {
    showTitle(TitleBuilder().apply(init).build())
}
