package io.github.lmliam.kotventure.core.uuid

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class UuidTest :
    StringSpec(
        {
            "parses a valid UUID string" {
                uuid("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25") shouldBe
                        UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")
            }

            "throws on an invalid UUID string" {
                shouldThrow<IllegalArgumentException> {
                    uuid("not-a-uuid")
                }
            }
        },
    )
