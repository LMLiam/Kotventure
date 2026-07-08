package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveInsertion
import io.github.lmliam.kotventure.test.text.shouldHaveStyle
import io.kotest.core.spec.style.StringSpec
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class StyledDslTest :
    StringSpec(
        {
            "applies a complete style to a component" {
                val header =
                    style {
                        color(gold)
                        bold()
                        insertion("/help")
                    }

                val title = text("Title") styled header

                title shouldContainText "Title"
                title shouldHaveStyle header
                title shouldHaveColor gold
                title shouldHaveDecoration TextDecoration.BOLD
                title shouldHaveInsertion "/help"
            }

            "replaces any style already present on the component" {
                val original = text("Title") { color(red) }
                val replacement = style { color(green) }

                val styled = original styled replacement

                styled shouldHaveColor green
            }
        },
    )
