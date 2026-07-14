package io.github.lmliam.kotventure.core.dsl

import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.kotest.core.spec.style.StringSpec

class InternalKotventureApiTest :
    StringSpec(
        {
            "requires external callers to opt in to internal APIs" {
                assertDoesNotCompile(
                    "InternalApiUsage.kt",
                    """
                    import io.github.lmliam.kotventure.core.dsl.once

                    val slot: String? by once()
                    """.trimIndent(),
                    "This is internal Kotventure infrastructure and is not intended for end-user code.",
                )
            }
        },
    )
