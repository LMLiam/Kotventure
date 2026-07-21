package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ScoreComponent

/**
 * Returns a matcher that compares the score name with [expected].
 */
public fun haveScoreName(expected: String): Matcher<ScoreComponent> =
    Matcher { value ->
        val actual = value.name()
        MatcherResult(
            actual == expected,
            { "Expected score name <$expected>, but was <$actual>." },
            { "Expected score name not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that compares the score objective with [expected].
 */
public fun haveScoreObjective(expected: String): Matcher<ScoreComponent> =
    Matcher { value ->
        val actual = value.objective()
        MatcherResult(
            actual == expected,
            { "Expected score objective <$expected>, but was <$actual>." },
            { "Expected score objective not to be <$expected>." },
        )
    }

/**
 * Verifies that this component is a [ScoreComponent].
 *
 * @return this component as a [ScoreComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeScoreComponent(): ScoreComponent = asComponentType("score")

/**
 * Verifies that this score component has the score name [expected].
 */
public infix fun ScoreComponent.shouldHaveScoreName(expected: String): ScoreComponent =
    apply {
        this should haveScoreName(expected)
    }

/**
 * Verifies that this score component has the objective [expected].
 */
public infix fun ScoreComponent.shouldHaveScoreObjective(expected: String): ScoreComponent =
    apply {
        this should haveScoreObjective(expected)
    }
