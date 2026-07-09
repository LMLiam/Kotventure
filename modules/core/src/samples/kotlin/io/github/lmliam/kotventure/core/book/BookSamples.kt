package io.github.lmliam.kotventure.core.book

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun bookSample() {
    book {
        title {
            text("Server Rules") { color(gold) }
        }
        author { text("Staff") }
        page {
            text("1. Be kind")
            newline()
            text("2. No griefing")
        }
        page { text("Contact staff for help.") }
    }
}
