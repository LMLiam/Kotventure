package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.component.component
import kotlinx.coroutines.suspendCancellableCoroutine
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

private val defaultLifetime: Duration =
    ClickCallback.DEFAULT_LIFETIME.toKotlinDuration()

/**
 * Sends [prompt] to this audience as a system message and awaits an option.
 *
 * The first clicked option resumes the calling coroutine with its value and later clicks do nothing.
 * Cancelling the calling coroutine stops the prompt, and later clicks do nothing.
 *
 * Each member of a multi-member audience receives the prompt.
 * The first member to click supplies the answer.
 *
 * @param prompt the reusable prompt to ask.
 * @param lifetime how long option buttons remain clickable.
 * @return the value of the first clicked option.
 * @throws IllegalStateException if the prompt has no options, an option label applies a click event, or
 *         a write-once slot is assigned twice. No message is sent.
 * @throws IllegalArgumentException if [lifetime] is not positive.
 * @sample io.github.lmliam.kotventure.coroutines.prompt.askPromptSample
 */
public suspend fun <T> Audience.ask(
    prompt: Prompt<T>,
    lifetime: Duration = defaultLifetime,
): T = awaitPrompt(prompt.build, lifetime)

/**
 * Builds and sends a one-use prompt to this audience, then awaits an option.
 *
 * Use [Prompt] when the same question is asked from more than once call site.
 *
 * @param lifetime how long option buttons remain clickable.
 * @param build configures the question text and its options.
 * @return the value of the first clicked option.
 * @throws IllegalStateException if [build] declares no options, an option label applies a click event, or
 *         a write-once slot is assigned twice. No message is sent.
 * @throws IllegalArgumentException if [lifetime] is not positive.
 * @sample io.github.lmliam.kotventure.coroutines.prompt.askSample
 */
public suspend fun <T> Audience.ask(
    lifetime: Duration = defaultLifetime,
    build: PromptScope<T>.() -> Unit,
): T = awaitPrompt(build, lifetime)

private suspend fun <T> Audience.awaitPrompt(
    build: PromptScope<T>.() -> Unit,
    lifetime: Duration,
): T =
    suspendCancellableCoroutine { continuation ->
        sendMessage(promptComponent(build, PendingPrompt(continuation), lifetime))
    }

private fun <T> Audience.promptComponent(
    build: PromptScope<T>.() -> Unit,
    pending: PendingPrompt<T>,
    lifetime: Duration,
): Component =
    component {
        val prompt = PromptBuilder(this, this@promptComponent, pending, lifetime)
        build(prompt)

        check(prompt.hasOption) {
            "ask { ... } must declare at least one option(value) { ... }, or it can never resume."
        }
    }
