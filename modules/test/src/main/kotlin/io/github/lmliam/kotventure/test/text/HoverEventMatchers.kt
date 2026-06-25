package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.HoverEvent

/**
 * Matches a component whose root hover event equals [expected]. Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun haveHoverEvent(expected: HoverEvent<*>): Matcher<Component> =
    Matcher { value ->
        val actual = value.hoverEvent()
        MatcherResult(
            actual == expected,
            { "Expected hover event <$expected>, but was <${actual ?: "null"}>." },
            { "Expected hover event not to be <$expected>." },
        )
    }

/**
 * Matches a component whose root hover event action is [expected].
 */
public fun haveHoverAction(expected: HoverEvent.Action<*>): Matcher<Component> =
    Matcher { value ->
        val actual = value.hoverEvent()?.action()
        MatcherResult(
            actual == expected,
            { "Expected hover action <$expected>, but was <${actual ?: "null"}>." },
            { "Expected hover action not to be <$expected>." },
        )
    }

/**
 * Matches a component whose root hover event shows the text payload [expected].
 */
public fun haveHoverText(expected: ComponentLike): Matcher<Component> =
    Matcher { value ->
        val payload = value.hoverEvent()?.value()
        val actual = payload as? Component
        val expectedComponent = expected.asComponent()
        MatcherResult(
            actual == expectedComponent,
            {
                "Expected hover text payload <$expectedComponent>, but was <${actual ?: hoverPayloadDescription(
                payload,
            )}>."
            },
            { "Expected hover text payload not to be <$expectedComponent>." },
        )
    }

/**
 * Matches a component whose root hover event shows the item payload [expected].
 */
public fun haveHoverItem(expected: HoverEvent.ShowItem): Matcher<Component> =
    Matcher { value ->
        val payload = value.hoverEvent()?.value()
        val actual = payload as? HoverEvent.ShowItem
        MatcherResult(
            actual == expected,
            { "Expected hover item payload <$expected>, but was <${actual ?: hoverPayloadDescription(payload)}>." },
            { "Expected hover item payload not to be <$expected>." },
        )
    }

/**
 * Matches a component whose root hover event shows the entity payload [expected].
 */
public fun haveHoverEntity(expected: HoverEvent.ShowEntity): Matcher<Component> =
    Matcher { value ->
        val payload = value.hoverEvent()?.value()
        val actual = payload as? HoverEvent.ShowEntity
        MatcherResult(
            actual == expected,
            { "Expected hover entity payload <$expected>, but was <${actual ?: hoverPayloadDescription(payload)}>." },
            { "Expected hover entity payload not to be <$expected>." },
        )
    }

/**
 * Matches a component that has no root hover event.
 */
public fun haveNoHoverEvent(): Matcher<Component> =
    Matcher { value ->
        val actual = value.hoverEvent()
        MatcherResult(
            actual == null,
            { "Expected hover event to be absent, but was <$actual>." },
            { "Expected hover event to be present." },
        )
    }

/**
 * Asserts that this component has exactly [expected] as its root hover event.
 */
public infix fun Component.shouldHaveHoverEvent(expected: HoverEvent<*>): Component =
    apply {
        this should haveHoverEvent(expected)
    }

/**
 * Asserts that this component has [expected] as its root hover event action.
 */
public infix fun Component.shouldHaveHoverAction(expected: HoverEvent.Action<*>): Component =
    apply {
        this should haveHoverAction(expected)
    }

/**
 * Asserts that this component has [expected] as its root text hover payload.
 */
public infix fun Component.shouldHaveHoverText(expected: ComponentLike): Component =
    apply {
        this should haveHoverText(expected)
    }

/**
 * Asserts that this component has [expected] as its root item hover payload.
 */
public infix fun Component.shouldHaveHoverItem(expected: HoverEvent.ShowItem): Component =
    apply {
        this should haveHoverItem(expected)
    }

/**
 * Asserts that this component has [expected] as its root entity hover payload.
 */
public infix fun Component.shouldHaveHoverEntity(expected: HoverEvent.ShowEntity): Component =
    apply {
        this should haveHoverEntity(expected)
    }

/**
 * Asserts that this component has no root hover event.
 */
public fun Component.shouldNotHaveHoverEvent(): Component =
    apply {
        this should haveNoHoverEvent()
    }

private fun hoverPayloadDescription(payload: Any?): String =
    when (payload) {
        null -> "no hover event"
        is Component -> "text payload <$payload>"
        is HoverEvent.ShowItem -> "item payload <$payload>"
        is HoverEvent.ShowEntity -> "entity payload <$payload>"
        else -> payload.toString()
    }
