package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.event.click
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope
import io.github.lmliam.kotventure.paper.dialog.fixture.FakeCallbackAction
import io.github.lmliam.kotventure.paper.dialog.fixture.builtDialog
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.NoticeType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import kotlin.time.Duration.Companion.seconds

private fun noticeButton(init: ButtonScope.() -> Unit) =
    builtDialog(notice) {
        title { text("t") }
        button(init)
    }.type.shouldBeInstanceOf<NoticeType>().action()

class ButtonActionTest :
    StringSpec(
        {
            "wires a button label, tooltip and width" {
                val button =
                    noticeButton {
                        label { text("Claim") }
                        tooltip { text("Adds it to your inventory") }
                        width(200)
                    }

                button.label() shouldHaveContent "Claim"
                button.tooltip().shouldBeInstanceOf<Component>() shouldHaveContent
                        "Adds it to your inventory"
                button.width() shouldBe 200
            }

            "leaves the action null when none is chosen" {
                noticeButton { label { text("OK") } }.action().shouldBeNull()
            }

            "invokes an onClick callback with the clicking audience" {
                var received: Audience? = null
                val button =
                    noticeButton {
                        label { text("Go") }
                        onClick { _, audience -> received = audience }
                    }

                val action = button.action().shouldBeInstanceOf<FakeCallbackAction>()
                val audience = emptyAudience()
                action.callback.accept(mockk<DialogResponseView>(), audience)

                received shouldBe audience
            }

            "records bounded uses on an onClick callback" {
                val button =
                    noticeButton {
                        label { text("Go") }
                        onClick(3, 10.seconds) { _, _ -> }
                    }

                button
                    .action()
                    .shouldBeInstanceOf<FakeCallbackAction>()
                    .options
                    .uses() shouldBe 3
            }

            "wires a run-command template action" {
                val button =
                    noticeButton {
                        label { text("Give") }
                        runCommand("give $(player) diamond")
                    }

                button
                    .action()
                    .shouldBeInstanceOf<DialogAction.CommandTemplateAction>()
                    .template() shouldBe "give $(player) diamond"
            }

            "wires a custom action with a key" {
                val key = Key.key("kotventure", "reward")
                val button =
                    noticeButton {
                        label { text("Custom") }
                        custom(key)
                    }

                val action = button.action().shouldBeInstanceOf<DialogAction.CustomClickAction>()
                action.id() shouldBe key
                action.additions().shouldBeNull()
            }

            "wires a custom action carrying additions" {
                val key = Key.key("kotventure", "reward")
                val additions = BinaryTagHolder.binaryTagHolder("{reward:1}")
                val button =
                    noticeButton {
                        label { text("Custom") }
                        custom(key, additions)
                    }

                button.action().shouldBeInstanceOf<DialogAction.CustomClickAction>().additions() shouldBe additions
            }

            "wires a static action from the core click DSL" {
                val button =
                    noticeButton {
                        label { text("Run") }
                        click { run("say hi") }
                    }

                button
                    .action()
                    .shouldBeInstanceOf<DialogAction.StaticAction>()
                    .value() shouldBe click { run("say hi") }
            }

            "throws when two actions are selected" {
                shouldThrow<IllegalStateException> {
                    noticeButton {
                        label { text("x") }
                        runCommand("a")
                        runCommand("b")
                    }
                }
            }

            "throws when a button label is missing" {
                shouldThrow<IllegalStateException> {
                    noticeButton { runCommand("a") }
                }
            }

            "throws when the button width is zero" {
                shouldThrow<IllegalArgumentException> {
                    noticeButton {
                        label { text("x") }
                        width(0)
                    }
                }
            }

            "throws when the button width exceeds 1024" {
                shouldThrow<IllegalArgumentException> {
                    noticeButton {
                        label { text("x") }
                        width(1025)
                    }
                }
            }
        },
    )
