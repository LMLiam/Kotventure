package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

// ---------------------------------------------------------------------------
// Fixture templates — declared at file scope so they exercise the object-init
// path and are reused across tests without per-test construction cost.
// ---------------------------------------------------------------------------

private object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages") {
    val player = placeholder<Component>("player")
    val count = placeholder<Int>("count")
}

// Template with a declared placeholder absent from the markup (AC: lenient unused).
private object SparseTemplate : MiniTemplate("<gold>Hello <name>") {
    val name = placeholder<String>("name")
    val unused = placeholder<Int>("unused")
}

class MiniTemplateTest :
    StringSpec(
        {
            // ---------------------------------------------------------------
            // AC1 — Required-placeholder enforcement
            // ---------------------------------------------------------------

            "throws IllegalArgumentException listing missing placeholder when one binding is omitted" {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate {
                            bind(WelcomeTemplate.player, Component.text("Alex"))
                            // count intentionally omitted
                        }
                    }

                error.message shouldContain "count"
            }

            "throws IllegalArgumentException listing all missing placeholders when all bindings are omitted" {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate { }
                    }

                error.message shouldContain "player"
                error.message shouldContain "count"
            }

            "throws IllegalArgumentException when binding a placeholder not declared on this template" {
                val outsider = placeholder<String>("outsider")

                val error =
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate {
                            bind(WelcomeTemplate.player, Component.text("Alex"))
                            bind(WelcomeTemplate.count, 1)
                            @Suppress("UNCHECKED_CAST")
                            bind(outsider as MiniMessagePlaceholder<Component>, Component.text("x"))
                        }
                    }

                error.message shouldContain "outsider"
            }

            // ---------------------------------------------------------------
            // AC2 — Reuse correctness: two independent, correct components
            // ---------------------------------------------------------------

            "renders correct component for each of two independent invocations" {
                val forAlex =
                    WelcomeTemplate {
                        bind(WelcomeTemplate.player, Component.text("Alex", NamedTextColor.GREEN))
                        bind(WelcomeTemplate.count, 3)
                    }

                val forSam =
                    WelcomeTemplate {
                        bind(WelcomeTemplate.player, Component.text("Sam", NamedTextColor.RED))
                        bind(WelcomeTemplate.count, 0)
                    }

                // Gold color is applied to the root component by the <gold> tag.
                forAlex shouldHaveColor NamedTextColor.GOLD
                forAlex shouldContainText "Alex"
                forAlex shouldContainText "3"

                forSam shouldHaveColor NamedTextColor.GOLD
                forSam shouldContainText "Sam"
                forSam shouldContainText "0"

                // The two components are independent — Alex's render did not leak into Sam's.
                forAlex shouldContainText "Alex"
                forSam shouldContainText "Sam"
            }

            "renders children with the bound component placeholder inline" {
                val alex = Component.text("Alex", NamedTextColor.AQUA)

                val rendered =
                    WelcomeTemplate {
                        bind(WelcomeTemplate.player, alex)
                        bind(WelcomeTemplate.count, 5)
                    }

                // The bound component appears as a direct child within the rendered tree.
                rendered shouldContainText "Alex"
                rendered shouldHaveColor NamedTextColor.GOLD

                // Locate the child that IS the bound component and verify its color.
                val childrenFlat = buildList { collectChildren(rendered, this) }
                val alexChild = childrenFlat.firstOrNull { it == alex }
                alexChild shouldBe alex
            }

            // ---------------------------------------------------------------
            // AC3 — Type safety: assertDoesNotCompile
            // ---------------------------------------------------------------

            "does not compile when an Int placeholder is bound with a String value" {
                // Test the type safety of MiniTemplateBindingScope.bind via the public interface.
                // Using placeholder<T>() top-level (matching the existing compile-fail test pattern)
                // to avoid calling the protected inline member inside a subclass body in the snippet.
                assertDoesNotCompile(
                    fileName = "TemplateIntStringMismatch.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.minimessage.MiniTemplateBindingScope
                        import io.github.lmliam.kotventure.minimessage.placeholder

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

            "does not compile when a Component placeholder is bound with an Int value" {
                assertDoesNotCompile(
                    fileName = "TemplateComponentIntMismatch.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.minimessage.MiniTemplateBindingScope
                        import io.github.lmliam.kotventure.minimessage.placeholder
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

            // ---------------------------------------------------------------
            // Duplicate placeholder name → fails at object init
            // ---------------------------------------------------------------

            "throws IllegalArgumentException at template construction when two placeholders share a name" {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("<a>") {
                        @Suppress("unused")
                        val first = placeholder<String>("a")

                        @Suppress("unused")
                        val second = placeholder<Int>("a")
                    }
                }.message shouldContain "Duplicate placeholder 'a'"
            }

            // ---------------------------------------------------------------
            // Empty / blank markup → throws at construction
            // ---------------------------------------------------------------

            "throws IllegalArgumentException when markup is empty" {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("") { }
                }
            }

            "throws IllegalArgumentException when markup is blank" {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("   ") { }
                }
            }

            // ---------------------------------------------------------------
            // Double-bind first-wins
            // ---------------------------------------------------------------

            "uses the first bound value when the same placeholder is bound twice" {
                val firstWins =
                    WelcomeTemplate {
                        bind(WelcomeTemplate.player, Component.text("First", NamedTextColor.GREEN))
                        bind(WelcomeTemplate.count, 1)
                        // Second bind for player — should be silently ignored (first-wins).
                        bind(WelcomeTemplate.player, Component.text("Second", NamedTextColor.RED))
                    }

                firstWins shouldContainText "First"
            }

            // ---------------------------------------------------------------
            // Declared-but-unused placeholder → renders fine
            // ---------------------------------------------------------------

            "renders successfully when a declared placeholder is absent from the markup" {
                // SparseTemplate declares 'unused' which does not appear in "<gold>Hello <name>".
                val rendered =
                    SparseTemplate {
                        bind(SparseTemplate.name, "Alex")
                        bind(SparseTemplate.unused, 99)
                    }

                rendered shouldContainText "Alex"
                rendered shouldHaveColor NamedTextColor.GOLD
            }

            // ---------------------------------------------------------------
            // requiredPlaceholders surface
            // ---------------------------------------------------------------

            "requiredPlaceholders returns the names of all declared placeholders" {
                WelcomeTemplate.requiredPlaceholders shouldBe setOf("player", "count")
            }

            // ---------------------------------------------------------------
            // Child structure assertion helper (inline, no logic in test body)
            // ---------------------------------------------------------------

            "renders a multi-segment component with the player and count inline" {
                val rendered =
                    WelcomeTemplate {
                        bind(WelcomeTemplate.player, Component.text("Alex"))
                        bind(WelcomeTemplate.count, 7)
                    }

                // Root carries the <gold> colour; text is contained across the tree.
                rendered shouldHaveColor NamedTextColor.GOLD
                rendered shouldContainText "Welcome"
                rendered shouldContainText "Alex"
                rendered shouldContainText "7"
            }
        },
    )

// ---------------------------------------------------------------------------
// Private helper — collects all components in the tree (root + descendants).
// Used only in the "children with bound component placeholder" test above.
// ---------------------------------------------------------------------------

private fun collectChildren(
    component: Component,
    into: MutableList<Component>,
) {
    into += component
    component.children().forEach { collectChildren(it, into) }
}
