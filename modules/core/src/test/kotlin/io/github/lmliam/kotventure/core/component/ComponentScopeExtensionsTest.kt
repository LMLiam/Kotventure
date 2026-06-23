package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.style.StyleScope
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.shouldBeKeybindComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

class ComponentScopeExtensionsTest :
    StringSpec(
        {
            "component scope extensions work with arbitrary component scope implementations" {
                val scope = RecordingComponentScope()

                with(scope) {
                    text("Hello")
                    keybind("key.jump") {
                        bold()
                    }
                }

                scope.appended shouldHaveSize 2
                scope.appended[0] shouldContainText "Hello"
                val keybind = scope.appended[1].shouldBeKeybindComponent()
                keybind shouldHaveDecoration TextDecoration.BOLD
            }
        },
    )

private class RecordingComponentScope : ComponentScope {
    val appended: MutableList<Component> = mutableListOf()

    override fun color(color: TextColor?) = Unit

    override fun shadow(color: ShadowColor?) = Unit

    override fun font(font: Key?) = Unit

    override fun insertion(insertion: String?) = Unit

    override fun style(style: Style) = Unit

    override fun style(init: StyleScope.() -> Unit) = Unit

    override fun click(event: ClickEvent<*>?) = Unit

    override fun hover(source: HoverEventSource<*>?) = Unit

    override fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    ) = Unit

    override fun decoration(
        decoration: TextDecoration,
        state: State,
    ) = Unit

    override fun append(component: Component) {
        appended += component
    }

    override fun newline() {
        appended += Component.newline()
    }
}
