package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component

class ScoreMatchersTest :
    StringSpec(
        {
            "matches score component names and objectives" {
                val component =
                    Component
                        .score("Alex", "kills")
                        .shouldBeScoreComponent()

                component shouldHaveScoreName "Alex"
                component shouldHaveScoreObjective "kills"
            }

            "reports score name mismatch with expected and actual names" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .score("Alex", "kills")
                            .shouldBeScoreComponent() shouldHaveScoreName "Steve"
                    }
                val expectedMessage = "Expected score name <Steve>, but was <Alex>."

                failure.message shouldContain expectedMessage
            }

            "reports score objective mismatch with expected and actual objectives" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .score("Alex", "kills")
                            .shouldBeScoreComponent() shouldHaveScoreObjective "deaths"
                    }
                val expectedMessage = "Expected score objective <deaths>, but was <kills>."

                failure.message shouldContain expectedMessage
            }

            "reports non-score components before score assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("plain").shouldBeScoreComponent()
                    }
                val expectedMessage = "Expected score component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }
        },
    )
