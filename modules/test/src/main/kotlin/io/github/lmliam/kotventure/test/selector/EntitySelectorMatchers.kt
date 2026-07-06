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
 * Matches an entity selector that renders as [expected] canonical selector source. Combine with
 * `and`/`or` or negate with `shouldNot`.
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
 * Asserts that this selector renders as [expected] canonical selector source.
 */
public infix fun EntitySelector.shouldRenderAs(expected: String): EntitySelector =
    apply {
        this should renderAs(expected)
    }

/**
 * Asserts that this string parses as an entity selector and renders back to itself unchanged.
 */
public fun String.shouldBeCanonicalSelector(): EntitySelector = parseSelector(this) shouldRenderAs this

/**
 * Asserts that parsing this string followed by [remainder] fails exactly at their boundary.
 */
public infix fun String.shouldFailToParseAt(remainder: String): EntitySelectorParseException {
    val parseFailure =
        shouldThrow<EntitySelectorParseException> {
            parseSelector(this + remainder)
        }

    parseFailure.offset shouldBe length
    return parseFailure
}
