package io.github.lmliam.kotventure.core.text

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveStyle
import io.github.lmliam.kotventure.test.text.shouldNotHaveDecoration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class ComponentDslTest :
    StringSpec(
        {
            "builds a text component with content" {
                val component =
                    component {
                        content("Hello")
                    }

                component shouldContainText "Hello"
            }

            "applies color to the root text component" {
                val component =
                    component {
                        content("Warning")
                        color(NamedTextColor.RED)
                    }

                component shouldHaveColor NamedTextColor.RED
            }

            "appends nested text children in declaration order" {
                val component =
                    component {
                        content("Hello ")
                        text {
                            content("world")
                            color(NamedTextColor.AQUA)
                        }
                        text {
                            content("!")
                        }
                    }

                component shouldHaveChildCount 2
                component.childAt(0) shouldContainText "world"
                component.childAt(0) shouldHaveColor NamedTextColor.AQUA
                component.childAt(1) shouldContainText "!"
            }

            "appends text children with initial content" {
                val component =
                    component {
                        text("Hello") {
                            color(NamedTextColor.AQUA)
                        }
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "Hello"
                component.childAt(0) shouldHaveColor NamedTextColor.AQUA
            }

            "applies a complete Adventure style" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                val component =
                    component {
                        content("Title")
                        style(style)
                    }

                component shouldHaveStyle style
            }

            "applies a decoration to the root text component" {
                val component =
                    component {
                        content("Marked root")
                        decorate(TextDecoration.BOLD)
                    }

                component shouldHaveDecoration TextDecoration.BOLD
            }

            "applies a decoration through the generic decoration hook" {
                val component =
                    component {
                        text("Marked") {
                            decorate(TextDecoration.UNDERLINED)
                        }
                    }

                component.childAt(0) shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "applies each named decoration toggle" {
                val component =
                    component {
                        text("Decorated") {
                            bold()
                            italic()
                            underlined()
                            strikethrough()
                            obfuscated()
                        }
                    }

                component.childAt(0) shouldHaveDecoration TextDecoration.BOLD
                component.childAt(0) shouldHaveDecoration TextDecoration.ITALIC
                component.childAt(0) shouldHaveDecoration TextDecoration.UNDERLINED
                component.childAt(0) shouldHaveDecoration TextDecoration.STRIKETHROUGH
                component.childAt(0) shouldHaveDecoration TextDecoration.OBFUSCATED
            }

            "applies decorations independently to nested children" {
                val component =
                    component {
                        text("plain")
                        text("loud") {
                            bold()
                        }
                    }

                component shouldHaveChildCount 2
                component.childAt(0) shouldContainText "plain"
                component.childAt(0) shouldNotHaveDecoration TextDecoration.BOLD
                component.childAt(1) shouldContainText "loud"
                component.childAt(1) shouldHaveDecoration TextDecoration.BOLD
            }

            "prevents implicit outer scope access in Kotventure-marked DSL blocks" {
                val source =
                    """
                    import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
                    import io.github.lmliam.kotventure.core.text.component

                    @KotventureDslMarker
                    class OuterScope {
                        fun outerOnly() {
                        }
                    }

                    fun outer(init: OuterScope.() -> Unit) {
                        OuterScope().init()
                    }

                    fun shouldNotCompile() {
                        outer {
                            component {
                                outerOnly()
                            }
                        }
                    }
                    """.trimIndent()

                val compilation =
                    KotlinCompilation().apply {
                        inheritClassPath = true
                        sources = listOf(SourceFile.kotlin("DslMarkerScopeTest.kt", source))
                    }

                val result = compilation.compile()

                result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                result.messages shouldContain "implicit receiver"
            }

            "builds a component tree when nested scopes stay explicit" {
                var outerWasCalled = false

                @KotventureDslMarker
                class OuterScope {
                    fun outerOnly() {
                        outerWasCalled = true
                    }
                }

                fun outer(init: OuterScope.() -> Unit) {
                    OuterScope().init()
                }

                lateinit var component: net.kyori.adventure.text.Component

                outer outer@{
                    component =
                        component {
                            text("child") {
                                bold()
                            }
                            this@outer.outerOnly()
                        }
                }

                outerWasCalled shouldBe true
                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "child"
                component.childAt(0) shouldHaveDecoration TextDecoration.BOLD
            }
        },
    )
