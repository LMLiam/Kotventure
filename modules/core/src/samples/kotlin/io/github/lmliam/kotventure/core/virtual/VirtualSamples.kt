package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import java.util.Locale

internal fun virtualSample() {
    val greeting =
        virtual<Locale> {
            fallback("Hello")
            render {
                text(if (context.language == "fr") "Bonjour" else "Hello")
            }
        }
}

internal fun virtualStyledFallbackSample() {
    val greeting =
        virtual<Locale> {
            fallback {
                color(gold)
                text("Hello")
            }
            render {
                text(if (context.language == "fr") "Bonjour" else "Hello") { color(gold) }
            }
        }
}
