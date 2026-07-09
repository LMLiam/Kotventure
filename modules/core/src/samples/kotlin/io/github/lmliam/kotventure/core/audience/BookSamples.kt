package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.book.book
import io.github.lmliam.kotventure.core.text.text

internal fun audienceOpenBookSample() {
    val audience = emptyAudience()
    val rules =
        book {
            title { text("Server Rules") }
            author { text("Staff") }
            page { text("Be kind.") }
        }

    audience.open(rules)
}

internal fun audienceBookSample() {
    val audience = emptyAudience()

    audience.book {
        title { text("Welcome") }
        page { text("Hello!") }
    }
}
