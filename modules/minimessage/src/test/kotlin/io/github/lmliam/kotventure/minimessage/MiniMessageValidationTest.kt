package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import io.github.lmliam.kotventure.minimessage.validation.MiniMessageDiagnostic
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.ParsingException

private object ValidationWelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
    val player = placeholder<Component>("player")
    val count = placeholder<Int>("count")
}

private object ValidationExtraTagTemplate : MiniTemplate("<gold>Hello <player> <oops></gold>") {
    val player = placeholder<String>("player")
}

class MiniMessageValidationTest :
    StringSpec(
        {
            "well-formed markup with all placeholders returns Success" {
                val player = placeholder<Component>("player")
                val count = placeholder<Int>("count")

                val result =
                    validate(
                        input = "<gold>Welcome <player>, <count> new messages</gold>",
                        placeholders = listOf(player, count),
                    )

                result shouldBe ValidationResult.Success
            }

            "markup with no spec and no placeholder tags returns Success" {
                val result =
                    validate(
                        input = "<gold>Hello world</gold>",
                        placeholders = emptyList(),
                    )

                result shouldBe ValidationResult.Success
            }

            "unclosed standard tag returns Failure with MalformedTag diagnostic" {
                val result =
                    validate(
                        input = "<gold>Hello world",
                        placeholders = emptyList(),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                failure.diagnostics shouldHaveSize 1
                failure.diagnostics[0].shouldBeInstanceOf<MiniMessageDiagnostic.MalformedTag>()
            }

            "MalformedTag carries non-unknown start and end index when Adventure reports location" {
                val result =
                    validate(
                        input = "<gold>Hello world",
                        placeholders = emptyList(),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val malformed = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MalformedTag>()
                malformed shouldHaveSize 1
                malformed[0].startIndex shouldNotBe MiniMessageDiagnostic.MalformedTag.LOCATION_UNKNOWN
                malformed[0].endIndex shouldNotBe MiniMessageDiagnostic.MalformedTag.LOCATION_UNKNOWN
            }

            "unclosed non-standard-tag in markup produces MalformedTag with message" {
                val player = placeholder<Component>("player")

                val result =
                    validate(
                        input = "<gold><player>joined",
                        placeholders = listOf(player),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val malformed = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MalformedTag>()
                malformed shouldHaveSize 1
                malformed[0].message.isNotEmpty() shouldBe true
            }

            "MalformedTag.LOCATION_UNKNOWN sentinel is sourced from Adventure not hardcoded" {
                MiniMessageDiagnostic.MalformedTag.LOCATION_UNKNOWN shouldBe ParsingException.LOCATION_UNKNOWN
            }

            "MalformedTag start and end indices are passed through from Adventure without modification" {
                val result =
                    validate(
                        input = "<gold>Hello world",
                        placeholders = emptyList(),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val malformed = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MalformedTag>()
                malformed shouldHaveSize 1
                malformed[0].startIndex shouldNotBe MiniMessageDiagnostic.MalformedTag.LOCATION_UNKNOWN
                malformed[0].endIndex shouldNotBe MiniMessageDiagnostic.MalformedTag.LOCATION_UNKNOWN
            }

            "placeholder in spec but absent from markup returns MissingPlaceholder" {
                val player = placeholder<Component>("player")
                val count = placeholder<Int>("count")

                val result =
                    validate(
                        input = "<player> joined",
                        placeholders = listOf(player, count),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val missing = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MissingPlaceholder>()
                missing shouldHaveSize 1
                missing[0].name shouldBe "count"
            }

            "placeholder tag in markup but not in spec returns ExtraPlaceholder" {
                val player = placeholder<Component>("player")

                val result =
                    validate(
                        input = "<player> joined with <oops>",
                        placeholders = listOf(player),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val extra = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.ExtraPlaceholder>()
                extra shouldHaveSize 1
                extra[0].name shouldBe "oops"
            }

            "placeholder present in both markup and spec is not flagged while unspecced tag is reported as extra" {
                val player = placeholder<Component>("player")

                val result =
                    validate(
                        input = "<player> <missing-one> <extra-one>",
                        placeholders = listOf(player, placeholder<String>("missing-one")),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val missing = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MissingPlaceholder>()
                val extra = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.ExtraPlaceholder>()
                missing shouldHaveSize 0
                extra shouldHaveSize 1
                extra[0].name shouldBe "extra-one"
            }

            "all three diagnostic kinds appear in one markup — malformed then missing then extra" {
                val player = placeholder<Component>("player")

                val result =
                    validate(
                        input = "<gold>Hello <extra>",
                        placeholders = listOf(player),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val malformed = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MalformedTag>()
                val missing = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MissingPlaceholder>()
                val extra = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.ExtraPlaceholder>()

                malformed shouldHaveSize 1
                missing shouldHaveSize 1
                extra shouldHaveSize 1
                missing[0].name shouldBe "player"
                extra[0].name shouldBe "extra"

                val malformedIdx = failure.diagnostics.indexOf(malformed[0])
                val missingIdx = failure.diagnostics.indexOf(missing[0])
                val extraIdx = failure.diagnostics.indexOf(extra[0])
                (malformedIdx < missingIdx) shouldBe true
                (missingIdx < extraIdx) shouldBe true
            }

            "standard Adventure tags are not reported as extra placeholders" {
                val result =
                    validate(
                        input = "<gold>Hello</gold> <bold>world</bold>",
                        placeholders = emptyList(),
                    )

                result shouldBe ValidationResult.Success
            }

            "standard tags with no spec and one extra non-standard tag reports only extra" {
                val result =
                    validate(
                        input = "<gold>Hello</gold> <my-placeholder>",
                        placeholders = emptyList(),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val extra = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.ExtraPlaceholder>()
                extra shouldHaveSize 1
                extra[0].name shouldBe "my-placeholder"
            }

            "MiniTemplate.validate() returns Success for a correct template" {
                val result = ValidationWelcomeTemplate.validate()

                result shouldBe ValidationResult.Success
            }

            "MiniTemplate.validate() returns Failure when markup has extra tag" {
                val result = ValidationExtraTagTemplate.validate()

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val extra = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.ExtraPlaceholder>()
                extra shouldHaveSize 1
                extra[0].name shouldBe "oops"
            }

            "ValidationResult.isSuccess is true for Success and false for Failure" {
                val player = placeholder<Component>("player")
                val success = validate("<player>", listOf(player))
                val failure = validate("<extra>", emptyList())

                success.isSuccess shouldBe true
                success.isFailure shouldBe false
                failure.isSuccess shouldBe false
                failure.isFailure shouldBe true
            }

            "ValidationResult.isFailure is true for Failure and false for Success" {
                val failure = validate("<extra>", emptyList())
                val success = ValidationResult.Success

                failure.isFailure shouldBe true
                success.isFailure shouldBe false
            }

            "Failure requires non-empty diagnostics list" {
                shouldThrow<IllegalArgumentException> {
                    ValidationResult.Failure(emptyList())
                }
            }

            "missing placeholders are emitted in spec declaration order" {
                val alpha = placeholder<String>("alpha")
                val beta = placeholder<String>("beta")
                val gamma = placeholder<String>("gamma")

                val result =
                    validate(
                        input = "<gold>No placeholders</gold>",
                        placeholders = listOf(alpha, beta, gamma),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val missing = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MissingPlaceholder>()
                missing.map { it.name } shouldBe listOf("alpha", "beta", "gamma")
            }

            "extra placeholders are emitted in markup-encounter order" {
                val result =
                    validate(
                        input = "<gold>Hello</gold> <first> then <second> then <third>",
                        placeholders = emptyList(),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val extra = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.ExtraPlaceholder>()
                extra.map { it.name } shouldBe listOf("first", "second", "third")
            }

            "duplicate placeholder tag in markup is recorded only once" {
                val player = placeholder<Component>("player")

                val result =
                    validate(
                        input = "<player> joined, then <player> left",
                        placeholders = listOf(player),
                    )

                result shouldBe ValidationResult.Success
            }

            "placeholder named same as standard tag is not falsely reported as MissingPlaceholder when used in markup" {
                val gold = placeholder<String>("gold")

                val result =
                    validate(
                        input = "<gold>text</gold>",
                        placeholders = listOf(gold),
                    )

                result shouldBe ValidationResult.Success
            }

            "placeholder named same as standard tag is reported as MissingPlaceholder when absent from markup" {
                val gold = placeholder<String>("gold")

                val result =
                    validate(
                        input = "<red>no gold here</red>",
                        placeholders = listOf(gold),
                    )

                val failure = result.shouldBeInstanceOf<ValidationResult.Failure>()
                val missing = failure.diagnostics.filterIsInstance<MiniMessageDiagnostic.MissingPlaceholder>()
                missing shouldHaveSize 1
                missing[0].name shouldBe "gold"
            }

            "standard tag not in spec is never reported as ExtraPlaceholder when spec has standard-named placeholder" {
                val gold = placeholder<String>("gold")

                val result =
                    validate(
                        input = "<bold><gold>text</gold></bold>",
                        placeholders = listOf(gold),
                    )

                result shouldBe ValidationResult.Success
            }

            "validate returns a result rather than throwing for markup that is well-formed in lenient mode" {
                val inputs =
                    listOf(
                        "",
                        "plain text",
                        "<gold>unclosed",
                        "<unknown-tag>",
                        "<gold arg='value'>text</gold>",
                    )

                for (input in inputs) {
                    validate(input, emptyList())
                }
            }
        },
    )
