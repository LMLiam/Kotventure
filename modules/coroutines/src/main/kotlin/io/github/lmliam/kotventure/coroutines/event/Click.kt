package io.github.lmliam.kotventure.coroutines.event

import io.github.lmliam.kotventure.core.event.ClickScope
import io.github.lmliam.kotventure.core.event.click
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import kotlin.time.Duration

/**
 * Applies a click event that runs a suspending [function] when a player clicks.
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * does nothing after the cancellation of [scope]. A failure in [function] goes to the scope's exception handling.
 *
 * @throws IllegalStateException when the click event is already set in this block.
 * @sample io.github.lmliam.kotventure.coroutines.event.clickSample
 */
public fun ClickScope.click(
    scope: CoroutineScope,
    function: suspend (Audience) -> Unit,
): Unit = click { callback { audience -> scope.launch { function(audience) } } }

/**
 * Applies a click event that runs a suspending [function] with [uses] and [lifetime].
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * does nothing after the cancellation of [scope]. A failure in [function] goes to the scope's exception handling.
 *
 * @throws IllegalStateException when the click event is already set in this block.
 * @throws IllegalArgumentException when Adventure rejects [uses] or [lifetime].
 * @sample io.github.lmliam.kotventure.coroutines.event.clickUsesLifetimeSample
 */
public fun ClickScope.click(
    scope: CoroutineScope,
    uses: Int,
    lifetime: Duration,
    function: suspend (Audience) -> Unit,
): Unit = click { callback(uses, lifetime) { audience -> scope.launch { function(audience) } } }

/**
 * Applies a click event that runs a suspending [function] with prebuilt [options].
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * does nothing after the cancellation of [scope]. A failure in [function] goes to the scope's exception handling.
 *
 * @throws IllegalStateException when the click event is already set in this block.
 * @sample io.github.lmliam.kotventure.coroutines.event.clickOptionsSample
 */
public fun ClickScope.click(
    scope: CoroutineScope,
    options: ClickCallback.Options,
    function: suspend (Audience) -> Unit,
): Unit = click { callback(options) { audience -> scope.launch { function(audience) } } }

/**
 * Applies a click event that runs a suspending [function] in the [CoroutineScope] from the context.
 *
 * Any implicit [CoroutineScope] satisfies the context. For example, a plugin class that implements [CoroutineScope]
 * supplies it automatically. The lambda must name its clicker parameter. A lambda without a parameter selects the
 * [ClickScope.click] action builder instead. This function has the same launch contracts as [click] with an explicit
 * scope parameter.
 *
 * @throws IllegalStateException when the click event is already set in this block.
 * @sample io.github.lmliam.kotventure.coroutines.event.contextClickSample
 */
context(scope: CoroutineScope)
public fun ClickScope.click(function: suspend (Audience) -> Unit): Unit =
    click { callback { audience -> scope.launch { function(audience) } } }

/**
 * Applies a click event that runs a suspending [function] with [uses] and [lifetime] in the [CoroutineScope] from
 * the context.
 *
 * Any implicit [CoroutineScope] satisfies the context. For example, a plugin class that implements [CoroutineScope]
 * supplies it automatically. This function has the same launch contracts as [click] with an explicit scope parameter.
 *
 * @throws IllegalStateException when the click event is already set in this block.
 * @throws IllegalArgumentException when Adventure rejects [uses] or [lifetime].
 * @sample io.github.lmliam.kotventure.coroutines.event.contextClickSample
 */
context(scope: CoroutineScope)
public fun ClickScope.click(
    uses: Int,
    lifetime: Duration,
    function: suspend (Audience) -> Unit,
): Unit = click { callback(uses, lifetime) { audience -> scope.launch { function(audience) } } }

/**
 * Applies a click event that runs a suspending [function] with prebuilt [options] in the [CoroutineScope] from the
 * context.
 *
 * Any implicit [CoroutineScope] satisfies the context. For example, a plugin class that implements [CoroutineScope]
 * supplies it automatically. This function has the same launch contracts as [click] with an explicit scope parameter.
 *
 * @throws IllegalStateException when the click event is already set in this block.
 * @sample io.github.lmliam.kotventure.coroutines.event.contextClickSample
 */
context(scope: CoroutineScope)
public fun ClickScope.click(
    options: ClickCallback.Options,
    function: suspend (Audience) -> Unit,
): Unit = click { callback(options) { audience -> scope.launch { function(audience) } } }

/**
 * Creates a reusable click event that runs a suspending [function] when a player clicks.
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * does nothing after the cancellation of [scope]. A failure in [function] goes to the scope's exception handling. This
 * function does not attach the event to a component.
 *
 * @sample io.github.lmliam.kotventure.coroutines.event.reusableClickSample
 */
public fun click(
    scope: CoroutineScope,
    function: suspend (Audience) -> Unit,
): ClickEvent<*> = click { callback { audience -> scope.launch { function(audience) } } }

/**
 * Creates a reusable click event that runs a suspending [function] with [uses] and [lifetime].
 *
 * This function has the same launch contracts as [click] with a scope and function. It does not attach the event to
 * a component.
 *
 * @throws IllegalArgumentException when Adventure rejects [uses] or [lifetime].
 * @sample io.github.lmliam.kotventure.coroutines.event.reusableClickSample
 */
public fun click(
    scope: CoroutineScope,
    uses: Int,
    lifetime: Duration,
    function: suspend (Audience) -> Unit,
): ClickEvent<*> = click { callback(uses, lifetime) { audience -> scope.launch { function(audience) } } }

/**
 * Creates a reusable click event that runs a suspending [function] with prebuilt [options].
 *
 * This function has the same launch contracts as [click] with a scope and function. It does not attach the event to
 * a component.
 *
 * @sample io.github.lmliam.kotventure.coroutines.event.reusableClickSample
 */
public fun click(
    scope: CoroutineScope,
    options: ClickCallback.Options,
    function: suspend (Audience) -> Unit,
): ClickEvent<*> = click { callback(options) { audience -> scope.launch { function(audience) } } }
