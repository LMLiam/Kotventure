package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorParseException
import io.kotest.assertions.throwables.shouldThrow
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

            "successful token consumption commits the cursor" {
                val reader = SelectorReader("abcXYZ")

                reader.consume("abc") shouldBe true
                reader.peek() shouldBe 'X'
                reader.consume("XYZ") shouldBe true
                reader.isAtEnd() shouldBe true
            }

            "failed token consumption preserves its starting offset for a later parse" {
                val reader = SelectorReader("abcdef")

                reader.consume("ab") shouldBe true
                val startingOffset = reader.offset

                reader.consume("cXYZ") shouldBe false
                reader.offset shouldBe startingOffset
                reader.peek() shouldBe 'c'

                reader.consume("cde") shouldBe true
                reader.peek() shouldBe 'f'
            }

            "failed expectation leaves the cursor at its failure offset" {
                val reader = SelectorReader("abc")

                val failure = shouldThrow<EntitySelectorParseException> { reader.expect('x') }

                failure.offset shouldBe 0
                reader.peek() shouldBe 'a'
                reader.consume("abc") shouldBe true
                reader.isAtEnd() shouldBe true
            }
        },
    )
