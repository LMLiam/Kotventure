package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ScoreComponent

/**
 * Matches a score component whose score name is [expected]. Combine with `and`/`or` or negate with `shouldNot`.
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
 * Matches a score component whose objective is [expected].
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
 * Asserts that this component is a [ScoreComponent] and returns it typed.
 */
public fun Component.shouldBeScoreComponent(): ScoreComponent = asComponentType("score")

/**
 * Asserts that this score component has [expected] as its score name.
 */
public infix fun ScoreComponent.shouldHaveScoreName(expected: String): ScoreComponent =
    apply {
        this should haveScoreName(expected)
    }

/**
 * Asserts that this score component has [expected] as its score objective.
 */
public infix fun ScoreComponent.shouldHaveScoreObjective(expected: String): ScoreComponent =
    apply {
        this should haveScoreObjective(expected)
    }
