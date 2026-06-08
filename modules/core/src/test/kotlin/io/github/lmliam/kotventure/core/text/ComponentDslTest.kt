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
import net.kyori.adventure.text.Component
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

            "appends existing Adventure components in declaration order" {
                val badge = Component.text("[OK]", NamedTextColor.GREEN)

                val component =
                    component {
                        text("Status: ")
                        append(badge)
                        text(" ready") {
                            italic()
                        }
                    }

                component shouldHaveChildCount 3
                component.childAt(0) shouldContainText "Status: "
                component.childAt(1) shouldBe badge
                component.childAt(2) shouldContainText " ready"
                component.childAt(2) shouldHaveDecoration TextDecoration.ITALIC
            }

            "appends a newline component" {
                val component =
                    component {
                        text("first")
                        newline()
                        text("second")
                    }

                component shouldHaveChildCount 3
                component.childAt(0) shouldContainText "first"
                component.childAt(1) shouldBe Component.newline()
                component.childAt(2) shouldContainText "second"
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

            "applies a style block to the current component" {
                val component =
                    component {
                        content("Title")
                        style {
                            color(NamedTextColor.GOLD)
                            bold()
                            underlined()
                        }
                    }

                component shouldHaveColor NamedTextColor.GOLD
                component shouldHaveDecoration TextDecoration.BOLD
                component shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "keeps nested style blocks scoped to their current child component" {
                val component =
                    component {
                        style {
                            color(NamedTextColor.GRAY)
                        }
                        text("child") {
                            style {
                                color(NamedTextColor.AQUA)
                                obfuscated()
                            }
                        }
                    }

                component shouldHaveColor NamedTextColor.GRAY
                component shouldHaveChildCount 1
                component.childAt(0) shouldHaveColor NamedTextColor.AQUA
                component.childAt(0) shouldHaveDecoration TextDecoration.OBFUSCATED
                component.childAt(0) shouldNotHaveDecoration TextDecoration.BOLD
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

            "prevents text scope access inside style blocks" {
                val source =
                    """
                    import io.github.lmliam.kotventure.core.text.component
                    import net.kyori.adventure.text.format.NamedTextColor

                    fun shouldNotCompile() {
                        component {
                            style {
                                color(NamedTextColor.GOLD)
                                content("leaked")
                            }
                        }
                    }
                    """.trimIndent()

                val compilation =
                    KotlinCompilation().apply {
                        inheritClassPath = true
                        sources = listOf(SourceFile.kotlin("StyleScopeLeakTest.kt", source))
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
