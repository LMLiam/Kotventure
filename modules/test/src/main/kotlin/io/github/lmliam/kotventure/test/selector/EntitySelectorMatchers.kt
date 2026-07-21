package io.github.lmliam.kotventure.test.selector

import io.github.lmliam.kotventure.core.selector.EntitySelector
import io.github.lmliam.kotventure.core.selector.EntitySelectorParseException
import io.github.lmliam.kotventure.core.selector.parseSelector
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

/**
 * Returns a matcher that accepts a selector with the canonical source [expected].
 */
public fun renderAs(expected: String): Matcher<EntitySelector> =
    Matcher { selector ->
        val actual = selector.asString()
        MatcherResult(
            actual == expected,
            { "Expected selector to render as <$expected>, but was <$actual>." },
            { "Expected selector not to render as <$expected>." },
        )
    }

/**
 * Verifies that this selector has the canonical source [expected].
 *
 * @return this selector, for chained assertions.
 * @throws AssertionError when the rendered source differs from [expected].
 */
public infix fun EntitySelector.shouldRenderAs(expected: String): EntitySelector =
    apply {
        this should renderAs(expected)
    }

/**
 * Verifies that the receiver is canonical entity-selector source.
 *
 * @return the parsed selector.
 * @throws EntitySelectorParseException when the receiver is not a valid selector.
 * @throws AssertionError when the parsed selector has different canonical source.
 */
public fun String.shouldBeCanonicalSelector(): EntitySelector = parseSelector(this) shouldRenderAs this

/**
 * Verifies that parsing the receiver followed by [remainder] fails at their boundary.
 *
 * @return the parse exception for additional assertions.
 * @throws AssertionError when parsing succeeds or fails at a different offset.
 */
public infix fun String.shouldFailToParseAt(remainder: String): EntitySelectorParseException {
    val parseFailure =
        shouldThrow<EntitySelectorParseException> {
            parseSelector(this + remainder)
        }

    parseFailure.offset shouldBe length
    return parseFailure
}
