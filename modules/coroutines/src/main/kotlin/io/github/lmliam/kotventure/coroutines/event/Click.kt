package io.github.lmliam.kotventure.coroutines.event

import io.github.lmliam.kotventure.core.event.ClickOptionsScope
import io.github.lmliam.kotventure.core.event.ClickScope
import io.github.lmliam.kotventure.core.event.click
import io.github.lmliam.kotventure.core.event.clickOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent

private fun CoroutineScope.launching(function: suspend (Audience) -> Unit): ClickCallback<Audience> =
    ClickCallback { audience -> launch { function(audience) } }

/**
 * Applies a click event that runs a suspending [function] when a player clicks.
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * does nothing after the cancellation of [scope]. A failure in [function] goes to the scope's exception handling.
 *
 * Set callback limits in [options]. Unset slots keep the Adventure defaults: one use, and a lifetime of twelve hours.
 *
 * @throws IllegalStateException when the click event is already set in this block, or when [options] sets a slot
 *         more than once.
 * @throws IllegalArgumentException when [options] supplies an invalid use count or lifetime.
 * @sample io.github.lmliam.kotventure.coroutines.event.clickSample
 * @sample io.github.lmliam.kotventure.coroutines.event.clickOptionsSample
 */
public fun ClickScope.click(
    scope: CoroutineScope,
    options: ClickOptionsScope.() -> Unit = {},
    function: suspend (Audience) -> Unit,
): Unit = click { callback(clickOptions(options), scope.launching(function)) }

/**
 * Applies a click event that runs a suspending [function] in the [CoroutineScope] from the context.
 *
 * Any implicit [CoroutineScope] satisfies the context. For example, a plugin class that implements [CoroutineScope]
 * supplies it automatically. The lambda must name its clicker parameter. A lambda without a parameter selects the
 * [ClickScope.click] action builder instead. This function has the same launch and options contracts as [click] with
 * an explicit scope parameter.
 *
 * @throws IllegalStateException when the click event is already set in this block, or when [options] sets a slot
 *         more than once.
 * @throws IllegalArgumentException when [options] supplies an invalid use count or lifetime.
 * @sample io.github.lmliam.kotventure.coroutines.event.contextClickSample
 * @sample io.github.lmliam.kotventure.coroutines.event.contextClickOptionsSample
 */
context(scope: CoroutineScope)
public fun ClickScope.click(
    options: ClickOptionsScope.() -> Unit = {},
    function: suspend (Audience) -> Unit,
): Unit = click { callback(clickOptions(options), scope.launching(function)) }

/**
 * Creates a reusable click event that runs a suspending [function] when a player clicks.
 *
 * This function has the same launch and options contracts as [ClickScope.click] with an explicit scope parameter.
 * It does not attach the event to a component.
 *
 * @throws IllegalStateException when [options] sets a slot more than once.
 * @throws IllegalArgumentException when [options] supplies an invalid use count or lifetime.
 * @sample io.github.lmliam.kotventure.coroutines.event.reusableClickSample
 */
public fun click(
    scope: CoroutineScope,
    options: ClickOptionsScope.() -> Unit = {},
    function: suspend (Audience) -> Unit,
): ClickEvent<*> = click { callback(clickOptions(options), scope.launching(function)) }
