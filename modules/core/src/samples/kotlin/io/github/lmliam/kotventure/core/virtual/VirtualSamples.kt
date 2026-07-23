package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.text.text
import java.util.Locale

internal fun virtualSample() {
    val greeting =
        virtual<Locale>(fallback = "Hello") {
            text(if (context.language == "fr") "Bonjour" else "Hello")
        }
}
