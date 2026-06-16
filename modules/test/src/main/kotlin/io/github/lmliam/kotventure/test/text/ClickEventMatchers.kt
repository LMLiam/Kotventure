package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent

/**
 * Matches a component whose root click event equals [expected]. Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun haveClickEvent(expected: ClickEvent<*>): Matcher<Component> =
    Matcher { value ->
        val actual = value.clickEvent()
        MatcherResult(
            actual == expected,
            { "Expected click event <$expected>, but was <${actual ?: "null"}>." },
            { "Expected click event not to be <$expected>." },
        )
    }

/**
 * Matches a component whose root click event action is [expected].
 */
public fun haveClickAction(expected: ClickEvent.Action<*>): Matcher<Component> =
    Matcher { value ->
        val actual = value.clickEvent()?.action()
        MatcherResult(
            actual == expected,
            { "Expected click action <$expected>, but was <${actual ?: "null"}>." },
            { "Expected click action not to be <$expected>." },
        )
    }

/**
 * Matches a component whose root click event carries the text payload [expected].
 */
public fun haveClickTextPayload(expected: String): Matcher<Component> =
    Matcher { value ->
        val payload = value.clickEvent()?.payload()
        val actual = (payload as? ClickEvent.Payload.Text)?.value()
        MatcherResult(
            actual == expected,
            { "Expected click text payload <$expected>, but was <${actual ?: payloadDescription(payload)}>." },
            { "Expected click text payload not to be <$expected>." },
        )
    }

/**
 * Matches a component whose root click event carries the integer payload [expected].
 */
public fun haveClickIntPayload(expected: Int): Matcher<Component> =
    Matcher { value ->
        val payload = value.clickEvent()?.payload()
        val actual = (payload as? ClickEvent.Payload.Int)?.integer()
        MatcherResult(
            actual == expected,
            { "Expected click integer payload <$expected>, but was <${actual ?: payloadDescription(payload)}>." },
            { "Expected click integer payload not to be <$expected>." },
        )
    }

/**
 * Matches a component that has no root click event.
 */
public fun haveNoClickEvent(): Matcher<Component> =
    Matcher { value ->
        val actual = value.clickEvent()
        MatcherResult(
            actual == null,
            { "Expected click event to be absent, but was <$actual>." },
            { "Expected click event to be present." },
        )
    }

/**
 * Asserts that this component has exactly [expected] as its root click event.
 */
public infix fun Component.shouldHaveClickEvent(expected: ClickEvent<*>): Component =
    apply {
        this should haveClickEvent(expected)
    }

/**
 * Asserts that this component has [expected] as its root click event action.
 */
public infix fun Component.shouldHaveClickAction(expected: ClickEvent.Action<*>): Component =
    apply {
        this should haveClickAction(expected)
    }

/**
 * Asserts that this component has [expected] as its root click event text payload.
 */
public infix fun Component.shouldHaveClickTextPayload(expected: String): Component =
    apply {
        this should haveClickTextPayload(expected)
    }

/**
 * Asserts that this component has [expected] as its root click event integer payload.
 */
public infix fun Component.shouldHaveClickIntPayload(expected: Int): Component =
    apply {
        this should haveClickIntPayload(expected)
    }

/**
 * Asserts that this component has no root click event.
 */
public fun Component.shouldNotHaveClickEvent(): Component =
    apply {
        this should haveNoClickEvent()
    }

private fun payloadDescription(payload: ClickEvent.Payload?): String =
    when (payload) {
        null -> "no click event"
        is ClickEvent.Payload.Text -> "text payload <${payload.value()}>"
        is ClickEvent.Payload.Int -> "integer payload <${payload.integer()}>"
        else -> payload.toString()
    }
