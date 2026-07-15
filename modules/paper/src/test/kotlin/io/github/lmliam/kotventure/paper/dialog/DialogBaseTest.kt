package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.paper.dialog.fixture.builtBase
import io.github.lmliam.kotventure.paper.dialog.fixture.builtDialog
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.papermc.paper.registry.data.dialog.DialogBase.DialogAfterAction

class DialogBaseTest :
    StringSpec(
        {
            "lands the required title on the base" {
                val base = builtBase { title { text("Daily reward") } }

                base.title() shouldHaveContent "Daily reward"
            }

            "lands the external title on the base" {
                val base =
                    builtBase {
                        title { text("Daily reward") }
                        externalTitle { text("Rewards") }
                    }

                base.externalTitle().shouldNotBeNull() shouldHaveContent "Rewards"
            }

            "leaves the external title unset by default" {
                val base = builtBase { title { text("Daily reward") } }

                base.externalTitle().shouldBeNull()
            }

            "defaults closable and pause to true" {
                val base = builtBase { title { text("t") } }

                base.canCloseWithEscape() shouldBe true
                base.pause() shouldBe true
            }

            "sets closeOnEscape and pausesGame to true with no argument" {
                val base =
                    builtBase {
                        title { text("t") }
                        closeOnEscape()
                        pausesGame()
                    }

                base.canCloseWithEscape() shouldBe true
                base.pause() shouldBe true
            }

            "lands closable, pause and after-action flags" {
                val base =
                    builtBase {
                        title { text("t") }
                        closeOnEscape(false)
                        pausesGame(false)
                        afterAction(wait)
                    }

                base.canCloseWithEscape() shouldBe false
                base.pause() shouldBe false
                base.afterAction() shouldBe DialogAfterAction.WAIT_FOR_RESPONSE
            }

            "maps the after-action scope members to the enum" {
                builtBase {
                    title { text("t") }
                    afterAction(close)
                }.afterAction() shouldBe DialogAfterAction.CLOSE

                builtBase {
                    title { text("t") }
                    afterAction(none)
                }.afterAction() shouldBe DialogAfterAction.NONE
            }

            "throws when the title is missing" {
                shouldThrow<IllegalStateException> {
                    builtDialog { }
                }
            }

            "throws when the title is set twice" {
                shouldThrow<IllegalStateException> {
                    builtDialog {
                        title { text("one") }
                        title { text("two") }
                    }
                }
            }

            "throws when a boolean flag is set twice" {
                shouldThrow<IllegalStateException> {
                    builtDialog {
                        title { text("t") }
                        pausesGame()
                        pausesGame(false)
                    }
                }
            }
        },
    )
