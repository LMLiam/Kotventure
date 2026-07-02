package io.github.lmliam.kotventure.core.selector

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

internal fun assertParseFailure(
    source: String,
    offset: Int,
    message: String,
) {
    val failure = shouldThrow<EntitySelectorParseException> { entitySelector(source) }
    failure.offset shouldBe offset
    failure.message shouldContain message
}
