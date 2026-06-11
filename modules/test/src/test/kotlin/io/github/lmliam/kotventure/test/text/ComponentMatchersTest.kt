package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents

class ComponentMatchersTest :
    StringSpec(
        {
            "matches text content on Adventure text components" {
                Component.text("Hello") shouldContainText "Hello"
            }

            "matches text content nested in child components" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                component shouldContainText "world"
            }

            "reports text mismatch with expected and actual content" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Hello") shouldContainText "Bye"
                    }
                val expectedMessage = "Expected component text to contain <Bye>, but was <Hello>."

                failure.message shouldContain expectedMessage
            }

            "reports nested text mismatch with the complete extracted content" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .append(Component.text("!"))
                        .build()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldContainText "missing"
                    }
                val expectedMessage =
                    "Expected component text to contain <missing>, but was <Hello world!>."

                failure.message shouldContain expectedMessage
            }

            "matches root component colors" {
                Component.text("Warning", NamedTextColor.RED) shouldHaveColor NamedTextColor.RED
            }

            "reports color mismatch with expected and actual colors" {
                val component = Component.text("Warning", NamedTextColor.RED)

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveColor NamedTextColor.BLUE
                    }
                val expectedMessage =
                    "Expected component color <${NamedTextColor.BLUE}>, " +
                            "but was <${NamedTextColor.RED}>."

                failure.message shouldContain expectedMessage
            }

            "reports missing root color with expected and actual colors" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Warning") shouldHaveColor NamedTextColor.RED
                    }
                val expectedMessage =
                    "Expected component color <${NamedTextColor.RED}>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "matches complete Adventure styles" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                Component.text("Title").style(style) shouldHaveStyle style
            }

            "matches root decorations" {
                Component
                    .text("Title")
                    .decoration(TextDecoration.BOLD, true) shouldHaveDecoration TextDecoration.BOLD
            }

            "matches missing root decorations" {
                Component.text("Title") shouldNotHaveDecoration TextDecoration.BOLD
            }

            "reports decoration mismatch with expected and actual state" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Title") shouldHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <TRUE>, " +
                            "but was <NOT_SET>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected root decorations" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Title")
                            .decoration(TextDecoration.BOLD, true) shouldNotHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <NOT_SET>, " +
                            "but was <TRUE>."

                failure.message shouldContain expectedMessage
            }

            "does not treat explicitly disabled decorations as missing" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Title")
                            .decoration(TextDecoration.BOLD, false) shouldNotHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <NOT_SET>, " +
                            "but was <FALSE>."

                failure.message shouldContain expectedMessage
            }

            "matches child count and retrieves children by index" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "world"
            }

            "reports missing child indexes clearly" {
                val failure =
                    shouldThrow<IllegalStateException> {
                        Component.text("Hello").childAt(0)
                    }
                val expectedMessage = "Expected child at index <0>, but component has <0> children."

                failure.message shouldContain expectedMessage
            }

            "matches translatable component keys" {
                Component.translatable("item.minecraft.diamond") shouldHaveTranslationKey "item.minecraft.diamond"
            }

            "reports translation key mismatch with expected and actual keys" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("item.minecraft.diamond") shouldHaveTranslationKey
                            "item.minecraft.emerald"
                    }
                val expectedMessage =
                    "Expected translation key <item.minecraft.emerald>, " +
                            "but was <item.minecraft.diamond>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable fallback text" {
                Component.translatable("missing.key", "Missing key") shouldHaveFallback "Missing key"
            }

            "reports fallback mismatch with expected and actual fallback text" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("missing.key", "Missing key") shouldHaveFallback "Other fallback"
                    }
                val expectedMessage =
                    "Expected translatable fallback <Other fallback>, " +
                            "but was <Missing key>."

                failure.message shouldContain expectedMessage
            }

            "reports fallback checks on non-translatable components" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain") shouldHaveFallback "Missing key"
                    }
                val expectedMessage = "Expected translatable fallback <Missing key>, but was <not translatable>."

                failure.message shouldContain expectedMessage
            }

            "matches absent translatable fallback text" {
                Component.translatable("missing.key").shouldNotHaveFallback()
            }

            "reports unexpected translatable fallback text" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("missing.key", "Missing key").shouldNotHaveFallback()
                    }
                val expectedMessage = "Expected translatable fallback to be absent, but was <Missing key>."

                failure.message shouldContain expectedMessage
            }

            "reports absent fallback checks on non-translatable components" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldNotHaveFallback()
                    }
                val expectedMessage = "Expected translatable fallback to be absent, but was <not translatable>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable argument counts" {
                val argument = TranslationArgument.component(Component.text("Diamond"))

                Component.translatable("item.count", null as String?, argument) shouldHaveArgumentCount 1
            }

            "reports argument count mismatch with expected and actual counts" {
                val argument = TranslationArgument.component(Component.text("Diamond"))

                val failure =
                    shouldThrow<AssertionError> {
                        Component.translatable("item.count", null as String?, argument) shouldHaveArgumentCount 2
                    }
                val expectedMessage = "Expected <2> translation arguments, but found <1>."

                failure.message shouldContain expectedMessage
            }

            "matches translatable arguments exactly" {
                val item = TranslationArgument.component(Component.text("Diamond"))
                val amount = TranslationArgument.numeric(3)

                Component.translatable("item.count", null as String?, item, amount).shouldHaveArguments(item, amount)
            }

            "reports argument mismatch with expected and actual arguments" {
                val item = TranslationArgument.component(Component.text("Diamond"))
                val actualAmount = TranslationArgument.numeric(3)
                val expectedAmount = TranslationArgument.numeric(4)

                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .translatable("item.count", null as String?, item, actualAmount)
                            .shouldHaveArguments(item, expectedAmount)
                    }
                val expectedMessage =
                    "Expected translation arguments <${listOf(item, expectedAmount)}>, " +
                            "but found <${listOf(item, actualAmount)}>."

                failure.message shouldContain expectedMessage
            }

            "matches keybind component keys" {
                Component
                    .keybind("key.jump")
                    .shouldBeKeybindComponent() shouldHaveKeybind "key.jump"
            }

            "reports keybind mismatch with expected and actual keys" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .keybind("key.jump")
                            .shouldBeKeybindComponent() shouldHaveKeybind "key.sneak"
                    }
                val expectedMessage = "Expected keybind <key.sneak>, but was <key.jump>."

                failure.message shouldContain expectedMessage
            }

            "reports non-keybind components before keybind assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldBeKeybindComponent()
                    }
                val expectedMessage = "Expected keybind component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }

            "matches score component names and objectives" {
                val component =
                    Component
                        .score("Alex", "kills")
                        .shouldBeScoreComponent()

                component shouldHaveScoreName "Alex"
                component shouldHaveScoreObjective "kills"
            }

            "reports score name mismatch with expected and actual names" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .score("Alex", "kills")
                            .shouldBeScoreComponent() shouldHaveScoreName "Steve"
                    }
                val expectedMessage = "Expected score name <Steve>, but was <Alex>."

                failure.message shouldContain expectedMessage
            }

            "reports score objective mismatch with expected and actual objectives" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .score("Alex", "kills")
                            .shouldBeScoreComponent() shouldHaveScoreObjective "deaths"
                    }
                val expectedMessage = "Expected score objective <deaths>, but was <kills>."

                failure.message shouldContain expectedMessage
            }

            "reports non-score components before score assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldBeScoreComponent()
                    }
                val expectedMessage = "Expected score component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }

            "matches selector patterns and separators" {
                val separator = Component.text(", ")
                val component =
                    Component
                        .selector("@a", separator)
                        .shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@a"
                component shouldHaveSelectorSeparator separator
            }

            "matches selectors without separators" {
                Component.selector("@p").shouldBeSelectorComponent().shouldNotHaveSelectorSeparator()
            }

            "reports non-selector components before separator assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldBeSelectorComponent()
                    }
                val expectedMessage = "Expected selector component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }

            "reports selector pattern mismatch with expected and actual patterns" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .selector("@p")
                            .shouldBeSelectorComponent() shouldHaveSelectorPattern "@a"
                    }
                val expectedMessage = "Expected selector pattern <@a>, but was <@p>."

                failure.message shouldContain expectedMessage
            }

            "reports selector separator mismatch with expected and actual separators" {
                val expected = Component.text(", ")
                val actual = Component.text(" | ")
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .selector("@a", actual)
                            .shouldBeSelectorComponent() shouldHaveSelectorSeparator expected
                    }
                val expectedMessage = "Expected selector separator <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports missing selector separators" {
                val expected = Component.text(", ")
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .selector("@a")
                            .shouldBeSelectorComponent() shouldHaveSelectorSeparator expected
                    }
                val expectedMessage = "Expected selector separator <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected selector separators" {
                val actual = Component.text(", ")
                val failure =
                    shouldThrow<AssertionError> {
                        Component.selector("@a", actual).shouldBeSelectorComponent().shouldNotHaveSelectorSeparator()
                    }
                val expectedMessage = "Expected selector separator to be absent, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "matches object component contents and fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val fallback = Component.text("[stone]")
                val component =
                    Component
                        .`object`()
                        .contents(contents)
                        .fallback(fallback)
                        .build()
                        .shouldBeObjectComponent()

                component shouldHaveObjectContents contents
                component shouldHaveObjectFallback fallback
            }

            "matches object components without fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))

                Component.`object`(contents).shouldBeObjectComponent().shouldNotHaveObjectFallback()
            }

            "reports non-object components before object assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldBeObjectComponent()
                    }
                val expectedMessage = "Expected object component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }

            "reports object contents mismatches" {
                val actual = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val expected = ObjectContents.sprite(Key.key("minecraft", "block/dirt"))

                val failure =
                    shouldThrow<AssertionError> {
                        Component.`object`(actual).shouldBeObjectComponent() shouldHaveObjectContents expected
                    }
                val expectedMessage = "Expected object contents <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports object fallback mismatches" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val actual = Component.text("[stone]")
                val expected = Component.text("[dirt]")

                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .`object`()
                            .contents(contents)
                            .fallback(actual)
                            .build()
                            .shouldBeObjectComponent() shouldHaveObjectFallback expected
                    }
                val expectedMessage = "Expected object fallback <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports missing object fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val expected = Component.text("[stone]")

                val failure =
                    shouldThrow<AssertionError> {
                        Component.`object`(contents).shouldBeObjectComponent() shouldHaveObjectFallback expected
                    }
                val expectedMessage = "Expected object fallback <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected object fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val fallback = Component.text("[stone]")

                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .`object`()
                            .contents(contents)
                            .fallback(fallback)
                            .build()
                            .shouldBeObjectComponent()
                            .shouldNotHaveObjectFallback()
                    }
                val expectedMessage = "Expected object fallback to be absent, but was <$fallback>."

                failure.message shouldContain expectedMessage
            }

            "matches root click events" {
                val clickEvent = ClickEvent.openUrl("https://example.com")

                Component.text("Open").clickEvent(clickEvent) shouldHaveClickEvent clickEvent
            }

            "matches root click event actions" {
                Component
                    .text("Run")
                    .clickEvent(ClickEvent.runCommand("/spawn")) shouldHaveClickAction ClickEvent.Action.RUN_COMMAND
            }

            "matches text click payloads" {
                Component
                    .text("Copy")
                    .clickEvent(ClickEvent.copyToClipboard("secret")) shouldHaveClickTextPayload "secret"
            }

            "matches integer click payloads" {
                Component
                    .text("Page")
                    .clickEvent(ClickEvent.changePage(3)) shouldHaveClickIntPayload 3
            }

            "matches components without click events" {
                Component.text("Plain").shouldNotHaveClickEvent()
            }

            "reports missing click events" {
                val expected = ClickEvent.openUrl("https://example.com")

                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Plain") shouldHaveClickEvent expected
                    }
                val expectedMessage = "Expected click event <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "reports click action mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Run")
                            .clickEvent(ClickEvent.runCommand("/spawn")) shouldHaveClickAction
                            ClickEvent.Action.SUGGEST_COMMAND
                    }
                val expectedMessage =
                    "Expected click action <${ClickEvent.Action.SUGGEST_COMMAND}>, " +
                            "but was <${ClickEvent.Action.RUN_COMMAND}>."

                failure.message shouldContain expectedMessage
            }

            "reports text click payload mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Copy")
                            .clickEvent(ClickEvent.copyToClipboard("actual")) shouldHaveClickTextPayload "expected"
                    }
                val expectedMessage = "Expected click text payload <expected>, but was <actual>."

                failure.message shouldContain expectedMessage
            }

            "reports text click payload type mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Page")
                            .clickEvent(ClickEvent.changePage(2)) shouldHaveClickTextPayload "expected"
                    }
                val expectedMessage = "Expected click text payload <expected>, but was <integer payload <2>>."

                failure.message shouldContain expectedMessage
            }

            "reports integer click payload mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Page")
                            .clickEvent(ClickEvent.changePage(2)) shouldHaveClickIntPayload 3
                    }
                val expectedMessage = "Expected click integer payload <3>, but was <2>."

                failure.message shouldContain expectedMessage
            }

            "reports integer click payload type mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Copy")
                            .clickEvent(ClickEvent.copyToClipboard("two")) shouldHaveClickIntPayload 2
                    }
                val expectedMessage = "Expected click integer payload <2>, but was <text payload <two>>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected click events" {
                val actual = ClickEvent.suggestCommand("/help")

                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Suggest").clickEvent(actual).shouldNotHaveClickEvent()
                    }
                val expectedMessage = "Expected click event to be absent, but was <$actual>."

                failure.message shouldContain expectedMessage
            }
        },
    )
