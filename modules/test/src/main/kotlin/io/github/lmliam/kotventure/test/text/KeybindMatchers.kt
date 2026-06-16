package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent

/**
 * Matches a keybind component whose keybind is [expected]. Combine with `and`/`or` or negate with `shouldNot`.
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
 * Asserts that this component is a [KeybindComponent] and returns it typed.
 */
public fun Component.shouldBeKeybindComponent(): KeybindComponent = asComponentType("keybind")

/**
 * Asserts that this keybind component has [expected] as its keybind.
 */
public infix fun KeybindComponent.shouldHaveKeybind(expected: String): KeybindComponent =
    apply {
        this should haveKeybind(expected)
    }
