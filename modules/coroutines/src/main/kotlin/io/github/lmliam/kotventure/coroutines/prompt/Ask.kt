package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.component.component
import kotlinx.coroutines.suspendCancellableCoroutine
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

private val defaultLifetime: Duration = ClickCallback.DEFAULT_LIFETIME.toKotlinDuration()

/**
 * Sends [prompt] to this audience as a system message and waits.
 *
 * The function resumes with the value of the first option that a player clicks. A later click does nothing.
 * Cancellation of the calling coroutine stops the prompt. Each click after cancellation does nothing.
 *
 * The function has no timeout parameter. Set a deadline with `withTimeout` or `withTimeoutOrNull`.
 *
 * Each member of an audience with many members receives the message. The first member to click claims the answer.
 * This behaviour is correct for a "first to click" broadcast.
 *
 * The awaiting coroutine is the scope of this call. Thus, the function has no
 * [CoroutineScope][kotlinx.coroutines.CoroutineScope] parameter.
 *
 * @param prompt the question and its options.
 * @param lifetime how long the option buttons stay clickable. The default is
 *   [Adventure's twelve hours][net.kyori.adventure.text.event.ClickCallback.DEFAULT_LIFETIME].
 * @throws IllegalStateException when [prompt] declares no option, applies a click event in an option label, or assigns
 *   a write-once slot two times. The function sends nothing when it fails.
 * @throws IllegalArgumentException when [lifetime] is not positive.
 * @sample io.github.lmliam.kotventure.coroutines.prompt.askPromptSample
 */
public suspend fun <T> Audience.ask(
    prompt: Prompt<T>,
    lifetime: Duration = defaultLifetime,
): T =
    suspendCancellableCoroutine { continuation ->
        sendMessage(promptComponent(prompt.build, PendingPrompt(continuation), lifetime))
    }

/**
 * Builds a prompt from [build] and asks it.
 *
 * Use this form for a question that one call site asks. Use the [Prompt] overload for a question that more than one
 * call site asks. Refer to that overload for the complete contract.
 *
 * @param lifetime how long the option buttons stay clickable. The default is
 *   [Adventure's twelve hours][net.kyori.adventure.text.event.ClickCallback.DEFAULT_LIFETIME].
 * @param build appends the question text and its options to the prompt scope.
 * @throws IllegalStateException when [build] declares no option, applies a click event in an option label, or assigns
 *   a write-once slot two times. The function sends nothing when it fails.
 * @throws IllegalArgumentException when [lifetime] is not positive.
 * @sample io.github.lmliam.kotventure.coroutines.prompt.askSample
 */
public suspend fun <T> Audience.ask(
    lifetime: Duration = defaultLifetime,
    build: PromptScope<T>.() -> Unit,
): T = ask(Prompt(build), lifetime)

private fun <T> Audience.promptComponent(
    build: PromptScope<T>.() -> Unit,
    pending: PendingPrompt<T>,
    lifetime: Duration,
): Component =
    component {
        val prompt = PromptBuilder(this, this@promptComponent, pending, lifetime)
        build(prompt)
        check(prompt.hasOptions) {
            "ask { ... } must declare at least one option(value) { ... }, or it can never resume."
        }
    }
