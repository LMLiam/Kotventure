package io.github.lmliam.kotventure.core.selector.parsing

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectorReaderTest :
    StringSpec(
        {
            "treats offsets at or beyond the source length as end of input" {
                val reader = SelectorReader("x")

                reader.isAtEnd() shouldBe false

                reader.skip()
                reader.isAtEnd() shouldBe true

                reader.skip()
                reader.isAtEnd() shouldBe true
            }
        },
    )
