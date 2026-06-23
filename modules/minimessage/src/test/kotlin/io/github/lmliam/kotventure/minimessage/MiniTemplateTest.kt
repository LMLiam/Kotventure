package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import io.github.lmliam.kotventure.minimessage.template.bind
import io.github.lmliam.kotventure.minimessage.template.invoke
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.shouldContainComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

// Fixtures declared at file scope so they exercise the object-init path and are shared across tests.

private object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
    val player = placeholder<Component>("player")
    val count = placeholder<Int>("count")
}

/** Declares an "unused" placeholder absent from the markup, making the definition invalid. */
private object SparseTemplate : MiniTemplate("<gold>Hello <name></gold>") {
    val name = placeholder<String>("name")
    val unused = placeholder<Int>("unused")
}

/** Declares a "player" placeholder of a different type than [WelcomeTemplate]. */
private object AltTemplate : MiniTemplate("<red>Alt <player>") {
    val player = placeholder<String>("player")
}

/** Declares a "player" descriptor structurally equal to [WelcomeTemplate]'s, but a distinct object. */
private object SameTypeAltTemplate : MiniTemplate("<red>Alt <player>") {
    val player = placeholder<Component>("player")
}

class MiniTemplateTest :
    DescribeSpec({
        describe("required-placeholder enforcement") {
            it("rejects a render that omits one binding") {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate { player bind Component.text("Alex") }
                }
            }

            it("rejects a render that omits all bindings") {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate { }
                }
            }

            it("rejects a render that binds only the second-declared placeholder") {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate { count bind 99 }
                }
            }
        }

        describe("placeholder identity") {
            it("rejects a placeholder not declared on this template") {
                val outsider = placeholder<String>("outsider")

                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        player bind Component.text("Alex")
                        count bind 1
                        @Suppress("UNCHECKED_CAST")
                        (outsider as MiniMessagePlaceholder<Component>) bind Component.text("x")
                    }
                }
            }

            it("rejects another template's same-named but differently-typed descriptor") {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        @Suppress("UNCHECKED_CAST")
                        (AltTemplate.player as MiniMessagePlaceholder<Component>) bind Component.text("Alex")
                        count bind 1
                    }
                }
            }

            it("rejects another template's structurally equal descriptor") {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        SameTypeAltTemplate.player bind Component.text("Alex")
                        count bind 1
                    }
                }
            }

            it("rejects binding the same placeholder twice in one render") {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        player bind Component.text("First", NamedTextColor.GREEN)
                        count bind 1
                        player bind Component.text("Second", NamedTextColor.RED)
                    }
                }
            }
        }

        describe("rendering") {
            it("renders independent components for repeated invocations") {
                val forAlex =
                    WelcomeTemplate {
                        player bind Component.text("Alex", NamedTextColor.GREEN)
                        count bind 3
                    }
                val forSam =
                    WelcomeTemplate {
                        player bind Component.text("Sam", NamedTextColor.RED)
                        count bind 0
                    }

                forAlex shouldHaveColor NamedTextColor.GOLD
                forAlex shouldContainText "Alex"
                forAlex shouldContainText "3"

                forSam shouldHaveColor NamedTextColor.GOLD
                forSam shouldContainText "Sam"
                forSam shouldContainText "0"
            }

            it("inlines the bound component placeholder into the tree") {
                val alex = Component.text("Alex", NamedTextColor.AQUA)

                val rendered =
                    WelcomeTemplate {
                        player bind alex
                        count bind 5
                    }

                rendered shouldHaveColor NamedTextColor.GOLD
                rendered shouldContainText "Alex"
                rendered shouldContainComponent alex
            }

            it("renders every literal and bound segment") {
                val rendered =
                    WelcomeTemplate {
                        player bind Component.text("Alex")
                        count bind 7
                    }

                rendered shouldHaveColor NamedTextColor.GOLD
                rendered shouldContainText "Welcome"
                rendered shouldContainText "Alex"
                rendered shouldContainText "7"
            }
        }

        describe("compile-time type safety") {
            it("rejects an Int placeholder bound with a String value") {
                assertDoesNotCompile(
                    fileName = "TemplateIntStringMismatch.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
                        import io.github.lmliam.kotventure.minimessage.template.MiniTemplateBindingScope

                        fun test(scope: MiniTemplateBindingScope) {
                            val count = placeholder<Int>("count")
                            scope.bind(count, "three")
                        }
                        """.trimIndent(),
                    "Argument type mismatch",
                    "String",
                    "Int",
                )
            }

            it("rejects a Component placeholder bound with an Int value") {
                assertDoesNotCompile(
                    fileName = "TemplateComponentIntMismatch.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
                        import io.github.lmliam.kotventure.minimessage.template.MiniTemplateBindingScope
                        import net.kyori.adventure.text.Component

                        fun test(scope: MiniTemplateBindingScope) {
                            val player = placeholder<Component>("player")
                            scope.bind(player, 42)
                        }
                        """.trimIndent(),
                    "Argument type mismatch",
                    "Int",
                    "Component",
                )
            }

            it("rejects calling placeholder() inside the render lambda") {
                // placeholder() is protected, so user code at the render site cannot declare new placeholders
                // even though the template type is a context receiver.
                assertDoesNotCompile(
                    fileName = "TemplatePlaceholderAtRenderSite.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
                        import io.github.lmliam.kotventure.minimessage.template.invoke

                        private object ScopeTestTemplate : MiniTemplate("<gold><name>") { }

                        fun test() {
                            ScopeTestTemplate {
                                placeholder<Int>("x")
                            }
                        }
                        """.trimIndent(),
                    "Cannot access",
                )
            }
        }

        describe("definition validation") {
            it("rejects duplicate placeholder names at construction") {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("<a>") {
                        @Suppress("unused")
                        val first = placeholder<String>("a")

                        @Suppress("unused")
                        val second = placeholder<Int>("a")
                    }
                }
            }

            it("rejects empty markup at construction") {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("") {}
                }
            }

            it("rejects blank markup at construction") {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("   ") {}
                }
            }

            it("rejects a blank placeholder name") {
                shouldThrow<IllegalArgumentException> {
                    placeholder<String>("")
                }
            }

            it("rejects a declared placeholder that is absent from the markup") {
                shouldThrow<IllegalArgumentException> {
                    SparseTemplate {
                        name bind "Alex"
                        unused bind 99
                    }
                }
            }
        }
    })
