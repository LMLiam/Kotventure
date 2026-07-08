package io.github.lmliam.kotventure.core.score

import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeScoreComponent
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveScoreName
import io.github.lmliam.kotventure.test.text.shouldHaveScoreObjective
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class ScoreDslTest :
    StringSpec(
        {
            "builds a score component with a name and objective" {
                val component = score("Alex", "kills").shouldBeScoreComponent()

                component shouldHaveScoreName "Alex"
                component shouldHaveScoreObjective "kills"
                component.value() shouldBe null
            }

            "applies style to the score root" {
                val component =
                    score("Alex", "kills") {
                        color(red)
                        strikethrough()
                        style {
                            italic()
                        }
                    }

                component shouldHaveColor red
                component shouldHaveDecoration TextDecoration.STRIKETHROUGH
                component shouldHaveDecoration TextDecoration.ITALIC
            }

            "appends child components" {
                val suffix = text(" kills")

                val component =
                    score("Alex", "kills") {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }
        },
    )
