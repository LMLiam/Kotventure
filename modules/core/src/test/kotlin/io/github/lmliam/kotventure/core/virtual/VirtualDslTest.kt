package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeVirtualComponent
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveContextType
import io.github.lmliam.kotventure.test.text.shouldHaveFallbackString
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponentRenderer

@Suppress("UNCHECKED_CAST")
class VirtualDslTest :
    StringSpec(
        {
            "equals the raw net.kyori construction" {
                val render: VirtualRenderScope<Viewer>.() -> Unit = { text(context.name) }

                val dsl = virtual<Viewer>(fallback = "player", render = render)
                val raw = Component.virtual(Viewer::class.java, VirtualScopeRenderer(render, "player"))

                dsl shouldBe raw
            }

            "exposes the reified context type" {
                val component = virtual<Viewer> { text(context.name) }

                component.contextType() shouldBe Viewer::class.java
                component.shouldHaveContextType<Viewer>()
            }

            "renders content from the supplied context" {
                val component = virtual<Viewer> { text(context.name) }
                val renderer = component.renderer() as VirtualComponentRenderer<Viewer>

                val alex = renderer.apply(Viewer("Alex")).asComponent()
                alex shouldBe Component.text().append(Component.text("Alex")).build()

                val steve = renderer.apply(Viewer("Steve")).asComponent()
                steve shouldBe Component.text().append(Component.text("Steve")).build()
            }

            "uses the fallback string as serialised content" {
                val component = virtual<Viewer>(fallback = "player") { text(context.name) }

                component shouldHaveFallbackString "player"
                component.content() shouldBe "player"
            }

            "defaults the fallback to an empty string" {
                val component = virtual<Viewer> { text(context.name) }

                component shouldHaveFallbackString ""
                component.content() shouldBe ""
            }

            "appends a virtual component inside component { }" {
                val message =
                    component {
                        text("Welcome, ")
                        virtual<Viewer>(fallback = "player") { text(context.name) }
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
                        renders++
                        text(context.name)
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
