package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.nbt.blockNbt
import io.github.lmliam.kotventure.core.nbt.blockPos
import io.github.lmliam.kotventure.core.nbt.entityNbt
import io.github.lmliam.kotventure.core.nbt.nbtPath
import io.github.lmliam.kotventure.core.nbt.storageNbt
import io.github.lmliam.kotventure.core.objectcomponent.display
import io.github.lmliam.kotventure.core.objectcomponent.sprite
import io.github.lmliam.kotventure.core.score.score
import io.github.lmliam.kotventure.core.selector.selector
import io.github.lmliam.kotventure.core.selector.self
import io.github.lmliam.kotventure.core.text.asSequence
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable
import io.github.lmliam.kotventure.test.text.shouldBeBlockNbtComponent
import io.github.lmliam.kotventure.test.text.shouldBeEntityNbtComponent
import io.github.lmliam.kotventure.test.text.shouldBeObjectComponent
import io.github.lmliam.kotventure.test.text.shouldBeSelectorComponent
import io.github.lmliam.kotventure.test.text.shouldBeStorageNbtComponent
import io.github.lmliam.kotventure.test.text.shouldBeVirtualComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveArguments
import io.github.lmliam.kotventure.test.text.shouldHaveChildren
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.github.lmliam.kotventure.test.text.shouldHaveHoverEntity
import io.github.lmliam.kotventure.test.text.shouldHaveHoverText
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.text.VirtualComponentRenderer
import net.kyori.adventure.text.event.HoverEvent
import java.util.Locale
import java.util.UUID

class VirtualRenderingTest :
    StringSpec(
        {
            "renders a root virtual component against a matching context" {
                val message = virtual<Viewer> { render { text(context.name) } }

                message.render(Viewer("Alex")) shouldBe renderedText("Alex")
            }

            "discards the complete fallback after a matching render" {
                val message =
                    virtual<Viewer> {
                        fallback {
                            color(red)
                            text("fallback")
                            text(" child")
                        }
                        render { text(context.name) }
                    }

                val rendered = message.render(Viewer("Alex"))

                rendered shouldBe renderedText("Alex")
                rendered shouldHaveContent "Alex"
                rendered.shouldHaveChildren(Component.text("Alex"))
            }

            "uses an arbitrary component type from a renderer" {
                val message =
                    Component.virtual(
                        Viewer::class.java,
                    ) { context -> Component.score(context.name, "wins") }

                message.render(Viewer("Alex")) shouldBe Component.score("Alex", "wins")
            }

            "uses the first matching context" {
                val message = virtual<Viewer> { render { text(context.name) } }

                message.render(Viewer("Alex"), Viewer("Steve")) shouldBe renderedText("Alex")
            }

            "renders nodes for contexts of different types" {
                val message =
                    component {
                        virtual<Viewer> { render { text(context.name) } }
                        virtual<Locale> { render { text(context.language) } }
                    }

                val rendered = message.render(Viewer("Alex"), Locale.UK)

                rendered.shouldHaveChildren(renderedText("Alex"), renderedText("en"))
            }

            "keeps unmatched virtual components for a later render" {
                val message = virtual<Viewer> { render { text(context.name) } }

                val unmatched = message.render(Locale.UK)

                unmatched.shouldBeVirtualComponent()
                unmatched.render(Viewer("Alex")) shouldBe renderedText("Alex")
            }

            "renders boxed primitive contexts" {
                val message = virtual<Int> { render { text(context.toString()) } }

                message.render(42) shouldBe renderedText("42")
            }

            "rejects a null result from an external renderer" {
                val message =
                    Component.virtual(
                        Viewer::class.java,
                        NullVirtualComponentRenderer(),
                    )

                shouldThrow<IllegalStateException> { message.render(Viewer("Alex")) }
            }

            "renders virtual components introduced by a renderer" {
                val message =
                    virtual<Viewer> {
                        render {
                            virtual<Locale> {
                                render { text(context.language) }
                            }
                        }
                    }

                val rendered = message.render(Viewer("Alex"), Locale.UK)

                rendered shouldContainText "en"
                rendered.asSequence().any { it is VirtualComponent } shouldBe false
            }

            "renders a virtual component returned directly by an external renderer" {
                val nested = virtual<Locale> { render { text(context.language) } }
                val message =
                    Component.virtual(
                        Viewer::class.java,
                    ) { nested }

                message.render(Viewer("Alex"), Locale.UK) shouldBe renderedText("en")
            }

            "keeps a recursive external renderer finite" {
                var message: VirtualComponent? = null
                message =
                    Component.virtual(
                        Viewer::class.java,
                    ) { requireNotNull(message) }

                message.render(Viewer("Alex")) shouldBe message
            }

            "renders virtual children in every non-virtual renderer hook" {
                val virtualChild = virtual<Viewer> { render { text(context.name) } }
                val sources =
                    listOf(
                        text("text"),
                        translatable("translation.key"),
                        keybind("key.jump"),
                        score("Alex", "wins"),
                        selector(self()),
                        blockNbt(blockPos(0, 64, 0), nbtPath("value")),
                        entityNbt(self(), nbtPath("value")),
                        storageNbt(key("kotventure", "messages"), nbtPath("value")),
                        display(sprite(key("minecraft", "block/stone"))),
                    ).map { it.append(virtualChild) }

                sources.forEach { source ->
                    val rendered = source.render(Viewer("Alex"))

                    rendered::class shouldBe source::class
                    rendered.shouldHaveChildren(renderedText("Alex"))
                }
            }

            "renders virtual translatable arguments" {
                val argument = virtual<Viewer> { render { text(context.name) } }
                val message =
                    translatable("translation.key") {
                        arg(argument)
                    }

                val rendered = message.render(Viewer("Alex"))

                rendered.shouldHaveArguments(TranslationArgument.component(renderedText("Alex")))
            }

            "renders virtual selector and NBT separators" {
                val separator = virtual<Viewer> { render { text(context.name) } }
                val selector = selector(self()) { separator(separator) }
                val blockNbt = blockNbt(blockPos(0, 64, 0), nbtPath("value")) { separator(separator) }
                val entityNbt = entityNbt(self(), nbtPath("value")) { separator(separator) }
                val storageNbt = storageNbt(key("kotventure", "messages"), nbtPath("value")) { separator(separator) }
                val expectedSeparator = renderedText("Alex")

                selector.render(Viewer("Alex")).shouldBeSelectorComponent().separator() shouldBe expectedSeparator
                blockNbt.render(Viewer("Alex")).shouldBeBlockNbtComponent().separator() shouldBe expectedSeparator
                entityNbt.render(Viewer("Alex")).shouldBeEntityNbtComponent().separator() shouldBe expectedSeparator
                storageNbt.render(Viewer("Alex")).shouldBeStorageNbtComponent().separator() shouldBe expectedSeparator
            }

            "renders virtual hover text and entity names" {
                val hoverText =
                    text("Inspect") {
                        hover {
                            text {
                                virtual<Viewer> { render { text(context.name) } }
                            }
                        }
                    }
                val hoverEntity =
                    text("Entity") {
                        hover {
                            entity(key("minecraft", "player"), UUID(0, 0)) {
                                virtual<Viewer> { render { text(context.name) } }
                            }
                        }
                    }
                val expectedName = Component.text().append(renderedText("Alex")).build()

                hoverText.render(Viewer("Alex")) shouldHaveHoverText expectedName
                hoverEntity.render(Viewer("Alex")) shouldHaveHoverEntity
                        HoverEvent.ShowEntity.showEntity(
                            key(
                                "minecraft",
                                "player",
                            ),
                            UUID(0, 0),
                            expectedName,
                        )
            }

            "renders virtual object fallbacks" {
                val fallback = virtual<Viewer> { render { text(context.name) } }
                val message =
                    display(sprite(key("minecraft", "block/stone"))) {
                        fallback(fallback)
                    }

                message.render(Viewer("Alex")).shouldBeObjectComponent().fallback() shouldBe renderedText("Alex")
            }

            "propagates exceptions from virtual renderers" {
                val failure = IllegalArgumentException("stop")
                val message = virtual<Viewer> { render { throw failure } }

                shouldThrow<IllegalArgumentException> { message.render(Viewer("Alex")) } shouldBe failure
            }
        },
    )

private fun renderedText(value: String): Component = Component.text().append(Component.text(value)).build()
