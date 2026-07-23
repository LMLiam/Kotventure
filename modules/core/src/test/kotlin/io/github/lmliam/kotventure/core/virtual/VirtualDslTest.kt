package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.gray
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeVirtualComponent
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveChildren
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveContextType
import io.github.lmliam.kotventure.test.text.shouldHaveFallbackString
import io.github.lmliam.kotventure.test.text.shouldHaveNoChildren
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.text.VirtualComponentRenderer
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style

@Suppress("UNCHECKED_CAST")
class VirtualDslTest :
    StringSpec(
        {
            "equals the raw net.kyori construction" {
                val body: VirtualRenderScope<Viewer>.() -> Unit = { text(context.name) }

                val dsl =
                    virtual<Viewer> {
                        fallback("player")
                        render(body)
                    }
                val raw = Component.virtual(Viewer::class.java, VirtualScopeRenderer(body, "player"))

                dsl shouldBe raw
            }

            "sets a static style and children on the fallback that equal the raw construction" {
                val body: VirtualRenderScope<Viewer>.() -> Unit = { text(context.name) }

                val dsl =
                    virtual<Viewer> {
                        fallback {
                            color(gold)
                            text("player")
                            text(" (online)") { color(gray) }
                        }
                        render(body)
                    }
                val raw =
                    Component
                        .virtual(Viewer::class.java, VirtualScopeRenderer(body, ""), Style.style(NamedTextColor.GOLD))
                        .children(
                            listOf(
                                Component.text("player"),
                                Component.text(" (online)").color(NamedTextColor.GRAY),
                            ),
                        ) as VirtualComponent

                dsl shouldBe raw
                dsl.shouldHaveColor(NamedTextColor.GOLD)
                dsl.shouldHaveChildren(
                    Component.text("player"),
                    Component.text(" (online)").color(NamedTextColor.GRAY),
                )
            }

            "defaults the fallback to empty content and no children" {
                val component = virtual<Viewer> { render { text(context.name) } }

                component shouldHaveFallbackString ""
                component.content() shouldBe ""
                component.shouldHaveNoChildren()
            }

            "throws when no render block is set" {
                shouldThrow<IllegalStateException> {
                    virtual<Viewer> { fallback("player") }
                }
            }

            "throws when the render block is set twice" {
                shouldThrow<IllegalStateException> {
                    virtual<Viewer> {
                        render { text(context.name) }
                        render { text(context.name) }
                    }
                }
            }

            "throws when the fallback text is set twice" {
                shouldThrow<IllegalStateException> {
                    virtual<Viewer> {
                        fallback("first")
                        fallback("second")
                        render { text(context.name) }
                    }
                }
            }

            "throws when the fallback text and block forms are mixed" {
                shouldThrow<IllegalStateException> {
                    virtual<Viewer> {
                        fallback("first")
                        fallback { text("second") }
                        render { text(context.name) }
                    }
                }
            }

            "exposes the reified context type" {
                val component = virtual<Viewer> { render { text(context.name) } }

                component.contextType() shouldBe Viewer::class.java
                component.shouldHaveContextType<Viewer>()
            }

            "renders content from the supplied context" {
                val component = virtual<Viewer> { render { text(context.name) } }
                val renderer = component.renderer() as VirtualComponentRenderer<Viewer>

                val alex = renderer.apply(Viewer("Alex")).asComponent()
                alex shouldBe Component.text().append(Component.text("Alex")).build()

                val steve = renderer.apply(Viewer("Steve")).asComponent()
                steve shouldBe Component.text().append(Component.text("Steve")).build()
            }

            "uses the fallback string as serialised content" {
                val component =
                    virtual<Viewer> {
                        fallback("player")
                        render { text(context.name) }
                    }

                component shouldHaveFallbackString "player"
                component.content() shouldBe "player"
            }

            "appends a virtual component inside component { }" {
                val message =
                    component {
                        text("Welcome, ")
                        virtual<Viewer> {
                            fallback("player")
                            render { text(context.name) }
                        }
                    }

                message shouldHaveChildCount 2
                val child = message.childAt(1).shouldBeVirtualComponent()
                child.shouldHaveContextType<Viewer>()
                child shouldHaveFallbackString "player"
            }

            "does not run the render block until the platform renders" {
                var renders = 0

                val component =
                    virtual<Viewer> {
                        render {
                            renders++
                            text(context.name)
                        }
                    }
                renders shouldBe 0

                val renderer = component.renderer() as VirtualComponentRenderer<Viewer>
                renderer.apply(Viewer("Alex"))
                renders shouldBe 1

                renderer.apply(Viewer("Steve"))
                renders shouldBe 2
            }
        },
    )
