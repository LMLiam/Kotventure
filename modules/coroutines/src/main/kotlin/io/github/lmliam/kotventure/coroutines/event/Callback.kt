package io.github.lmliam.kotventure.coroutines.event

import io.github.lmliam.kotventure.core.event.ClickActionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration

/**
 * Selects a server-side callback click action that runs a suspending [function].
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * after [scope] is cancelled does nothing. A failure in [function] goes to the scope's exception handling.
 *
 * @sample io.github.lmliam.kotventure.coroutines.event.callbackSample
 */
public fun ClickActionScope.callback(
    scope: CoroutineScope,
    function: suspend (Audience) -> Unit,
): Unit = callback { audience -> scope.launch { function(audience) } }

/**
 * Selects a server-side callback click action that runs a suspending [function] with [uses] and [lifetime].
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * after [scope] is cancelled does nothing. A failure in [function] goes to the scope's exception handling.
 *
 * @throws IllegalArgumentException when Adventure rejects [uses] or [lifetime].
 * @sample io.github.lmliam.kotventure.coroutines.event.callbackUsesLifetimeSample
 */
public fun ClickActionScope.callback(
    scope: CoroutineScope,
    uses: Int,
    lifetime: Duration,
    function: suspend (Audience) -> Unit,
): Unit = callback(uses, lifetime) { audience -> scope.launch { function(audience) } }

/**
 * Selects a server-side callback click action that runs a suspending [function] with prebuilt [options].
 *
 * A click launches [function] into [scope]. The body runs on the scope's dispatcher, not the click thread. A click
 * after [scope] is cancelled does nothing. A failure in [function] goes to the scope's exception handling.
 *
 * @sample io.github.lmliam.kotventure.coroutines.event.callbackOptionsSample
 */
public fun ClickActionScope.callback(
    scope: CoroutineScope,
    options: ClickCallback.Options,
    function: suspend (Audience) -> Unit,
): Unit = callback(options) { audience -> scope.launch { function(audience) } }
