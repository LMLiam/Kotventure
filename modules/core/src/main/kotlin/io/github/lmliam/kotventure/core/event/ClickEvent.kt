package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import java.time.temporal.TemporalAmount
import kotlin.time.toJavaDuration
import kotlin.time.Duration as KotlinDuration

/**
 * Builds an Adventure click event that opens [url].
 *
 * @throws IllegalArgumentException when Adventure rejects the URL payload.
 */
public fun open(url: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.openUrl(url)

/**
 * Builds an Adventure click event that opens this URL.
 *
 * @throws IllegalArgumentException when Adventure rejects this URL payload.
 */
@JvmName("openString")
public fun String.open(): ClickEvent<ClickEvent.Payload.Text> = open(this)

/**
 * Builds an Adventure click event that opens [url].
 *
 * @throws IllegalArgumentException when Adventure rejects the URL payload.
 */
public fun openUrl(url: String): ClickEvent<ClickEvent.Payload.Text> = open(url)

/**
 * Builds an Adventure click event that opens a local [file] path.
 */
public fun openFile(file: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.openFile(file)

/**
 * Builds an Adventure click event that runs [command].
 */
public fun runCommand(command: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.runCommand(command)

/**
 * Builds an Adventure click event that suggests [command] in chat.
 */
public fun suggestCommand(command: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.suggestCommand(command)

/**
 * Builds an Adventure click event that changes a book to [page].
 *
 * @throws IllegalArgumentException when [page] is less than one.
 */
public fun changePage(page: Int): ClickEvent<ClickEvent.Payload.Int> = ClickEvent.changePage(page)

/**
 * Builds an Adventure click event that copies [text] to the clipboard.
 */
public fun copy(text: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.copyToClipboard(text)

/**
 * Builds an Adventure click event that copies this text to the clipboard.
 */
@JvmName("copyString")
public fun String.copy(): ClickEvent<ClickEvent.Payload.Text> = copy(this)

/**
 * Builds an Adventure click event that copies [text] to the clipboard.
 */
public fun copyToClipboard(text: String): ClickEvent<ClickEvent.Payload.Text> = copy(text)

/**
 * Builds an Adventure server-side callback click event from [function].
 */
public fun callback(function: ClickCallback<Audience>): ClickEvent<*> = ClickEvent.callback(function)

/**
 * Builds an Adventure server-side callback click event from [function] with [uses] and [lifetime].
 */
public fun callback(
    uses: Int,
    lifetime: TemporalAmount,
    function: ClickCallback<Audience>,
): ClickEvent<*> =
    ClickEvent.callback(function) { options ->
        options.uses(uses)
        options.lifetime(lifetime)
    }

/**
 * Builds an Adventure server-side callback click event from [function] with [uses] and [lifetime].
 */
public fun callback(
    uses: Int,
    lifetime: KotlinDuration,
    function: ClickCallback<Audience>,
): ClickEvent<*> = callback(uses, lifetime.toJavaDuration(), function)

/**
 * Builds an Adventure server-side callback click event from [function] with prebuilt [options].
 */
public fun callback(
    options: ClickCallback.Options,
    function: ClickCallback<Audience>,
): ClickEvent<*> = ClickEvent.callback(function, options)

/**
 * Builds an Adventure click event from a typed [action] and [payload].
 *
 * @throws IllegalArgumentException when Adventure rejects the action/payload pair.
 */
public fun <P : ClickEvent.Payload> clickEvent(
    action: ClickEvent.Action<P>,
    payload: P,
): ClickEvent<P> = ClickEvent.clickEvent(action, payload)
