package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.paper.dialog.fixture.builtDialog
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.type.ConfirmationType
import io.papermc.paper.registry.data.dialog.type.DialogListType
import io.papermc.paper.registry.data.dialog.type.MultiActionType
import io.papermc.paper.registry.data.dialog.type.NoticeType
import io.papermc.paper.registry.data.dialog.type.ServerLinksType
import io.papermc.paper.registry.set.RegistrySet

class DialogTypeTest :
    StringSpec(
        {
            "selects a default notice type from the notice kind" {
                val type =
                    builtDialog(notice) {
                        title { text("t") }
                    }.type

                type.shouldBeInstanceOf<NoticeType>()
            }

            "selects a notice type with a configured button" {
                val type =
                    builtDialog(notice) {
                        title { text("t") }
                        button { label { text("Understood") } }
                    }.type.shouldBeInstanceOf<NoticeType>()

                type.action().label() shouldHaveContent "Understood"
            }

            "throws when the notice button is configured twice" {
                shouldThrow<IllegalStateException> {
                    builtDialog(notice) {
                        title { text("t") }
                        button { label { text("One") } }
                        button { label { text("Two") } }
                    }
                }
            }

            "wires a confirmation type from yes and no buttons" {
                val type =
                    builtDialog(confirmation) {
                        title { text("t") }
                        yes { label { text("Claim") } }
                        no { label { text("Later") } }
                    }.type.shouldBeInstanceOf<ConfirmationType>()

                type.yesButton().label() shouldHaveContent "Claim"
                type.noButton().label() shouldHaveContent "Later"
            }

            "throws when confirmation omits the yes button" {
                shouldThrow<IllegalStateException> {
                    builtDialog(confirmation) {
                        title { text("t") }
                        no { label { text("Later") } }
                    }
                }
            }

            "throws when confirmation omits the no button" {
                shouldThrow<IllegalStateException> {
                    builtDialog(confirmation) {
                        title { text("t") }
                        yes { label { text("Claim") } }
                    }
                }
            }

            "wires a multi-action type accumulating buttons with columns and exit" {
                val type =
                    builtDialog(multiAction) {
                        title { text("t") }
                        button { label { text("One") } }
                        button { label { text("Two") } }
                        columns(3)
                        exitButton { label { text("Exit") } }
                    }.type.shouldBeInstanceOf<MultiActionType>()

                type.actions() shouldHaveSize 2
                type.actions()[0].label() shouldHaveContent "One"
                type.actions()[1].label() shouldHaveContent "Two"
                type.columns() shouldBe 3
                type.exitAction().shouldNotBeNull().label() shouldHaveContent "Exit"
            }

            "throws when a multi-action type has no buttons" {
                shouldThrow<IllegalStateException> {
                    builtDialog(multiAction) {
                        title { text("t") }
                        columns(2)
                    }
                }
            }

            "throws when a multi-action type has non-positive columns" {
                shouldThrow<IllegalArgumentException> {
                    builtDialog(multiAction) {
                        title { text("t") }
                        button { label { text("One") } }
                        columns(0)
                    }
                }
            }

            "wires a dialog-list type passing the registry set through" {
                val entries: RegistrySet<Dialog> = RegistrySet.valueSet(RegistryKey.DIALOG, emptyList())
                val type =
                    builtDialog(dialogList) {
                        title { text("t") }
                        dialogs(entries)
                        columns(2)
                        buttonWidth(120)
                        exitButton { label { text("Back") } }
                    }.type.shouldBeInstanceOf<DialogListType>()

                type.dialogs() shouldBeSameInstanceAs entries
                type.columns() shouldBe 2
                type.buttonWidth() shouldBe 120
                type.exitAction().shouldNotBeNull().label() shouldHaveContent "Back"
            }

            "throws when a dialog list omits the dialogs slot" {
                shouldThrow<IllegalStateException> {
                    builtDialog(dialogList) {
                        title { text("t") }
                    }
                }
            }

            "throws when a dialog-list buttonWidth exceeds 1024" {
                val entries: RegistrySet<Dialog> = RegistrySet.valueSet(RegistryKey.DIALOG, emptyList())

                shouldThrow<IllegalArgumentException> {
                    builtDialog(dialogList) {
                        title { text("t") }
                        dialogs(entries)
                        buttonWidth(1025)
                    }
                }
            }

            "wires a server-links type from required columns and width" {
                val type =
                    builtDialog(serverLinks) {
                        title { text("t") }
                        columns(2)
                        buttonWidth(150)
                        exitButton { label { text("Close") } }
                    }.type.shouldBeInstanceOf<ServerLinksType>()

                type.columns() shouldBe 2
                type.buttonWidth() shouldBe 150
                type.exitAction().shouldNotBeNull().label() shouldHaveContent "Close"
            }

            "throws when server-links omits the columns slot" {
                shouldThrow<IllegalStateException> {
                    builtDialog(serverLinks) {
                        title { text("t") }
                        buttonWidth(150)
                    }
                }
            }

            "throws when server-links columns is not positive" {
                shouldThrow<IllegalArgumentException> {
                    builtDialog(serverLinks) {
                        title { text("t") }
                        columns(0)
                        buttonWidth(200)
                    }
                }
            }
        },
    )
