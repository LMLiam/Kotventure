package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.shouldBeItalic
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldNotBeItalic
import io.kotest.core.spec.style.StringSpec
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

class NonItalicNameTest :
    StringSpec(
        {
            "applies an explicit non-italic default" {
                val name = text("X").nonItalicByDefault()

                name.shouldHaveDecoration(TextDecoration.ITALIC, State.FALSE)
                name.shouldNotBeItalic()
            }

            "preserves an explicit italic decoration" {
                val name = text("X") { italic() }.nonItalicByDefault()

                name.shouldBeItalic()
            }
        },
    )
