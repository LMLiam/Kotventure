package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent

/**
 * Returns a matcher that compares the keybind identifier with [expected].
 */
public fun haveKeybind(expected: String): Matcher<KeybindComponent> =
    Matcher { value ->
        val actual = value.keybind()
        MatcherResult(
            actual == expected,
            { "Expected keybind <$expected>, but was <$actual>." },
            { "Expected keybind not to be <$expected>." },
        )
    }

/**
 * Verifies that this component is a [KeybindComponent].
 *
 * @return this component as a [KeybindComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeKeybindComponent(): KeybindComponent = asComponentType("keybind")

/**
 * Verifies that this keybind component has the identifier [expected].
 */
public infix fun KeybindComponent.shouldHaveKeybind(expected: String): KeybindComponent =
    apply {
        this should haveKeybind(expected)
    }
