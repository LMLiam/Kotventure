@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.action

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.inRange
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.event.ClickActionScope
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import io.github.lmliam.kotventure.core.event.click as buildClickEvent

internal class ButtonBuilder : ButtonScope {
    private var label: Component? by once()
    private var tooltip: Component? by once()
    private var width: Int? by once().inRange(1..1024)
    private var selectedAction: DialogAction? by once {
        "a button action is already selected."
    }

    override fun label(init: ComponentScope.() -> Unit) = label(component(init))

    override fun <T : ComponentLike> label(component: T) {
        label = component.asComponent()
    }

    override fun tooltip(init: ComponentScope.() -> Unit) = tooltip(component(init))

    override fun <T : ComponentLike> tooltip(component: T) {
        tooltip = component.asComponent()
    }

    override fun width(value: Int) {
        width = value
    }

    override fun onClick(callback: DialogActionCallback) = onClick(ClickCallback.Options.builder().build(), callback)

    override fun onClick(
        uses: Int,
        lifetime: Duration,
        callback: DialogActionCallback,
    ) = onClick(
        ClickCallback.Options
            .builder()
            .uses(uses)
            .lifetime(lifetime.toJavaDuration())
            .build(),
        callback,
    )

    override fun onClick(
        options: ClickCallback.Options,
        callback: DialogActionCallback,
    ) {
        selectedAction = DialogAction.customClick(callback, options)
    }

    override fun runCommand(template: String) {
        selectedAction = DialogAction.commandTemplate(template)
    }

    override fun custom(id: Key) {
        selectedAction = DialogAction.customClick(id, null)
    }

    override fun custom(
        id: Key,
        additions: BinaryTagHolder,
    ) {
        selectedAction = DialogAction.customClick(id, additions)
    }

    override fun click(init: ClickActionScope.() -> Unit) {
        selectedAction = DialogAction.staticAction(buildClickEvent(init))
    }

    internal fun build(): ActionButton {
        val buttonLabel = checkNotNull(label) { "a button requires a 'label' slot." }

        return ActionButton
            .builder(buttonLabel)
            .apply {
                tooltip?.let(::tooltip)
                width?.let(::width)
                selectedAction?.let(::action)
            }.build()
    }
}
