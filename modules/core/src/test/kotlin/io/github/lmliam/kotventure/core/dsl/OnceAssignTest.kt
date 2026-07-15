package io.github.lmliam.kotventure.core.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class OnceAssignTest :
    StringSpec(
        {
            "accepts a value within range" {
                var slot: Int? by once().inRange(1..10)
                slot = 5
                slot shouldBe 5
            }

            "rejects a value outside range" {
                var slot: Int? by once().inRange(1..10)

                shouldThrow<IllegalArgumentException> {
                    slot = 11
                }
            }

            "does not consume the slot when an in-range validation fails" {
                var slot: Int? by once().inRange(1..10)

                shouldThrow<IllegalArgumentException> {
                    slot = 11
                }
                slot = 5

                slot shouldBe 5
            }

            "accepts a positive int" {
                var slot: Int? by once().positive()
                slot = 3
                slot shouldBe 3
            }

            "rejects a non-positive int" {
                var slot: Int? by once().positive()

                shouldThrow<IllegalArgumentException> {
                    slot = 0
                }
            }

            "accepts a positive float" {
                var slot: Float? by once().positive()
                slot = 1.5f
                slot shouldBe 1.5f
            }

            "rejects a non-positive float" {
                var slot: Float? by once().positive()

                shouldThrow<IllegalArgumentException> {
                    slot = -1f
                }
            }

            "does not consume the slot when a positive validation fails" {
                var slot: Int? by once().positive()

                shouldThrow<IllegalArgumentException> {
                    slot = 0
                }
                slot = 4

                slot shouldBe 4
            }

            "throws on a double-set before validation" {
                var slot: Int? by once().inRange(1..10)
                slot = 5

                shouldThrow<IllegalStateException> {
                    slot = 20
                }
            }

            "infers Int from the property type with no explicit type argument" {
                var slot: Int? by once()
                slot = 7
                slot shouldBe 7
            }

            "infers the chained inRange type from the property type" {
                var slot: Int? by once().inRange(1..1024)
                slot = 512
                slot shouldBe 512
            }

            "infers the chained positive type for Int from the property type" {
                var slot: Int? by once().positive()
                slot = 2
                slot shouldBe 2
            }

            "infers the chained positive type for Float from the property type" {
                var slot: Float? by once().positive()
                slot = 2.5f
                slot shouldBe 2.5f
            }

            "throws on a double-set of a bare once delegate" {
                var slot: Int? by once()
                slot = 1

                shouldThrow<IllegalStateException> {
                    slot = 2
                }
            }
        },
    )
