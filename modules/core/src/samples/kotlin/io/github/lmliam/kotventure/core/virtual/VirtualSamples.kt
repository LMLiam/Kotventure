package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import java.util.Locale
import java.util.UUID

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

internal fun virtualRenderingSample() {
    val message =
        component {
            virtual<Locale> {
                render {
                    text(if (context.language == "fr") "Bonjour" else "Hello")
                }
            }
            text(" Inspect") {
                hover {
                    text {
                        virtual<UUID> {
                            render { text(context.toString()) }
                        }
                    }
                }
            }
        }

    message.render(Locale.UK, UUID(0, 0))
}
