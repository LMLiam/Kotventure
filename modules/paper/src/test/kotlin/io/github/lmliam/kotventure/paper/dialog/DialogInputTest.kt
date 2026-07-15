package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.paper.dialog.fixture.builtBase
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.papermc.paper.registry.data.dialog.input.BooleanDialogInput
import io.papermc.paper.registry.data.dialog.input.NumberRangeDialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput

class DialogInputTest :
    StringSpec(
        {
            "wires a text input with its knobs and multiline options" {
                val input =
                    builtBase {
                        title { text("t") }
                        inputs {
                            text("nickname") {
                                label {
                                    text("Name")
                                    visible(false)
                                }
                                maxLength(32)
                                width(160)
                                default("Steve")
                                multiline {
                                    maxLines(4)
                                    height(64)
                                }
                            }
                        }
                    }.inputs().single().shouldBeInstanceOf<TextDialogInput>()

                input.key() shouldBe "nickname"
                input.label() shouldHaveContent "Name"
                input.maxLength() shouldBe 32
                input.width() shouldBe 160
                input.labelVisible() shouldBe false
                input.initial() shouldBe "Steve"
                val multiline = input.multiline().shouldNotBeNull()
                multiline.maxLines() shouldBe 4
                multiline.height() shouldBe 64
            }

            "sets a text input's label visible to true with no argument" {
                val input =
                    builtBase {
                        title { text("t") }
                        inputs {
                            text("nickname") {
                                label {
                                    text("Name")
                                    visible()
                                }
                            }
                        }
                    }.inputs().single().shouldBeInstanceOf<TextDialogInput>()

                input.labelVisible() shouldBe true
            }

            "wires a boolean input with default and command values" {
                val input =
                    builtBase {
                        title { text("t") }
                        inputs {
                            boolean("subscribe") {
                                label { text("Subscribe") }
                                default()
                                values {
                                    true("yes")
                                    false("no")
                                }
                            }
                        }
                    }.inputs().single().shouldBeInstanceOf<BooleanDialogInput>()

                input.key() shouldBe "subscribe"
                input.initial() shouldBe true
                input.onTrue() shouldBe "yes"
                input.onFalse() shouldBe "no"
            }

            "sets a boolean input's default to true with no argument" {
                val input =
                    builtBase {
                        title { text("t") }
                        inputs {
                            boolean("subscribe") {
                                label { text("Subscribe") }
                                default()
                            }
                        }
                    }.inputs().single().shouldBeInstanceOf<BooleanDialogInput>()

                input.initial() shouldBe true
            }

            "throws when the same boolean value polarity is set twice" {
                shouldThrow<IllegalStateException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            boolean("subscribe") {
                                label { text("Subscribe") }
                                values {
                                    true("yes")
                                    true("yep")
                                }
                            }
                        }
                    }
                }
            }

            "wires a number-range input from the range and knobs" {
                val input =
                    builtBase {
                        title { text("t") }
                        inputs {
                            range("count", 1f..64f) {
                                label { text("Count") }
                                step(1f)
                                default(8f)
                                width(120)
                                format(label, ": ", value)
                            }
                        }
                    }.inputs().single().shouldBeInstanceOf<NumberRangeDialogInput>()

                input.start() shouldBe 1f
                input.end() shouldBe 64f
                input.step() shouldBe 1f
                input.initial() shouldBe 8f
                input.width() shouldBe 120
                input.labelFormat() shouldBe $$"%1$s: %2$s"
            }

            "uses a single format part as a raw translation key" {
                val input =
                    builtBase {
                        title { text("t") }
                        inputs {
                            range("count", 1f..64f) {
                                label { text("Count") }
                                format("options.generic_value")
                            }
                        }
                    }.inputs().single().shouldBeInstanceOf<NumberRangeDialogInput>()

                input.labelFormat() shouldBe "options.generic_value"
            }

            "throws when format is called with no parts" {
                shouldThrow<IllegalArgumentException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            range("count", 1f..64f) {
                                label { text("Count") }
                                format()
                            }
                        }
                    }
                }
            }

            "wires a single-option input accumulating options in order" {
                val input =
                    builtBase {
                        title { text("t") }
                        inputs {
                            option("class") {
                                label {
                                    text("Class")
                                    visible(false)
                                }
                                width(160)
                                options {
                                    "mage" {
                                        display { text("Mage") }
                                        default()
                                    }
                                    +"rogue"
                                }
                            }
                        }
                    }.inputs().single().shouldBeInstanceOf<SingleOptionDialogInput>()

                input.width() shouldBe 160
                input.labelVisible() shouldBe false
                input.entries() shouldHaveSize 2
                input.entries()[0].id() shouldBe "mage"
                input.entries()[0].display().shouldNotBeNull() shouldHaveContent "Mage"
                input.entries()[0].initial() shouldBe true
                input.entries()[1].id() shouldBe "rogue"
                input.entries()[1].initial() shouldBe false
            }

            "throws when a single-option input has no options" {
                shouldThrow<IllegalStateException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            option("class") {
                                label { text("Class") }
                                options {}
                            }
                        }
                    }
                }
            }

            "throws when a single-option id is declared twice" {
                shouldThrow<IllegalStateException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            option("class") {
                                label { text("Class") }
                                options {
                                    +"mage"
                                    +"mage"
                                }
                            }
                        }
                    }
                }
            }

            "throws when more than one single option is marked default" {
                shouldThrow<IllegalStateException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            option("class") {
                                label { text("Class") }
                                options {
                                    "mage" { default() }
                                    "rogue" { default() }
                                }
                            }
                        }
                    }
                }
            }

            "accumulates inputs across two blocks in call order" {
                val inputs =
                    builtBase {
                        title { text("t") }
                        inputs {
                            boolean("a") { label { text("A") } }
                        }
                        inputs {
                            text("b") { label { text("B") } }
                        }
                    }.inputs()

                inputs shouldHaveSize 2
                inputs[0].key() shouldBe "a"
                inputs[1].key() shouldBe "b"
            }

            "throws when an input label is missing" {
                shouldThrow<IllegalStateException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            text("x") { maxLength(4) }
                        }
                    }
                }
            }

            "throws when an input knob is set twice" {
                shouldThrow<IllegalStateException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            text("x") {
                                label { text("X") }
                                maxLength(4)
                                maxLength(8)
                            }
                        }
                    }
                }
            }

            "throws when a text input maxLength is not positive" {
                shouldThrow<IllegalArgumentException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            text("x") {
                                label { text("X") }
                                maxLength(0)
                            }
                        }
                    }
                }
            }

            "throws when a text multiline height exceeds 512" {
                shouldThrow<IllegalArgumentException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            text("x") {
                                label { text("X") }
                                multiline { height(513) }
                            }
                        }
                    }
                }
            }

            "throws when a number-range input step is zero" {
                shouldThrow<IllegalArgumentException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            range("count", 1f..64f) {
                                label { text("Count") }
                                step(0f)
                            }
                        }
                    }
                }
            }

            "throws when a single-option input width exceeds 1024" {
                shouldThrow<IllegalArgumentException> {
                    builtBase {
                        title { text("t") }
                        inputs {
                            option("class") {
                                label { text("Class") }
                                width(1025)
                                options { +"mage" }
                            }
                        }
                    }
                }
            }
        },
    )
