package io.github.lmliam.kotventure.core.audience

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SignatureTest :
    StringSpec(
        {
            "wraps the raw bytes" {
                val bytes = byteArrayOf(1, 2, 3)

                signature(bytes).bytes() shouldBe bytes
            }
        },
    )
